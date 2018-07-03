/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public class DataElementRepository {
	private final Logger logger = LoggerFactory.getLogger(DataElementRepository.class);
    
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
