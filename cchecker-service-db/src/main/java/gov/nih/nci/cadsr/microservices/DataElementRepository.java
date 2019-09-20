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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.CategoryCde;
import gov.nih.nci.cadsr.data.CategoryNrds;
@Repository
public class DataElementRepository {
	private static final Logger logger = LoggerFactory.getLogger(DataElementRepository.class);
    
	@Autowired
	private JdbcTemplate jdbcTemplate;
    @Transactional(readOnly=true)
    public List<DataElements> findAll() {
        return getAll("select preferred_definition from SBR.DATA_ELEMENTS_VIEW where de_idseq = '99BA9DC8-2CC9-4E69-E034-080020C9C0E0'", DataElements.class);
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
    @Transactional
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
     * @param reportData not null and required fields not null
     * @return UUID - caDSR IDSEQ shall exist in CC_PARSER_DATA
     */
    @Transactional
    public String createReportError(CCCReport reportData, String idseq) {
    	//TODO do we need to check the data
    	//delete the previous session report
    	logger.debug("createReportError REPORT_OWNER: " + reportData.getReportOwner() + ", FILE_NAME: " + reportData.getFileName() + ", idseq: " + idseq);
    	jdbcTemplate.update(dbcon -> {
    	    PreparedStatement ps = dbcon.prepareStatement("DELETE FROM SBREXT.CC_REPORT_ERROR WHERE CCHECKER_IDSEQ = ?");
    	    ps.setString(1, idseq);
    	    return ps;
    	});
    	jdbcTemplate.update(dbcon -> {
    	    PreparedStatement ps = dbcon.prepareStatement(
    	    	"INSERT INTO SBREXT.CC_REPORT_ERROR (CCHECKER_IDSEQ, FILE_NAME, REPORT_OWNER, ERROR_REPORT_BLOB)  values(?, ?, ?, ?)");
    	    ps.setString(1, idseq);
    	    ps.setString(2, reportData.getFileName());
    	    ps.setString(3, reportData.getReportOwner());
    	    byte[] jsonStr = writeToJSON(reportData);
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
		return retrieveBlobData(idseq, ALSData.class, retrieveAlsQuery(), "PARSER_BLOB");
	}
	/**
	 * 
	 * @param idseq expected to be a valid DB value
	 * @return CCCReport or null
	 * @throws DataAccessException
	 */
	public CCCReport retrieveReportError(String idseq) {
		return retrieveBlobData(idseq, CCCReport.class, retrieveErrorReportQuery(), "ERROR_REPORT_BLOB");
	}
	//FIXME Do we have a data class for Full report?
	/**
	 * 
	 * @param idseq expected to be a valid DB value
	 * @return CCCReport or null
	 * @throws DataAccessException
	 */
	public CCCReport retrieveFullReport(String idseq) {
		return retrieveBlobData(idseq, CCCReport.class, retrieveFullReportQuery(), "FULL_REPORT_BLOB");
	}
	/**
	 * 
	 * @param idseq expected to be a valid DB value
	 * @return ALSData or null
	 * @throws DataAccessException
	 */
	public <T> T retrieveBlobData(String idseq, Class<T> clazz, String retrieveQueryStr, String columnName) {
		T blobData = null;
		try {
			byte[] dataByteArr = null;
			LobHandler lobHandler = new DefaultLobHandler();
			logger.debug("retrieveBlobData for ID: " + idseq + ", retrieveQueryStr: " + retrieveQueryStr);
			dataByteArr = (byte[]) jdbcTemplate.queryForObject(retrieveQueryStr, new Object[] { idseq },
				new RowMapper<Object>() {
						// queryForObject expects that at least one object is found, otherwise: DataAccessException
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							byte[] requestData = lobHandler.getBlobAsBytes(rs, columnName);
							return requestData;
						}
					});
			blobData = readFromJSON(dataByteArr, clazz);
		} 
		catch (Exception e) {
			logger.error("retrieveBlobData error on idseq: " + idseq, e);
			e.printStackTrace();
		}
		return blobData;
	}
	/**
	 * 
	 * @return List<CategoryCde>
	 */
	public List<CategoryCde> retrieveCdeModuleTypeList () {
		List<CategoryCde> resultList;
		resultList = getAll(retrieveCategoryCdeListQuery(), CategoryCde.class);
		if ((resultList != null) && (! resultList.isEmpty())) {
			CategoryCde[] arr = new CategoryCde[resultList.size()];
			arr = resultList.toArray(arr);
			Arrays.sort(arr, new SortCdes());
			resultList = Arrays.asList(arr);
		}
		return resultList;
	}
	/**
	 * 
	 * @return List<CategoryNrds
	 */
	public List<CategoryNrds> retrieveNrdsCdeList () {
		List<CategoryNrds> resultList;
		resultList = getAll(retrieveNrdsCdeListQuery(), CategoryNrds.class);
		if ((resultList != null) && (! resultList.isEmpty())) {
			CategoryNrds[] arr = new CategoryNrds[resultList.size()];
			arr = resultList.toArray(arr);
			Arrays.sort(arr, new SortNrds());
			resultList = Arrays.asList(arr);
		}
		return resultList;
	}
    protected <T> String retrieveAlsQuery() {
		return "SELECT PARSER_BLOB from SBREXT.CC_PARSER_DATA WHERE CCHECKER_IDSEQ = ?";
	}
    protected String retrieveErrorReportQuery(/*String idseq*/) {
		return "SELECT ERROR_REPORT_BLOB from SBREXT.CC_REPORT_ERROR WHERE CCHECKER_IDSEQ = ?";
	}
    protected String retrieveFullReportQuery(/*String idseq*/) {
		return "SELECT FULL_REPORT_BLOB from SBREXT.CC_REPORT_FULL WHERE CCHECKER_IDSEQ = ?";
	}
    /**
     * Retrieve CDE SQL with CRF category to populate CRF cache.
     * 
     * @return SQL string
     */
    protected String retrieveCategoryCdeListQuery() {
		return "SELECT * from SBREXT.MDSR_STANDARD_FORM_CDE_AGR where MODULE_TYPE = 'Mandatory'";
	}
    protected String retrieveNrdsCdeListQuery() {
		return "SELECT CDE_ID, de.VERSION DE_VERSION, de.LONG_NAME DE_NAME, de.QUESTION DE_QUESTION FROM sbr.data_elements_view de "
			+ "inner join sbr.contexts_view cnt on "
			+ "de.CONTE_IDSEQ = cnt.CONTE_IDSEQ and cnt.NAME = 'NRDS' and de.ASL_NAME = 'RELEASED'";
	}

    /**
     * Return java generated UUID to upper case.
     * We could use Oracle caDSR function 
     * 
     * @return String
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
	protected static <T> byte[] writeToJSON (T dataToJson) {
		//FIXME decide on alsData JSON serialization
		//FIXME do we need an exception here?
		ObjectMapper jsonMapper = new ObjectMapper();
		try {
            String jsonStr = jsonMapper.writeValueAsString(dataToJson);
            byte[] arr = jsonStr.getBytes();
            return arr;
        } 
		catch (IOException e) {
            logger.error("writeToJSON error" + e, e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
		catch (NullPointerException ex) {
			 logger.error("writeToJSON error  NullPointerException", ex);
			 throw ex;
		}
	}
	
//    /**
//     * There must be a better way to create a stream.
//     * 
//     * @param alsData shall be not null
//     * @return byte[]
//     */
//	protected static byte[] writeToJSON (ALSData alsData) {
//		//FIXME decide on alsData JSON serialization
//		//FIXME do we need an exception here?
//		ObjectMapper jsonMapper = new ObjectMapper();
//		try {
//            String jsonStr = jsonMapper.writeValueAsString(alsData);
//            byte[] arr = jsonStr.getBytes();
//            return arr;
//        } 
//		catch (IOException e) {
//            logger.error("writeToJSON error: " + alsData.getFileName() + ", owner: " + alsData.getReportOwner(), e);
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//		catch (NullPointerException ex) {
//			 logger.error("writeToJSON error  NullPointerException", ex);
//			 throw ex;
//		}
//	}
	/**
	 * 
	 * @param arr shall not be null
	 * @return 
	 * @return ALSData or null on any error
	 */
	protected static <T> T readFromJSON (byte[] arr, Class<T> clazz) {
		T result = null;
		if (arr != null) {
		try {
				ObjectMapper jsonMapper = new ObjectMapper();
				result = jsonMapper.readValue(arr, clazz);
			}
			catch (Exception e) {
				String msg = "readFromJSON: error reading user data: " + e;
				logger.error(msg, e);
				//TODO remove
				e.printStackTrace();
			}
		}
		return result;
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
    class SortCdes implements Comparator<CategoryCde>
    {
        public int compare(CategoryCde a, CategoryCde b)
        {
            	if (a == null) return -1;
            else 
            	return a.compareTo(b);
        }
    }    
    class SortNrds implements Comparator<CategoryNrds>
    {
        public int compare(CategoryNrds a, CategoryNrds b)
        {
            	if (a == null) return -1;
            else 
            	return a.compareTo(b);
        }
    } 
}
