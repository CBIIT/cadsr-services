package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.ncicb.cadsr.common.exception.DMLException;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.FormInstructionDAO;
import gov.nih.nci.ncicb.cadsr.common.resource.Instruction;

import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class JDBCFormInstructionDAOV2 extends JDBCInstructionDAOV2
  implements FormInstructionDAO {
  
private static Logger logger = Logger.getLogger(JDBCFormInstructionDAOV2.class.getName());
	
	public JDBCFormInstructionDAOV2(DataSource dataSource) {
		super(dataSource);
	}

  /**
   * Creates a new form instruction component (just the header info).
   *
   * @param <b>formInstr</b> FormInstruction object
   *
   * @return <b>int</b> 1 - success, 0 - failure.
   *
   * @throws <b>DMLException</b>
   */
  public int createInstruction(Instruction formInstr, String parentId)
    throws DMLException {
   return  super.createInstruction(formInstr,parentId,"FORM_INSTR","FORM_INSTRUCTION");
  }

  public int createFooterInstruction(Instruction formInstr, String parentId)
    throws DMLException {
   return  super.createInstruction(formInstr,parentId,"FOOTER","FORM_INSTRUCTION");
  }
  
  public List getInstructions(String formID)
    throws DMLException {
    return super.getInstructions(formID,"FORM_INSTR");
  }
  
  public List getFooterInstructions(String formID)
    throws DMLException {
    return super.getInstructions(formID,"FOOTER");
  }


}
