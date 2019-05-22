package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.ncicb.cadsr.common.dto.DataElementTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ModuleTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ValueDomainV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.exception.DMLException;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ModuleDAOV2;
import gov.nih.nci.ncicb.cadsr.common.resource.Module;
import gov.nih.nci.ncicb.cadsr.common.resource.Question;
import gov.nih.nci.ncicb.cadsr.common.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.object.StoredProcedure;

//Modification off JDBCModuleV2DAO
public class JDBCModuleDAOV2 extends JDBCAdminComponentDAOV2 implements ModuleDAOV2 {
	
	public JDBCModuleDAOV2(DataSource dataSource) {
		super(dataSource);
	}
  // based on JDBCModuleDAO#getQuestionsInAModule / QuestionsInAModuleQuery_STMT
  
  public List<QuestionTransferObject> getQuestionsInAModuleV2(String moduleId) {
    QuestionsInAModuleQuery_STMT query = new QuestionsInAModuleQuery_STMT();

    query.setDataSource(dataSource);
    query._setSql(moduleId);

    return query.execute();
  }
 

  class QuestionsInAModuleQuery_STMT extends MappingSqlQuery {
    QuestionsInAModuleQuery_STMT() {
      super();
    }

    public void _setSql(String idSeq) {
      super.setSql(
        "SELECT a.*, b.EDITABLE_IND, b.QC_ID, c.RULE, d.PREFERRED_NAME as DE_SHORT_NAME, " +
        	"d.PREFERRED_DEFINITION as DE_PREFERRED_DEFINITION FROM SBREXT.FB_QUESTIONS_VIEW a, " +
        	" CABIO31_QUESTIONS_VIEW b, COMPLEX_DATA_ELEMENTS_VIEW c, SBR.DATA_ELEMENTS_VIEW d " +
        	" where a.MODULE_IDSEQ = '" + idSeq + "' and a.ques_idseq=b.QC_IDSEQ and b.DE_IDSEQ = c.P_DE_IDSEQ(+) and b.de_idseq = d.de_idseq" +
        	" order by A.DISPLAY_ORDER");
//       declareParameter(new SqlParameter("MODULE_IDSEQ", Types.VARCHAR));
    }

    protected Object mapRow(
      ResultSet rs,
      int rownum) throws SQLException {
      Question question = new QuestionTransferObject();
      question.setQuesIdseq(rs.getString("QUES_IDSEQ"));  //QUES_IDSEQ
      question.setLongName(rs.getString("LONG_NAME"));   // LONG_NAME
      question.setDisplayOrder(rs.getInt("DISPLAY_ORDER")); // DISPLAY_ORDER
      question.setAslName(rs.getString("WORKFLOW"));//Workflow
      question.setPreferredDefinition(rs.getString("DEFINITION"));
      question.setMandatory("Yes".equalsIgnoreCase(rs.getString("MANDATORY_IND")));
      question.setPublicId(rs.getInt("QC_ID"));
      question.setVersion(new Float(rs.getFloat("VERSION")));
      
      String editableInd = rs.getString("EDITABLE_IND");
      boolean editable = (editableInd==null||editableInd.trim().equals("")||editableInd.equalsIgnoreCase("Yes"))?true:false;
      question.setEditable(editable);
      
      String derivRule = rs.getString("RULE");
      if (derivRule != null && !derivRule.trim().equals("")) {
    	  question.setDeDerived(true);
      }
      else {
    	  question.setDeDerived(false);
      }
      
      String deIdSeq = rs.getString("DE_IDSEQ");
      if(deIdSeq!=null)
       {
        DataElementTransferObject dataElementTransferObject =
          new DataElementTransferObject();       
        dataElementTransferObject.setDeIdseq(deIdSeq); // DE_IDSEQ
        dataElementTransferObject.setLongCDEName(rs.getString(15)); // DOC_TEXT 
        dataElementTransferObject.setVersion(new Float(rs.getFloat(16))); // VERSION
        dataElementTransferObject.setLongName(rs.getString(17)); // DE_LONG_NAME
        dataElementTransferObject.setCDEId(Integer.toString(rs.getInt(18)));
        dataElementTransferObject.setAslName(rs.getString("DE_WORKFLOW"));
        dataElementTransferObject.setPreferredName(rs.getString("DE_SHORT_NAME"));
        dataElementTransferObject.setPreferredDefinition(rs.getString("DE_PREFERRED_DEFINITION"));
        question.setDataElement(dataElementTransferObject); 
      
      
        ValueDomainV2TransferObject valueDomainV2TransferObject = 
                                         new ValueDomainV2TransferObject();
        valueDomainV2TransferObject.setVdIdseq(rs.getString(19)); // VD_IDSEQ
        dataElementTransferObject.setValueDomain(valueDomainV2TransferObject);
    }
    return question;
   }
  }
  
  /**
   * @inheritDoc
   */
  public String createModuleComponent(Module sourceModule)
    throws DMLException {

    // check if the user has the privilege to create module
    //This check need to be done only at the Form level
  /**
    boolean create = 
      this.hasCreate(sourceModule.getCreatedBy(), "QUEST_CONTENT", 
        sourceModule.getConteIdseq());
    if (!create) {
       DMLException dml = new DMLException("The user does not have the create module privilege.");
       dml.setErrorCode(this.INSUFFICIENT_PRIVILEGES);
       throw dml;
    }
   **/
    sourceModule.setPreferredName(generatePreferredName(sourceModule.getLongName()));

    InsertQuestContent  insertQuestContent  = 
      new InsertQuestContent (this.getDataSource());
    String qcIdseq = generateGUID(); 
    int res = insertQuestContent.createContent(sourceModule, qcIdseq);
    if (res != 1) {
       DMLException dml = new DMLException("Did not succeed creating module record in the " + 
        " quest_contents_ext table.");
       dml.setErrorCode(this.ERROR_CREATEING_MODULE);
       throw dml;        
    }
    
    InsertQuestRec  insertQuestRec  = 
      new InsertQuestRec (this.getDataSource());
    //TODO: why do we need this seqid? == Shan
    String qrIdseq = generateGUID();
    int resRec = insertQuestRec.createContent(sourceModule, qcIdseq, qrIdseq);
    if (resRec == 1) {
      return qcIdseq;
    }
    else {
       DMLException dml = new DMLException("Did not succeed creating form module relationship " +  
        "record in the quest_recs_ext table.");
       dml.setErrorCode(this.ERROR_CREATEING_MODULE);
       throw dml;          
    }
  }
  
  /**
   * Inner class that accesses database to create a module in the
   * quest_contents_ext table.
   */
 private class InsertQuestContent extends SqlUpdate {
    public InsertQuestContent(DataSource ds) {
      // super(ds, contentInsertSql);
      String contentInsertSql = 
      " INSERT INTO sbrext.quest_contents_view_ext " + 
      " (qc_idseq, version, preferred_name, long_name, preferred_definition, " + 
      "  conte_idseq, proto_idseq, asl_name, created_by, qtl_name, display_order ) " +
      " VALUES " +
      " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

      this.setDataSource(ds);
      this.setSql(contentInsertSql);
      declareParameter(new SqlParameter("p_qc_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_version", Types.VARCHAR));
      declareParameter(new SqlParameter("p_preferred_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_long_name", Types.VARCHAR));
      declareParameter(
        new SqlParameter("p_preferred_definition", Types.VARCHAR));
      declareParameter(new SqlParameter("p_conte_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_proto_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_asl_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_created_by", Types.VARCHAR));
      declareParameter(new SqlParameter("p_qtl_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_display_order", Types.NUMERIC));
      compile();
    }
    protected int createContent (Module sm, String qcIdseq) 
    {
    	String contextSeqid;
    	if (sm.getForm() != null)
    		contextSeqid = sm.getForm().getContext().getConteIdseq();
    	else
    		contextSeqid = sm.getConteIdseq();
    	
      Object [] obj = 
        new Object[]
          {qcIdseq, 
           sm.getVersion().toString(),
           generatePreferredName(sm.getLongName()),
           sm.getLongName(),
           sm.getPreferredDefinition(),
           contextSeqid,
           //module is not associate with protocol any more.
           //sm.getForm().getProtocol().getProtoIdseq(),
           null,
           sm.getAslName(),
           sm.getCreatedBy(),
           "MODULE",
           sm.getDisplayOrder()
          };
      
	    int res = update(obj);
      return res;
    }
  }

 
  
  /**
   * Inner class that accesses database to create a form and module relationship
   * record in the qc_recs_ext table.
   */
 private class InsertQuestRec extends SqlUpdate {
    public InsertQuestRec(DataSource ds) {
      String questRecInsertSql = 
      " INSERT INTO sbrext.qc_recs_view_ext " +
      " (qr_idseq, p_qc_idseq, c_qc_idseq, display_order, rl_name, created_by)" +  
      " VALUES " + 
      "( ?, ?, ?, ?, ?, ? )";

      this.setDataSource(ds);
      this.setSql(questRecInsertSql);
      declareParameter(new SqlParameter("p_qr_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_qc_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("c_qc_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_display_order", Types.INTEGER));
      declareParameter(new SqlParameter("p_rl_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_created_by", Types.VARCHAR));
      compile();
    }
    protected int createContent (Module sm, String qcIdseq, String qrIdseq) 
    {
      Object [] obj = 
        new Object[]
          {qrIdseq, 
           sm.getForm().getFormIdseq(),
           qcIdseq,
           new Integer(sm.getDisplayOrder()),
           "FORM_MODULE",
           sm.getCreatedBy()
          };
      
	    int res = update(obj);
      return res;
    }
  }
 
 /**
  * Changes several fields of a module.
  *
  * @param <b>Module</b> Module component.
  *
  * @return <b>int</b> 1 - success, 0 - failure.
  *
  * @throws <b>DMLException</b>
  */
 public int updateModuleComponent(Module module) throws DMLException {

   UpdateModuleComponent updateModuleComponent =
     new UpdateModuleComponent(this.getDataSource());
   int res = updateModuleComponent.updateModule(module);

   if (res != 1) {
     DMLException dmlExp = new DMLException("Did not succeed updating module's long name");
     dmlExp.setErrorCode(ERROR_UPDATING_MODULE);    
     throw dmlExp;
   }

   return 1;
 }
 
 /**
  * Inner class that updates long name of the question. 
  * 
  */
 private class UpdateModuleComponent extends SqlUpdate {
   public UpdateModuleComponent(DataSource ds) {
     String updateSql =
       " UPDATE sbrext.quest_contents_view_ext " + 
       " SET LONG_NAME = ?,  modified_by = ? " +
       " WHERE QC_IDSEQ = ? ";

     this.setDataSource(ds);
     this.setSql(updateSql);
     declareParameter(new SqlParameter("long_name", Types.VARCHAR));
     declareParameter(new SqlParameter("modified_by", Types.VARCHAR));
     declareParameter(new SqlParameter("qc_idseq", Types.VARCHAR));
     compile();
   }

   protected int updateModule(
     Module module) {

     Object[] obj =
       new Object[] {
         module.getLongName(),
         module.getModifiedBy(),
         module.getModuleIdseq()
       };

     int res = update(obj);

     return res;
   }
 }
 
 /**
  * Deletes the specified module and all its associated components.
  *
  * @param <b>moduleId</b> Idseq of the module component.
  *
  * @return <b>int</b> 1 - success, 0 - failure.
  *
  * @throws <b>DMLException</b>
  */
 public int deleteModule(String moduleId) throws DMLException {
	 DeleteModule deleteMod = new DeleteModule(this.getDataSource());
	 Map out = deleteMod.executeDeleteCommand(moduleId);

	 String returnCode = (String) out.get("p_return_code");
	 String returnDesc = (String) out.get("p_return_desc");
	 if (!StringUtils.doesValueExist(returnCode)) {
		 return 1;
	 }
	 else{
		 DMLException dmlExp = new DMLException(returnDesc);
		 dmlExp.setErrorCode(ERROR_DELETEING_MODULE);    
		 throw dmlExp;
	 }
 }
 
 /**
  * Inner class that accesses database to delete a module.
  */
 private class DeleteModule extends StoredProcedure {
	 public DeleteModule(DataSource ds) {
		 super(ds, "sbrext_form_builder_pkg.remove_module");
		 declareParameter(new SqlParameter("p_mod_idseq", Types.VARCHAR));
		 declareParameter(new SqlOutParameter("p_return_code", Types.VARCHAR));
		 declareParameter(new SqlOutParameter("p_return_desc", Types.VARCHAR));
		 compile();
	 }

	 public Map executeDeleteCommand(String modIdseq) {
		 Map in = new HashMap();
		 in.put("p_mod_idseq", modIdseq);

		 Map out = execute(in);

		 return out;
	 }

 }
 
 public ModuleTransferObject getModulePublicIdVersionBySeqid(String moduleseqid) {
	 if (moduleseqid == null || moduleseqid.length() == 0)
		 return null;
	 
	 String sql = "select QC_ID, VERSION from sbrext.quest_contents_view_ext " +
			 "where QC_IDSEQ=:moduleseqid and QTL_NAME='MODULE'";
	 
	 MapSqlParameterSource params = new MapSqlParameterSource();
     params.addValue("moduleseqid", moduleseqid);
      
     List<ModuleTransferObject> modules = this.namedParameterJdbcTemplate.query(sql, params, 
     		new RowMapper<ModuleTransferObject>() {
     	public ModuleTransferObject mapRow(ResultSet rs, int rowNum) throws SQLException {
     		ModuleTransferObject module = new ModuleTransferObject();
     		module.setPublicId(rs.getInt("QC_ID"));
     		module.setVersion(rs.getFloat("VERSION"));
         	return module;
         }
     });
   
     return (modules != null && modules.size() > 0) ? modules.get(0) : null;
	 
 }
}

