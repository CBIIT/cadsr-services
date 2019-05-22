package gov.nih.nci.cadsr.formloader.repository;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.ncicb.cadsr.common.dto.DataElementTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.FormV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.PermissibleValueV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ReferenceDocumentTransferObject;
import gov.nih.nci.ncicb.cadsr.common.resource.FormV2;
import gov.nih.nci.ncicb.cadsr.common.resource.PermissibleValueV2;
import gov.nih.nci.ncicb.cadsr.common.resource.ReferenceDocument;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueDomainV2;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface FormLoaderRepository {
	public List<FormV2> getFormsForPublicIDs(List<String> pubicIDList);
	public List<QuestionTransferObject> getQuestionsByPublicId(String publicId);
	public List<DataElementTransferObject> getCDEByPublicId(String cdePublicId);
	public List<ReferenceDocumentTransferObject> getReferenceDocsForQuestionCde(String cdePublicId, String cdeVersion);
	//public List<PermissibleValueV2TransferObject> getValueDomainPermissibleValuesByVdId(String vdSeqId);
	
	public List<QuestionTransferObject> getQuestionsByPublicIds(List<String> publicIds);
	public List<DataElementTransferObject> getCDEsByPublicIds(List<String> cdePublicIds);
	public HashMap<String, List<ReferenceDocumentTransferObject>> getReferenceDocsByCdePublicIds(List<String> cdePublicIds);
	public HashMap<String, List<PermissibleValueV2TransferObject>> getPermissibleValuesByVdIds(List<String> vdSeqIds);

	public String getContextSeqIdByName(String contextName);
	
	public String createForm(FormDescriptor form, String xmlPathName);
	public String updateForm(FormDescriptor form, String userName, String xmlPathName);
	public void setPublicIdVersionBySeqids(List<FormDescriptor> forms);
	
	public String createFormCollectionRecords(FormCollection coll);
	public int getMaxNameRepeatForCollection(String collName);
	
	/**
	 * Check to see if user has right to edit form (load includes create, update and delete) in the given context. Currently,
	 * this only checks the create right, assuming it covers also update and delete as shown in Form Builder.
	 * 
	 * @param form form that will be edited on. 
	 * @param userName
	 * @param contextName
	 * @return
	 */
	public boolean hasLoadFormRight(FormDescriptor form, String userName, String contextName);
	
	public List<FormCollection> getAllLoadedCollectionsByUser(String userName);
	
	public void checkWorkflowStatusName(FormDescriptor form);
	
	public void unloadForm(FormDescriptor form);
	
	public String createFormNewVersion(FormDescriptor form, String loggedinUser, String xmlPathName);
	
	public float getLatestVersionForForm(String publicId);
	
	public void updateFormInCollectionRecord(FormCollection coll, FormDescriptor form);
	
	public HashMap<String, Date> getModifiedDateForForms(List<String> formSeqids);
	
	public List<String> getDefinitionTextsByVmIds(String vmSeqid);
	public List<String> getDesignationNamesByVmIds(String vmSeqid);
	
	public ValueDomainV2 getValueDomainBySeqid(String vdseqid);
	
	public List<FormDescriptor> getAllFormsWithCollectionId(String collectionSeqid);
	
	public List<FormV2TransferObject> getFormsInCadsrBySeqids(List<String> formSeqids);
	
	public boolean designationTypeExists(String designName);
	public boolean definitionTypeValid(String definitionType);
	public boolean validClassificationScheme(String publicId, String version);
	public boolean validClassificationSchemeItem(String publicId, String version);
	
	public String getOrganizationSeqidByName(String orgName); 
	public boolean isContactCommunicationTypeValid(String contactCommType);
}
