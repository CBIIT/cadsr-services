package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.ncicb.cadsr.common.CaDSRConstants;
import gov.nih.nci.ncicb.cadsr.common.CaDSRUtil;
import gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.FormTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.FormV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ModuleTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.jdbc.JDBCFormTransferObject;
import gov.nih.nci.ncicb.cadsr.common.exception.DMLException;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.FormV2DAO;
import gov.nih.nci.ncicb.cadsr.common.resource.ConceptDerivationRule;
import gov.nih.nci.ncicb.cadsr.common.resource.Context;
import gov.nih.nci.ncicb.cadsr.common.resource.Form;
import gov.nih.nci.ncicb.cadsr.common.resource.FormElement;
import gov.nih.nci.ncicb.cadsr.common.resource.FormV2;
import gov.nih.nci.ncicb.cadsr.common.resource.FormValidValue;
import gov.nih.nci.ncicb.cadsr.common.resource.Module;
import gov.nih.nci.ncicb.cadsr.common.resource.Protocol;
import gov.nih.nci.ncicb.cadsr.common.resource.Question;
import gov.nih.nci.ncicb.cadsr.common.resource.QuestionRepitition;
import gov.nih.nci.ncicb.cadsr.common.resource.ReferenceDocument;
import gov.nih.nci.ncicb.cadsr.common.resource.TriggerAction;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueDomain;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueDomainV2;
import gov.nih.nci.ncicb.cadsr.common.util.StringUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.object.StoredProcedure;

//Modification off JDBCFormV2Dao
public class JDBCFormDAOV2 extends JDBCAdminComponentDAOV2 implements FormV2DAO {
	
	private static final Logger logger = LoggerFactory.getLogger(JDBCFormDAOV2.class.getName());
	
	public JDBCFormDAOV2(DataSource dataSource) {
		super(dataSource);
	}
    

  public FormV2 findFormV2ByPrimaryKey(String formId) throws DMLException {
    FormV2 myForm = null;
    FormV2ByPrimaryKey query = new FormV2ByPrimaryKey();
    query.setDataSource(this.dataSource);
    query.setSql();

    List result = (List) query.execute(formId);

    if (result == null || result.isEmpty()){
        DMLException dmlExp = new DMLException("No matching record found.");
              dmlExp.setErrorCode(NO_MATCH_FOUND);
          throw dmlExp;
    }

    myForm = (FormV2) (query.execute(formId).get(0));

    FormV2 myFormExtensions = null;
    FormV2ExtensionsByPrimaryKey queryExtensions = new FormV2ExtensionsByPrimaryKey();
    queryExtensions.setDataSource(this.dataSource);
    queryExtensions.setSql();
    List resultExtensions = (List) queryExtensions.execute(formId);

    if (resultExtensions == null || resultExtensions.isEmpty()){
        DMLException dmlExp = new DMLException("No matching record found.");
              dmlExp.setErrorCode(NO_MATCH_FOUND);
          throw dmlExp;
    }

    myFormExtensions = (FormV2) (queryExtensions.execute(formId).get(0));
    myForm.setModifiedBy(myFormExtensions.getModifiedBy());
    myForm.setRegistrationStatus(myFormExtensions.getRegistrationStatus());


    //get protocols
    myForm.setProtocols(getProtocols(myForm));
    return myForm;
  }


    private List getProtocols(Form form){
        FormProtocolByFormPrimaryKey query = new FormProtocolByFormPrimaryKey();
        query.setDataSource(this.dataSource);
        query.setSql();
        List protocols = query.execute(form.getFormIdseq());
        return protocols;
    }
    
  /**
   * Inner class that accesses database to get a form using the form idseq
   */
  class FormV2ByPrimaryKey extends MappingSqlQuery {
    FormV2ByPrimaryKey() {
      super();
    }

    public void setSql() {
      super.setSql("SELECT * FROM FB_FORMS_VIEW where QC_IDSEQ = ? ");
      declareParameter(new SqlParameter("QC_IDSEQ", Types.VARCHAR));
    }

    protected Object mapRow( ResultSet rs, int rownum) throws SQLException {
      FormV2 form = new FormV2TransferObject();
      form.setFormIdseq(rs.getString(1)); // QC_IDSEQ
      form.setIdseq(rs.getString(1));
      form.setLongName(rs.getString(9)); //LONG_NAME
      form.setPreferredName(rs.getString(7)); // PREFERRED_NAME

      //setContext(new ContextTransferObject(rs.getString("context_name")));
      ContextTransferObject contextTransferObject = new ContextTransferObject();
      contextTransferObject.setConteIdseq(rs.getString(4)); //CONTE_IDSEQ
      contextTransferObject.setName(rs.getString(12)); // CONTEXT_NAME
      form.setContext(contextTransferObject);
      form.setDateCreated(rs.getTimestamp(14));
      form.setDateModified(rs.getTimestamp(15));


      //multiple protocols will be set later

      form.setFormType(rs.getString(3)); // TYPE
      form.setAslName(rs.getString(6)); // WORKFLOW
      form.setVersion(new Float(rs.getString(2))); // VERSION
      form.setPreferredDefinition(rs.getString(8)); // PREFERRED_DEFINITION
      form.setCreatedBy(rs.getString(13)); // CREATED_BY
      form.setFormCategory(rs.getString(5));
      form.setPublicId(rs.getInt(17));
      form.setChangeNote(rs.getString(18));

      return form;
    }
  }

  /**
   * Inner class that accesses database to get a form using the form idseq
   */
  class FormV2ExtensionsByPrimaryKey extends MappingSqlQuery {
    FormV2ExtensionsByPrimaryKey() {
      super();
    }

    public void setSql() {
      super.setSql("SELECT * FROM CABIO31_FORMS_VIEW where QC_IDSEQ = ? ");
      declareParameter(new SqlParameter("QC_IDSEQ", Types.VARCHAR));
    }

    protected Object mapRow( ResultSet rs, int rownum) throws SQLException {
      FormV2 form = new FormV2TransferObject();
      form.setModifiedBy(rs.getString(22));
      form.setRegistrationStatus(rs.getString(23));

      return form;
    }
  }

    /**
     * Inner class that accesses database to get a form's protocols using the form idseq
     */
    class FormProtocolByFormPrimaryKey extends MappingSqlQuery {
      FormProtocolByFormPrimaryKey() {
        super();
      }

      public void setSql() {
        String sql = " SELECT p.proto_idseq, p.version, p.conte_idseq, p.preferred_name, p.preferred_definition, p.asl_name, p.long_name, p.LATEST_VERSION_IND, p.begin_date, p.END_DATE, p.PROTOCOL_ID, p.TYPE, p.PHASE, p.LEAD_ORG, p.origin, p.PROTO_ID, c.name contextname " +
                     " FROM protocol_qc_ext fp, sbrext.protocols_view_ext p , sbr.contexts_view c" +
                     " where QC_IDSEQ = ? and fp.PROTO_IDSEQ = p.PROTO_IDSEQ " +
                     " AND p.conte_idseq = c.conte_idseq";
        super.setSql(sql);
        declareParameter(new SqlParameter("QC_IDSEQ", Types.VARCHAR));
      }

      protected Object mapRow( ResultSet rs, int rownum) throws SQLException {
        Protocol protocol = new ProtocolTransferObject();
        protocol.setProtoIdseq(rs.getString(1));
        protocol.setVersion(rs.getFloat(2));
        protocol.setConteIdseq(rs.getString(3));
        protocol.setPreferredName(rs.getString(4));
        protocol.setPreferredDefinition(rs.getString(5));
        protocol.setAslName(rs.getString(6));
        protocol.setLongName(rs.getString(7));
        protocol.setLatestVersionInd(rs.getString(8));
        protocol.setBeginDate(rs.getDate(9));
        protocol.setEndDate(rs.getDate(10));
        protocol.setProtocolId(rs.getString(11));
        protocol.setType(rs.getString(12));
        protocol.setPhase(rs.getString(13));
        protocol.setLeadOrg(rs.getString(14));
        protocol.setOrigin(rs.getString(15));
        Float publicId = rs.getFloat(16);
        protocol.setPublicId(publicId.intValue());
        String contextName = rs.getString(17);
        Context context = new ContextTransferObject();
        context.setConteIdseq(rs.getString(3));
        context.setName(contextName);
        protocol.setContext(context);

        return protocol;
      }
    }//end of class FormProtocolByFormPrimaryKey
    
    /**
     * (Copy from JDBCFormDAO)
     * 
     * Gets all the modules that belong to the specified form
     *
     * @param formId corresponds to the form idseq
     *
     * @return modules that belong to the specified form
     */
    public List<ModuleTransferObject> getModulesInAForm(String formId) {
      ModulesInAFormQuery_STMT query = new ModulesInAFormQuery_STMT();
      query.setDataSource(this.dataSource);
      query._setSql(formId);

      return query.execute();
     
    }
    /**
     * Inner class that accesses database to get all the modules that belong to
     * the specified form
     */
    class ModulesInAFormQuery_STMT extends MappingSqlQuery {
      ModulesInAFormQuery_STMT() {
        super();
      }

      public void _setSql(String id) {
        super.setSql("SELECT a.*, b.PUBLIC_ID FROM FB_MODULES_VIEW a, AC_PUBLIC_ID_VIEW_EXT b where a.CRF_IDSEQ = '" + id + "' and a.MOD_IDSEQ = b.AC_IDSEQ");
//         declareParameter(new SqlParameter("CRF_IDSEQ", Types.VARCHAR));
      }

     /**
      * 3.0 Refactoring- Removed JDBCTransferObject
      */
      protected Object mapRow(
        ResultSet rs,
        int rownum) throws SQLException {
       Module module = new ModuleTransferObject();
       module.setModuleIdseq(rs.getString(1));  // MOD_IDSEQ
       module.setVersion(rs.getFloat(3)); //version
       module.setConteIdseq(rs.getString(4)); //context idseq
       module.setAslName(rs.getString(5));  //workflow status
       module.setPreferredName(rs.getString(6));
       module.setPreferredDefinition(rs.getString(7));
       module.setLongName(rs.getString(8));     // LONG_NAME
       module.setDisplayOrder(rs.getInt(13));   // DISPLAY_ORDER
       module.setNumberOfRepeats(rs.getInt(14));//repeat_no
       module.setPublicId(rs.getInt(15));
       return module;
      }
    }
    
    /*
     * Moved from JDBCFormDAO
     */
    public void addFormProtocol(String formIdseq, String protocoldIdseq, String createdBy){
        AddFormProtocolQuery query = new AddFormProtocolQuery(getDataSource());
        query.addFormProtocol(formIdseq, protocoldIdseq, createdBy);
        return;
    }
    
    private class AddFormProtocolQuery extends SqlUpdate {
        public AddFormProtocolQuery(DataSource ds) {
          this.setDataSource(ds);
        }

      protected int addFormProtocol( String formIdseq, String protocolIdseq, String createdBy) {
          String sql =
                "insert into protocol_qc_ext  (qc_idseq, proto_idseq, created_by ) values (?,?,?)";
            this.setSql(sql);
            declareParameter(new SqlParameter("qc_idseq", Types.VARCHAR));
            declareParameter(new SqlParameter("proto_idseq", Types.VARCHAR));
            declareParameter(new SqlParameter("created_by", Types.VARCHAR));
            compile();
            int res = update(new Object[]{formIdseq,protocolIdseq, createdBy});
            return res;
        }
      }
    
    /**
     * Get versions for public ids. -- Form Loader
     * @param publicIds
     * @return a list of form dtos with only seqid, public id and version populated
     */
    public List<FormV2> getExistingVersionsForPublicIds(List<String> publicIds) {
       
        String sql = 
        	"SELECT FV.QC_IDSEQ, FV.PUBLIC_ID, FV.VERSION, FV.WORKFLOW FROM FB_FORMS_VIEW fv WHERE FV.PUBLIC_ID in (:ids) " +
        			"ORDER BY FV.PUBLIC_ID, FV.VERSION";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", publicIds);
        
        FormV2 form = new FormV2TransferObject();

        List<FormV2> forms = 
        this.namedParameterJdbcTemplate.query(sql, params, 
        		new RowMapper<FormV2>() {
        	public FormV2 mapRow(ResultSet rs, int rowNum) throws SQLException {
            	FormV2 form = new FormV2TransferObject();
            	form.setIdseq(rs.getString("QC_IDSEQ"));
            	form.setPublicId(rs.getInt("PUBLIC_ID"));
            	form.setVersion(rs.getFloat("VERSION"));
            	form.setAslName(rs.getString("WORKFLOW"));
            	return form;
            }
        });
      
        
        return forms;
    }
    
    /**
     * Get form headers (public id and version) by a list of form seq ids
     * @param 
     * @return hashmap of formseqid/FormTransfertObject
     */
    public HashMap<String, FormV2TransferObject> getFormsBySeqids(List<String> seqids) {
       
        String sql = 
        	"SELECT QC_IDSEQ, PUBLIC_ID, VERSION FROM FB_FORMS_VIEW where QC_IDSEQ in (:ids)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", seqids);
        
        final HashMap<String, FormV2TransferObject> idMap = new HashMap<String, FormV2TransferObject>();

        List<FormV2> forms = 
        this.namedParameterJdbcTemplate.query(sql, params, 
        		new RowMapper<FormV2>() {
        	public FormV2TransferObject mapRow(ResultSet rs, int rowNum) throws SQLException {
        		String seqid = rs.getString("QC_IDSEQ");
        		FormV2TransferObject form = new FormV2TransferObject();
            	form.setPublicId(rs.getInt("PUBLIC_ID"));
            	form.setVersion(rs.getFloat("VERSION"));
            	idMap.put(seqid, form);
            	return form;
            }
        });
        
        return idMap;
    }
    
    public FormV2TransferObject getFormPublicIdVersion(String seqid) {
    	String sql = 
    			"SELECT QC_IDSEQ, PUBLIC_ID, VERSION FROM FB_FORMS_VIEW where QC_IDSEQ=:seqid";

    	MapSqlParameterSource params = new MapSqlParameterSource();
    	params.addValue("seqid", seqid);

    	List<FormV2TransferObject> forms = 
    			this.namedParameterJdbcTemplate.query(sql, params, 
    					new RowMapper<FormV2TransferObject>() {
    				public FormV2TransferObject mapRow(ResultSet rs, int rowNum) throws SQLException {

    					FormV2TransferObject form = new FormV2TransferObject();
    					form.setPublicId(rs.getInt("PUBLIC_ID"));
    					form.setVersion(rs.getFloat("VERSION"));
    					form.setFormIdseq(rs.getString("QC_IDSEQ"));

    					return form;
    				}
    			});

    	return forms.get(0);
    }
    
    /**
     * Get forms by a list of form seq ids
     * 
     * @param publicIds
     * @return a list of form dtos with only seqid, public id and version populated
     */
    public List<FormV2TransferObject> getFormHeadersBySeqids(List<String> seqids) {
       
        //String sql = 
        //	"SELECT * FROM FB_FORMS_VIEW where QC_IDSEQ in (:ids)";
        
        String sql = "SELECT qcve.*, FV.CONTEXT_NAME, FV.PROTOCOL_LONG_NAME FROM quest_contents_view_ext qcve, " +
        		" FB_FORMS_VIEW fv where qcve.QC_IDSEQ in (:ids) " +
        		" and QCVE.QC_IDSEQ = FV.QC_IDSEQ";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", seqids);
        
        List<FormV2TransferObject> forms = 
        this.namedParameterJdbcTemplate.query(sql, params, 
        		new RowMapper<FormV2TransferObject>() {
        	public FormV2TransferObject mapRow(ResultSet rs, int rowNum) throws SQLException {
        		FormV2TransferObject form = new FormV2TransferObject();
            	form.setPublicId(rs.getInt("QC_ID"));
            	form.setVersion(rs.getFloat("VERSION"));
            	form.setFormIdseq(rs.getString("QC_IDSEQ"));
            	
            	form.setLongName(rs.getString("LONG_NAME"));
            	form.setAslName(rs.getString("ASL_NAME"));
            	form.setContextName(rs.getString("CONTEXT_NAME"));
            	form.setFormType(rs.getString("QTL_NAME"));
            	form.setCreatedBy(rs.getString("CREATED_BY"));
            	form.setModifiedBy(rs.getString("MODIFIED_BY"));
            	form.setDateCreated(rs.getTimestamp("DATE_CREATED"));
            	form.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
            	form.setProtocolLongName(rs.getString("PROTOCOL_LONG_NAME"));
            
            	return form;
            }
        });
        
        return forms;
    }
    
    /**
     * Get form by a form seq id. This does NOT return protocol long name
     * 
     * @param publicIds
     * @return a list of form dtos with only seqid, public id and version populated
     */
    public FormV2TransferObject getFormHeadersBySeqid(String seqid) {

        String sql = "SELECT qcve.*, FV.CONTEXT_NAME, FV.PROTOCOL_LONG_NAME FROM quest_contents_view_ext qcve, " +
        		" FB_FORMS_VIEW fv where qcve.QC_IDSEQ=:seqid " +
        		" and QCVE.QC_IDSEQ = FV.QC_IDSEQ";
        
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("seqid", seqid);
        
        List<FormV2TransferObject> forms = 
        this.namedParameterJdbcTemplate.query(sql, params, 
        		new RowMapper<FormV2TransferObject>() {
        	public FormV2TransferObject mapRow(ResultSet rs, int rowNum) throws SQLException {
        		FormV2TransferObject form = new FormV2TransferObject();
        		form.setPublicId(rs.getInt("QC_ID"));
            	form.setVersion(rs.getFloat("VERSION"));
            	form.setFormIdseq(rs.getString("QC_IDSEQ"));
            	
            	form.setLongName(rs.getString("LONG_NAME"));
            	form.setAslName(rs.getString("ASL_NAME"));
            	form.setContextName(rs.getString("CONTEXT_NAME"));
            	form.setFormType(rs.getString("QTL_NAME"));
            	form.setCreatedBy(rs.getString("CREATED_BY"));
            	form.setModifiedBy(rs.getString("MODIFIED_BY"));
            	form.setDateCreated(rs.getTimestamp("DATE_CREATED"));
            	form.setDateModified(rs.getTimestamp("DATE_MODIFIED"));
            	form.setProtocolLongName("PROTOCOL_LONG_NAME FROM");
            
            	return form;
            }
        });
        
        return forms.get(0);
    }
    
    public String createFormComponent(FormV2 sourceForm) throws DMLException {
    	// check if the user has the privilege to create module
    	boolean create =
    			this.hasCreate(
    					sourceForm.getCreatedBy(), "QUEST_CONTENT", sourceForm.getConteIdseq());

    	if (!create) {
    		DMLException dmlExp = new DMLException("The user does not have the create form privilege.");
    		dmlExp.setErrorCode(INSUFFICIENT_PRIVILEGES);
    		throw dmlExp;
    	}
    	
    	String contentInsertSql =
    	          " INSERT INTO sbrext.quest_contents_view_ext " +
    	          " (qc_idseq, version, preferred_name, long_name, preferred_definition, " +
    	          "  conte_idseq, proto_idseq, asl_name, created_by, qtl_name, qcdl_name, change_note) " +
    	          " VALUES " + " (:qc_idseq, :version, :preferred_name, :long_name, :preferred_definition, " +
    	          "  :conte_idseq, :proto_idseq, :asl_name, :created_by, :qtl_name, :qcdl_name, :change_note ) ";
    	
    	String protocolIdSeq = null; //bypass
    	
    	String qcIdseq = generateGUID();
    	HashMap<String, String> namedParameters = new HashMap<String, String>();
    		
    	namedParameters.put("qc_idseq", qcIdseq);
    	namedParameters.put("version", String.valueOf(sourceForm.getVersion()));
    	namedParameters.put("preferred_name", generatePreferredName(sourceForm.getLongName()));
    	namedParameters.put("long_name", sourceForm.getLongName());
    	namedParameters.put("preferred_definition", sourceForm.getPreferredDefinition());
    	namedParameters.put("conte_idseq", sourceForm.getConteIdseq());
    	namedParameters.put("proto_idseq", protocolIdSeq);
    	namedParameters.put("asl_name", sourceForm.getAslName());
    	namedParameters.put("created_by", sourceForm.getCreatedBy());
    	namedParameters.put("qtl_name", sourceForm.getFormType());
    	namedParameters.put("qcdl_name", sourceForm.getFormCategory());
    	namedParameters.put("change_note", sourceForm.getChangeNote());
    	
    	int res = namedParameterJdbcTemplate.update(contentInsertSql, namedParameters);

    	if (res != 1) {
    		DMLException dmlExp = new DMLException("Did not succeed creating form record in the " +
    				" quest_contents_ext table.");
    		dmlExp.setErrorCode(ERROR_CREATEING_FORM);
    		throw dmlExp;
    	}
    	
    	
    	//TODO
    	/*
    	sourceForm.setFormIdseq(qcIdseq);
    	
    	//insert protocols
    	List protocols = sourceForm.getProtocols();
    	if ( protocols!=null && (!protocols.isEmpty())){
    		Iterator it = protocols.iterator();
    		List ids = new ArrayList();
    		while (it.hasNext()){
    			Protocol p = (Protocol)it.next();
    			ids.add(p.getProtoIdseq());
    		}
    		addFormProtocols(qcIdseq, ids, sourceForm.getCreatedBy());
    	}
    	*/
    	return qcIdseq;
    }
    
    /**
     * Due to the fact that protocol id is a required in table, this methode should be deprecated
     * 
     * @param protocolIds
     * @return
     */
    public HashMap<String, String> getProtocolSeqidsByIds(List<String> protocolIds) {
    	String sql = 
    			"select proto_idseq, protocol_Id, conte_idseq, long_name from sbrext.protocols_view_ext " +
    					" where protocol_id in (:ids)";

    	final HashMap<String, String> protoMap = 
    			new HashMap<String, String>();

    	MapSqlParameterSource params = new MapSqlParameterSource();
    	params.addValue("ids", protocolIds);

    	List<String> des = 
    			this.namedParameterJdbcTemplate.query(sql, params, 
    					new RowMapper<String>() {
    				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    					protoMap.put(rs.getString("protocol_Id"), rs.getString("proto_idseq"));

    					//Not really using this
    					return rs.getString("proto_idseq");
    				}
    			});

    	  return protoMap;
    }
    
    public boolean formProtocolExists(String formSeqId, String protocolSeqId) {
    	String sql = "select qc_idseq, proto_idseq from protocol_qc_ext " +
    			" where qc_idseq=:formSeqId and proto_idseq=:protocolSeqId";
    	
    	MapSqlParameterSource params = new MapSqlParameterSource();
    	params.addValue("formSeqId", formSeqId);
    	params.addValue("protocolSeqId", protocolSeqId);

    	List<String> rows = 
    			this.namedParameterJdbcTemplate.query(sql, params, 
    					new RowMapper<String>() {
    				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    					

    					//Not really using this
    					return rs.getString("qc_idseq");
    				}
    			});

    	  return (rows.size() > 0) ? true : false;
    }
    
    public Float getMaxFormVersion(int publicId){
        MaxFormVersionsQuery query = new MaxFormVersionsQuery();
        query.setDataSource(getDataSource());
        query.setSql(publicId);
        List result = (List) query.execute();

        if (result == null || result.isEmpty()){
            DMLException dmlExp = new DMLException("No matching record found.");
                  dmlExp.setErrorCode(NO_MATCH_FOUND);
              throw dmlExp;
        }

        Float maxVersion = (Float) (result.get(0));
        return maxVersion;
    }
    
    /**
     * Inner class that accesses database to get the maximum form version
     * by the form public id.
     */
    class  MaxFormVersionsQuery  extends MappingSqlQuery {
       public MaxFormVersionsQuery() {
       super();
      }

      public void setSql(int publicId) {
      String maxFormVersionsByPublicId =
               " SELECT MAX(version) from SBREXT.QUEST_CONTENTS_VIEW_EXT crf where (crf.QTL_NAME = 'CRF' or crf.QTL_NAME = 'TEMPLATE') AND QC_ID = " + publicId;
      super.setSql(maxFormVersionsByPublicId);
      compile();
     }

     protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
       Float maxVersion = rs.getFloat(1);
       return maxVersion;
     }

    }
    
    public int updateFormComponent(FormV2 newForm) throws DMLException {

        UpdateFormComponent  updateFormComponent  =
          new UpdateFormComponent (this.getDataSource());
        int res = updateFormComponent.updateFormFields(newForm);
        if (res != 1) {
             DMLException dmlExp = new DMLException("Did not succeed updating form record in the " +
            " quest_contents_ext table.");
    	       dmlExp.setErrorCode(ERROR_UPDATING_FORM);
               throw dmlExp;
        }
        return res;
    }
    
    /**
     * Inner class to update the Form component.
     *
     */
     //Form is now associated with multiple protocols. So form header does not include protocols any more
    private class UpdateFormComponent extends SqlUpdate {
      public UpdateFormComponent(DataSource ds) {
        /*String updateFormSql =
          " UPDATE quest_contents_ext SET " +
          " qtl_name = ?, conte_idseq = ?, asl_name = ?, preferred_name = ?, " +
          " preferred_definition = ?, proto_idseq = ?, long_name = ?, qcdl_name = ?, " +
          " modified_by = ? " +
          " WHERE qc_idseq = ? ";
         */
         
         //update form should keep the existing proto_idseq 
         //even though the protocols are stored in a new table in 3.1
         String updateFormSql =
           " UPDATE sbrext.quest_contents_view_ext SET " +
           " qtl_name = ?, conte_idseq = ?, asl_name = ?, preferred_name = ?, " +
           " preferred_definition = ?, long_name = ?, qcdl_name = ?, " +
           " modified_by = ?, change_note=? " +
           " WHERE qc_idseq = ? ";        

        this.setDataSource(ds);
        this.setSql(updateFormSql);
        declareParameter(new SqlParameter("qtl_name", Types.VARCHAR));
        declareParameter(new SqlParameter("conte_idseq", Types.VARCHAR));
        declareParameter(new SqlParameter("asl_name", Types.VARCHAR));
        declareParameter(new SqlParameter("preferred_name", Types.VARCHAR));
        declareParameter(new SqlParameter("preferred_definition", Types.VARCHAR));
        //declareParameter(new SqlParameter("proto_idseq", Types.VARCHAR));
        declareParameter(new SqlParameter("long_name", Types.VARCHAR));
        declareParameter(new SqlParameter("qcdl_name", Types.VARCHAR));
        declareParameter(new SqlParameter("modified_by", Types.VARCHAR));
        declareParameter(new SqlParameter("change_note", Types.VARCHAR));
       declareParameter(new SqlParameter("qc_idseq", Types.VARCHAR));
        compile();
      }

      protected int updateFormFields(FormV2  form) {

        //String protocolIdSeq = null;
        //form is now associated with mulitple forms. Form fields do not include protocols.
        /*if(form.getProtocol()!=null) {
           protocolIdSeq = form.getProtocol().getProtoIdseq();
        }
        */
    	String preferredName = generatePreferredName(form.getLongName());
        Object[] obj =
          new Object[] {
            form.getFormType(), form.getContext().getConteIdseq(), form.getAslName(),
            preferredName, form.getPreferredDefinition(),
            //protocolIdSeq, 
            form.getLongName(),
            form.getFormCategory(), form.getModifiedBy(), form.getChangeNote(), form.getFormIdseq()
          };
        int res = update(obj);

        return res;
      }
    }
    
    

    
    /**
     * Gets all the forms that match the given criteria
     *
     * @param formName
     * @param protocolIdseq
     * @param contextIdseq
     * @param workflow
     * @param category
     * @param type
     *
     * @return forms that match the criteria.
     */
    public Collection getAllForms(
      String formLongName,
      String protocolIdSeq,
      String contextIdSeq,
      String workflow,
      String categoryName,
      String type,
      String classificationIdseq,
      String contextRestriction,
      String publicId,
      String version,
      String moduleLongName,
      String cdePublicId,
      String createdBy,
      String start,
      String size) {
      FormQuery query = new FormQuery();
      query.setDataSource(getDataSource());
      query.setSql(
        formLongName, protocolIdSeq, contextIdSeq, workflow, categoryName, type,
        classificationIdseq,contextRestriction, publicId, version, moduleLongName, cdePublicId, createdBy, start, size);

      Collection forms =  query.execute();
      //add protocols
      fetchProtocols(forms);
      return forms;
    }


   //Publis Change Order
    /**
     * Gets all the forms that has been classified by this Classification
     *
     * @param <b>formId</b> Idseq of the Classification
     *
     * @return <b>Collection</b> List of Forms
     */
    public Collection getAllFormsForClassification(String classificationIdSeq, String start, String size)
    {
      FormByClassificationQuery query = new FormByClassificationQuery();
      query.setDataSource(getDataSource());
      query.setQuerySql(classificationIdSeq, start, size);
      List forms = query.execute();
      fetchProtocols(forms);
      return forms;
    }
    
    /**
     * Gets count of all the forms that match the given criteria
     *
     * @param formName
     * @param protocolIdseq
     * @param contextIdseq
     * @param workflow
     * @param category
     * @param type
     *
     * @return forms that match the criteria.
     */
    public Integer getFormCount(
      String formLongName,
      String protocolIdSeq,
      String contextIdSeq,
      String workflow,
      String categoryName,
      String type,
      String classificationIdseq,
      String contextRestriction,
      String publicId,
      String version,
      String moduleLongName,
      String cdePublicId,
      String createdBy) {
      FormCountQuery query = new FormCountQuery();
      query.setDataSource(getDataSource());
      query.setSql(
        formLongName, protocolIdSeq, contextIdSeq, workflow, categoryName, type,
        classificationIdseq,contextRestriction, publicId, version, moduleLongName, cdePublicId, createdBy);

      Collection rs =  query.execute();
      
      Integer formCount = (Integer)((ArrayList) rs).get(0);

      return formCount;
    }
    
    /**
     * Gets count of all the forms that has been classified by this Classification
     *
     * @param <b>formId</b> Idseq of the Classification
     *
     * @return <b>Collection</b> List of Forms
     */
    public Integer getFormClassificationCount(String classificationIdSeq)
    {
      FormCountClassificationQuery query = new FormCountClassificationQuery();
      query.setDataSource(getDataSource());
      query.setQuerySql(classificationIdSeq);
      Collection rs = query.execute();
      
      Integer formCount = (Integer)((ArrayList) rs).get(0);

      return formCount;
    }
    
    private void fetchProtocols( Collection forms){
        if (forms == null || forms.isEmpty()){
            return;
        }

        Iterator it = forms.iterator();
        while (it.hasNext()){
            Form form = (Form)it.next();
            form.setProtocols(getProtocols(form));
        }
        return;
    }

    /**
     * Inner class that accesses database to get all the forms and modules that
     * match the given criteria
     */
    class FormQuery extends MappingSqlQuery {
      FormQuery() {
        super();
      }

      public void setSql(
        String formLongName,
        String protocolIdSeq,
        String contextIdSeq,
        String workflow,
        String categoryName,
        String type,
        String classificationIdseq,
        String contextRestriction,
        String publicId,
        String version,
        String moduleName,
        String cdePublicId,
        String createdBy,
        String start,
        String size) {

        String selectWhat = "SELECT distinct f.qc_idseq, f.version, f.type, f.conte_idseq, f.CATEGORY_NAME, f.workflow, f.preferred_name, f.definition, " +
                            " f.long_name, f.context_name, f.public_id, latest_version_ind,  f.DATE_MODIFIED, f.DATE_CREATED ";
        StringBuffer fromWhat = new StringBuffer(" FROM FB_FORMS_VIEW f ");
        StringBuffer initialWhere = new StringBuffer();
        boolean hasWhere = false;
        if (StringUtils.doesValueExist(moduleName) || StringUtils.doesValueExist(cdePublicId)){
          fromWhat.append(", FB_QUEST_MODULE_VIEW q ");
          initialWhere.append(" where ( f.QC_IDSEQ = q.FORM_IDSEQ )");
          hasWhere = true;
        }
        if (StringUtils.doesValueExist(protocolIdSeq) || StringUtils.doesValueExist(protocolIdSeq)){
          fromWhat.append(", protocol_qc_ext p ");
          if (hasWhere){
              initialWhere.append(" AND ( f.QC_IDSEQ = p.qc_idseq) and (p.proto_idseq IN (" + protocolIdSeq + ")) ");
          }else{
              initialWhere.append(" where ( f.QC_IDSEQ = p.qc_idseq) and (p.proto_idseq IN (" + protocolIdSeq + ")) ");
              hasWhere = true;
          }
        }
        String whereClause = makeWhereClause(
                            formLongName, protocolIdSeq, contextIdSeq, workflow, categoryName,
                            type, classificationIdseq,contextRestriction,
                            publicId, version, moduleName, cdePublicId, createdBy, hasWhere);
        String sql = selectWhat.toString() + " " + fromWhat.toString() + " "
                      + initialWhere.toString() + whereClause;
        
        int startRow = (Integer.parseInt(start) -1) * Integer.parseInt(size) + 1;
        int endRow = (Integer.parseInt(start) * Integer.parseInt(size) );
        String wrapSql1 = "SELECT * from ( SELECT /*+ FIRST_ROWS(" + size + ") */ a.*, ROWNUM rnum from ( ";
        String wrapSql2 = " ) a where ROWNUM <=" + endRow + " ) where rnum  >= " + startRow;
        super.setSql(wrapSql1 + sql + wrapSql2);
        

      }   
      
      /**
       * 3.0 Refactoring- Removed JDBCTransferObject
       */
       protected Object mapRow(
         ResultSet rs,
         int rownum) throws SQLException {
         String formName = rs.getString("LONG_NAME");

         Form form = new FormTransferObject();
   /*      String selectWhat = "distinct f.qc_idseq, f.version, f.type, f.conte_idseq, f.CATEGORY_NAME, f.workflow, f.preferred_name, f.definition, " +
                             " f.long_name, f.context_name, f.public_id, latest_version_ind, f. ";
   */
        form.setFormIdseq(rs.getString(1)); // QC_IDSEQ
        form.setIdseq(rs.getString(1));
        form.setLongName(rs.getString(9)); //LONG_NAME
        form.setPreferredName(rs.getString(7)); // PREFERRED_NAME

        //setContext(new ContextTransferObject(rs.getString("context_name")));
        ContextTransferObject contextTransferObject = new ContextTransferObject();
        contextTransferObject.setConteIdseq(rs.getString(4)); //CONTE_IDSEQ
        contextTransferObject.setName(rs.getString(10)); // CONTEXT_NAME
        form.setContext(contextTransferObject);
        form.setDateModified(rs.getTimestamp(13));

        //multiple protcols will be set later
        form.setFormType(rs.getString(3)); // TYPE
        form.setAslName(rs.getString(6)); // WORKFLOW
        form.setVersion(new Float(rs.getString(2))); // VERSION
        form.setPublicId(rs.getInt(11)); //Public ID
        form.setPreferredDefinition(rs.getString(8)); // PREFERRED_DEFINITION
        form.setCreatedBy(rs.getString(14)); // CREATED_BY
        form.setFormCategory(rs.getString(5));

       return form;
       }
       
      public String makeWhereClause(
    	      String formName,
    	      String protocol,
    	      String context,
    	      String workflow,
    	      String category,
    	      String type,
    	      String classificationIdseq,
    	      String contextRestriction,
    	      String publicId,
    	      String version,
    	      String moduleName,
    	      String cdePublicId,
    	      String createdBy,
    	      boolean hasWhere)
    	    {
    	      String where = "";
    	      StringBuffer whereBuffer = new StringBuffer("");

    	      if (StringUtils.doesValueExist(formName)) {
    	        String temp = StringUtils.strReplace(formName, "*", "%");
    	        temp = StringUtils.strReplace(temp, "'", "''");

    	        if (hasWhere) {
    	          whereBuffer.append(
    	            " AND UPPER(f.LONG_NAME) LIKE " + "UPPER('" + temp + "')");
    	        }
    	        else {
    	          whereBuffer.append(
    	            " WHERE UPPER(f.LONG_NAME) LIKE " + "UPPER('" + temp + "')");
    	          hasWhere = true;
    	        }
    	      }
/*
    	      if (StringUtils.doesValueExist(protocol)) {
    	        if (hasWhere) {
    	          whereBuffer.append(" AND p.PROTO_IDSEQ IN (" + protocol + ")");
    	        }
    	        else {
    	          whereBuffer.append(" WHERE p.PROTO_IDSEQ IN (" + protocol + ")");
    	          hasWhere = true;
    	        }
    	      }
*/

    	      //context filter or exclude contexts
    	      if (StringUtils.doesValueExist(context)) {
    	        if (hasWhere) {
    	          whereBuffer.append(" AND f.CONTE_IDSEQ IN (" + context + ")");
    	        }
    	        else {
    	          whereBuffer.append(" WHERE f.CONTE_IDSEQ IN (" + context + ")");
    	          hasWhere = true;
    	        }
    	      }
    	      else if (StringUtils.doesValueExist(contextRestriction)) {
    	        if (hasWhere) {
    	          whereBuffer.append(" AND ");
    	        }
    	        else {
    	          whereBuffer.append(" WHERE ");
    	          hasWhere = true;
    	        }
    	        whereBuffer.append("(f.CONTEXT_NAME not in (" + contextRestriction + "))");
    	      }

    	      if (StringUtils.doesValueExist(workflow)) {
    	        if (hasWhere) {
    	          whereBuffer.append(" AND f.WORKFLOW ='" + workflow + "'");
    	        }
    	        else {
    	          whereBuffer.append(" WHERE f.WORKFLOW ='" + workflow + "'");
    	          hasWhere = true;
    	        }
    	      }

    	      if (StringUtils.doesValueExist(category)) {
    	        if (hasWhere) {
    	          whereBuffer.append(" AND f.CATEGORY_NAME ='" + category + "'");
    	        }
    	        else {
    	          whereBuffer.append(" WHERE f.CATEGORY_NAME ='" + category + "'");
    	          hasWhere = true;
    	        }
    	      }

    	      if (StringUtils.doesValueExist(type)) {
    	        if (hasWhere) {
    	          whereBuffer.append(" AND f.TYPE ='" + type + "'");
    	        }
    	        else {
    	          whereBuffer.append(" WHERE f.TYPE ='" + type + "'");
    	          hasWhere = true;
    	        }
    	      }

    	      if (StringUtils.doesValueExist(classificationIdseq)) {
    	        if (hasWhere) {
    	          whereBuffer.append(
    	            " AND f.QC_IDSEQ in (select ac_idseq from sbr.ac_csi_view where CS_CSI_IDSEQ IN (" +
    	            classificationIdseq + "))");
    	        }
    	        else {
    	          whereBuffer.append(
    	            " WHERE f.QC_IDSEQ in (select ac_idseq from sbr.ac_csi_view where CS_CSI_IDSEQ IN (" +
    	            classificationIdseq + "))");
    	          hasWhere = true;
    	        }
    	      }

    	        if (StringUtils.doesValueExist(publicId)) {
    	          if (hasWhere) {
    	            whereBuffer.append(" AND (f.PUBLIC_ID IN (" + publicId + "))");
    	          }
    	          else {
    	            whereBuffer.append(" WHERE (f.PUBLIC_ID IN (" + publicId + "))");
    	            hasWhere = true;
    	          }
    	        }

    	        if (StringUtils.doesValueExist(version) && "latestVersion".equals(version)) {
    	            if (hasWhere) {
    	              whereBuffer.append(" AND (f.LATEST_VERSION_IND='Yes')");
    	            }
    	            else {
    	              whereBuffer.append(" WHERE (f.LATEST_VERSION_IND='Yes')");
    	              hasWhere = true;
    	            }
    	        }//eles indicate all versions.

    	         if (StringUtils.doesValueExist(moduleName)) {
    	            String temp = StringUtils.strReplace(moduleName, "*", "%");
    	            temp = StringUtils.strReplace(temp, "'", "''");
    	             if (hasWhere) {
    	               whereBuffer.append(" AND (UPPER(q.MODULE_NAME) like UPPER('" + temp + "'))");
    	             }
    	             else {
    	               whereBuffer.append(" WHERE (UPPER(q.MODULE_NAME) like UPPER('" + temp + "'))");
    	               hasWhere = true;
    	             }
    	         }

    	        if (StringUtils.doesValueExist(cdePublicId)) {
    	            if (hasWhere) {
    	              whereBuffer.append(" AND (q.CDE_ID='" + cdePublicId + "')");
    	            }
    	            else {
    	              whereBuffer.append(" WHERE (q.CDE_ID='" + cdePublicId + "')");
    	              hasWhere = true;
    	            }
    	        }
    	        
      	      
    	        if (StringUtils.doesValueExist(createdBy)) {
	    	        if (hasWhere) {
		  	          whereBuffer.append(
		  	            " AND UPPER(f.CREATED_BY) = " + "UPPER('" + createdBy + "')");
		  	        }
		  	        else {
		  	          whereBuffer.append(
		  	            " WHERE UPPER(f.CREATED_BY) = " + "UPPER('" + createdBy + "')");
		  	          hasWhere = true;
		  	        }
    	        }

    	      where = whereBuffer.toString();

    	      return where;
    	    }
    	  }
    
    /**
     * Inner class that accesses database to get all the forms and modules that
     * match the given criteria
     */
    class FormCountQuery extends FormQuery {
    	FormCountQuery() {
        super();
      }

      public void setSql(
        String formLongName,
        String protocolIdSeq,
        String contextIdSeq,
        String workflow,
        String categoryName,
        String type,
        String classificationIdseq,
        String contextRestriction,
        String publicId,
        String version,
        String moduleName,
        String cdePublicId,
        String createdBy) {

        String selectWhat = "SELECT COUNT(*) ";
        StringBuffer fromWhat = new StringBuffer(" FROM FB_FORMS_VIEW f ");
        StringBuffer initialWhere = new StringBuffer();
        boolean hasWhere = false;
        if (StringUtils.doesValueExist(moduleName) || StringUtils.doesValueExist(cdePublicId)){
          fromWhat.append(", FB_QUEST_MODULE_VIEW q ");
          initialWhere.append(" where ( f.QC_IDSEQ = q.FORM_IDSEQ )");
          hasWhere = true;
        }
        if (StringUtils.doesValueExist(protocolIdSeq) || StringUtils.doesValueExist(protocolIdSeq)){
          fromWhat.append(", protocol_qc_ext p ");
          if (hasWhere){
              initialWhere.append(" AND ( f.QC_IDSEQ = p.qc_idseq) and (p.proto_idseq IN (" + protocolIdSeq + ")) ");
          }else{
              initialWhere.append(" where ( f.QC_IDSEQ = p.qc_idseq) and (p.proto_idseq IN (" + protocolIdSeq + ")) ");
              hasWhere = true;
          }
        }
        String whereClause = makeWhereClause(
                            formLongName, protocolIdSeq, contextIdSeq, workflow, categoryName,
                            type, classificationIdseq,contextRestriction,
                            publicId, version, moduleName, cdePublicId, createdBy, hasWhere);
        String sql = selectWhat.toString() + " " + fromWhat.toString() + " "
                      + initialWhere.toString() + whereClause;

        super.setSql(sql);
        

      }   
      
      /**
       * 3.0 Refactoring- Removed JDBCTransferObject
       */
       protected Object mapRow(
         ResultSet rs,
         int rownum) throws SQLException {
    	   return new Integer(rs.getInt(1));
       }
   	  }

    	//Publish Change Order

	  /**
	   * Inner class that accesses database to get all the forms
	   * match the given criteria
	   */
	  class FormByClassificationQuery extends MappingSqlQuery {
	    FormByClassificationQuery() {
	      super();
	    }

	    public void setQuerySql(String csidSeq, String start, String size) {
	     String querySql = " SELECT f.* FROM FB_FORMS_VIEW f, sbr.cs_csi_view csc,sbr.ac_csi_view acs "
	        + " where  csc.cs_idseq IN ("+ csidSeq +")"
	        + " and csc.cs_csi_idseq = acs.cs_csi_idseq "
					+ " and acs.AC_IDSEQ=f.QC_IDSEQ "
	        + " ORDER BY upper(protocol_long_name), upper(context_name)";
	     
          int startRow = (Integer.parseInt(start) -1) * Integer.parseInt(size) + 1;
          int endRow = (Integer.parseInt(start) * Integer.parseInt(size) );
          String wrapSql1 = "SELECT * from ( SELECT /*+ FIRST_ROWS(" + size + ") */ a.*, ROWNUM rnum from ( ";
          String wrapSql2 = " ) a where ROWNUM <=" + endRow + " ) where rnum  >= " + startRow;
          super.setSql(wrapSql1 + querySql + wrapSql2);
	    }


	    protected Object mapRow(
	      ResultSet rs,
	      int rownum) throws SQLException {
	      String formName = rs.getString("LONG_NAME");

	      return new JDBCFormTransferObject(rs);
	    }

	  }  
	  
	  /**
	   * Inner class that accesses database to get all the forms
	   * match the given criteria
	   */
	  class FormCountClassificationQuery extends MappingSqlQuery {
		  FormCountClassificationQuery() {
	      super();
	    }

	    public void setQuerySql(String csidSeq) {
	     String querySql = " SELECT COUNT(*) FROM FB_FORMS_VIEW f, sbr.cs_csi_view csc,sbr.ac_csi_view acs "
	        + " where  csc.cs_idseq IN ("+ csidSeq +")"
	        + " and csc.cs_csi_idseq = acs.cs_csi_idseq "
					+ " and acs.AC_IDSEQ=f.QC_IDSEQ "
	        + " ORDER BY upper(protocol_long_name), upper(context_name)";
	     
          super.setSql(querySql);
	    }


       protected Object mapRow(
         ResultSet rs,
         int rownum) throws SQLException {
    	   return new Integer(rs.getInt(1));
       }

	  }
    	  
    		
    		
		public FormV2 getFormDetailsV2(String formPK)
	    {
	        FormV2 myForm = null;
	        JDBCFormDAOV2 fdao = new JDBCFormDAOV2(getDataSource());
	        JDBCQuestionRepititionDAOV2 qrdao = new JDBCQuestionRepititionDAOV2(getDataSource());
	        JDBCFormInstructionDAOV2 fInstrdao = new JDBCFormInstructionDAOV2(getDataSource());

	        JDBCModuleDAOV2 mdao = new JDBCModuleDAOV2(getDataSource());
	        JDBCModuleInstructionDAOV2 mInstrdao = new JDBCModuleInstructionDAOV2(getDataSource());

	        JDBCQuestionDAOV2 qdao = new JDBCQuestionDAOV2(getDataSource());
	        JDBCQuestionInstructionDAOV2 qInstrdao = new JDBCQuestionInstructionDAOV2(getDataSource());

//    	        FormValidValueDAO vdao = daoFactory.getFormValidValueDAO();
	        JDBCFormValidValueInstructionDAOV2 vvInstrdao = new JDBCFormValidValueInstructionDAOV2(getDataSource());
	        JDBCContextDAOV2 cdao = new JDBCContextDAOV2(getDataSource());

	        JDBCFormDAOV2 fV2dao = new JDBCFormDAOV2(getDataSource());
	        myForm = fV2dao.findFormV2ByPrimaryKey(formPK);

	        List refDocs =
	            fdao.getAllReferenceDocuments(formPK, ReferenceDocument.REF_DOC_TYPE_IMAGE);
	        myForm.setReferenceDocs(refDocs);

	        List instructions = fInstrdao.getInstructions(formPK);
	        myForm.setInstructions(instructions);

	        List footerInstructions = fInstrdao.getFooterInstructions(formPK);
	        myForm.setFooterInstructions(footerInstructions);

	        List modules = (List)fdao.getModulesInAForm(formPK);
	        Iterator mIter = modules.iterator();
	        List questions;
	        Iterator qIter;
	        List values;
	        Iterator vIter;
	        Module block;
	        Question term;
	        FormValidValue value;

	        Map<String,FormElement> possibleTargets = new HashMap<String,FormElement>();
	        List<TriggerAction>  allActions = new ArrayList<TriggerAction>();

	        while (mIter.hasNext())
	        {
	            block = (Module)mIter.next();
	            block.setForm(myForm);

	            String moduleId = block.getModuleIdseq();

	            List mInstructions = mInstrdao.getInstructions(moduleId);
	            block.setInstructions(mInstructions);

	            questions = (List)mdao.getQuestionsInAModuleV2(moduleId);
	            qIter = questions.iterator();

	            while (qIter.hasNext())
	            {
	                term = (Question)qIter.next();
	                term.setModule(block);
	                String termId = term.getQuesIdseq();
	                 possibleTargets.put(termId,term); //one of the possible targets

	                if (term.getDataElement() != null){
	                    List<ReferenceDocument> deRefDocs =
	                    getReferenceDocuments(term.getDataElement().getDeIdseq());
	                    term.getDataElement().setReferenceDocs(deRefDocs);
	                    
	                    
	                    //set the value domain 
	                    ValueDomain currentVd = term.getDataElement().getValueDomain();                    
	                    if (currentVd!=null && currentVd.getVdIdseq()!=null){
	                    	JDBCValueDomainDAOV2 vdDAO = new JDBCValueDomainDAOV2(getDataSource());                      
	                        ValueDomainV2 vd = vdDAO.getValueDomainV2ById(currentVd.getVdIdseq());
	                        
	                        //set concept to the value domain.
	                        String cdrId = vd.getConceptDerivationRule().getIdseq();
	                        if (cdrId!=null){
	                        	JDBCConceptDAOV2 conceptDAO = new JDBCConceptDAOV2(getDataSource());
	                            ConceptDerivationRule cdr =  
	                                conceptDAO.findConceptDerivationRule(vd.getConceptDerivationRule().getIdseq());
	                            vd.setConceptDerivationRule(cdr);
	                        }                        
	                        term.getDataElement().setValueDomain(vd);
	                    }//end of if   
	                }
	                List qInstructions = qInstrdao.getInstructions(termId);
	                term.setInstructions(qInstructions);
	                                
	                
	                values = (List)qdao.getValidValues(termId);
	                term.setValidValues(values);

	                vIter = values.iterator();
	                while (vIter.hasNext())
	                {
	                    FormValidValue vv = (FormValidValue)vIter.next();
	                    vv.setQuestion(term);
	                    String vvId = vv.getValueIdseq();
	                    List vvInstructions = vvInstrdao.getInstructions(vvId);
	                    vv.setInstructions(vvInstructions);
	                    //Set Skip Patterns
	                    List<TriggerAction> actions= getAllTriggerActionsForSource(vvId);
	                    allActions.addAll(actions);
	                    setSourceForTriggerActions(vv,actions);
	                    vv.setTriggerActions(actions);
	                }
	                
	                //set question default 
	                term = setQuestionDefaultValue(term);
	                List<QuestionRepitition> qReps = qrdao.getQuestionRepititions(term.getQuesIdseq());
	                setQuestionRepititionDefaults(qReps,values);
	                term.setQuestionRepitition(qReps);                
	            }

	            block.setQuestions(questions);
	            //Set Skip Patterns
	             possibleTargets.put(moduleId,block); //one of the possible targets
	            List<TriggerAction> actions= getAllTriggerActionsForSource(moduleId);
	            allActions.addAll(actions);
	            setSourceForTriggerActions(block,actions);
	            block.setTriggerActions(actions);
	        }

	        myForm.setModules(modules);
	        Context context = null;
	        try
	        {
	        	context = cdao.getContextByName(CaDSRUtil.getDefaultContextName());
	        }
	        catch ( IOException e) {
	        	context = cdao.getContextByName(CaDSRConstants.CONTEXT_NCIP);
	        }

	        myForm
	        .setPublished(fdao.isFormPublished(myForm.getIdseq(), context.getConteIdseq()));

	        //Collection formCSIs = fdao.retrieveClassifications(formPK);
	        //myForm.setClassifications(formCSIs);
	        setTargetsForTriggerActions(possibleTargets,allActions);

	        // set contact communication info
	        JDBCContactCommunicationDAOV2 ccdao = new JDBCContactCommunicationDAOV2(getDataSource());
	        myForm.setContactCommunicationV2(ccdao.getContactCommunicationV2sForAC(myForm.getIdseq()));
	// test hack using another AC's data to get contact communication by org  
	//myForm.setContactCommunicationV2(ccdao.getContactCommunicationV2sForAC("0FBEFA30-4787-6717-E044-0003BA3F9857"));
	// test hack using another AC's data to get contact communication person  
	//myForm.setContactCommunicationV2(ccdao.getContactCommunicationV2sForAC("21C05FEA-450A-716D-E044-0003BA3F9857"));

    	        
    	        return myForm;
    	    }
    		
    	    private List<ReferenceDocument> getReferenceDocuments(String acIdseq)
    	    {
    	    	JDBCReferenceDocumentDAOV2 myDAO = new JDBCReferenceDocumentDAOV2(getDataSource());
    	        List refDocs = myDAO.getAllReferenceDocuments(acIdseq, 
    	                        null);
    	        return refDocs;        
    	    }
   
    	    private void setTargetsForTriggerActions(Map<String,FormElement> possibleTargetMap, List<TriggerAction> actions)
    	    {
    	        for(TriggerAction action : actions)
    	        {
    	            FormElement element = possibleTargetMap.get(action.getActionTarget().getIdseq());
    	            // the target FormElement may not may not in the possibleTargetMap considering it may
	            //skip to a FormElement in a different module for example.
	            //In case the target FormElement is not in the possibleTargetMap, element will be null,
	            //In this case just keep the idseq which is already in the target action.
	            if (element!=null){
	                action.setActionTarget(element);
	            }    

	        }
	    }
	    
	    public List<TriggerAction> getAllTriggerActionsForSource(String sourceId)
	    {
	    	JDBCTriggerActionDAOV2 dao = new JDBCTriggerActionDAOV2(getDataSource());
	        List<TriggerAction> triggerActions = dao.getTriggerActionsForSource(sourceId);
	        for(TriggerAction action:triggerActions)
	        {
	            action.setProtocols(dao.getAllProtocolsForTriggerAction(action.getIdSeq()));
	            action.setClassSchemeItems(dao.getAllClassificationsForTriggerAction(action.getIdSeq()));
	        }
	        return triggerActions;
	    }
	    
	    private void setSourceForTriggerActions(FormElement source, List<TriggerAction> actions)
	    {
	        for(TriggerAction action : actions)
	        {
	            action.setActionSource(source);

	        }
	    }
	    
	    private Question setQuestionDefaultValue(Question term ){
	    	JDBCQuestionDAOV2 dao = new JDBCQuestionDAOV2(getDataSource());
	        term = dao.getQuestionDefaultValue(term);
	        return term;
	    }
	    
	    /**
	     * Get FormValidValue from the question and assign it to Question repitition.
	     * This is done since the Question repitition quesry only retrives ids
	     * @param qreps
	     * @param values
	     */
	    private void setQuestionRepititionDefaults(List<QuestionRepitition> qreps,List values)
	    {
	        for(QuestionRepitition repitition:qreps)
	        {
	            if(repitition.getDefaultValidValue()!=null)
	            {
	              if(values!=null)
	              {
	                  Iterator it = values.iterator();
	                  FormValidValue value =null;
	                  while(it.hasNext())
	                  {
	                       value = (FormValidValue)it.next();
	                       if(value.getValueIdseq().equals(repitition.getDefaultValidValue().getValueIdseq()))
	                       {
	                           repitition.setDefaultValidValue(value);
	                       }
	                  }
	                  
	              }
	              
	            }
	        }
	    }
	    
	    /**
	     * Checks if the Form Component is published
	     *
	     * @param <b>acId</b> Idseq of an admin component
	     */
	    public boolean isFormPublished(String formIdSeq,String conteIdSeq) {
	      return this.isClassifiedForPublish(formIdSeq,conteIdSeq);

	    }    	
	    
	    /**
	     * Creates a new form version
	     *
	     * @param <b>formIdSeq</b> existing form IdSeq.
	     * @param <b>newVersionNumber</b> new version number
	     *
	     * @return <b>String</b> the new Form Idseq.
	     * @throws <b>DMLException</b>
	     */
	  public String createNewFormVersion(String formIdSeq, Float newVersionNumber, String changeNote, String createdBy) 
			  throws DMLException {
	      VersionForm vForm = new VersionForm(this.getDataSource());

	      Map out = vForm.execute(formIdSeq, newVersionNumber, changeNote, createdBy);

	      if ((out.get("p_return_code")) == null) {
	        /*newForm.setFormIdseq((String) out.get("p_new_idseq"));
	        return newForm;*/
	        return (String) out.get("p_new_idseq");
	      }
	      else {
	          DMLException dmlExp = new DMLException((String) out.get("p_return_desc"));
	          dmlExp.setErrorCode(ERROR_CREATE_NEW_VERSION_FORM);
	           throw dmlExp;
	      }
	  }
	    
	    
	    /**
	     * Inner class that make a new version of the source form
	     */
	    private class VersionForm extends StoredProcedure {
	      public VersionForm(DataSource ds) {
	        super(ds, "Sbrext_Form_Builder_Pkg.CRF_VERSION");
	        declareParameter(new SqlParameter("P_Idseq", Types.VARCHAR));
	        declareParameter(new SqlParameter("p_version", Types.FLOAT));
	        declareParameter(new SqlParameter("p_change_note", Types.VARCHAR));
	        declareParameter(new SqlParameter("p_Created_by", Types.VARCHAR));

	        declareParameter(new SqlOutParameter("p_new_idseq", Types.VARCHAR));
	        declareParameter(new SqlOutParameter("p_return_code", Types.VARCHAR));
	        declareParameter(new SqlOutParameter("p_return_desc", Types.VARCHAR));
	        compile();
	      }

	      public Map execute(
	        String sourceFormId,
	        Float newVersionNumber,
	        String changeNote,
	        String createdBy) {
	        Map in = new HashMap();

	        in.put("P_Idseq", sourceFormId);
	        in.put("p_version", newVersionNumber);
	        in.put("p_change_note", changeNote);
	        in.put("p_Created_by", createdBy);
	        Map out = execute(in);
	        return out;
	      }
	    }//end of private class
    	    
	    /**
	     * Creates a new verion of the form. It only makes a very shallow copy of the old verion - only header info.
	     *
	     * @param <b>formIdSeq</b> existing form IdSeq.
	     * @param <b>newVersionNumber</b> new version number
	     *
	     * @return <b>String</b> the new Form Idseq.
	     * @throws <b>DMLException</b>
	     */
	  public String createNewFormVersionShellOnly(String formIdSeq, Float newVersionNumber, String changeNote, String createdBy) 
			  throws DMLException {
		  VersionFormShellOnly vForm = new VersionFormShellOnly(this.getDataSource());

	      Map out = vForm.execute(formIdSeq, newVersionNumber, changeNote, createdBy);

	      if ((out.get("p_return_code")) == null) {
	        /*newForm.setFormIdseq((String) out.get("p_new_idseq"));
	        return newForm;*/
	        return (String) out.get("p_new_idseq");
	      }
	      else {
	          DMLException dmlExp = new DMLException((String) out.get("p_return_desc"));
	          dmlExp.setErrorCode(ERROR_CREATE_NEW_VERSION_FORM);
	           throw dmlExp;
	      }
	  }
	    
	    
	    /**
	     * Inner class that make a new version of the source form
	     */
	    private class VersionFormShellOnly extends StoredProcedure {
	      public VersionFormShellOnly(DataSource ds) {
	        super(ds, "Sbrext_Form_Builder_Pkg.CRF_VERSION_SHELL");
	        declareParameter(new SqlParameter("P_Idseq", Types.VARCHAR));
	        declareParameter(new SqlParameter("p_version", Types.FLOAT));
	        declareParameter(new SqlParameter("p_change_note", Types.VARCHAR));
	        declareParameter(new SqlParameter("p_Created_by", Types.VARCHAR));

	        declareParameter(new SqlOutParameter("p_new_idseq", Types.VARCHAR));
	        declareParameter(new SqlOutParameter("p_return_code", Types.VARCHAR));
	        declareParameter(new SqlOutParameter("p_return_desc", Types.VARCHAR));
	        compile();
	      }

	      public Map execute(
	        String sourceFormId,
	        Float newVersionNumber,
	        String changeNote,
	        String createdBy) {
	        Map in = new HashMap();

	        in.put("P_Idseq", sourceFormId);
	        in.put("p_version", newVersionNumber);
	        in.put("p_change_note", changeNote);
	        in.put("p_Created_by", createdBy);

	        Map out = execute(in);
	        return out;
	      }
	    }//end of private class
    	    
	    public static void main(String[] args) {
	    	
	    	//DataSource ds = DataSourceUtil.getDriverManagerDS(
	    	//		"oracle.jdbc.OracleDriver", 
	    	//		"jdbc:oracle:thin:@ncidb-dsr-d:1551:DSRDEV", 
	    	//		"FORMBUILDER", "FORMBUILDER");
	    	
	    	DataSource ds = new DriverManagerDataSource("jdbc:oracle:thin:@ncidb-dsr-q.nci.nih.gov:1551:dsrqa", 
	    			"formloader", "F0rmloader");
	    	
	    	JDBCFormDAOV2 form2Dao = new JDBCFormDAOV2(ds);   	
    	/*
	    	FormV2 form = form2Dao.findFormV2ByPrimaryKey("BFC76B3A-AC92-45C7-E040-BB89AD430B2C");
	    	String workflow = form.getAslName();
	    	System.out.println("Got workflow: " + workflow);

	    	String xmlFile = "";
			try {
				xmlFile = FormXMLConverter.instance().convertFormToXML(form);
			}
			catch (Exception e) 
			{
				System.out.println(e);
			}
			System.out.println("XML****");
			System.out.println(xmlFile);   */
			
			//Collection forms = form2Dao.getAllForms("", "'CFA642A2-2438-2400-E040-BB89AD432B4D','CFA63964-B053-288D-E040-BB89AD437F2C'", "", "", "", "", "", "", "", "", "", "", "","2","1");
	    	
	    	//Collection forms = form2Dao.getAllFormsForClassification("'3D3E3B76-50FC-45E7-E044-0003BA3F9857','2C39E82F-DEC1-6BF9-E044-0003BA3F9857'","1","1");
	    	
	    	//Integer total = form2Dao.getFormCount("", "'CFA642A2-2438-2400-E040-BB89AD432B4D','CFA63964-B053-288D-E040-BB89AD437F2C'", "", "", "", "", "", "", "", "", "", "", "");
	    	
	    	FormV2 form = form2Dao.getFormDetailsV2("B6FE2275-F5E5-0321-E040-BB89AD43405B");
			
			
	    }
	    
	    public float getLatestVersionForForm(int publicId) {
	    	String sql = "select VERSION from sbrext.quest_contents_view_ext " +
	    			" where QC_ID=:publicId and LATEST_VERSION_IND='Yes'";
	    	
	    	 MapSqlParameterSource params = new MapSqlParameterSource();
			 params.addValue("publicId", publicId);
			 
			 List<Float> latest = this.namedParameterJdbcTemplate.query(sql, params, 
			     		new RowMapper<Float>() {
			     	public Float mapRow(ResultSet rs, int rowNum) throws SQLException {
			     		return rs.getFloat("version");
			         }
			     });
			 
			 return (latest.size() > 0) ? latest.get(0).floatValue() : 0;

	    }
	    
	    public int updateLatestVersionIndicator(String formSeqid, String latest, String modifiedBy) {
	 	   String sql = "UPDATE sbrext.quest_contents_view_ext SET LATEST_VERSION_IND=:latest, modified_by=:modifiedby " +
	 			   " where qc_idseq=:formSeqid";

	 	   MapSqlParameterSource params = new MapSqlParameterSource();
	 	   params.addValue("formSeqid", formSeqid);
	 	   params.addValue("latest", latest);
	 	   params.addValue("modifiedby", modifiedBy);

	 	   int res = this.namedParameterJdbcTemplate.update(sql, params);
	 	   return res;

	    }
	    
	    public int updateLatestVersionIndicatorByPublicIdAndVersion(int publicId, float version, String latest, String modifiedBy) {
		 	   String sql = "UPDATE sbrext.quest_contents_view_ext SET LATEST_VERSION_IND=:latest, modified_by=:modifiedby " +
		 			   "where qc_id=:publicId and version=:version";

		 	   MapSqlParameterSource params = new MapSqlParameterSource();
		 	   params.addValue("publicId", publicId);
		 	  params.addValue("version", version);
		 	   params.addValue("latest", latest);
		 	   params.addValue("modifiedby", modifiedBy);

		 	   int res = this.namedParameterJdbcTemplate.update(sql, params);
		 	   return res;

		    }
	    
	    public boolean isLatestVersionForForm(String formseqid) {
	    	String sql = "select LATEST_VERSION_IND from sbrext.quest_contents_view_ext " +
	    			" where QC_IDSEQ=:formseqid";
	    	
	    	 MapSqlParameterSource params = new MapSqlParameterSource();
			 params.addValue("formseqid", formseqid);

			 List<String> inds = this.namedParameterJdbcTemplate.query(sql, params, 
			     		new RowMapper<String>() {
			     	public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			     		return rs.getString("LATEST_VERSION_IND");
			         }
			     });
			 
			return (inds.size() > 0 && inds.get(0).startsWith("Y"))  ? true : false;

	    }
	    
	    public HashMap<String, Date> getFormModifiedDateByIds(List<String> formSeqids) {
	    	String sql = "select QC_IDSEQ, DATE_MODIFIED from sbrext.fb_formS_view " +
	    			" where QC_IDSEQ in (:formSeqids)";

	    	final HashMap<String, Date> dateMap = 
	    			new HashMap<String, Date>();

	    	MapSqlParameterSource params = new MapSqlParameterSource();
	    	params.addValue("formSeqids", formSeqids);

	    	List<String> des = 
	    			this.namedParameterJdbcTemplate.query(sql, params, 
	    					new RowMapper<String>() {
	    				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	    					String seqid = rs.getString("QC_IDSEQ");
	    					Timestamp timestamp = rs.getTimestamp("DATE_MODIFIED");
	    					
	    					if (timestamp != null)
	    						dateMap.put(seqid, new Date(timestamp.getTime()));

	    					//Not really using this
	    					return seqid;
	    				}
	    			});

	    	  return dateMap;
	    }
}
