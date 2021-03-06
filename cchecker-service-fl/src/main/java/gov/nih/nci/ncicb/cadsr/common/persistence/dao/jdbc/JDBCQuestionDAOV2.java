package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.DataElementTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.FormValidValueTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ValueDomainV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ValueMeaningTransferObject;
import gov.nih.nci.ncicb.cadsr.common.exception.DMLException;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.QuestionDAO;
import gov.nih.nci.ncicb.cadsr.common.resource.FormValidValue;
import gov.nih.nci.ncicb.cadsr.common.resource.Question;
import gov.nih.nci.ncicb.cadsr.common.resource.QuestionChange;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueMeaning;
import gov.nih.nci.ncicb.cadsr.common.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.object.StoredProcedure;


public class JDBCQuestionDAOV2 extends JDBCAdminComponentDAOV2 implements QuestionDAO {
  
private static final Logger logger = LoggerFactory.getLogger(JDBCQuestionDAOV2.class.getName());
	
	public JDBCQuestionDAOV2(DataSource dataSource) {
		super(dataSource);
	}

  /**
   * Gets all the valid values that belong to the specified question
   *
   * @param questionId corresponds to the question idseq
   *
   * @return valid values that belong to the specified question
   */
  public List<FormValidValueTransferObject> getValidValues(String questionId) {
	//List<FormValidValue> = new ArrayList();
    ValidValuesForAQuestionQuery_STMT query = new ValidValuesForAQuestionQuery_STMT();
    query.setDataSource(this.dataSource);
    query._setSql(questionId);

    List<FormValidValueTransferObject> results =  query.execute();
    return results;
  }

  public Question addValidValues(
    String questionId,
    Collection validValues) throws DMLException {
    return null;
  }

  /**
   * Deletes the specified question and all its associated components.
   *
   * @param <b>questionId</b> Idseq of the question component.
   *
   * @return <b>int</b> 1 - success, 0 - failure.
   *
   * @throws <b>DMLException</b>
   */
  public int deleteQuestion(String questionId) throws DMLException {
    DeleteQuestion deleteQuestion = new DeleteQuestion(this.dataSource);
    Map out = deleteQuestion.executeDeleteCommand(questionId);

    String returnCode = (String) out.get("p_return_code");
    String returnDesc = (String) out.get("p_return_desc");

    if (!StringUtils.doesValueExist(returnCode)) {
      return 1;
    }
    else {
       DMLException dml = new DMLException(returnDesc);
       dml.setErrorCode(this.ERROR_DELETEING_QUESTION);
       throw dml;
    }
  }

  /**
   * Changes the display order of the specified question. Display order of the
   * other questions in the module is also updated accordingly.
   * 
   * @param <b>questionId</b> Idseq of the question component.
   * @param <b>newDisplayOrder</b> New display order of the question component.
   *
   * @return <b>int</b> 1 - success, 0 - failure.
   *
   * @throws <b>DMLException</b>
   */
  public int updateDisplayOrder(
    String questionId,
    int newDisplayOrder, String username) throws DMLException {

    return updateDisplayOrderDirect(questionId, "MODULE_ELEMENT", 
      newDisplayOrder, username);
  }
  
  public int updateQuestionLongName(
    String questionId,
    String newLongName, String userName) throws DMLException {
    UpdateQuestionLongName  questionLongName  = new UpdateQuestionLongName (this.dataSource);
    int res = questionLongName.updateLongName(questionId,newLongName, userName);
    
    if (res != 1) {
       DMLException dml = new DMLException("Did not succeed in updateing the long name");
       dml.setErrorCode(this.ERROR_UPDATING_QUESTION);
       throw dml;    
    }
    return 1;
  }
  
  public int createQuestionDefaultValue(QuestionChange change, String userName){
      //default value
      String pk = generateGUID();    
      CreateQuestAttrQuery createQuestAttr= new CreateQuestAttrQuery(this.dataSource);
      int res = createQuestAttr.createRecord(change, pk, userName);
      return res;
  }

    public int updateQuestAttr(QuestionChange change, String userName){
        UpdateQuestAttrQuery updateQuestAttr= new UpdateQuestAttrQuery(this.dataSource);
        if (change.isQuestAttrChange()){
        /*
            //will not delete considering the mandatory_ind
            //if both is null, delete
            String defaultValueId = change.getDefaultValidValue()==null? 
                        null:change.getDefaultValidValue().getValueIdseq();
            if ( (change.getDefaultValue()==null || change.getDefaultValue().length()==0) &&
                (defaultValueId==null || defaultValueId.length()==0) ) {//delete
                DeleteQuestAttrQuery deleteQuestAttr = new DeleteQuestAttrQuery(this.dataSource);
                    return deleteQuestAttr.deleteRecord(change);
                } 
            else{
          */
          int res = updateQuestAttr.updateRecord(change, userName);
          if (res == 0){
               CreateQuestAttrQuery createQuery = new CreateQuestAttrQuery(this.dataSource);
               String pk = generateGUID();
               return createQuery.createRecord(change, pk, userName);
          }
        }
        return 0;
    }
    
    public Question  getQuestionDefaultValue(  Question question){
        QuestionDefaultValueQuery query = new QuestionDefaultValueQuery();
        query.setDataSource(this.dataSource);
        String qId = question.getIdseq();
        query.setSql(qId);
        List qList = query.execute();
        if (qList != null && !qList.isEmpty()){
            Question retQ = (Question)qList.get(0);
            if (retQ != null){
                question.setDefaultValue(retQ.getDefaultValue());
                question.setDefaultValidValue(retQ.getDefaultValidValue());
            }
        }        
        return question;
    }




  /**
   * Creates a new question component (just the header info).
   *
   * @param <b>newQuestion</b> Question object
   *
   * @return <b>newQuestion</b> returns Question object
   *
   * @throws <b>DMLException</b>
   */
   public Question createQuestionComponent(Question newQuestion)
    throws DMLException {
    // check if the user has the privilege to create module
    //This need to be done only at the form level-skakkodi
   
   /**
    * boolean create =
      this.hasCreate(
        newQuestion.getCreatedBy(), "QUEST_CONTENT", newQuestion.getConteIdseq());

    if (!create) {
       DMLException dml = new DMLException("The user does not have the privilege to create question.");
       dml.setErrorCode(this.INSUFFICIENT_PRIVILEGES);
       throw dml;
    }
    **/

    InsertQuestContent insertQuestContent =
      new InsertQuestContent(this.dataSource);
    String qcIdseq = generateGUID();
    int res = insertQuestContent.createContent(newQuestion, qcIdseq);

    if (res != 1) {

       DMLException dml = new DMLException("Did not succeed creating question record in the " +
        " quest_contents_ext table.");
       dml.setErrorCode(this.ERROR_CREATEING_QUESTION);
       throw dml;        
    }

    InsertQuestRec insertQuestRec = new InsertQuestRec(this.dataSource);
    String qrIdseq = generateGUID();
    int resRec = insertQuestRec.createContent(newQuestion, qcIdseq, qrIdseq);

    if (resRec != 1) {
       DMLException dml = new DMLException(
        "Did not succeed creating module question relationship " +
        "record in the quest_recs_ext table.");
       dml.setErrorCode(this.ERROR_CREATEING_QUESTION);
       throw dml;      
    }
    
    newQuestion.setQuesIdseq(qcIdseq);
    //default value, 
    String defaultValue = newQuestion.getDefaultValue();
    String defaultValidValueIdSeq = null;
    FormValidValue defaultValidValueObj = newQuestion.getDefaultValidValue();
    if (defaultValidValueObj!=null){
        defaultValidValueIdSeq = defaultValidValueObj.getValueIdseq();
    }
    
/*    if ( (defaultValidValueIdSeq!=null && defaultValidValueIdSeq.length()!=0) ||
        (defaultValue!=null && defaultValue.length()!=0) || (newQuestion.isMandatory()) ){*/
        String pk = generateGUID();    
        CreateQuestAttrQuery createQuestAttr= new CreateQuestAttrQuery(this.dataSource);
        createQuestAttr.createRecord(newQuestion, pk, newQuestion.getCreatedBy());
    /*}*/
    return newQuestion;
  }

  public int updateQuestionDEAssociation(
    String questionId,
    String newDEId,
    String username) throws DMLException {
    UpdateQuestionDEAssociation nQuestion = new UpdateQuestionDEAssociation(this.dataSource);

    Map out = nQuestion.execute(
      questionId,
      newDEId,
      username.toUpperCase());

    if ((out.get("p_return_code")) == null) {
      return 1;
    }
    else {
       DMLException dml = new DMLException((String) out.get("p_return_desc"));
       dml.setErrorCode(this.ERROR_UPDATING_QUESTION);
       throw dml;
    }
  }

  public int updateQuestionDEAssociation(
    String questionId,
    String newDEId,
    String newLongName,
    String username) throws DMLException {

    int ret_val = 0;
    try{
      ret_val = updateQuestionDEAssociation(questionId,newDEId,username);
    }
    catch (DMLException de) {
      ret_val = 0;
      de.printStackTrace();
    }
   
    try{
      ret_val = updateQuestionLongName(questionId,newLongName, username);
    }
    catch (DMLException de) {
      ret_val = 0;
      de.printStackTrace();
    }
   
    if (ret_val == 1) {
      return ret_val;
    }
   else
   {
       DMLException dml = new DMLException("Error updating long name or valid value for question");
       dml.setErrorCode(this.ERROR_UPDATING_QUESTION); 
       throw dml;      
    }
  }

  public int createQuestionComponents(Collection questions)
    throws DMLException {
    return 0;
  }

  /**
   * Changes the long name, display order, and de_idseq of a question.
   *
   * @param <b>question</b> the question component.
   *
   * @return <b>int</b> 1 - success, 0 - failure.
   *
   * @throws <b>DMLException</b>
   */
  public int updateQuestionLongNameDispOrderDeIdseq(
    Question question) throws DMLException {

    int res = updateDisplayOrder(question.getQuesIdseq(), question.getDisplayOrder(),question.getModifiedBy());
    if (res != 1) {
       DMLException dml = new DMLException("Did not succeed updating question's display order.");
       dml.setErrorCode(this.ERROR_UPDATING_QUESTION); 
       throw dml;             
    }

    UpdateQuestionLongNameDeIdseq updateQuestionLnDe =
      new UpdateQuestionLongNameDeIdseq(this.dataSource);
    res = updateQuestionLnDe.updateQuestion(question);

    if (res != 1) {
       DMLException dml = new DMLException("Did not succeed updating question's long name, de idseq.");
       dml.setErrorCode(this.ERROR_UPDATING_QUESTION); 
       throw dml;        
    }

    return 1;
      
  }

/**
 * retrieve the value meaning attributes (designations, classifications) and put it in the ValueMeaning class
 */
    private ValueMeaning retrieveValueMeaningAttr(ValueMeaning vm){
        vm.setDesignations(getDesignations(vm.getIdseq(), null)); ///this should be the VM_IDSEQ;
        vm.setDefinitions(getDefinitions(vm.getIdseq())); ///this should be the VM_IDSEQ;
        vm.setPublicId(123456);	//JR417 new
        vm.setVersion(1.0f);	//JR417 new
        return vm;
    }

  /**
   * Inner class that accesses database to create a question record in the
   * quest_contents_ext table.
   */
  private class InsertQuestContent extends SqlUpdate {
    public InsertQuestContent(DataSource ds) {
      // super(ds, contentInsertSql);
      String contentInsertSql =
        " INSERT INTO sbrext.quest_contents_view_ext " +
        " (qc_idseq, version, preferred_name, long_name, preferred_definition, " +
        "  conte_idseq, asl_name, created_by, qtl_name, de_idseq) " +
        " VALUES " + " (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

      this.setDataSource(ds);
      this.setSql(contentInsertSql);
      declareParameter(new SqlParameter("p_qc_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_version", Types.VARCHAR));
      declareParameter(new SqlParameter("p_preferred_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_long_name", Types.VARCHAR));
      declareParameter(
        new SqlParameter("p_preferred_definition", Types.VARCHAR));
      declareParameter(new SqlParameter("p_conte_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_asl_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_created_by", Types.VARCHAR));
      declareParameter(new SqlParameter("p_qtl_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_de_idseq", Types.VARCHAR));
      compile();
    }

    protected int createContent(
      Question sm,
      String qcIdseq) {
      String conextIdSeq = null;
      String deIdseq = null;
      
      if( sm.getDataElement()!=null)
      {
         deIdseq = sm.getDataElement().getDeIdseq();
      }       
      Object[] obj =
        new Object[] {
          qcIdseq, sm.getVersion().toString(),
          generatePreferredName(sm.getLongName()), sm.getLongName(),
          sm.getPreferredDefinition(), sm.getContext().getConteIdseq(),
          sm.getAslName(), sm.getCreatedBy(),
          "QUESTION", deIdseq
        };


      int res = update(obj);

      return res;
    }
  }

  /**
   * Inner class that accesses database to create a module and question
   * relationship record in the qc_recs_ext table.
   */
  private class InsertQuestRec extends SqlUpdate {
    public InsertQuestRec(DataSource ds) {
      String questRecInsertSql =
        " INSERT INTO sbrext.qc_recs_view_ext " +
        " (qr_idseq, p_qc_idseq, c_qc_idseq, display_order, rl_name, created_by)" +
        " VALUES " + "( ?, ?, ?, ?, ?, ? )";

      this.setDataSource(ds);
      this.setSql(questRecInsertSql);
      declareParameter(new SqlParameter("p_qr_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_qc_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("c_qc_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_pisplay_order", Types.INTEGER));
      declareParameter(new SqlParameter("p_rl_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_created_by", Types.VARCHAR));
      compile();
    }

    protected int createContent(
      Question sm,
      String qcIdseq,
      String qrIdseq) {
      Object[] obj =
        new Object[] {
          qrIdseq, sm.getModule().getModuleIdseq(), qcIdseq,
          new Integer(sm.getDisplayOrder()), "MODULE_ELEMENT", sm.getCreatedBy()
        };

      int res = update(obj);

      return res;
    }
  }

  /**
   * Inner class that accesses database to get all the questions that belong to
   * the specified module
   */
  class ValidValuesForAQuestionQuery_STMT extends MappingSqlQuery {
    ValidValuesForAQuestionQuery_STMT() {
      super();
    }

    public void _setSql(String idSeq) {
      super.setSql(
        "SELECT * FROM SBREXT.FB_VALID_VALUES_VIEW where QUES_IDSEQ = '" + idSeq + "' order by display_order");
//       declareParameter(new SqlParameter("QUESTION_IDSEQ", Types.VARCHAR));
    }
   /**
    * 3.0 Refactoring- Removed JDBCTransferObject
    */
    protected FormValidValueTransferObject mapRow(
      ResultSet rs,
      int rownum) throws SQLException {
          FormValidValueTransferObject fvv = new FormValidValueTransferObject();
//          FormValidValue fvv = new FormValidValueTransferObject();	//JR391
          fvv.setValueIdseq(rs.getString(1));     // VV_IDSEQ
          fvv.setVpIdseq(rs.getString(8));        // VP_IDSEQ
          fvv.setLongName(rs.getString(9));       // LONG_NAME
          fvv.setDisplayOrder(rs.getInt(14));     // DISPLAY_ORDER
          fvv.setShortMeaning(rs.getString(15));    // Meaning  
          fvv.setVersion(new Float(rs.getString(2))); // VERSION
          //Bug Fix tt#1058
          //added for value meaning
          ValueMeaning vm = new ValueMeaningTransferObject();
          vm.setIdseq(rs.getString("VM_IDSEQ"));
          vm.setLongName(rs.getString("short_meaning"));
          vm.setPreferredDefinition(rs.getString("VM_DESCRIPTION"));
          vm.setPublicId(rs.getInt("VM_PUBLIC_ID"));	//JR417 upper case vm_public_id
          vm.setVersion(rs.getFloat("VM_VERSION"));		//JR417 upper case vm_version
          
          fvv.setAslName(rs.getString(5));
          fvv.setPreferredDefinition(rs.getString(7));
          fvv.setFormValueMeaningText(rs.getString(16)); //Meaning_text
          if (vm.getPublicId() > 0)	//JR417 tagged
        	  fvv.setFormValueMeaningIdVersion(String.valueOf(vm.getPublicId()) + "v"+String.valueOf(vm.getVersion())); //Meaning_id version

          fvv.setFormValueMeaningDesc(rs.getString("DESCRIPTION_TEXT")); //DESCRIPTION_TEXT          
          ContextTransferObject contextTransferObject = new ContextTransferObject();
          contextTransferObject.setConteIdseq(rs.getString(4)); //CONTE_IDSEQ
          fvv.setContext(contextTransferObject);
          
          //No need to get designations and definitions for vm anymore
          vm = retrieveValueMeaningAttr(vm);	//JR391 uncomment it as it is breaking the Modify functionality
          
          fvv.setValueMeaning(vm);
          
         return fvv;
    }
  }


  /**
   * Inner class that accesses database to get all the questions that belong to
   * the specified module
   */
  class ValidValuesForAQuestionQuery extends MappingSqlQuery {
    ValidValuesForAQuestionQuery(DataSource ds) {
      super();
      setDataSource(ds);
      setSql();
      compile();
    }

    public void setSql() {
      super.setSql(
        "SELECT * FROM SBREXT.FB_VALID_VALUES_VIEW where QUES_IDSEQ = ? ");
      declareParameter(new SqlParameter("QUESTION_IDSEQ", Types.VARCHAR));
    }
   /**
    * 3.0 Refactoring- Removed JDBCTransferObject
    */    protected Object mapRow(
      ResultSet rs,
      int rownum) throws SQLException {
          FormValidValue fvv = new FormValidValueTransferObject();
          fvv.setValueIdseq(rs.getString(1));     // VV_IDSEQ
          fvv.setVpIdseq(rs.getString(8));        // VP_IDSEQ
          fvv.setLongName(rs.getString(9));       // LONG_NAME
          fvv.setDisplayOrder(rs.getInt(14));     // DISPLAY_ORDER
          fvv.setShortMeaning(rs.getString(15));    // Meaning
          fvv.setFormValueMeaningText(rs.getString(16)); //Meaning_text
          fvv.setFormValueMeaningDesc(rs.getString("DESCRIPTION_TEXT")); //DESCRIPTION_TEXT
    	  fvv.setFormValueMeaningIdVersion(String.valueOf(rs.getInt("VM_PUBLIC_ID")) + "v"+String.valueOf(rs.getFloat("VM_VERSION"))); //Meaning_id version JR417

         return fvv;
    }
  }

  /**
   * Inner class that accesses database to delete a question.
   */
  private class DeleteQuestion extends StoredProcedure {
    public DeleteQuestion(DataSource ds) {
      super(ds, "sbrext_form_builder_pkg.remove_question");
      declareParameter(new SqlParameter("p_ques_idseq", Types.VARCHAR));
      declareParameter(new SqlOutParameter("p_return_code", Types.VARCHAR));
      declareParameter(new SqlOutParameter("p_return_desc", Types.VARCHAR));
      compile();
    }

    public Map executeDeleteCommand(String quesIdseq) {
      Map in = new HashMap();
      in.put("p_ques_idseq", quesIdseq);

      Map out = execute(in);

      return out;
    }
  }
 /**
   * Inner class that copies the source form to a new form
   */
  private class UpdateQuestionDEAssociation extends StoredProcedure {
    public UpdateQuestionDEAssociation(DataSource ds) {
      super(ds, "sbrext_form_builder_pkg.update_de");
      declareParameter(new SqlParameter("p_prm_qc_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_de_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_created_by", Types.VARCHAR));
      declareParameter(new SqlOutParameter("p_return_code", Types.VARCHAR));
      declareParameter(new SqlOutParameter("p_return_desc", Types.VARCHAR));
      compile();
    }

    public Map execute(
      String p_prm_qc_idseq,
      String p_de_idseq,
      String p_created_by) {
      Map in = new HashMap();

      in.put("p_prm_qc_idseq", p_prm_qc_idseq);
      in.put("p_de_idseq", p_de_idseq);
      in.put("p_created_by", p_created_by);
      
      Map out = execute(in);
      return out;
    }
  } 
/**
   * Inner class that accesses database to update an longname.
   */
 private class UpdateQuestionLongName extends SqlUpdate {
    public UpdateQuestionLongName(DataSource ds) {
      String longNameUpdateSql = 
      " UPDATE sbrext.Quest_contents_view_ext " +
      " SET long_name = ?,  modified_by = ? " +
      " WHERE qc_idseq =  ?" ;

      this.setDataSource(ds);
      this.setSql(longNameUpdateSql);
      declareParameter(new SqlParameter("p_long_name", Types.VARCHAR));
      declareParameter(new SqlParameter("modified_by", Types.VARCHAR));
      declareParameter(new SqlParameter("p_qc_idseq", Types.VARCHAR));
      compile();
    }
    protected int updateLongName (String questionId, String newLongName, String userName) 
    {
      Object [] obj = 
        new Object[]
          {
          newLongName,
          userName,
          questionId
          };
      
	    int res = update(obj);
      return res;
    }
  }

  
  /**
   * Inner class that updates long name, display order, and de idseq of
   * the question. 
   * 
   */
  private class UpdateQuestionLongNameDeIdseq extends SqlUpdate {
    public UpdateQuestionLongNameDeIdseq(DataSource ds) {
      String updateSql =
        " UPDATE sbrext.quest_contents_view_ext " + 
        " SET DE_IDSEQ = ? , LONG_NAME = ? ,  modified_by = ? " +
        " WHERE QC_IDSEQ = ? ";

      this.setDataSource(ds);
      this.setSql(updateSql);
      declareParameter(new SqlParameter("de_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("long_name", Types.VARCHAR));
      declareParameter(new SqlParameter("modified_by", Types.VARCHAR));
      declareParameter(new SqlParameter("qc_idseq", Types.VARCHAR));
      compile();
    }

    protected int updateQuestion(
      Question question) {

      String deIdseq = null;
      if (question.getDataElement() != null) {
        deIdseq = question.getDataElement().getDeIdseq();
      }
      
      Object[] obj =
        new Object[] {
          deIdseq,
          question.getLongName(),
          question.getModifiedBy(),
          question.getQuesIdseq()
        };

      int res = update(obj);

      return res;
    }
  }

  /**
   * Inner class that insert into quest_attributes_ext table
   * 
   */
  private class CreateQuestAttrQuery extends SqlUpdate {
    public CreateQuestAttrQuery(DataSource ds) {
      String createSql =
        " insert  into sbrext.quest_attributes_ext(VV_IDSEQ, QC_IDSEQ, QUEST_IDSEQ, CREATED_BY, DEFAULT_VALUE, EDITABLE_IND, MANDATORY_IND ) values(?,?,?,?,?, ?, ?)";

      this.setDataSource(ds);
      this.setSql(createSql);
      declareParameter(new SqlParameter("VV_IDSEQ", Types.VARCHAR));
      declareParameter(new SqlParameter("QC_IDSEQ", Types.VARCHAR));
      declareParameter(new SqlParameter("QUEST_IDSEQ", Types.VARCHAR));
      declareParameter(new SqlParameter("CREATED_BY", Types.VARCHAR));
      declareParameter(new SqlParameter("DEFAULT_VALUE", Types.VARCHAR));
      declareParameter(new SqlParameter("EDITABLE_IND", Types.VARCHAR));
      declareParameter(new SqlParameter("MANDATORY_IND", Types.VARCHAR));
      compile();
    }

    protected int createRecord(
      Question question, String pk, String userName ) {

      Object[] obj =
        new Object[] {
          question.getDefaultValidValue()==null? 
            null: question.getDefaultValidValue().getValueIdseq(),
          question.getQuesIdseq(),
          pk,
          userName,
          question.getDefaultValue(),
          question.isEditable()?"Yes":"No",
          question.isMandatory()?"Yes":"No"
        };
      int res = update(obj);
      return res;
    }

      protected int createRecord(
        QuestionChange change, String pk, String userName ) {
        
        String vvId = change.getDefaultValidValue()==null? 
              null: change.getDefaultValidValue().getValueIdseq();
        String defaultValue =  change.getDefaultValue();
        if (vvId==null && (defaultValue==null || defaultValue.length()==0) && !change.isMandatory() && !change.isEditable()){
            return 0;//no empty record. record means NOT MANDATORY
        }
        Object[] obj =
          new Object[] {
            vvId,
            change.getQuestionId(),
            pk,
            userName,
            defaultValue,
            (change.isEditable())? "Yes": "No",
            (change.isMandatory())? "Yes": "No"
          };
        int res = update(obj);
        return res;
      }
  }
  
    /**
     * Inner class that update quest_attributes_ext table
     * 
     */
    private class UpdateQuestAttrQuery extends SqlUpdate {
      public UpdateQuestAttrQuery(DataSource ds) {
        String createSql =
          " update quest_attributes_ext set VV_IDSEQ=?, MODIFIED_BY=?, DEFAULT_VALUE=?,  MANDATORY_IND=?, EDITABLE_IND=? where QC_IDSEQ=?";

        this.setDataSource(ds);
        this.setSql(createSql);
        declareParameter(new SqlParameter("VV_IDSEQ", Types.VARCHAR));
        declareParameter(new SqlParameter("MODIFIED_BY", Types.VARCHAR));
        declareParameter(new SqlParameter("DEFAULT_VALUE", Types.VARCHAR));
          declareParameter(new SqlParameter("MANDATORY_IND", Types.VARCHAR));
          declareParameter(new SqlParameter("EDITABLE_IND", Types.VARCHAR));
        declareParameter(new SqlParameter("QC_IDSEQ", Types.VARCHAR));
        compile();
      }

      protected int updateRecord(
        QuestionChange change, String userName) {

        Object[] obj =
          new Object[] {
            change.getDefaultValidValue()==null? 
              null: change.getDefaultValidValue().getValueIdseq(),
            userName,
            change.getDefaultValue(),
            (change.isMandatory()? "Yes" : "No"),
            (change.isEditable()? "Yes" : "No"),
            change.getQuestionId()
          };
        int res = update(obj);
        return res;
      }

    }

    private class DeleteQuestAttrQuery extends SqlUpdate {
      public DeleteQuestAttrQuery(DataSource ds) {
        String deleteSql =
          " delete from quest_attributes_ext where qc_idseq=?";

        this.setDataSource(ds);
        this.setSql(deleteSql);
        declareParameter(new SqlParameter("QC_IDSEQ", Types.VARCHAR));
        compile();
      }

      protected int deleteRecord(
        QuestionChange change) {

        Object[] obj =
          new Object[] {
            change.getQuestionId()
          };
        int res = update(obj);
        return res;
      }
    }  


/**
 * get question default value
 */
    private class QuestionDefaultValueQuery extends MappingSqlQuery {
      QuestionDefaultValueQuery() {
        super();
      }

      public void setSql(String idSeq) {
        super.setSql(        
          " select qa.QUEST_IDSEQ, qa.default_value,  qa.QC_IDSEQ, qa.VV_IDSEQ, qa.EDITABLE_IND, vv.LONG_NAME from quest_attributes_ext qa, sbrext.quest_contents_view_ext vv " +
          " where qa.VV_IDSEQ =vv.QC_IDSEQ(+) and qa.QC_IDSEQ = '" + idSeq + "'");
    //       declareParameter(new SqlParameter("QUESTION_IDSEQ", Types.VARCHAR));
            compile();
      }
     /**
      * 3.0 Refactoring- Removed JDBCTransferObject
      */
      protected Object mapRow(
        ResultSet rs,
        int rownum) throws SQLException {
            Question question = new QuestionTransferObject();
            String defaultValueStr = rs.getString(2);
            question.setDefaultValue(defaultValueStr);
            question.setIdseq(rs.getString(3));
            if (defaultValueStr==null || defaultValueStr.length()==0 ){ //default value Id
             FormValidValue fvv = new FormValidValueTransferObject();	//JR417 is the vm public id and version ok?
             fvv.setValueIdseq(rs.getString(4));   // VV_IDSEQ
             fvv.setLongName(rs.getString(6));       // LONG_NAME
             //fvv.setIdseq(rs.getString(1));
             fvv.setQuestion(question);
             fvv.setLongName(rs.getString(6));
             question.setDefaultValidValue(fvv);
            } 
        return question;
        }
}
    
    /**
     * Get a list of question dtos with public ids
     * @param publicIds
     * @return
     */
    public List<QuestionTransferObject> getQuestionsByPublicIds(List<String> publicIds) {
    	 String sql = 
    	      "select Q.QC_IDSEQ, Q.QC_ID, Q.VERSION, Q.PREFERRED_DEFINITION, Q.LONG_NAME, " +
    	    		  "Q.DE_IDSEQ from QUEST_CONTENTS_VIEW_EXT q " +
    	    		  "where Q.QC_ID in (:ids) and Q.QTL_NAME='QUESTION'";

    	 MapSqlParameterSource params = new MapSqlParameterSource();
    	 params.addValue("ids", publicIds);

    	List<QuestionTransferObject> questions = 
    			 this.namedParameterJdbcTemplate.query(sql, params, 
    					 new RowMapper() {
    				 public QuestionTransferObject mapRow(ResultSet rs, int rowNum) throws SQLException {
    					 QuestionTransferObject quest = new QuestionTransferObject();
    					 //quest.setIdseq(rs.getString(1)); //this is the super class object id
    					 quest.setQuesIdseq(rs.getString(1));
    					 quest.setPublicId(rs.getInt(2));
    					 quest.setVersion(rs.getFloat(3));
    					 quest.setPreferredDefinition(rs.getString(4));
    					 quest.setLongName(rs.getString(5));
    					 return quest;
    				 }
    			 });


    	 return questions;
    }
    
    /**
     * Get a list of question dtos with public ids
     * @param publicIds
     * @return
     */
    public QuestionTransferObject getQuestionsPublicIdVersionBySeqid(String questSeqid) {
    	 String sql = 
    	      "select Q.QC_IDSEQ, Q.QC_ID, Q.VERSION, Q.PREFERRED_DEFINITION, Q.LONG_NAME, " +
    	    		  "Q.DE_IDSEQ from QUEST_CONTENTS_VIEW_EXT q " +
    	    		  "where Q.QC_IDSEQ=:seqid";

    	 MapSqlParameterSource params = new MapSqlParameterSource();
    	 params.addValue("seqid", questSeqid);

    	List<QuestionTransferObject> questions = 
    			 this.namedParameterJdbcTemplate.query(sql, params, 
    					 new RowMapper() {
    				 public QuestionTransferObject mapRow(ResultSet rs, int rowNum) throws SQLException {
    					 QuestionTransferObject quest = new QuestionTransferObject();
    					 //quest.setIdseq(rs.getString(1)); //this is the super class object id
    					 quest.setQuesIdseq(rs.getString("QC_IDSEQ"));
    					 quest.setPublicId(rs.getInt("QC_ID"));
    					 quest.setVersion(rs.getFloat("VERSION"));

    					 return quest;
    				 }
    			 });


    	 return questions.get(0);
    }
    
    /**
     * Gets question dto by public id and version.
     * @param publicId
     * @param version
     * @return
     */
    public List<QuestionTransferObject> getQuestionByPublicIdAndVersion(int publicId, float version) {
    	String sql = "SELECT a.*, b.QC_ID " +
    			"FROM SBREXT.FB_QUESTIONS_VIEW a, CABIO31_QUESTIONS_VIEW b " +
    			"where B.QC_ID=:qcId and A.VERSION=:vers and a.ques_idseq=b.QC_IDSEQ";
    	
    	MapSqlParameterSource params = new MapSqlParameterSource();
      	params.addValue("qcId", publicId);
      	params.addValue("vers", version);

      	List<QuestionTransferObject> questions = 
      			 this.namedParameterJdbcTemplate.query(sql, params, 
      					 new RowMapper() {
      				public QuestionTransferObject mapRow(ResultSet rs,
      				      int rownum) throws SQLException {
      					QuestionTransferObject question = new QuestionTransferObject();
      				      question.setQuesIdseq(rs.getString("QUES_IDSEQ"));  //QUES_IDSEQ
      				      question.setLongName(rs.getString("LONG_NAME"));   // LONG_NAME
      				      question.setDisplayOrder(rs.getInt("DISPLAY_ORDER")); // DISPLAY_ORDER
      				      question.setAslName(rs.getString("WORKFLOW"));//Workflow
      				      question.setPreferredDefinition(rs.getString("DEFINITION"));
      				      question.setMandatory("Yes".equalsIgnoreCase(rs.getString("MANDATORY_IND")));
      				      question.setPublicId(rs.getInt("QC_ID"));
      				      question.setVersion(new Float(rs.getFloat("VERSION")));
      				      
      				     /* String editabl
      				      eInd = rs.getString("EDITABLE_IND");
      				      boolean editable = (editableInd==null||editableInd.trim().equals("")||editableInd.equalsIgnoreCase("Yes"))?true:false;
      				      question.setEditable(editable);
      				      */
      				     
      				      /*
      				      String derivRule = rs.getString("RULE");
      				      if (derivRule != null && !derivRule.trim().equals("")) {
      				    	  question.setDeDerived(true);
      				      }
      				      else {
      				    	  question.setDeDerived(false);
      				      }
      				      */
      				      
      				      String deIdSeq = rs.getString("DE_IDSEQ");
      				     /*
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
      				    }*/
      				    return question;
      				   }
      				 
      				
      			 });


      	 return questions;
    }

    /**
     * Gets the full question dto, with its associated data element dto attached.
     * @param publicId
     * @return
     */
    public List<QuestionTransferObject> getQuestionsByPublicId(int publicId) {
   	 String sql = 
   	      "SELECT a.*, b.EDITABLE_IND, b.QC_ID, c.RULE, d.PREFERRED_NAME as DE_SHORT_NAME, " +
   			 "d.PREFERRED_DEFINITION as DE_PREFERRED_DEFINITION " +
   			 "FROM SBREXT.FB_QUESTIONS_VIEW a, CABIO31_QUESTIONS_VIEW b, COMPLEX_DATA_ELEMENTS_VIEW c, DATA_ELEMENTS_VIEW d " +
   			 "where B.QC_ID=:qcId and a.ques_idseq=b.QC_IDSEQ and b.DE_IDSEQ = c.P_DE_IDSEQ(+) " +
   			 "and b.de_idseq = d.de_idseq";

   	 MapSqlParameterSource params = new MapSqlParameterSource();
   	 params.addValue("qcId", publicId);

   	List<QuestionTransferObject> questions = 
   			 this.namedParameterJdbcTemplate.query(sql, params, 
   					 new RowMapper() {
   				public QuestionTransferObject mapRow(ResultSet rs,
   				      int rownum) throws SQLException {
   					QuestionTransferObject question = new QuestionTransferObject();
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
   				 
   				
   			 });


   	 return questions;
   }
    


    public List<DataElementTransferObject> getCdesByPublicId(String cdePublicId) {
    	String sql = 
    			"select de.* from DATA_ELEMENTS_VIEW de " +
    					"where de.cde_id=:id";

    	MapSqlParameterSource params = new MapSqlParameterSource();
    	params.addValue("id", cdePublicId);

    	List<DataElementTransferObject> des = 
    			this.namedParameterJdbcTemplate.query(sql, params, 
    					new RowMapper() {
    				public DataElementTransferObject mapRow(ResultSet rs, int rowNum) throws SQLException {
    					DataElementTransferObject de = new DataElementTransferObject();
    					de.setDeIdseq(rs.getString("DE_IDSEQ"));
    					de.setPublicId(rs.getInt("CDE_ID"));
    					de.setVersion(rs.getFloat("VERSION"));
    					de.setLongName(rs.getString("LONG_NAME"));
    					de.setVdIdseq(rs.getString("VD_IDSEQ"));
    					return de;
    				}
    			});

    	return des;
    }
    
    public List<DataElementTransferObject> getCdesByPublicIds(List<String> cdePublicIds) {
    	String sql = 
    			"select de.* from DATA_ELEMENTS_VIEW de " +
    					"where de.cde_id in(:ids)";

    	MapSqlParameterSource params = new MapSqlParameterSource();
    	params.addValue("ids", cdePublicIds);

    	List<DataElementTransferObject> des = 
    			this.namedParameterJdbcTemplate.query(sql, params, 
    					new RowMapper() {
    				public DataElementTransferObject mapRow(ResultSet rs, int rowNum) throws SQLException {
    					DataElementTransferObject de = new DataElementTransferObject();
    					de.setDeIdseq(rs.getString("DE_IDSEQ"));
    					de.setPublicId(rs.getInt("CDE_ID"));
    					de.setVersion(rs.getFloat("VERSION"));
    					de.setLongName(rs.getString("LONG_NAME"));
    					de.setVdIdseq(rs.getString("VD_IDSEQ"));

    					return de;
    				}
    			});

    	return des;
    }
    
}
