package gov.nih.nci.ncicb.cadsr.common.persistence.dao;

import gov.nih.nci.ncicb.cadsr.common.dto.ModuleTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.exception.DMLException;

import java.util.List;


public interface ModuleDAOV2 extends AdminComponentDAOV2 {

  public List<QuestionTransferObject> getQuestionsInAModuleV2(String moduleId) throws DMLException;
  public ModuleTransferObject getModulePublicIdVersionBySeqid(String moduleseqid);

  }
