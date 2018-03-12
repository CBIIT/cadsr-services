package gov.nih.nci.ncicb.cadsr.formbuilder.ejb.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import gov.nih.nci.ncicb.cadsr.common.resource.AdminComponentType;
import gov.nih.nci.ncicb.cadsr.common.resource.Form;
import gov.nih.nci.ncicb.cadsr.common.resource.FormInstructionChanges;
import gov.nih.nci.ncicb.cadsr.common.resource.FormV2;
import gov.nih.nci.ncicb.cadsr.common.resource.Instruction;
import gov.nih.nci.ncicb.cadsr.common.resource.Module;
import gov.nih.nci.ncicb.cadsr.common.resource.ModuleChanges;
import gov.nih.nci.ncicb.cadsr.common.resource.NCIUser;
import gov.nih.nci.ncicb.cadsr.common.resource.Protocol;
import gov.nih.nci.ncicb.cadsr.common.resource.QuestionRepitition;
import gov.nih.nci.ncicb.cadsr.common.resource.ReferenceDocument;
import gov.nih.nci.ncicb.cadsr.common.resource.TriggerAction;
import gov.nih.nci.ncicb.cadsr.common.resource.TriggerActionChanges;
import gov.nih.nci.ncicb.cadsr.common.resource.Version;


public interface FormBuilderService {
    public Collection getAllForms(String formLongName, String protocolIdSeq,
        String contextIdSeq, String workflow, String categoryName, String type,
        String classificationIdSeq,
        String publicId, String version, String moduleLongName, String cdePublicId,
        NCIUser user, String contextRestriction);

    public Form getFormDetails(String formPK);

    public FormV2 getFormDetailsV2(String formPK);

    public Form updateForm(String formIdSeq, Form formHeader, Collection updatedModules,
        Collection deletedModules,Collection addedModules,
        Collection addedProtocols, Collection removedProtocols,
        Collection protocolTriggerActionChanges,
        FormInstructionChanges instructionChanges, String username);

    public Module updateModule(String moduleIdSeq,ModuleChanges moduleChanges, String username);

    public Form getFormRow(String formPK);

    public Form copyForm(String sourceFormPK, Form newForm);

    public Form editFormRow(String formPK);

    public int deleteForm(String formPK);

    /**
     * Creates a Module
     *
     * @param module a <code>Module</code> value
     * @param modInstrustion a <code>ModuleInstruction</code> value
     *
     * @return The PK of the newly created Module
     *
     * @exception Exception if an error occurs
     */
    public String createModule(Module module, Instruction modInstrustion);

    public int removeModule(String formPK, String modulePK);

    public Form copyModules(String formPK, Collection modules);

    public Form createQuestions(String modulePK, Collection questions);

    public Form removeQuestions(String modulePK, Collection questions);

    public Form copyQuestions(String modulePK, Collection questions);

    public Form createValidValues(String modulePK, Collection validValues);

    public Form removeValidValues(String modulePK, Collection validValues);

    public Form copyValidValues(String modulePK, Collection validValues);

    public Collection getAllContexts();

    public Collection getAllFormCategories();

    public Collection getStatusesForACType(String acType);

    public boolean validateUser(String username, String password);

 //   public CDECart retrieveCDECart(String userName) throws Exception;

 //   public int addToCDECart(Collection items,String userName) throws Exception;

 //   public int removeFromCDECart(Collection items,String userName) throws Exception;

    public int updateDEAssociation(String questionId, String deId,
        String newLongName, String username);

    public Map getValidValues(Collection vdIdSeqs);
    
    public Map getVDPermissibleValues(Collection vdIdSeqs);
    
    public Map getCDEPermissibleValues(Collection cdeIdSeqs);
    
    public Map getQuestionValidValues(Collection quesIdSeqs);

    /**
     * Assigns the specified classification to an admin component
     *
     * @param <b>acId</b> Idseq of an admin component
     * @param <b>csCsiId</b> csCsiId
     * @param <b>csCDEIndicator</b>if the CDES on this form should be classified as well.
     *
     * @return <b>int</b> 1 - success; 0 - failure
     */
    public int assignFormClassification(List acIdList, List csCsiIdList);

    /**
     * Removes the specified classification assignment for an admin component
     *
     * @param <b>acCsiId</b> acCsiId
     *
     * @return <b>int</b> 1 - success; 0 - failure
     */
    public int removeFormClassification(String acCsiId);

    public int removeFFormClassification(String cscsiIdseq, String acId);
    public void removeFormClassificationUpdateTriggerActions(
        String cscsiId,  String acIdSeq, List<TriggerActionChanges> triggerChangesList, String username);

    /**
     * Retrieves all the assigned classifications for an admin component
     *
     * @param <b>acId</b> Idseq of an admin component
     *
     * @return <b>Collection</b> Collection of CSITransferObject
     */
    public Collection retrieveFormClassifications(String acId);

    public Form createForm(Form form, Instruction formHeaderInstruction,
        Instruction formFooterInstruction);

    //Publish Change Order
    public Collection getAllPublishedFormsForProtocol(String protocolIdSeq);
    //Publish Change Order
    public Collection getAllFormsForClassification(String classificationIdSeq);
    /**
     * Publishes the form by assigning publishing classifications to the form
     *
     * @inheritDoc
     */
    public void publishForm(String formIdSeq,String formType, String contextIdSeq);

    //Publish Change Order
    /**
     * Removes the publishing classifications assigned to this form
     * @inheritDoc
     */
      public void unpublishForm(String formIdSeq, String formType, String contextIdSe);

      public ReferenceDocument createReferenceDocument (ReferenceDocument refDoc, String acIdseq);

      public void deleteReferenceDocument (String rdIdseq);

      public void updateReferenceDocument (ReferenceDocument refDoc);

      public void deleteAttachment (String name);

      public Collection getAllDocumentTypes();

      public int saveDesignation(String contextIdSeq, List acIdList, String username);
      
      public Boolean isAllACDesignatedToContext(List cdeIdList , String contextIdSeq);
        
      public String createNewFormVersion(String formIdSeq, Float newVersionNumber, String changeNote, String username);

      public List getFormVersions(int publicId);

      public void setLatestVersion(Version oldVersion, Version newVersion, List changedNoteList, String username);
      public Float getMaxFormVersion(int publicId);
      public void removeFormProtocol(String formIdseq, String protocoldIdseq);
      public void removeFormProtocols(String formIdseq, Collection protocolds);
      public void addFormProtocol(String formIdseq, String protocoldIdseq, String username);
      public void addFormProtocols(String formIdseq, Collection protocolds, String username);

      public Protocol getProtocolByPK(String protocoldIdseq);
      public List getAllTriggerActionsForSource(String sourceId);
    
     public List getAllTriggerActionsForTarget(String targetId);
       
     public List<TriggerAction> getAllTriggerActionsForTargets(List<String> targetIds);
     
     public boolean isTargetForTriggerAction(List<String> targetIds);
     
     public TriggerAction createTriggerAction(TriggerAction action, String username);

     public TriggerAction updateTriggerAction(TriggerActionChanges changes, String username);
     public void updateTriggerActions(List<TriggerActionChanges> changesList, String username);

     public void deleteTriggerAction(String triggerActionId);

    public List getRreferenceDocuments(String acId);
    
    public AdminComponentType getComponentType(String publicId, String version);
                
    public Module saveQuestionRepititons(String moduleId,int repeatCount
            , Map<String,List<QuestionRepitition>> repititionMap,List<String> questionWithoutRepitions, String username);
    
    public boolean isDEDerived(String deIdSeq);

    public String getIdseq(int publicId, Float version);                
    }
