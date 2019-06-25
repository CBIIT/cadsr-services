package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.object.StoredProcedure;

import gov.nih.nci.ncicb.cadsr.common.exception.DMLException;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.FormValidValueDAO;
import gov.nih.nci.ncicb.cadsr.common.resource.FormValidValue;
import gov.nih.nci.ncicb.cadsr.common.util.StringUtils;

public class JDBCFormValidValueDAOV2 extends JDBCAdminComponentDAOV2
  implements FormValidValueDAO {
	
	private static Logger logger = Logger.getLogger(JDBCFormValidValueDAOV2.class.getName());
  
	public JDBCFormValidValueDAOV2(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * This mimicks the behavior of the store procedure "sbrext_form_builder_pkg.ins_value()
	 * 
	 * @param newVV
	 * @param parentId
	 * @param userName
	 * @return
	 */
	public String createValidValue(FormValidValue newVV, String parentId, String userName)
			throws DataAccessException {
		
		String vvidseq = generateGUID();
		String recidseq = generateGUID();
		
		int res = createtValidValue(newVV, vvidseq);
		
		if (res == 1) {//success
			res = createComponentValidValueMapping(recidseq, parentId, vvidseq, newVV, userName);
		}
		
		return (res == 1) ? vvidseq : null;
	}
	
	protected int createtValidValue(FormValidValue newVV, String vvidseq) 
			throws DataAccessException {
		
		String sql = "INSERT INTO quest_contents_ext " +
                  "(qc_idseq, VERSION, preferred_name, long_name, " +
                  " preferred_definition, conte_idseq, " +
                  " asl_name, vp_idseq, created_by, qtl_name " +
                 " ) " +
          " VALUES (:v_qc_idseq, :p_version, :v_preferred_name, :p_long_name, " +
                   ":p_preferred_definition, :p_conte_idseq,  " +
                  " :p_asl_name, :p_vp_idseq, :p_created_by, 'VALID_VALUE' " +
                  ")";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("v_qc_idseq", vvidseq);
		
		params.addValue("p_version", newVV.getVersion().toString());
		logger.debug(newVV.getVersion().toString());
		params.addValue("v_preferred_name", newVV.getPreferredName());
		logger.debug(newVV.getPreferredName());
		params.addValue("p_long_name", newVV.getLongName());
		logger.debug(newVV.getLongName());
		
		String prefDef =  newVV.getPreferredDefinition();
		if (prefDef == null || prefDef.length() == 0)
			prefDef = "No Definition";

		params.addValue("p_preferred_definition", prefDef);
		logger.debug(newVV.getPreferredDefinition());
		params.addValue("p_conte_idseq", newVV.getContext().getConteIdseq());
		logger.debug(newVV.getContext().getConteIdseq());
		//params.addValue("p_proto_idseq", null);
		
		params.addValue("p_asl_name", newVV.getAslName());
		logger.debug(newVV.getAslName());
		params.addValue("p_vp_idseq", newVV.getVpIdseq());	//JR417 newVV's vpIdseq is not empty anymore (fixed in this ticket)
		logger.debug( newVV.getVpIdseq());
		params.addValue("p_created_by", newVV.getCreatedBy());		
		logger.debug(newVV.getCreatedBy());

		try {
			int res = this.namedParameterJdbcTemplate.update(sql, params);
			return res;
		} catch (Exception de) {
			logger.debug("createtValidValue error: " + de + ". FormValidValue: " + newVV);
			throw de;
		}
		
		
	}
	
	protected int createComponentValidValueMapping(String recSeqid, String compSeqid, String vvSeqid, 
			FormValidValue newVV, String userName) 
			throws DMLException {
		String sql = "INSERT INTO sbrext.qc_recs_view_ext " +
                  "(qr_idseq, p_qc_idseq, c_qc_idseq, display_order, " +
                  " rl_name, created_by " +
                  ") " +
           "VALUES (:v_qr_idseq, :p_ques_idseq, :v_qc_idseq, :p_display_order, " +
                 "  'ELEMENT_VALUE', :p_created_by " +
                 " )";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("v_qr_idseq", recSeqid);
		params.addValue("p_ques_idseq", compSeqid);
		params.addValue("v_qc_idseq", vvSeqid);
		params.addValue("p_display_order", newVV.getDisplayOrder());
		params.addValue("p_created_by", userName);
	
		int res = this.namedParameterJdbcTemplate.update(sql, params);
		return res;
	}
	
	public int createValidValueAttributes(String vvSeqid, String meaningText, String description, String userName) {
		String sql = 
				"insert into valid_values_att_ext (qc_idseq, meaning_text, description_text, created_by) " +
				" values(:vvseqid, :meaningText, :desc, :createdBy)";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("vvseqid", vvSeqid);
		if (meaningText.length() > 1999) {
			// Valid Value Meaning Text Column in the DB has a limit of 2000 chars.
			logger.debug("****** Valid Value Meaning text over 2000 chars, so truncating...");
			meaningText = meaningText.substring(0, 2000); 
		}
		params.addValue("meaningText", meaningText);
		params.addValue("desc", description);
		params.addValue("createdBy", userName);
		
		int res = this.namedParameterJdbcTemplate.update(sql, params);
		return res;
	}
	
	public int createValidValueAssociation(FormValidValue newVV, String parentId, String userName) 
			throws DMLException {
	
		//qr_idseq: generated
		//p_qc_idseq: question seqid
		//c_qc_idseq: vv seq id (generated)
		//
		
		String sql = "INSERT INTO qc_recs_ext " +
                  "(qr_idseq, p_qc_idseq, c_qc_idseq, display_order, " +
                  " rl_name, created_by " +
                  ")  " +
           "VALUES (v_qr_idseq, p_ques_idseq, v_qc_idseq, p_display_order, " +
                   "'ELEMENT_VALUE', p_created_by  " +
                  ")";
	      
		String idseq = generateGUID();
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("v_qc_idseq", idseq);
		logger.debug(parentId);
		params.addValue("p_version", newVV.getVersion().toString());
		logger.debug(newVV.getVersion().toString());
		params.addValue("v_preferred_name", newVV.getPreferredName());
		logger.debug(newVV.getPreferredName());
		params.addValue("p_long_name", newVV.getLongName());
		logger.debug(newVV.getLongName());
		params.addValue("p_preferred_definition", newVV.getPreferredDefinition());
		logger.debug(newVV.getPreferredDefinition());
		params.addValue("p_conte_idseq", newVV.getContext().getConteIdseq());
		logger.debug(newVV.getContext().getConteIdseq());
		params.addValue("p_proto_idseq", null);
		
		params.addValue("p_asl_name", newVV.getAslName());
		logger.debug(newVV.getAslName());
		params.addValue("p_vp_idseq", newVV.getVpIdseq());
		logger.debug( newVV.getVpIdseq());
		params.addValue("p_created_by", newVV.getCreatedBy());		
		logger.debug(newVV.getCreatedBy());
		int res = this.namedParameterJdbcTemplate.update(sql, params);
		return res;
		
	}
	
	public List<String> getValidValueSeqidsByQuestionSeqid(String questId) {
		String sql = "select C_QC_IDSEQ from sbrext.qc_recs_ext " +
				" where RL_NAME='ELEMENT_VALUE' and P_QC_IDSEQ=:questId";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("questId", questId);
		
		List<String> rows = 
    			this.namedParameterJdbcTemplate.query(sql, params, 
    					new RowMapper<String>() {
    				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    					
    					return rs.getString("C_QC_IDSEQ");
    				}
    			});
		return rows;

	}

  /**
   * Creates a new form valid value component (just the header info).
   *
   * @param <b>newValidValue</b> FormValidValue object
   *
   * @return <b>int</b> 1 - success, 0 - failure.
   *
   * @throws <b>DMLException</b>
   */
  public String createFormValidValueComponent(FormValidValue newValidValue, String parentId, String userName)
    //throws DMLException 
    {

    // check if the user has the privilege to create valid value
    // This check only need to be at the form level -skakkodi
   /** boolean create = 
      this.hasCreate(newValidValue.getCreatedBy(), "QUEST_CONTENT", 
        newValidValue.getConteIdseq());
    if (!create) {
      DMLException dml = new DMLException("The user does not have the privilege to create valid value.");
      dml.setErrorCode(INSUFFICIENT_PRIVILEGES);
      throw dml;
    }
    **/
    
  
    InsertFormValidValue insertValidValue = new InsertFormValidValue(this.getDataSource());
    Map out = insertValidValue.executInsertCommand(newValidValue,parentId);		//JR417 formDesc, formText and formIdVersion is not supposed to be empty (fixed)

    String returnCode = (String) out.get("p_return_code");
    String returnDesc = (String) out.get("p_return_desc");	//JR417 gave error: ORA-01400: cannot insert NULL into ("SBREXT"."QUEST_CONTENTS_EXT"."VERSION") etc
    String newFVVIdSeq = (String) out.get("p_val_idseq");
    
    if (!StringUtils.doesValueExist(returnCode)) {
        updateValueMeaning(newFVVIdSeq, newValidValue.getFormValueMeaningText(), 
                            newValidValue.getFormValueMeaningDesc(), userName);
//      return newFVVIdSeq;
    }
    //JR417 begin KISS
    else{
//      DMLException dml =  new DMLException(returnDesc);
//      dml.setErrorCode(this.ERROR_CREATEING_VALID_VALUE);
//      throw dml;
    	logger.info("JDBCFormValidValueDAOV2.java#createFormValidValueComponent Failed to create VV, error = [" + returnDesc + "]");
    }
    return newFVVIdSeq;
    //JR417 end
  }

  /**
   * Changes the display order of the specified form valid value. Display order
   * of the other form valid values in the question is also updated accordingly.
   *
   * @param <b>validValueId</b> Idseq of the form valid value component.
   * @param <b>newDisplayOrder</b> New display order of the form valid value.
   *
   * @return <b>int</b> 1 - success, 0 - failure.
   *
   * @throws <b>DMLException</b>
   */
  public int updateDisplayOrder(
    String validValueId,
    int newDisplayOrder, String username) throws DMLException {

    return updateDisplayOrderDirect(validValueId, "ELEMENT_VALUE", 
      newDisplayOrder,username);
  }

    /**
     * Changes the value meaning text in valid_value_att_ext table.
     */
    public int updateValueMeaning(String vvIdSeq, String updatedValueMeaningText, 
                                    String updatedValueMeaningDesc, String userName)
    throws DMLException {
        int count = 0;
        if ( (updatedValueMeaningText==null || updatedValueMeaningText.length()==0) &&
            (updatedValueMeaningDesc==null || updatedValueMeaningDesc.length()==0)){
            //remove this value meaning text
             DeleteValidValuesAtt sqlDeleteValidValuesAtt = 
                                new DeleteValidValuesAtt(this.getDataSource());
             return sqlDeleteValidValuesAtt.deleteValidValueAtt(vvIdSeq);
        }

        UpdateValidValuesAtt sqlUpdateValidValuesAtt = 
                                new UpdateValidValuesAtt(this.getDataSource());
        count = sqlUpdateValidValuesAtt.updateValueMeaning(
            vvIdSeq, updatedValueMeaningText, updatedValueMeaningDesc, userName);
        if (count != 0){
            return count;
        }
        InsertValidValuesAtt sqlInsertValidValuesAtt = new InsertValidValuesAtt(this.getDataSource());
        return sqlInsertValidValuesAtt.insertValueMeaning(
            vvIdSeq, updatedValueMeaningText, updatedValueMeaningDesc,userName);
    }
    
    
    
  /**
   * Deletes the specified form valid value and all its associated components.
   * 
   * @param <b>validValueId</b> Idseq of the form valid value component.
   *
   * @return <b>int</b> 1 - success, 0 - failure.
   *
   * @throws <b>DMLException</b>
   */
  public int deleteFormValidValue(String validValueId)
    throws DMLException {
    
    DeleteFormValidValue deleteValidValue = new DeleteFormValidValue(this.getDataSource());
    Map out = deleteValidValue.executeDeleteCommand(validValueId);

    String returnCode = (String) out.get("p_return_code");
    String returnDesc = (String) out.get("p_return_desc");
    
    if (!StringUtils.doesValueExist(returnCode)) {
      return 1;
    }
    else {
      DMLException dml =  new DMLException(returnDesc);
      dml.setErrorCode(this.ERROR_DELETEING_VALID_VALUE);
      throw dml;
    }
  }

  /**
   * Inner class that accesses database to create a valid value record in the
   * quest_contents_ext table.
   */
 private class InsertQuestContent extends SqlUpdate {
    public InsertQuestContent(DataSource ds) {
      String contentInsertSql = 
      " INSERT INTO sbrext.quest_contents_view_ext " + 
      " (qc_idseq, version, preferred_name, long_name, preferred_definition, " + 
      "  conte_idseq, proto_idseq, asl_name, created_by, qtl_name, vp_idseq) " +
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
      declareParameter(new SqlParameter("p_vp_idseq", Types.VARCHAR));
      compile();
    }
    protected int createContent (FormValidValue sm, String qcIdseq) 
    {
      String protocolIdSeq = null;
      
      //protocol is no more associated with questions
        /*
      if( sm.getQuestion().getModule().getForm().getProtocol()!=null)
      {
         protocolIdSeq=sm.getQuestion().getModule().getForm().getProtocol().getProtoIdseq();
      }
      */
      
      Object [] obj = 
        new Object[]
          {qcIdseq,
           sm.getVersion().toString(),
           generatePreferredName(sm.getLongName()),
           sm.getLongName(),
           sm.getPreferredDefinition(),
           sm.getContext().getConteIdseq(),
           protocolIdSeq,
           sm.getAslName(),
           sm.getCreatedBy(),
           "VALID_VALUE",
           sm.getVpIdseq()
          };
      
	    int res = update(obj);
      return res;
    }
  }

  /**
   * Inner class that accesses database to create a question and valid value 
   * relationship record in the qc_recs_ext table.
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
      declareParameter(new SqlParameter("p_pisplay_order", Types.INTEGER));
      declareParameter(new SqlParameter("p_rl_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_created_by", Types.VARCHAR));
      compile();
    }
    protected int createContent (FormValidValue sm, String qcIdseq, String qrIdseq) 
    {
      Object [] obj = 
        new Object[]
          {qrIdseq, 
           sm.getQuestion().getQuesIdseq(),
           qcIdseq,
           new Integer(sm.getDisplayOrder()),
           "ELEMENT_VALUE",
           sm.getCreatedBy()
          };
      
	    int res = update(obj);
      return res;
    }
  }

  /**
   * Inner class that accesses database to delete a valid value.
   */
  private class DeleteFormValidValue extends StoredProcedure {
    public DeleteFormValidValue(DataSource ds) {
      super(ds, "sbrext_form_builder_pkg.remove_value");
      declareParameter(new SqlParameter("p_val_idseq", Types.VARCHAR));
      declareParameter(new SqlOutParameter("p_return_code", Types.VARCHAR));
      declareParameter(new SqlOutParameter("p_return_desc", Types.VARCHAR));
      compile();
    }

    public Map executeDeleteCommand(String valueIdseq) {
      Map in = new HashMap();
      in.put("p_val_idseq", valueIdseq);

      Map out = execute(in);

      return out;
    }
  }

  /**
   * Inner class that accesses database to delete a valid value.
   */
  private class InsertFormValidValue extends StoredProcedure {
    public InsertFormValidValue(DataSource ds) {
      super(ds, "sbrext_form_builder_pkg.ins_value");
      declareParameter(new SqlParameter("p_ques_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_version", Types.FLOAT));
      declareParameter(new SqlParameter("p_preferred_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_long_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_preferred_definition", Types.VARCHAR));
      declareParameter(new SqlParameter("p_conte_idseq", Types.CHAR));
      declareParameter(new SqlParameter("p_proto_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_asl_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_vp_idseq", Types.VARCHAR));
      declareParameter(new SqlParameter("p_created_by", Types.VARCHAR));
      declareParameter(new SqlParameter("p_display_order", Types.NUMERIC));
      
      declareParameter(new SqlOutParameter("p_val_idseq", Types.VARCHAR));
      declareParameter(new SqlOutParameter("p_qr_idseq", Types.VARCHAR));
      declareParameter(new SqlOutParameter("p_return_code", Types.VARCHAR));
      declareParameter(new SqlOutParameter("p_return_desc", Types.VARCHAR));
      
      compile();
    }

    public Map executInsertCommand(FormValidValue fvv, String parentId) {
      String protocolIdSeq = null;

    //question is no more associated with protocols.
    /*
      if( fvv.getQuestion().getModule().getForm().getProtocol()!=null)
      {
         protocolIdSeq=fvv.getQuestion().getModule().getForm().getProtocol().getProtoIdseq();
      }      
     */
     
      Map in = new HashMap();
      
      logger.debug("question seqid: " + parentId);
      
      fvv.setVersion(Float.valueOf("3.0"));
      
      in.put("p_ques_idseq", parentId);
      logger.debug("p_version: >" + fvv.getVersion() + "<");
      in.put("p_version", fvv.getVersion());
      logger.debug("p_preferred_name: " + "TestingPrepareName");
      in.put("p_preferred_name", "TestingPrepareName");
      logger.debug("qp_long_name: " + fvv.getLongName());
      in.put("p_long_name", fvv.getLongName());
      logger.debug("p_preferred_definition: " + fvv.getPreferredDefinition());
      in.put("p_preferred_definition", fvv.getPreferredDefinition());
      logger.debug("p_conte_idseq: " + fvv.getContext().getConteIdseq());

      in.put("p_conte_idseq", fvv.getContext().getConteIdseq());
      
      //logger.debug("p_proto_idseq: " + fvv.getVpIdseq());
      in.put("p_proto_idseq", protocolIdSeq);
      in.put("p_proto_idseq", fvv.getVpIdseq());
      
      
      logger.debug("p_asl_name: " + fvv.getAslName());
      in.put("p_asl_name", fvv.getAslName());
      logger.debug("p_vp_idseq: " + fvv.getVpIdseq());
      in.put("p_vp_idseq", fvv.getVpIdseq());
      logger.debug("p_created_by: " + fvv.getCreatedBy());
      in.put("p_created_by", fvv.getCreatedBy());
      logger.debug("p_display_order: " + new Integer(fvv.getDisplayOrder()));
      in.put("p_display_order", new Integer(fvv.getDisplayOrder()));

      //System.out.println("JDBCFormValidValueDAOV2.java#executInsertCommand in [" + in + "]");	//JR417 no values supposed to be empty!
      Map out = execute(in);
      return out;
    }
  }
  
    /**
     * Inner class that accesses database to create a question and valid value 
     * relationship record in the qc_recs_ext table.
     */
    private class UpdateValidValuesAtt extends SqlUpdate {
      public UpdateValidValuesAtt(DataSource ds) {
        String updateValidValueAttrSql = 
        " update valid_values_att_ext set meaning_text = ?, description_text=?, modified_by=? " +
        " where qc_idseq=?";

        this.setDataSource(ds);
        this.setSql(updateValidValueAttrSql);
        declareParameter(new SqlParameter("meaning_text", Types.VARCHAR));
        declareParameter(new SqlParameter("description_text", Types.VARCHAR));
        declareParameter(new SqlParameter("modified_by", Types.VARCHAR));
        declareParameter(new SqlParameter("qc_idseq", Types.VARCHAR));
        compile();
      }
      protected int updateValueMeaning(String qcIdSeq, String valueMeaningText, 
                                        String valueMeaningDesc,String userName) 
      {
  		if (valueMeaningText.length() > 1999) {
			// Valid Value Meaning Text Column in the DB has a limit of 2000 chars.
			logger.debug("****** Valid Value Meaning text over 2000 chars, so truncating...");
			valueMeaningText = valueMeaningText.substring(0, 2000); 
		}    	  
        Object [] obj = 
          new Object[]
            {valueMeaningText,
            valueMeaningDesc,
             userName,
             qcIdSeq};
        
              int res = update(obj);
        return res;
      }
    }
  
    private class InsertValidValuesAtt extends SqlUpdate {
      public InsertValidValuesAtt(DataSource ds) {
        String insertValidValueAttrSql = 
        " insert into valid_values_att_ext (qc_idseq, meaning_text, description_text, created_by) values(?,?, ?, ?)";

        this.setDataSource(ds);
        this.setSql(insertValidValueAttrSql);
        declareParameter(new SqlParameter("qc_idseq", Types.VARCHAR));
        declareParameter(new SqlParameter("meaning_text", Types.VARCHAR));
        declareParameter(new SqlParameter("description_text", Types.VARCHAR));
        declareParameter(new SqlParameter("created_by", Types.VARCHAR));
        compile();
      }
      protected int insertValueMeaning(String qcIdSeq, String valueMeaningText, 
                    String valueMeaningDesc, String userName) 
      {
        Object [] obj = 
          new Object[]
            {qcIdSeq,
             valueMeaningText,
             valueMeaningDesc,
             userName};            
        int res = update(obj);
        return res;
      }
    }


    private class DeleteValidValuesAtt extends SqlUpdate {
      public DeleteValidValuesAtt(DataSource ds) {
        String deleteValidValueAttrSql = 
        " delete from valid_values_att_ext where qc_idseq=?";
        this.setDataSource(ds);
        this.setSql(deleteValidValueAttrSql);
        declareParameter(new SqlParameter("qc_idseq", Types.VARCHAR));
        compile();
      }
      protected int deleteValidValueAtt(String qcIdSeq) 
      {
        Object [] obj = 
          new Object[]
                {qcIdSeq};
        
        int res = update(obj);
        return res;
      }
    }


	@Override
	public void createFormValidValueComponents(List arg0, String arg1) throws DMLException {
		throw new DMLException("Not implemented", new Exception("Not implemented"));
	}

}
