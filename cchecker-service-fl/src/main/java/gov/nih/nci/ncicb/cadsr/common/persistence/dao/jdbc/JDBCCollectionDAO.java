package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;
        
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

@Repository
public class JDBCCollectionDAO extends JDBCBaseDAOV2 implements CollectionDAO {
	
	
	private static Logger logger = Logger.getLogger(JDBCFormDAOV2.class.getName());
	
	public JDBCCollectionDAO(DataSource dataSource) {
		super(dataSource);
	}
	
	public String createCollectionRecord(String name, String desc, String fileName, String filePath, String createdBy, int name_repeat) {
		String sql = "INSERT into sbrext.FORM_COLLECTIONS (form_collection_idseq, description, name, " +
				" xml_file_name, xml_file_path, created_by, name_repeat_num) " +
				" VALUES (:idseq, :description, :name, :xml_file_name, :xml_file_path,:created_by, :name_repeat)";
		
		String idseq = generateGUID();		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("idseq", idseq);
		params.addValue("description", desc);
		params.addValue("name", name);
		params.addValue("xml_file_name", fileName);
		params.addValue("xml_file_path", filePath);
		params.addValue("created_by", createdBy.toUpperCase());
		params.addValue("name_repeat", name_repeat);
		
		int res = this.namedParameterJdbcTemplate.update(sql, params);
		
		return idseq;
	}
	
	public int createCollectionFormMappingRecord(String collectionseqid, String formseqid, 
			int formpublicid, float formversion, String loadType, int loadStatus, 
			String longName, float prevLatestVersion, Date loadDate) {
		String sql = "INSERT into sbrext.FORMS_IN_COLLECTION (FORM_COLLECTION_IDSEQ, FORM_IDSEQ, " +
			" PUBLIC_ID, VERSION, LOAD_TYPE, LOAD_STATUS, LONG_NAME, LOAD_UNLOAD_DATE";
		if (prevLatestVersion > 0)
			sql += ", PREVIOUS_LATEST_VERSION) ";
		else 
			sql += ")";
		
		sql += " VALUES (:collectionseqid, :formseqid, :formpublicid, :formversion, :loadtype, :loadstatus, :longname, :loadDate";
		
		if (prevLatestVersion > 0)
			sql += ", :prevLatestVersion)";
		else 
			sql += ")";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("collectionseqid", collectionseqid);
		params.addValue("formseqid", formseqid);
		params.addValue("formpublicid", formpublicid);
		params.addValue("formversion", formversion);
		params.addValue("loadtype", loadType);
		params.addValue("loadstatus", loadStatus);
		params.addValue("longname", longName);
		params.addValue("loadDate", loadDate);
		if (prevLatestVersion > 0)
			params.addValue("prevLatestVersion", prevLatestVersion);
		
		int res = this.namedParameterJdbcTemplate.update(sql, params);
		return res;
		
	}
	
	public int updateCollectionFormMappingRecord(String collectionseqid, String formseqid, String loadType, int loadStatus) {
		String sql = "Update sbrext.FORMS_IN_COLLECTION SET LOAD_TYPE=:loadtype, LOAD_STATUS=:loadstatus" +
				" where FORM_COLLECTION_IDSEQ=:collectionseqid and FORM_IDSEQ=:formseqid";
				
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("collectionseqid", collectionseqid);
		params.addValue("formseqid", formseqid);
		
		params.addValue("loadtype", loadType);
		params.addValue("loadstatus", loadStatus);
		
	
		int res = this.namedParameterJdbcTemplate.update(sql, params);
		return res;
		
	}
	
	 @Override
	public List<FormCollection> getAllLoadedCollectionsByUser(String userName) {
		String sql = 
		      "select * from SBREXT.FORM_COLLECTIONS where created_by=:user order by date_created desc";
		
		 MapSqlParameterSource params = new MapSqlParameterSource();
	     params.addValue("user", userName.toUpperCase());
	      
	     List<FormCollection> collections = this.namedParameterJdbcTemplate.query(sql, params, 
	     		new RowMapper<FormCollection>() {
	     	public FormCollection mapRow(ResultSet rs, int rowNum) throws SQLException {
	     		FormCollection aColl = new FormCollection();
	     		aColl.setId(rs.getString("FORM_COLLECTION_IDSEQ"));
				aColl.setName(rs.getString("NAME"));
				aColl.setDescription(rs.getString("DESCRIPTION"));
				aColl.setCreatedBy(rs.getString("CREATED_BY"));
				Timestamp timestamp = rs.getTimestamp("DATE_CREATED");
				
				aColl.setDateCreated(timestamp);
				
				//(rs.getTime("DATE_CREATED"));
				aColl.setXmlFileName(rs.getString("XML_FILE_NAME"));
				aColl.setXmlPathOnServer(rs.getString("XML_FILE_PATH"));
				aColl.setNameRepeatNum(rs.getInt("NAME_REPEAT_NUM"));
				
				return aColl;
	         }
	     });
 
		return collections;      
	}
	 
	 public List<String> getAllFormSeqidsForCollection(String collseqid) {
		 String sql = 
				 "select FORM_IDSEQ from sbrext.forms_in_collection " +
						 " where form_collection_idseq=:collseqid";

		 MapSqlParameterSource params = new MapSqlParameterSource();
		 params.addValue("collseqid", collseqid);
		 
		 List<String> seqid = 
				 this.namedParameterJdbcTemplate.query(sql, params, 
						 new RowMapper<String>() {
					 public String mapRow(ResultSet rs, int rowNum) throws SQLException {
						 return rs.getString("FORM_IDSEQ");

					 }
				 });

		 return seqid;

	 }
	 
	 public List<FormDescriptor> getAllFormInfoForCollection(String collseqid) {
		 String sql = 
				 "select * from sbrext.forms_in_collection " +
						 " where form_collection_idseq=:collseqid";

		 MapSqlParameterSource params = new MapSqlParameterSource();
		 params.addValue("collseqid", collseqid);
		 
		 List<FormDescriptor> forms = 
				 this.namedParameterJdbcTemplate.query(sql, params, 
						 new RowMapper<FormDescriptor>() {
					 public FormDescriptor mapRow(ResultSet rs, int rowNum) throws SQLException {
						 FormDescriptor form = new FormDescriptor();
						 form.setFormSeqId(rs.getString("FORM_IDSEQ"));
						 form.setPublicId(rs.getString("PUBLIC_ID"));
						 form.setVersion(rs.getString("VERSION"));
						 form.setLoadType(rs.getString("LOAD_TYPE").trim());
						 form.setLoadStatus(rs.getInt("LOAD_STATUS"));
						 form.setLongName(rs.getString("LONG_NAME"));
						 Timestamp timestamp = rs.getTimestamp("LOAD_UNLOAD_DATE");
						 form.setLoadUnloadDate(timestamp);
						 
						 form.setPreviousLatestVersion(rs.getFloat("PREVIOUS_LATEST_VERSION"));
						 return form;

					 }
				 });

		 return forms;

	 }
	 
	 public int getMaxNameRepeatNum(String collectionName) {
		 String sql = "select max(name_repeat_num) from sbrext.form_collections fc " +
				" where FC.NAME=:collname";
		 
		 MapSqlParameterSource params = new MapSqlParameterSource();
		 params.addValue("collname", collectionName);
		 //FORMBUILD-609 this method queryForInt is removed from v.4.3.20
		 //int maxNum = this.namedParameterJdbcTemplate.queryForInt(sql, params);
		 Integer maxNumObj = this.namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
		 int maxNum = (maxNumObj == null) ? 0 : maxNumObj.intValue();
		 return maxNum;
		 
	 }

}
