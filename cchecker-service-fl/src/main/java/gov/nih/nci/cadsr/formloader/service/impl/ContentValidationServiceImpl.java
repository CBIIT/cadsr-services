package gov.nih.nci.cadsr.formloader.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.domain.ModuleDescriptor;
import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor;
import gov.nih.nci.cadsr.formloader.repository.impl.FormLoaderRepositoryImpl;
import gov.nih.nci.cadsr.formloader.repository.impl.LoadServiceRepositoryImpl;
import gov.nih.nci.cadsr.formloader.service.ContentValidationService;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderHelper;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderServiceException;
import gov.nih.nci.cadsr.formloader.service.common.QuestionsPVLoader;
import gov.nih.nci.cadsr.formloader.service.common.StaXParser;
import gov.nih.nci.ncicb.cadsr.common.dto.ClassificationTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ContactCommunicationV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.DataElementTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.DefinitionTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.DesignationTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.PermissibleValueV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ReferenceDocumentTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ValueMeaningV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.resource.FormV2;
import gov.nih.nci.ncicb.cadsr.common.resource.ReferenceDocument;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueDomainV2;
import gov.nih.nci.ncicb.cadsr.common.util.ValueHolder;

@Service
public class ContentValidationServiceImpl implements ContentValidationService {
	
	private static final Logger logger = LoggerFactory.getLogger(ContentValidationServiceImpl.class.getName());
	
	FormLoaderRepositoryImpl repository;
	
	public ContentValidationServiceImpl() {}
	
	public ContentValidationServiceImpl(FormLoaderRepositoryImpl repository) {
		this.repository = repository;
	}

	public FormLoaderRepositoryImpl getRepository() {
		return repository;
	}

	public void setRepository(FormLoaderRepositoryImpl repository) {
		this.repository = repository;
	}
	
	@Autowired
	private LoadServiceRepositoryImpl loadServiceRepositoryImpl; // santhanamv 

	@Override
	public FormCollection validateXmlContent(FormCollection aCollection) 
			throws FormLoaderServiceException {
		
		// santhanamv - commented out methods that may not be necessary, to just get the questions validated
		
/*		quickCheckOnCollection(aCollection);
		
		String xmlPathName = FormLoaderHelper.checkInputFile(aCollection.getXmlPathOnServer(), aCollection.getXmlFileName());
			
		if (aCollection.isSelectAllForms()) 
			aCollection.resetAllSelectFlag(true); */
		
		List<FormDescriptor> formHeaders = aCollection.getForms();
		/*determineLoadType(formHeaders);
		 
		validateFormInfo(xmlPathName, formHeaders, aCollection.getCreatedBy()); */ // santhanamv - end
		
		// santhanamv - xmlPathName is not required (temporarily disabled inside validateQuestions method) 		
		validateQuestions("File path name", formHeaders); // validateQuestions(xmlPathName, formHeaders);
		
		aCollection.resetAllSelectFlag(false);
		return aCollection;
	}
	
	/**
	 * Quick sanity check to make sure the collection has necessary elements.
	 * 
	 * @param aCollection
	 * @throws FormLoaderServiceException
	 */
	protected void quickCheckOnCollection(FormCollection aCollection) 
			throws FormLoaderServiceException {
		if (aCollection == null)
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_COLLECTION_NULL,
					"Input collection is null");
		
		List<FormDescriptor> formHeaders = aCollection.getForms();
		if (formHeaders == null || formHeaders.size() == 0) {
			logger.error("Input form list is null or empty. Nothing to do");
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_EMPTY_FORM_LIST,
					"Input form list is null or empty. Unable to validate form content.");
		}
		
		String loggedinUser = aCollection.getCreatedBy();
		if (loggedinUser == null || loggedinUser.length() == 0) {
			logger.error("Collection's createdBy (Form Loader's logged in user) is not set. Unable to proceed");
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_USER_INVALID,
					"Collection's createdBy (Form Loader's logged in user) is not set. Unable to proceed.");
		}
	}
	
	/**
	 * 1st part of the content validation
	 * @param formDescriptors
	 */
	protected void determineLoadType(List<FormDescriptor> formDescriptors) {
		
		List<String> pidList = new ArrayList<String>();
		
		for (FormDescriptor form : formDescriptors) {
			if (!form.isSelected()) 
				continue;
			
			if (form.getLoadStatus() < FormDescriptor.STATUS_XML_VALIDATED) {
				form.setSelected(false); //turn this off so we won't do other val check.
				continue;
			}
			
			//Collect public ids so we could do one query for the batch
			String publicid = form.getPublicId();
			if (publicid != null && publicid.length() > 0)
				pidList.add(publicid); 	
			else {
				form.setLoadType(FormDescriptor.LOAD_TYPE_NEW);
			}
		}
		
		if (pidList.size() > 0) {
			List<FormV2> formDtos = loadServiceRepositoryImpl.getFormsForPublicIDs(pidList);
			HashMap<String, List<Float>> publicIdVersions = createFormExistingVersionMap(formDtos);
			determineLoadTypeByVersion(formDescriptors, publicIdVersions);

			//extra info we want from the dtos, eg. seq id for update form and new version
			transferFormSeqids(formDtos, formDescriptors);
			transferFormCurrentWorkflow(formDtos, formDescriptors);
		}
		
	}
	
	/**
	 * Validate form's context name, workflow status, form type and Form Loader loggedin user's load right in 
	 * the form's specified context. Any discrepancy will be entered as a message in the form message list
	 *  
	 * @param formDescriptors
	 */
	protected void validateFormInfo(String xmlPathName, List<FormDescriptor> formDescriptors, String loggedinUser) {
		
		for (FormDescriptor form : formDescriptors) {		
			if (!form.isSelected()) 
				continue;
			
			this.loadServiceRepositoryImpl.checkWorkflowStatusName(form);
			
			if (!validContextName(form)) {
				form.setLoadStatus(FormDescriptor.STATUS_CONTENT_VALIDATION_FAILED);
				form.setSelected(false);
				continue;
			}			
			
			verifyFormType(form);
			verifyCredential(form, loggedinUser);
		
			StaXParser parser = new StaXParser();
			parser.parseFormDetails(xmlPathName, form, form.getIndex());
			
			form.setRefdocs(parser.getRefdocs());
			verifyProtocols(form, form.getProtocols());		
			verifyDesignations(form, form.getDesignations());
			verifyDefinitions(form, form.getDefinitions());
			verifyContactCommnunications(form, form.getContactCommnunications());
			verifyClassifications(form);
		}
	}
	
	protected void verifyClassifications(FormDescriptor form)
	{
		List<ClassificationTransferObject> classifications = form.getClassifications();
		if (classifications != null && classifications.size() > 0)
		{
			List<ClassificationTransferObject> newClassifications = new ArrayList<ClassificationTransferObject>();
			
			int idx = 0;
			for (ClassificationTransferObject classification : classifications)
			{
				if ((classification.getPublicID() == null || classification.getPublicID().length() == 0) || 
					(classification.getCsiPublicID() == null || classification.getCsiPublicID().length() == 0))
				{
					form.addMessage("Classification/Classification Scheme Item " + (idx+1) + " does not have a Public ID. It cannot be loaded." );
				}
				else if ((classification.getVersion() == null || classification.getVersion().length() == 0) || 
						(classification.getCsiVersion() == null || classification.getCsiVersion().length() == 0))
				{
					form.addMessage("Classification/Classification Scheme Item " + (idx + 1) + " does not have a Version. It cannot be loaded." );
				}
				else
				{
					String csCsiIdSeq = this.loadServiceRepositoryImpl.getClassificationSchemeItem(classification.getPublicID(), classification.getVersion(),
																				  classification.getCsiPublicID(), classification.getCsiVersion());
					if (csCsiIdSeq == null || csCsiIdSeq.length() == 0 )
					{
						form.addMessage("Classification [" + classification.getName() +  " - " + classification.getPublicID() + "v" + classification.getVersion() + 
										"] with Classification Scheme Item [" + classification.getCsiName() +  " - " + classification.getCsiPublicID() + "v" + classification.getCsiVersion() + 
										"] does not exist. It cannot be loaded");
					}
					else
					{
						classification.setCsCsiIdSeq(csCsiIdSeq);
						newClassifications.add(classification);
					}
				}
				idx++;
			}
			form.setClassifications(newClassifications);
		}
	}
	
	protected void verifyProtocols(FormDescriptor form, List<ProtocolTransferObjectExt> protocols) {
		if (protocols == null || protocols.size() == 0)
			return;
		
		List<ProtocolTransferObjectExt> protos = new ArrayList<ProtocolTransferObjectExt>();
		
		int idx = 1;
		for (ProtocolTransferObjectExt proto : protocols) {
			String preferredName = proto.getPreferredName();
			
			if (preferredName == null || preferredName.length() == 0) {
				form.addMessage("Protocol " + idx + " has null or empty shortName value. Unable to load it.");
				continue;
			}
			
			String contextSeqid = this.loadServiceRepositoryImpl.getContextSeqIdByName(proto.getContextName());
			if (contextSeqid == null || contextSeqid.length() == 0)
				contextSeqid = form.getContextSeqid();
			
			String protoSeqid = this.loadServiceRepositoryImpl.getProtocolSeqidByPreferredName(preferredName, contextSeqid);
			if (protoSeqid != null && protoSeqid.length() > 0) {
				proto.setConteIdseq(contextSeqid);
				proto.setIdseq(protoSeqid);
				protos.add(proto);
			}
			else
			{
				form.addMessage("Protocol [" + preferredName +  "] does not exist, will not be loaded with this form.");
			}
		}
		
		if (protos.size() > 0) 
			form.setProtocols(protos);
	}
	
	/**
	 * Check on the designations for a form from the input xml.
	 * 
	 * 1. designation name can't be empty and type has to match one in db
	 * 2. languageName could be invalid and be defaulted to ENGLISH
	 * 3. If <classification> is present, use its publicid+version AND the classificationSchemaName's publicid_version to validate
	 * 4. If exist, create associate between form and designation
	 * 5. If not, create then create association (for update form)
	 * 
	 * If the context in the designation block is valid, use that. Otherwise, use the form's
	 * 
	 * @param form
	 * @param designations
	 */
	protected void verifyDesignations(FormDescriptor form, List<DesignationTransferObjectExt> designations) {
		
		if (designations == null || designations.size() == 0)
			return;
		
		List<DesignationTransferObjectExt> desigs = new ArrayList<DesignationTransferObjectExt>();
		
		int idx = 0;
		for (DesignationTransferObjectExt des : designations) {
			idx++;
			if (des.getName() == null || des.getName().length() == 0) {
				form.addMessage("Designation #" + idx + " has invalid name. Skip loading.");
				continue;  
			}
			
			if (!loadServiceRepositoryImpl.designationTypeExists(des.getType())) {
				form.addMessage("Designation #" + idx + " has invalid type \"" + des.getType() + "\". Skip loading.");
				continue;
			}
			
			if (classificationValid(des.getClassficationPublicIdVersionPairs())) {
				//if good, add to the form
				desigs.add(des);
			} else
				form.addMessage("Form's designation #" + idx + " has invalid ClassificationScheme or ClassificationSchemeItem");
		}		
		
		form.setDesignations(desigs);
	}
	
	/**
	 * Check on the contact commnunications for the form
	 * 
	 * - value must not be null or empty
	 * - type must not be null or empty
	 * - type needs to be a valid type in db (hardcoded for now)
	 * 
	 * @param form
	 * @param contacts
	 */
	protected void verifyContactCommnunications(FormDescriptor form, List<ContactCommunicationV2TransferObject> contacts) {
		
		if (contacts == null || contacts.size() == 0)
			return;
		
		List<ContactCommunicationV2TransferObject> communications = new ArrayList<ContactCommunicationV2TransferObject>();
		
		int idx = 0;
		for (ContactCommunicationV2TransferObject con : contacts) {
			idx++;
			if (con.getValue() == null || con.getType().length() == 0) {
				form.addMessage("ContactCommunication #" + idx + " has empty or null value. Skip loading.");
				continue;  
			}
			
			String type = con.getType();
			
			if (type == null || type.length() == 0) {
				form.addMessage("ContactCommunication #" + idx + " has invalid empty or null type. Skip loading.");
				continue;  
			}
			
			if (!loadServiceRepositoryImpl.isContactCommunicationTypeValid(type)) {
				form.addMessage("ContactCommunication type [" + type + "] is invalid. Skip loading.");
				continue;
			}
			
			String orgSeqid = loadServiceRepositoryImpl.getOrganizationSeqidByName(con.getOrganizationName());
			if (orgSeqid == null || orgSeqid.length() == 0) {
				form.addMessage("ContactCommunication #" + idx + " has invalid organization name [" + con.getOrganizationName() + "]. Skip loading.");
				continue;
			} 			
			
			communications.add(con);
		}	
		
		form.setContactCommnunications(communications);
	}
	
	protected boolean classificationValid(List<String> classificationPublicIdVersionPairs) {
		if (classificationPublicIdVersionPairs == null || classificationPublicIdVersionPairs.size() == 0)
			return true;
		
		for (int i = 0; i < classificationPublicIdVersionPairs.size(); i++) {
			String[] pair = classificationPublicIdVersionPairs.get(i).split(",");
			
			if (i == 0) {
				if (!this.loadServiceRepositoryImpl.validClassificationScheme(pair[0], pair[1]))
					return false;
			} else {
				if (!this.loadServiceRepositoryImpl.validClassificationSchemeItem(pair[0], pair[1]))
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Check on definitions to the form from xml
	 * 
	 * 1. Definition text can't be empty and type has to match one in db
	 * 2. languageName could be invalid and be defaulted to ENGLISH
	 * 3. If <classification> is present, use its publicid+version AND the classificationSchemaName's publicid_version to validate
	 * 4. If exist, create associate between form and designation
	 * 5. If not, create then create association (for update form)
	 * 
	 * If the context in the designation block is valid, use that. Otherwise, use the form's
	 * @param form
	 * @param designations
	 */
	protected void verifyDefinitions(FormDescriptor form, List<DefinitionTransferObjectExt> definitions) {
		
		if (definitions == null || definitions.size() == 0)
			return;
		
		List<DefinitionTransferObjectExt> defs = new ArrayList<DefinitionTransferObjectExt>();
		
		int idx = 0;
		for (DefinitionTransferObjectExt def : definitions) {
			idx++;
			if (def.getDefinition() == null || def.getDefinition().length() == 0) {
				form.addMessage("Definition #" + idx + " has invalid text value. Skip loading.");
				continue; 
			}
			
			if (!loadServiceRepositoryImpl.definitionTypeValid(def.getType())) {
				form.addMessage("Definition #" + idx + " has invalid type \"" + def.getType() + "\". Skip loading.");
				continue;
			}
			
			if (classificationValid(def.getClassficationPublicIdVersionPairs())) {
				//if good, add to the form
				defs.add(def);
			} else
				form.addMessage("Form's definition #" + idx + " has invalid ClassificationScheme or ClassificationSchemeItem. Skip loading");
		}		
		
		form.setDefinitions(defs);
	}
	
	/**
	 * 
	 * @param form
	 */
	protected void verifyFormType(FormDescriptor form) {
		
		if (form == null) return;
		
		//This should be done with list from database
		//But we couldn't find the table that contains the list
		String formType = form.getType();
		if (formType == null || formType.length() == 0) {
			form.addMessage("Form type in xml is empty. Use default type CRF");
			form.setType("CRF");
			form.setDefaultWorkflowName();
		} else {
			if (!formType.equals("CRF") && !formType.equals("TEMPLATE")) {
				form.addMessage("Form type \"" + formType + "\" is invalid. Use default type CRF");
				form.setType("CRF");
				form.setDefaultWorkflowName();
			}
		}
	}
	
	/**
	 * Set the form's CreateBy to FORMLOADER
	 * Set the form's ModifiedBy to loggedin user name and verify that he /she has right to load in db
	 * 
     * CreatedBy and modifiedBy from xml go into the changenote.
	 * @param form
	 * @param loggedinUser
	 */
	protected void verifyCredential(FormDescriptor form, String loggedinUser) {
		if (form == null) return;
		
		if (!this.loadServiceRepositoryImpl.hasLoadFormRight(form, loggedinUser, form.getContext())) {
			form.addMessage("Form Loader logged in user [" + loggedinUser + "] doesn't have load form right in context [" + 
					form.getContext() + "]. Validation failed");
			form.setLoadStatus(FormDescriptor.STATUS_CONTENT_VALIDATION_FAILED);
			form.setSelected(false);
		} else {
			
			if (form.getChangeNote() == null || form.getChangeNote().length() == 0) {
				String changeNote = "Created/Updated using Form Loader by [" + loggedinUser + 
					"], XML document contained createdBy [" + form.getCreatedBy() + "] and modifiedBy [" + form.getModifiedBy() + "]";
				form.setChangeNote(changeNote);
			}
			
			form.setCreatedBy("FORMLOADER");
			form.setModifiedBy(loggedinUser);
		}
		
		
	}
	
	/**
	 * If context name is null or empty, use default "NCIP". If context name is present but not matching any valid value
	 * from database, add a message to the form and reject loading the form.
	 * 
	 * @param form from xml
	 * @return true if context name in the form is valid or empty, thus, reset to default "NCIP"; false if context name invalid
	 */
	protected boolean validContextName(FormDescriptor form) {
		
		String contextName = form.getContext();
		if (contextName == null || contextName.length() == 0) {
			form.addMessage("New Forms will contain 1 caDSR Context (if not, it should have the default \"NCIP\")");
			form.setContext("NCIP");
			form.setDefaultWorkflowName();
		}
		
		String contextSeqid = this.loadServiceRepositoryImpl.getContextSeqIdByName(form.getContext());
		if (contextSeqid == null || contextSeqid.length() == 0) {
			form.addMessage("Context name in form [" + form.getContext() + "] is not valid. Unable to load form");
			return false;
		} 
		
		form.setContextSeqid(contextSeqid);
		return true;
	}
	
	/**
	 * Assumption is that the list of dtos is ordered by public id and version
	 * @param formDtos
	 * @return
	 */
	protected HashMap<String, List<Float>> createFormExistingVersionMap(List<FormV2> formDtos) {
		HashMap<String, List<Float>> map = new HashMap<String, List<Float>>();
		for (FormV2 formdto : formDtos) {
			String pubId = String.valueOf(formdto.getPublicId());
			List<Float> vers = (!map.containsKey(pubId)) ? new ArrayList<Float>() : map.get(pubId);
			
			vers.add(Float.valueOf(formdto.getVersion()));		
			map.put(pubId, vers);
		}
		
		return map;
	}
	
	/**
	 * 
	 * @param formHeaders forms parsed from xml
	 * @param existingVersions hashmap with form public id as key and a list of existing versions from db as value
	 */
	protected void determineLoadTypeByVersion(List<FormDescriptor> formHeaders,
			HashMap<String, List<Float>> existingVersions) {

		//For duplicate check
		//Seems the only real duplicates are those with the same public id + version and has a match in db
		HashMap<String, String> processed = new HashMap<String, String>();
		
		for (FormDescriptor form : formHeaders) {
			if (!form.isSelected())
				continue;
			
			String publicid = form.getPublicId();
			
			if (publicid == null || publicid.length() == 0) {
				form.setLoadType(FormDescriptor.LOAD_TYPE_NEW);
			} else if (!existingVersions.containsKey(publicid)) {// invalid public id from xml
				form.setLoadType(FormDescriptor.LOAD_TYPE_UNKNOWN);
				form.setSelected(false);
				form.addMessage(
					"Form public id from xml doesn't have a match in database. Please correct or it'll be skipped loading");
				form.setLoadStatus(FormDescriptor.STATUS_CONTENT_VALIDATION_FAILED);
			} else {
				String existingVersForForm = formatVersionList(existingVersions.get(publicid));
				form.setVersionCadsr(existingVersForForm);
				determineLoadTypeForForm(form, existingVersions.get(publicid));
			}
		}
		
	}
	
	protected String formatVersionList(List<Float> versions) {
		if (versions == null)
			return "";
		
		//Use set to filter duplicates
		SortedSet<String> versionSet = new TreeSet<String>();
		for (Float version : versions) {
			String v = FormLoaderHelper.formatVersion(version.floatValue());
			versionSet.add(v);
		}
		
		StringBuilder sb = new StringBuilder();
		for (String aVers : versionSet) {
			if (sb.length() > 0) 
				sb.append(", ");			
			sb.append(aVers);
		}
		
		return sb.toString();
	}
	
	/**
	 * Determine load type. We get here only if public id in xml has a match in db
	 * @param form
	 * @param existingVersions assumption is that existing versions are ordered from least to greatest
	 */
	protected void determineLoadTypeForForm(FormDescriptor form, List<Float> existingVersions) {
		
		String version = form.getVersion();
		if (version == null || version.length() == 0) {
			form.setLoadType(FormDescriptor.LOAD_TYPE_UNKNOWN);
			form.setSelected(false);
			form.addMessage("Form public id from xml has a match in database but version string is null. Please correct or it'll be skipped loading");
		}
		else {
			float versNum = Float.valueOf(version).floatValue();
			float highestVers = existingVersions.get(existingVersions.size()-1);
			boolean matched = false;
			for (Float existing : existingVersions) {
				if (versNum == existing.floatValue()) {
					form.setLoadType(FormDescriptor.LOAD_TYPE_UPDATE_FORM);
					if (versNum < highestVers) 
						form.addMessage("A newer version of this form exists in dabatase. Updating an older version of the form.");
					matched = true;
					break;
				}
			}
			
			if (!matched) {
				form.setLoadType(FormDescriptor.LOAD_TYPE_NEW_VERSION);
			}
			
		}
	}
	
	/**
	 * Transfer extra info from form dtos to the form descriptor list for "update" forms or new version forms, for later use
	 * @param formDtos list of dtos from a query by public id query
	 * @param forms forms parsed from xml
	 */
	protected void transferFormSeqids(List<FormV2>formDtos, List<FormDescriptor> forms) {
		for (FormDescriptor formDesc : forms) {
			if (formDesc.getLoadType().equals(FormDescriptor.LOAD_TYPE_NEW)) 
				continue;
			
			FormV2 match = findMatchingFormDto(formDtos, formDesc);
			if (match != null)
				formDesc.setFormSeqId(match.getIdseq());
		}
	}
	
	/**
	 * If a form is determined to be a Update Form or New Version, set the currently existing 
	 * workflow status
	 * @param formDtos
	 * @param forms
	 */
	protected void transferFormCurrentWorkflow(List<FormV2>formDtos, List<FormDescriptor> forms) {
		for (FormDescriptor formDesc : forms) {
			if (formDesc.getLoadType().equals(FormDescriptor.LOAD_TYPE_NEW)) 
				continue;
			
			FormV2 match = findMatchingFormDto(formDtos, formDesc);
			if (match != null) 
				formDesc.setWorkflowStatusCadsr(match.getAslName());
		}
	}
	
	/**
	 * Find a matching form dto based on a xml form (form parsed from xml)'s public id or/and version,
	 * depending on whether it's a new version form or an update form
	 * 
	 * @param formDtos form dtos from a query by public ids
	 * @param form a form from xml
	 * @return a matching dto or null
	 */
	protected FormV2 findMatchingFormDto(List<FormV2> formDtos, FormDescriptor form) {
		
		for (FormV2 formdto : formDtos) {
			try {
				int publicId = Integer.parseInt(form.getPublicId());
				float version = Float.parseFloat(form.getVersion());

				if (formdto.getPublicId() == publicId) {
					if (form.getLoadType().equals(FormDescriptor.LOAD_TYPE_NEW_VERSION))
						return formdto;
					else if (form.getLoadType().equals(FormDescriptor.LOAD_TYPE_UPDATE_FORM)
							&& formdto.getVersion().floatValue() == version) 
						return formdto;
				} 

			} catch (NumberFormatException ne) {
				logger.debug("Error while converting string to number: >" + form.getPublicId() + "< and >" + form.getVersion() + "<");
				continue;
			}

		}
		
		return null;
	}
	
	/**
	 * Check if a form having the same valid (has match in db) public id and version has been 
	 * processed. If so, add a message to inform user
	 * @param processed
	 * @param currPublicid
	 * @param currVersion
	 * @param currForm
	 */
	protected void checkDuplicate(HashMap<String, String> processed, 
			String currPublicid, String currVersion, FormDescriptor currForm) {
		if (processed == null || processed.size() == 0)
			return;
		
		if (currForm.getLoadType().equals(FormDescriptor.LOAD_TYPE_UPDATE_FORM)) {
			if (processed.containsKey(currPublicid) ) {
				if (processed.get(currPublicid).contains(currVersion)) 
					currForm.addMessage("Duplicate form found - there is already a form in xml that has the same " +
							"public id and version. This form will overwrite the earlier one.");
				else
					processed.put(currPublicid, processed.get(currPublicid) + "," + currVersion);
			} else 
				processed.put(currPublicid, currVersion);
		}
		
	}

	/**
	 * 2nd part of the content validation
	 * @param xmlPathName
	 * @param formHeaders
	 */
	protected void validateQuestions(String xmlPathName, List<FormDescriptor> formDescriptors) {
		// santhanamv - start
		/*List<FormDescriptor> forms = getFormQuestionsFromXml(xmlPathName, formDescriptors);
		
		if (forms == null) {
			logger.error("form list is null. This should not happen");
			return;
		}*/ // santhanamv - end
			
		//First pass, collect question public ids and their cde ids so we could get necessary data from 
		//database as a list (= less queries)
		for (FormDescriptor form : formDescriptors) {
			if (!form.isSelected()) 
				continue;
			
			String formLoadType = form.getLoadType();
			
			//Temp disabling Update form for v4.1
			if (formLoadType.equals(FormDescriptor.LOAD_TYPE_UPDATE_FORM)) {
				form.setLoadStatus(FormDescriptor.STATUS_CONTENT_VALIDATED);
				continue;
			}
			//Temp disabling Update form for v4.1
			
			if (!formLoadType.equals(FormDescriptor.LOAD_TYPE_NEW_VERSION)
					&& !formLoadType.equals(FormDescriptor.LOAD_TYPE_NEW) 
					&& !formLoadType.equals(FormDescriptor.LOAD_TYPE_UPDATE_FORM)) {
				//We should not get here. Check code if we see this message.
				form.addMessage("The form has undetermined load type. Unable to validate its questions.");
				form.setSelected(false);
				continue;
			}
			
			logger.debug("Start validating questions for form [" + form.getPublicId() + "|" + form.getVersion() + "|" + form.getFormSeqId() + "]");
			
			//JR417 refactored into FormLoaderHelper!
//			List<String> questPublicIds = new ArrayList<String>();
//			List<String> questCdePublicIds = new ArrayList<String>();
List<ModuleDescriptor> modules = null;	//form.getModules();
//collectPublicIdsForModules(modules, questPublicIds, questCdePublicIds, formLoadType);
			
List<QuestionTransferObject> questDtos = null;	//repository.getQuestionsByPublicIds(questPublicIds);
List<DataElementTransferObject> cdeDtos = null;	//repository.getCDEsByPublicIds(questCdePublicIds);
//			
//			HashMap<String, List<ReferenceDocumentTransferObject>> refdocDtos = repository.getReferenceDocsByCdePublicIds(questCdePublicIds);
//			List<String> vdSeqIds = new ArrayList<String>();
//			for (DataElementTransferObject de: cdeDtos) {
//				String vdseqId = de.getVdIdseq();
//				if (vdseqId != null && vdseqId.length() > 0)
//					vdSeqIds.add(vdseqId);
//			}
			
//			HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos = 
//					repository.getPermissibleValuesByVdIds(vdSeqIds);	//JR417 pv has the vpIdseq and vm has the vmIdseq after this successful call!

			logger.info("ContentValidationServiceImpl.java#validateQuestions before FormLoaderHelper.populateQuestionsPV");
			ValueHolder vh = FormLoaderHelper.populateQuestionsPV(form, loadServiceRepositoryImpl); // santhanamv - repository was null here, so passing LoadServiceRepositoryImpl instead
			logger.info("ContentValidationServiceImpl.java#validateQuestions after FormLoaderHelper.populateQuestionsPV");
			HashMap<String, List<ReferenceDocumentTransferObject>> refdocDtos = null;
			HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos = null;
			try {
				List data = (ArrayList) vh.getValue();
				pvDtos = (HashMap<String, List<PermissibleValueV2TransferObject>>) data.get(QuestionsPVLoader.PV_INDEX);
				modules = (List<ModuleDescriptor>) data.get(QuestionsPVLoader.MODULE_INDEX);
				questDtos = (List<QuestionTransferObject>) data.get(QuestionsPVLoader.QUESTION_INDEX);
				cdeDtos = (List<DataElementTransferObject>) data.get(QuestionsPVLoader.CDE_INDEX);
				refdocDtos = (HashMap<String, List<ReferenceDocumentTransferObject>>) data.get(QuestionsPVLoader.REF_DOC_INDEX);
				pvDtos = (HashMap<String, List<PermissibleValueV2TransferObject>>) data.get(QuestionsPVLoader.PV_INDEX);
			} catch(Exception e) {
				e.printStackTrace();
			}
			//JR417 end

			logger.info("ContentValidationServiceImpl.java#validateQuestions before validateQuestionsInModules");
			validateQuestionsInModules(modules, form, questDtos, cdeDtos, refdocDtos, pvDtos);		
			logger.info("ContentValidationServiceImpl.java#validateQuestions after validateQuestionsInModules");
			
			form.setLoadStatus(FormDescriptor.STATUS_CONTENT_VALIDATED);
			
			logger.debug("Done validating questions for form [" + form.getPublicId() + "|" + form.getVersion()  + "]");
		}
		
	}
	
	protected void validateQuestionsInModules(List<ModuleDescriptor> modules, FormDescriptor form, 
			List<QuestionTransferObject> questDtos, List<DataElementTransferObject> cdeDtos, 
			HashMap<String, List<ReferenceDocumentTransferObject>> refdocDtos, HashMap<String, 
			List<PermissibleValueV2TransferObject>> pvDtos) {
		if (modules == null) {
			logger.debug("Module list is null. Unable to validate questions.");
			return;
		}
		
		for (ModuleDescriptor module : modules) {
			logger.debug("Start validating questions for module [" + module.getPublicId() + "|" + 
					module.getVersion() + "|" + module.getModuleSeqId()  + "]");
			
			//TODO: waiting on Denise to answer the question about "existing module" - 2014-01-27
			
			List<QuestionDescriptor> questions = module.getQuestions();		
			for (QuestionDescriptor question : questions) {
				
				logger.debug("Start validating question [" + question.getPublicId() + "|" + question.getVersion() + 
						"|" + question.getQuestionSeqId()  + "]");
				validateQuestion(question, form, questDtos, cdeDtos, refdocDtos, pvDtos);
			}
			
			logger.debug("Done validating questions for module [" + module.getPublicId() + "|" + module.getVersion() + 
					"|" + module.getModuleSeqId()  + "]");
		}
	}
	
	/**
	 * Collect public ids for questions and their cde from all modules of a form, so that
	 * we could query database with a list.
	 * @param modules
	 * @param questPublicIds
	 * @param questCdePublicIds
	 * @param formLoadType
	 */
//	protected void collectPublicIdsForModules(List<ModuleDescriptor> modules, 
//			List<String> questPublicIds, List<String> questCdePublicIds, String formLoadType) {
//		
//		if (modules == null) {
//			logger.debug("Module list is null. Unable to collect public ids.");
//			return;
//		}
//			
//		for (ModuleDescriptor module : modules) {
//			List<QuestionDescriptor> questions = module.getQuestions();
//			
//			for (QuestionDescriptor question : questions) {
//				String questPubId = question.getPublicId();
//				//Only need to validate question public id + version if it's an update form
//				if (formLoadType.equals(FormDescriptor.LOAD_TYPE_UPDATE_FORM)
//						&& questPubId != null && questPubId.length() > 0)
//					questPublicIds.add(questPubId);
//				
//				String cdePublicId = question.getCdePublicId();
//				if (cdePublicId != null && cdePublicId.length() > 0)
//					questCdePublicIds.add(cdePublicId); 
//				else
//					question.addMessage("Question has not associated data element public id. Unable to validate");				
//			}
//			
//			logger.debug("Collected " + questPublicIds.size() + " question public ids and " + questCdePublicIds.size() +
//					" cde public ids in module [" + module.getPublicId() + "|" + module.getVersion() + "]");
//		}
//		
//	}
	
	/**
	 * Parse questions in module for all the forms in xml
	 * 
	 * @param xmlPathName
	 * @param formHeaders
	 * @return
	 */
	protected List<FormDescriptor> getFormQuestionsFromXml(String xmlPathName, List<FormDescriptor> formHeaders) {
		StaXParser parser = new StaXParser();
		List<FormDescriptor> forms = parser.parseFormQuestions(xmlPathName, formHeaders);
		
		return forms;
	}
	
	protected void validateQuestion(QuestionDescriptor question, FormDescriptor form, 
			List<QuestionTransferObject> questDtos, List<DataElementTransferObject> cdeDtos,
			HashMap<String, List<ReferenceDocumentTransferObject>> refdocDtos, 
			HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos) {
		
		verifyQuestionPublicIdVersion(question, form, questDtos);
		
		verifyQuestionAgainstCde(question, form, cdeDtos, refdocDtos, pvDtos);
	}
	
	/**
	 * Concerns only questions in an "update" form.
	 * 1) use same public id + version if they have a match in db
	 * 2) If public id + version are present in xml but have no match in db, skip loading and add error message 
	 * 3. If public id + version are null, generate new ones at load time;
	 * 
	 * For new form or new version of a form, question's public id + version will be generated at load time
	 * by default
	 * 	
	 * @param question
	 * @param form 
	 */
	protected void verifyQuestionPublicIdVersion(QuestionDescriptor question, FormDescriptor form,
			List<QuestionTransferObject> questDtos) {
		
		String publicId = question.getPublicId();
		String version = question.getVersion();
		String loadType = form.getLoadType();
		
		//////TODO: Requirement slightly changed. Should check only if 1) update form and 2) existing module
		// Otherwise, question public id and version should be ignored
		
		//Denise: If Question in existing Module, and Question id version do not match Question in existing Module, 
		//add new Question.  If Question in New Module, ignore Question ID/Version
		
		if (loadType.equals(FormDescriptor.LOAD_TYPE_UPDATE_FORM)) {
			if (questDtos != null && publicId != null && publicId.length() > 0 && 
					version != null && version.length() > 0) {
				
				int match = 0;
				for (QuestionTransferObject qDto : questDtos) {
					if ((StringUtils.isNumeric(question.getCdePublicId())) && (qDto.getPublicId() == Integer.parseInt(publicId))) {
						match = 1;
						if (qDto.getVersion().floatValue() == Float.parseFloat(version)) {
							match = 2;
						}
					}
				}
				//check db
				
				if (match < 2) {
					question.setSkip(true);
					if (match == 0)
						form.addMessage("Question [" + publicId + "|" + version + "] will not be loaded because public id has no match in db");
					else if (match == 1)
						form.addMessage("Question [" + publicId + "|" + version + "] will not be loaded because version has no match in db");
					
				}
			} else {
				//question's public id and version will be generated at load time
				//question.setPublicId("");
				//question.setVersion("");
			}
		}
		
	}
	
	/**
	 * Check question's question text, valid values and default value against matching CDE.
	 * 
	 * @param question
	 * @param form
	 * @param cdeDtos
	 * @param refdocDtos
	 * @param pvDtos
	 */
	protected void verifyQuestionAgainstCde(QuestionDescriptor question, FormDescriptor form,
			List<DataElementTransferObject> cdeDtos,
			HashMap<String, List<ReferenceDocumentTransferObject>> refdocDtos, 
			HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos) {
		String msg;
		String cdePublicId = question.getCdePublicId();
		String cdeVersion = question.getCdeVersion();
		
		String errField = verifyPublicIdAndVersion(cdePublicId, cdeVersion); 
		if (errField.length() > 0) {
			msg = "Question in xml doesn't contain valid CDE " + errField + ". Unable to validate question in xml.";
			prepareQuestionForLoadWithoutValidation(form, question, msg);
			return;
		}
		
		DataElementTransferObject matchingCde = getMatchingDataElement(cdePublicId, cdeVersion, question, cdeDtos);
		if (matchingCde == null) {
			msg = "Unable to load a Data Element with [" + cdePublicId + "|" + cdeVersion + "] from database. Unable to validate question content.";
			prepareQuestionForLoadWithoutValidation(form, question, msg);
			return; 
		}
		
		if (!isAssociatedVdValid(question, matchingCde)) {
			msg = "Question is disassociated with CDE [" + matchingCde.getPublicId() + "|" + matchingCde.getVersion() + 
					" in xml because the valueDomain fields have no match in database.";
			prepareQuestionForLoadWithoutValidation(form, question, msg);
			return; 
		}
		
		//FORMBUILD-500: Add check if CDE'S value domain is non-enumerated but there are valid values for the question, dissociate CDE
		String vdseqid = matchingCde.getVdIdseq();
		if (vdseqid != null)
		{
			ValueDomainV2 vd = loadServiceRepositoryImpl.getValueDomainBySeqid(vdseqid);
			if (vd != null && "N".equalsIgnoreCase(vd.getVDType()) && 
				question.getValidValues() != null && question.getValidValues().size() > 0)
			{
				String message = "CDE " + matchingCde.getPublicId() + " is not enumerated but question has Valid Values in XML. Question will be dissociated from CDE.";
				logger.debug(message);
				question.addMessage(message);
				question.addInstruction(message);
				question.setCdeSeqId(""); //disassociate cde if it's there.
				return;
			}
		}
			
		List<PermissibleValueV2TransferObject> pValDtos = pvDtos.get(matchingCde.getVdIdseq());		//JR417 vm pub id is good
		//JR368 begin
		if(pValDtos != null) //validate only if it is enumerated VD
		{
			verifyQuestionDefaultValue(form, question, pValDtos, matchingCde);
			verifyQuestionValidValues(form, question, pValDtos, matchingCde);		
		}
		
		//FORMBUILD-529 Question Text has to be set similarly for both enumerated and non-enumerated CDEs
		List<ReferenceDocumentTransferObject> rdDtos = refdocDtos.get("" + matchingCde.getPublicId() + "-" + matchingCde.getVersion());
		verifyQuestionText(form, question, rdDtos, matchingCde);
		
		logger.debug("Done validating question against CDE");
	}
	
	
	protected boolean isAssociatedVdValid(QuestionDescriptor question, DataElementTransferObject matchingCde) {
		
		//TODO: New req. for validating valueDomain inside DataElement
		String vdseqid = matchingCde.getVdIdseq();
		if (vdseqid == null || vdseqid.length() == 0) {
			if (question.hasVDValueInDE())
				question.setCdeSeqId(""); 
				return false;
		}
			
		//1. get vd from db with vdseqid
		ValueDomainV2 vd = loadServiceRepositoryImpl.getValueDomainBySeqid(vdseqid);
		if (vd == null) {
			if (question.hasVDValueInDE())
				question.setCdeSeqId(""); 
				return false;
		}
		
		//2. compare what's from xml and what's from db
		String vdPublicId = question.getCdeVdPublicId();
		String vdVersion = question.getCdeVdVersion();
		if (vdPublicId != null && vdPublicId.length() > 0 || vdVersion != null && vdVersion.length() > 0) {
		
			if (!FormLoaderHelper.samePublicIdVersions(question.getCdeVdPublicId(), question.getCdeVdVersion(), 
					vd.getPublicId(), vd.getVersion())) {
				if (question.hasVDValueInDE())
					question.setCdeSeqId(""); 
					return false;
			}
		}
		
		if (!question.allPresentedVdFieldsMatched(vd.getDatatype(), vd.getDecimalPlace(), vd.getDisplayFormat(),
				vd.getHighValue(), vd.getLowValue(), vd.getMaxLength(), vd.getMinLength(), vd.getUnitOfMeasure())) {
			question.setCdeSeqId("");
			return false;
		}
				
		return true;
	}
	
	/**
	 * In the case where no valid cde can be used to validate question data against, make sure question's requirement fields are there
	 * for load.
	 * 
	 * @param form
	 * @param question
	 * @param message
	 */
	protected void prepareQuestionForLoadWithoutValidation(FormDescriptor form, QuestionDescriptor question, String message) {
		logger.debug(message);
		question.addMessage(message);
		question.addInstruction(message);
		question.setCdeSeqId(""); //disassociate cde if it's there.
		form.setDefaultWorkflowName();
		checkQuestionValidValueFieds(question);
	}
	
	protected String verifyPublicIdAndVersion(String publicid, String version) {
		String errField = "";
		if (publicid == null || publicid.length() == 0 ) {
			errField = "public id";
		} else {
			try {
				int num = Integer.parseInt(publicid);
			} catch (NumberFormatException ne) {
				errField = "public id";
			}
		}

		if (version == null || version.length() == 0) {
			errField += (errField.length() > 0) ? " and version" : "version";
		} else {
			try {
				float ver = Float.parseFloat(version); 
			} catch (NumberFormatException ne) {
				errField += (errField.length() > 0) ? " and version" : "version";
			}
		}

		return errField;
	}
	
	/**
	 * 
	 * @param cdePublicId
	 * @param cdeVersion
	 * @param question
	 * @param cdeDtos
	 * @return
	 */
	protected DataElementTransferObject getMatchingDataElement(String cdePublicId, String cdeVersion, 
		QuestionDescriptor question, List<DataElementTransferObject> cdeDtos) {
		
		if (cdeDtos == null || cdeDtos.size() == 0) {
			question.addInstructionAndMessage(
					"Question in xml contains CDE public id but it has no match in database. Unable to validate question.");
			return null;
		} 
		
		boolean pidMatched = false;
		try {
			int cdePubIdNum = Integer.parseInt(cdePublicId);
			float cdeVerNum = Float.parseFloat(cdeVersion);
			
			for (DataElementTransferObject cde : cdeDtos) {
				if (cde.getPublicId() == cdePubIdNum)
					pidMatched = true;
				else
					continue;

				if (cdeVerNum == cde.getVersion().floatValue()) {
					
					//TODO: validate valueDomain inside DataElement
					//cde.getValueDomain().g
					
					question.setCdeSeqId(cde.getDeIdseq());
					
					return cde;
				}
			}
		} catch (NumberFormatException ne) {
			question.addInstructionAndMessage(
					"Question in xml contains malformatted CDE public id or version. Unable to validate question.");
			return null;
		}

		if (pidMatched)
			question.addInstructionAndMessage(
				"Question in xml contains CDE public id and verion but the version has no match in database. Unable to validate question.");
		else
			question.addInstructionAndMessage(
				"Question in xml contains CDE public id and verion but the public id has no match in database. Unable to validate question.");
		
		return null;
			
		
	}

	protected List<ReferenceDocumentTransferObject> getReferenceDocsForCdeFromDB(String cdePublicId, String cdeVersion,
			QuestionDescriptor question) {
		
		List<ReferenceDocumentTransferObject> refDocs = loadServiceRepositoryImpl.getReferenceDocsForQuestionCde(cdePublicId, cdeVersion);
		 
		if (refDocs == null || refDocs.size() == 0) {
			logger.debug("Unable to load any reference document from db with CDE public id and version [" + cdePublicId +
					"|" + cdeVersion + "] in xml");
		}
		
		return refDocs;
		
	}
	
	/**
	 * Verify the question's default value against CDE's permissible values / value
	 * 
	 * If the default value doesn't match any of the CDE's permissible values , mark the question for loading, 
	 * set its default value to null and add a warning message in the question's instruction field 
	 * ("Default value <value> is invalid").
	 * 
	 * @param question
	 * @param pValues
	 */
	protected void verifyQuestionDefaultValue(FormDescriptor form, QuestionDescriptor question, List<PermissibleValueV2TransferObject> pValues,
			DataElementTransferObject matchingCde) {
		String msg;
		if (pValues == null || pValues.size() == 0) {
			msg = "The value domain associated with data Element [" + matchingCde.getPublicId() + " " + matchingCde.getVersion() + " " +
					matchingCde.getLongName() + 
					"] does not have permissible values. Unable to verify question's default value";
			prepareQuestionForLoadWithoutValidation(form,  question, msg);
			return;
		} 	 
		
		String defaultValue = question.getDefaultValue();
		boolean validated = false;
		if (defaultValue != null && defaultValue.length() > 0) {
			logger.debug("Verifying quesiton's default value [" + defaultValue + "]");
			for (PermissibleValueV2TransferObject pVal : pValues) {
				if (defaultValue.equalsIgnoreCase(pVal.getValue()))
					validated = true;
			}

			if (!validated) {
				msg = "Question's default value [" + defaultValue + "] doesn't match any of the associated CDE's permissible values";
				question.addInstruction(msg);
				question.addMessage(msg);
				question.setDefaultValue("");
				question.setCdeSeqId("");
				form.setDefaultWorkflowName();
			}
		} 
	}
	
	/**
	 * Verify each of the question's valid value and its value meaning against CDE's permissible values / value
	 * a. If one or more valid values in xml don't match the CDE's permissible values, list the non-matching 
	 * values in the question's instruction field.
	 * b. Create valid value and its value meaning for the question that matches CDE permissible values.
	 * 
	 * 
	 * Update 2014-01-14:
	 * 
	 * To validate a question's valid value's value field -
	 * see if it matches the pv's value
	 * If not match found, skip loading the valid value
	 * 
	 * To validate a question's valid value's meaningText field -
	 * 
	 * 1. See if it matches the pv's value meaning's longName, if not ->
	 * 2. see if it matches the pv's value meaning's designation's name, if not -
	 * 3. see if it machtes the pv's value meaning's definition's text
	 * If not match found, skip loading the valid value
	 * 
	 * To validate a question's valid value's description field -
	 * 
	 * 4. see if it matches the pv's value meaning's preferred definition, if not ->
	 * 5. see if it matches the pv's value meaning's definition's text
	 * 
	 * If the question's valid value's description is empty or doesn't have a match from #4 or #5, assign to it
	 * the value from the pv's value meaning's preferred definition (#4) 
	 * 
	 * (confirmed by Denise 1/14/2014)
	 * 
	 * @param question
	 * @param pValues
	 * @param matchingCde
	 */
	protected void verifyQuestionValidValues(FormDescriptor form, QuestionDescriptor question, List<PermissibleValueV2TransferObject> pValues,
			DataElementTransferObject matchingCde) {
		
		List<QuestionDescriptor.ValidValue> validValues = question.getValidValues();
		if (validValues == null || validValues.size() == 0)
			 return; 
		 
		if (pValues == null || pValues.size() == 0) {
			
			String msg = "Question not associated with data element [" + matchingCde.getPublicId() + " " + matchingCde.getVersion() +
					"] because the value domain associated with data Element [" + matchingCde.getLongName() +
					" is non-enumerated. Unable to verify question's valid values";
			prepareQuestionForLoadWithoutValidation(form, question, msg);	
			return;
		} 	
		
		
		for (QuestionDescriptor.ValidValue vVal : validValues) {
			validateQuestionValidValue(form, question, vVal, pValues, matchingCde);
		}
		 
	}
	
	/**
	 * Make sure valid values have valid required field values. Otherwise, db will 
	 * complain
	 * 
	 * This is especially important for questions that don't have an associated CDE in xml
	 * @param question
	 */
	protected void checkQuestionValidValueFieds(QuestionDescriptor question) {
		if (question == null) return;
		
		List<QuestionDescriptor.ValidValue> vvs = question.getValidValues();
		if (vvs == null || vvs.size() == 0) return;
		
		for (QuestionDescriptor.ValidValue vv : vvs) {
			if (vv.getValue() == null || vv.getValue().length() == 0) {
				vv.setSkip(true);
				continue;
			}
			
			if (vv.getMeaningText() == null || vv.getMeaningText().length() == 0) {
				vv.setSkip(true);
				continue;
			}
				
			if (vv.getDescription() == null || vv.getDescription().length() == 0)
				vv.setDescription(vv.getMeaningText());
		}
	}
	
	protected void validateQuestionValidValue(FormDescriptor form, QuestionDescriptor question, QuestionDescriptor.ValidValue vVal,
			List<PermissibleValueV2TransferObject> pValues, DataElementTransferObject matchingCde) 
	{
		String msg;	
		String val = vVal.getValue();
		String valMeaning = FormLoaderHelper.normalizeSpace(vVal.getMeaningText());
		String valDesc = FormLoaderHelper.normalizeSpace(vVal.getDescription());

		PermissibleValueV2TransferObject matchedPv = null;
		for (PermissibleValueV2TransferObject pVal : pValues) {		//JR417 you have everything you need in pVal (vd_iqseq in the pv itself and vm_idseq in the pv.vm) 
			String pValStr = pVal.getValue().trim();
			if (val.equals(pValStr)) { //question vv's value field
				matchedPv = pVal;
				break;
			}
		}
		
		if (matchedPv == null)
		{
			msg = "Valid value [" + val + "] doesn't match any of permissible values of the associated CDE [" + 
					matchingCde.getPublicId() + "|" + matchingCde.getVersion() + "]. Unable to validate Valid Value/Value Meaning.";
			//vVal.setSkip(true);
			//question.addInstruction(msg);
			//question.addMessage(msg);
			prepareQuestionForLoadWithoutValidation(form, question, msg);
		}
		//FORMBUILD- 424, 425 : If matched PV found, further validate the Valid Values' Value Meaning Text and Description.
		else
		{
			ValueMeaningV2TransferObject valMeaningDto = (ValueMeaningV2TransferObject) matchedPv.getValueMeaningV2();	//JR417 vm pub id is good
			String valMeaningLongName = FormLoaderHelper.normalizeSpace(valMeaningDto.getLongName());

			if (!valMeaning.equalsIgnoreCase(valMeaningLongName)) { //question vv's meantingText field
				//2. see if it matches the pv's value meaning's designation's name, if not -
				//3. see if it matches the pv's value meaning's definition's text
				//  * If no match found, skip loading the valid value
				//Fix for FORMBUILD-501
				if (!ableToValidateByAlternatives(valMeaning, valMeaningDto.getIdseq())) {
					msg = "Valid value meaning text [" + valMeaning + "] doesn't match any of the associated CDE's permissible value meaning. " +
							"However, the Valid Value/Value Meaning will be loaded but Question will be dissociated from CDE.";
					//				vVal.setSkip(true);
					//				question.addInstruction(msg);
					//				question.addMessage(msg);
					prepareQuestionForLoadWithoutValidation(form, question, msg);
				}	
			}

			vVal.setPreferredName(String.valueOf(valMeaningDto.getPublicId()) + "v" + valMeaningDto.getVersion());
			vVal.setVdPermissibleValueSeqid(matchedPv.getIdseq());
			
			String valMeaningPreferredDefinition = FormLoaderHelper.normalizeSpace(valMeaningDto.getPreferredDefinition());
			if (valDesc == null || valDesc.length() == 0) {
				vVal.setDescription(valMeaningPreferredDefinition);
			}	
			else
			{
				if (!valDesc.equals(valMeaningPreferredDefinition))
					//see if it matches the pv's value meaning's definition's text
					if (!matchMeaningDefinitions(valDesc, valMeaningDto.getIdseq()))
						vVal.setDescription(valMeaningPreferredDefinition);
			}
		}
	}
	
	protected boolean ableToValidateByAlternatives(String valMeaningLongName, String vmSeqid) {

		List<String> desNames = this.loadServiceRepositoryImpl.getDesignationNamesByVmIds(vmSeqid);
		if (desNames != null && desNames.size() > 0) {
			for (String desName : desNames) {
				desName = FormLoaderHelper.normalizeSpace(desName);
				if (valMeaningLongName.equalsIgnoreCase(desName)) 
					return true;
			}
		}
		return false;
	}
	
	protected boolean matchMeaningDefinitions(String valDesc, String vmSeqid) {
		List<String> defTexts = this.loadServiceRepositoryImpl.getDefinitionTextsByVmIds(vmSeqid);
		if (defTexts != null && defTexts.size() > 0) {
			for (String defTest : defTexts) {
				defTest = FormLoaderHelper.normalizeSpace(defTest);
				if (valDesc.equalsIgnoreCase(defTest)) 
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Verify the question's question text against CDE reference document.
	 * a. If the question's question text in the xml is not null but does not match 
	 * the doctext field value of preferred or alternate type of the CDE's reference document, 
	 * mark it for loading and set as default text "Question text invalid".
	 * b. If the question's question text in xml is null while the CDE is valid, use CDE 
	 * reference document preferred text as its question text.
	 * c. If a question's question text in xml is null and the CDE is valid but doesn't have 
	 * a reference document preferred text, set "Data Element <long name> does not have Preferred Question Text" 
	 * as default question text for the question.
	 * 
	 * @param question
	 * @param refDocs
	 * @param matchingCde
	 */
	protected void verifyQuestionText(FormDescriptor form, QuestionDescriptor question, List<ReferenceDocumentTransferObject> refDocs, 
			DataElementTransferObject matchingCde) {
		
		if (refDocs == null || refDocs.size() == 0) {
			question.addInstruction("Unable to load any reference document with CDE public id and version [" + 
				matchingCde.getPublicId() + "|" + matchingCde.getVersion() + "] in xml. Unable to verify question text.");
			form.setDefaultWorkflowName();
			//FORMBUILD-428 if CDE does not have any reference docs, retain the CDE association with question.
			//Do not NULL it.
			//question.setCdeSeqId(""); //disassociate cde from question
			//return;
		}
		
		String questionText = question.getQuestionText();
		String cdePublicId = question.getCdePublicId();
		String cdeVersion = question.getCdeVersion();
		
		//refdoc list is ordered by type, preferred first
		if (questionText == null || questionText.length() == 0) 
		{	
			if (refDocs != null)
			{
				String pqt = null;
				String aqt = null;
				for (ReferenceDocument refdoc : refDocs)
				{				
					if ("Preferred Question Text".equalsIgnoreCase(refdoc.getDocType()))
						pqt = refdoc.getDocText();
					if ("Alternate Question Text".equalsIgnoreCase(refdoc.getDocType()))
						aqt = refdoc.getDocText();
				}
				questionText = (pqt != null && pqt.length() > 0) ? pqt : aqt;
				if (questionText == null || questionText.length() == 0) {
					questionText = "Data Element " + matchingCde.getLongName() + " does not have Preferred Question Text";
				}
			}
		}
		else 
		{
			boolean matched = false;
			if (refDocs != null)
			{
				for (ReferenceDocument refdoc : refDocs)
				{
					if ("Preferred Question Text".equalsIgnoreCase(refdoc.getDocType()) ||
						"Alternate Question Text".equalsIgnoreCase(refdoc.getDocType()))
					{
						if (questionText.equalsIgnoreCase(refdoc.getDocText())) {
							matched = true;
							break;
						}
					}
				}
			}
			if (!matched) {
				//FORMBUILD-528
				String message = "Question is dissociated from CDE [" + cdePublicId + "v" + cdeVersion + "] because question text does not match with CDE's PQT or AQT.";
				question.addMessage(message);
				question.addInstruction(message);
				question.setCdeSeqId("");
			}
		}
		question.setQuestionText(questionText);
	}
}
