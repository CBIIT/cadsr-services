package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.ncicb.cadsr.common.dto.DesignationTransferObjectExt;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class JDBCDesignationDAO extends JDBCBaseDAOV2 {
	private static Logger logger = Logger.getLogger(JDBCDesignationDAO.class.getName());
	
	public JDBCDesignationDAO(DataSource dataSource) {
		super(dataSource);
	}
	
	public int createDesignationForComponent(String componentSeqid, String contextSeqid, String createdBy,
			DesignationTransferObjectExt desig) {
		String idseq = generateGUID();
		String sql = "INSERT INTO sbr.designations_view (desig_idseq, ac_idseq, conte_idseq, name, detl_name, lae_name, created_by) " +
				"VALUES (:desig_idseq, :ac_idseq, :conte_idseq, :name, " +
                   ":detl_name, :lae_name,  :created_by)";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("desig_idseq", idseq);
		params.addValue("ac_idseq", componentSeqid);
		
		params.addValue("conte_idseq", contextSeqid);
		params.addValue("name", desig.getName());
		params.addValue("detl_name", desig.getType());
		params.addValue("lae_name", desig.getLanguage());
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
