package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.object.StoredProcedure;

import gov.nih.nci.ncicb.cadsr.common.dto.InstructionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.exception.DMLException;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.InstructionDAO;
import gov.nih.nci.ncicb.cadsr.common.resource.Instruction;
import gov.nih.nci.ncicb.cadsr.common.util.StringUtils;

public abstract class JDBCInstructionDAOFB extends JDBCAdminComponentDAOFB implements InstructionDAO
{
    private class DeleteInstruction extends StoredProcedure
    {

        public Map executeDeleteCommand(String crfIdseq)
        {
            Map in = new HashMap();
            in.put("p_qc_idseq", crfIdseq);
            Map out = execute(in);
            return out;
        }

        //final JDBCInstructionDAOFB this$0;

        public DeleteInstruction(DataSource ds)
        {
        	super(ds, "sbrext_form_builder_pkg.remove_instr");
            //this$0 = JDBCInstructionDAOFB.this;
            declareParameter(new SqlParameter("p_qc_idseq", 12));
            declareParameter(new SqlOutParameter("p_return_code", 12));
            declareParameter(new SqlOutParameter("p_return_desc", 12));
            compile();
        }
    }

    private class UpdateInstruction extends SqlUpdate
    {

        protected int updateInstruction(Instruction instruction)
        {
            Object obj[] = {
                instruction.getLongName(), instruction.getPreferredDefinition(), instruction.getModifiedBy(), instruction.getIdseq()
            };
            int res = update(obj);
            return res;
        }

        final JDBCInstructionDAOFB this$0;

        public UpdateInstruction(DataSource ds)
        {
        	super();
            this$0 = JDBCInstructionDAOFB.this;
            String updateFormSql = " UPDATE sbrext.quest_contents_view_ext SET  long_name = ? , preferred_definition = ? , modified_by = ?  WHERE qc_idseq = ? ";
            setDataSource(ds);
            setSql(updateFormSql);
            declareParameter(new SqlParameter("long_name", 12));
            declareParameter(new SqlParameter("preferred_definition", 12));
            declareParameter(new SqlParameter("modified_by", 12));
            declareParameter(new SqlParameter("qc_idseq", 12));
            compile();
        }
    }

    class InstructionQuery_STMT extends MappingSqlQuery
    {

        public void setSql(String idSeq, String type)
        {
            super.setSql((new StringBuilder()).append("SELECT * FROM SBREXT.FB_INSTRUCTIONS_VIEW where P_QC_IDSEQ = '").append(idSeq).append("' and QTL_NAME = '").append(type).append("'").toString());
        }

        protected Object mapRow(ResultSet rs, int rownum)
            throws SQLException
        {
            Instruction instruction = new InstructionTransferObject();
            instruction.setIdseq(rs.getString(1));
            instruction.setVersion(Float.valueOf(rs.getFloat(2)));
            instruction.setConteIdseq(rs.getString(4));
            instruction.setPreferredName(rs.getString(6));
            instruction.setPreferredDefinition(rs.getString(7));
            instruction.setLongName(rs.getString(8));
            instruction.setDisplayOrder(rs.getInt(12));
            instruction.setAslName(rs.getString(5));
            return instruction;
        }

        final JDBCInstructionDAOFB this$0;

        InstructionQuery_STMT()
        {
        	super();
            this$0 = JDBCInstructionDAOFB.this;
        }
    }

    private class InsertQuestRec extends SqlUpdate
    {

        protected int createContent(Instruction intruction, String parentIdseq, String qcIdseq, String qrIdseq, String rlType)
        {
            Object obj[] = {
                qrIdseq, parentIdseq, qcIdseq, new Integer(intruction.getDisplayOrder()), rlType, intruction.getCreatedBy()
            };
            int res = update(obj);
            return res;
        }

        final JDBCInstructionDAOFB this$0;

        public InsertQuestRec(DataSource ds)
        {
        	super();
            this$0 = JDBCInstructionDAOFB.this;
            String questRecInsertSql = " INSERT INTO sbrext.qc_recs_view_ext  (qr_idseq, p_qc_idseq, c_qc_idseq, display_order, rl_name, created_by) VALUES ( ?, ?, ?, ?, ?, ? )";
            setDataSource(ds);
            setSql(questRecInsertSql);
            declareParameter(new SqlParameter("p_qr_idseq", 12));
            declareParameter(new SqlParameter("p_qc_idseq", 12));
            declareParameter(new SqlParameter("c_qc_idseq", 12));
            declareParameter(new SqlParameter("p_pisplay_order", 4));
            declareParameter(new SqlParameter("p_rl_name", 12));
            declareParameter(new SqlParameter("p_created_by", 12));
            compile();
        }
    }

    private class InsertQuestContent extends SqlUpdate
    {

        protected int createContent(Instruction instruction, String qcIdseq, String instructionType)
        {
            Object obj[] = {
                qcIdseq, instruction.getVersion().toString(), generatePreferredName(instruction.getLongName()), instruction.getLongName(), instruction.getPreferredDefinition(), instruction.getContext().getConteIdseq(), instruction.getAslName(), instruction.getCreatedBy(), instructionType
            };
            int res = update(obj);
            return res;
        }

        final JDBCInstructionDAOFB this$0;

        public InsertQuestContent(DataSource ds)
        {
        	super();
            this$0 = JDBCInstructionDAOFB.this;
            String contentInsertSql = " INSERT INTO sbrext.quest_contents_view_ext  (qc_idseq, version, preferred_name, long_name, preferred_definition,   conte_idseq, asl_name, created_by, qtl_name)  VALUES  (?, ?, ?, ?, ?,?, ?, ?, ?) ";
            setDataSource(ds);
            setSql(contentInsertSql);
            declareParameter(new SqlParameter("p_qc_idseq", 12));
            declareParameter(new SqlParameter("p_version", 12));
            declareParameter(new SqlParameter("p_preferred_name", 12));
            declareParameter(new SqlParameter("p_long_name", 12));
            declareParameter(new SqlParameter("p_preferred_definition", 12));
            declareParameter(new SqlParameter("p_conte_idseq", 12));
            declareParameter(new SqlParameter("p_asl_name", 12));
            declareParameter(new SqlParameter("p_created_by", 12));
            declareParameter(new SqlParameter("p_qtl_name", 12));
            compile();
        }
    }


    public JDBCInstructionDAOFB()
    {
        super();
    }

    public List getInstructions(String componentId, String type)
    {
        InstructionQuery_STMT query = new InstructionQuery_STMT();
        query.setDataSource(getDataSource());
        query.setSql(componentId, type);
        return query.execute();
    }

    public int createInstruction(Instruction instruction, String parentId, String type, String rlType)
        throws DMLException
    {
        InsertQuestContent insertQuestContent = new InsertQuestContent(getDataSource());
        String qcIdseq = generateGUID();
        int res = insertQuestContent.createContent(instruction, qcIdseq, type);
        if(res != 1)
            throw new DMLException("Did not succeed creating form instruction record in the  quest_contents_ext table.");
        InsertQuestRec insertQuestRec = new InsertQuestRec(getDataSource());
        String qrIdseq = generateGUID();
        int resRec = insertQuestRec.createContent(instruction, parentId, qcIdseq, qrIdseq, rlType);
        if(resRec == 1)
            return 1;
        else
            throw new DMLException("Did not succeed creating form instrction relationship record in the quest_recs_ext table.");
    }

    public int updateInstruction(Instruction newInstruction)
        throws DMLException
    {
        UpdateInstruction updateInstruction = new UpdateInstruction(getDataSource());
        int res = updateInstruction.updateInstruction(newInstruction);
        if(res != 1)
        {
            DMLException dmlExp = new DMLException("Did not succeed updating form record in the  quest_contents_ext table.");
            dmlExp.setErrorCode("DML0021");
            throw dmlExp;
        } else
        {
            return res;
        }
    }

    public int deleteInstruction(String instructionId)
        throws DMLException
    {
        DeleteInstruction deleteForm = new DeleteInstruction(getDataSource());
        Map out = deleteForm.executeDeleteCommand(instructionId);
        String returnCode = (String)out.get("p_return_code");
        String returnDesc = (String)out.get("p_return_desc");
        if(!StringUtils.doesValueExist(returnCode))
        {
            return 1;
        } else
        {
            DMLException dmlExp = new DMLException(returnDesc);
            dmlExp.setErrorCode("DML004");
            throw dmlExp;
        }
    }
}
