package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.ncicb.cadsr.common.dto.DefinitionTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.DesignationTransferObjectExt;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class JDBCDefinitionDAO extends JDBCBaseDAOV2 {
	private static Logger logger = Logger.getLogger(JDBCDesignationDAO.class.getName());
	
	public JDBCDefinitionDAO(DataSource dataSource) {
		super(dataSource);
	}
	
	public int createDefinitionForComponent(String componentSeqid, String contextSeqid, String createdBy,
			DefinitionTransferObjectExt def) {
		String idseq = generateGUID();
		String sql = "INSERT INTO sbr.DEFINITIONS_VIEW (defin_idseq, ac_idseq, conte_idseq, definition, defl_name, lae_name, created_by) " +
				" VALUES (:def_idseq, :ac_idseq, :conte_idseq, :definition, " +
                   ":defl_name, :lae_name,  :created_by)";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("def_idseq", idseq);
		params.addValue("ac_idseq", componentSeqid);
		
		params.addValue("conte_idseq", contextSeqid);
		params.addValue("definition", def.getDefinition());
		params.addValue("defl_name", def.getType());
		params.addValue("lae_name", def.getLanguage());
		params.addValue("created_by", createdBy);
		
		try {
			int res = this.namedParameterJdbcTemplate.update(sql, params);
			return res;
		} catch (DataAccessException de) {
			logger.debug(de.getMessage());
			throw de;
		}
		
		
		
	}

}
