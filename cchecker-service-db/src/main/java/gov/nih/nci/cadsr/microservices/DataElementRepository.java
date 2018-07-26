/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.nci.cadsr.data.ALSData;
@Repository
public class DataElementRepository {
	private static final Logger logger = LoggerFactory.getLogger(DataElementRepository.class);
    
	@Autowired
	private JdbcTemplate jdbcTemplate;
    @Transactional(readOnly=true)
    public List<DataElements> findAll() {
        return getAll("select preferred_definition from SBR.DATA_ELEMENTS where de_idseq = '99BA9DC8-2CC9-4E69-E034-080020C9C0E0'", DataElements.class);
        //return getAll("select 'test' preferred_definition from dual", DataElements.class);
    }
    /**
     * @param sql
     * @param type
     * @param <T>
     * @return
     */
    public <T> List<T> getAll( String sql, Class<T> type )
    {

        List<T> allColumns = jdbcTemplate.query(
                sql, new BeanPropertyRowMapper( type )
        );

        return allColumns;
    }
    /**
     * 
     * @param alsData not null and required fields not null
     * @return UUID - caDSR IDSEQ
     */
    public String createAlsData(ALSData alsData, String idseq) {
    	//TODO do we need to check the data
    	logger.debug("createAlsData alsData REPORT_OWNER: " + alsData.getReportOwner() + ", FILE_NAME: " + alsData.getFileName() + ", idseq: " + idseq);
    	jdbcTemplate.update(dbcon -> {
    	    PreparedStatement ps = dbcon.prepareStatement(
    	    	"INSERT INTO SBREXT.CC_PARSER_DATA (CCHECKER_IDSEQ, FILE_NAME, REPORT_OWNER, PARSER_BLOB)  values(?, ?, ?, ?)");
    	    ps.setString(1, idseq);
    	    ps.setString(2, alsData.getFileName());
    	    ps.setString(3, alsData.getReportOwner());
    	    byte[] jsonStr = writeToJSON(alsData);
    	    InputStream bs = new ByteArrayInputStream(jsonStr);
    	    ps.setBinaryStream(4, bs);
    	    return ps;
    	});
    	return idseq;
    }
    
	/**
	 * 
	 * @param idseq expected to be a valid DB value
	 * @return ALSData or null
	 * @throws DataAccessException
	 */
	public ALSData retrieveAlsData(String idseq) {
		ALSData alsData = null;
		try {
			byte[] alsDataByteArr = null;
			LobHandler lobHandler = new DefaultLobHandler();
			alsDataByteArr = (byte[]) jdbcTemplate.queryForObject(retrieveAlsQuery(), new Object[] { idseq },
				new RowMapper<Object>() {
						// queryForObject expects that at least one object is found, otherwise: DataAccessException
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							byte[] requestData = lobHandler.getBlobAsBytes(rs, "PARSER_BLOB");
							return requestData;
						}
					});
			alsData = readFromJSON(alsDataByteArr);
		} 
		catch (Exception e) {
			logger.error("retrieveAlsData error on idseq: " + idseq, e);
			e.printStackTrace();
		}
		return alsData;
	}
    
    /**
     * 
     * @return String
     */
    protected String retrieveAlsQuery(/*String idseq*/) {
		return "SELECT PARSER_BLOB from SBREXT.CC_PARSER_DATA WHERE CCHECKER_IDSEQ = ?";
    	//FIXME check format of idseq
		//return "SELECT PARSER_BLOB from SBREXT.CC_PARSER_DATA WHERE CCHECKER_IDSEQ = '" + idseq +"'";
	}
	/**
     * Return java generated UUID to upper case.
     * We could use Oracle caDSR function 
     * 
     * @return
     */
    
    public String retrieveIdseq( ) {
    	String idseq = java.util.UUID.randomUUID().toString().toUpperCase();
    	return idseq;
    }
    /**
     * There must be a better way to create a stream.
     * 
     * @param alsData shall be not null
     * @return byte[]
     */
	protected static byte[] writeToJSON (ALSData alsData) {
		//FIXME decide on alsData JSON serialization
		//FIXME do we need an exception here?
		ObjectMapper jsonMapper = new ObjectMapper();
		try {
            String jsonStr = jsonMapper.writeValueAsString(alsData);
            byte[] arr = jsonStr.getBytes();
            return arr;
        } 
		catch (IOException e) {
            logger.error("writeToJSON error: " + alsData.getFileName() + ", owner: " + alsData.getReportOwner(), e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
		catch (NullPointerException ex) {
			 logger.error("writeToJSON error  NullPointerException", ex);
			 throw ex;
		}
	}
	/**
	 * 
	 * @param arr shall not be null
	 * @return ALSData or null on any error
	 */
	protected static ALSData readFromJSON(byte[] arr) {
		ALSData alsData = null;
		if (arr != null) {
		try {
				ObjectMapper jsonMapper = new ObjectMapper();
				alsData = jsonMapper.readValue(arr, ALSData.class);
			}
			catch (Exception e) {
				String msg = "readFromJSON: error reading user data: " + e;
				logger.error(msg, e);
				e.printStackTrace();
			}
		}
		return alsData;
	}
    
    ////////////
    //FIXME this call does not work. The procedure needs the third 'out' parameter to be passes: p_de_search_res which is sys_refcursor;
    public Map<String, Object> retrieveCdeType(String publicId, String version) {
    	SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
    		.withSchemaName("SBREXT")
    		.withCatalogName("MDSR_CDE_595_PKG")
    		.withProcedureName("rtrieve_cde_by_id");

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("p_cde_id", publicId);
		inParamMap.put("p_version", "version");
		SqlParameterSource in = new MapSqlParameterSource(inParamMap);

		Map<String, Object> simpleJdbcCallResult = simpleJdbcCall.execute(in);
		
		logger.debug("retrieveCdeType result: " + simpleJdbcCallResult.toString());
    	return simpleJdbcCallResult;
    }
    
    
}
