package gov.nih.nci.cadsr.formloader.repository.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.nci.cadsr.formloader.domain.DomainObjectTranslator;
import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.domain.ModuleDescriptor;
import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderHelper;
import gov.nih.nci.cadsr.formloader.service.common.QuestionHelper;
import gov.nih.nci.cadsr.formloader.service.common.QuestionsPVLoader;
import gov.nih.nci.ncicb.cadsr.common.dto.AdminComponentTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ClassificationTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ContactCommunicationV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.DataElementTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.DefinitionTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.DesignationTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.FormV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.FormValidValueTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.InstructionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ModuleTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.PermissibleValueV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.RefdocTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.ReferenceDocumentTransferObject;
import gov.nih.nci.ncicb.cadsr.common.exception.DMLException;
import gov.nih.nci.ncicb.cadsr.common.resource.Instruction;
import gov.nih.nci.ncicb.cadsr.common.util.StringUtils;
import gov.nih.nci.ncicb.cadsr.common.util.ValueHolder;
@ConditionalOnBean(name = "dataSource")

public class LoadServiceRepositoryImpl extends FormLoaderRepositoryImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(LoadServiceRepositoryImpl.class.getName());
	
//	FormLoaderRepositoryImpl repository;
//	
//	public LoadServiceRepositoryImpl() {}
//	
//	public LoadServiceRepositoryImpl(FormLoaderRepositoryImpl repository) {
//		this.repository = repository;
//	}
//
//	public FormLoaderRepositoryImpl getRepository() {
//		return repository;
//	}
//
//	public void setRepository(FormLoaderRepositoryImpl repository) {
//		this.repository = repository;
//	}
	private DataSource dataSource;
	public LoadServiceRepositoryImpl(DataSource dataSource2) {
		super(dataSource2);
	}

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	/**
	 * Create new form
	 */
	@Transactional
	public String createForm(FormDescriptor form, String xmlPathName) {
		
		String formSeqid = "";
		
		try {
			if (form.getContextSeqid()!=null) {
				FormV2TransferObject formdto = DomainObjectTranslator.translateIntoFormDTO(form);
				if (formdto == null) 
					return null;
	
				formSeqid = formV2Dao.createFormComponent(formdto);		
				logger.debug("Created form. Seqid: " + formSeqid);
				
				retrievePublicIdForForm(formSeqid, form);
	
				formdto.setFormIdseq(formSeqid);
				form.setFormSeqId(formSeqid);
	
				createFormInstructions(form, formdto);
	
				processFormdetails(form, xmlPathName, form.getIndex());
	
				createModulesInForm(form, formdto);
			}
			else {
				logger.error("Form received with empty Context IDSEQ: " + form);
			}
		} catch (DMLException dbe) {
			logger.error(dbe.getMessage());
			form.addMessage(dbe.getMessage());
			form.setLoadStatus(FormDescriptor.STATUS_LOAD_FAILED);
		}
		
		return formSeqid;
	}
	
	/**
	 * Create new form or new version of an existing form
	 * <br><br>
	 * This will first call the store procedure Sbrext_Form_Builder_Pkg.CRF_VERSION which will make a full copy
	 * of the form with the same public id and an upgraded version number. Then it'll apply an update on the 
	 * copy with content from the xml. This is very inefficient. 
	 * <br><br>
	 * Need to explore better way. Key quetions: how public id
	 * is generated? Is it possible to insert new form row withough triggering a new public id generation.
	 */
	@Transactional
	public String createFormNewVersion(FormDescriptor form, String loggedinUser, String xmlPathName) {
		
		String formSeqid = "";
		
		try {
			FormV2TransferObject formdto = DomainObjectTranslator.translateIntoFormDTO(form);	
			if (formdto == null) 
				return null;

			formSeqid = formV2Dao.createNewFormVersionShellOnly(formdto.getFormIdseq(), formdto.getVersion(), 
					"New version form by Form Loader", formdto.getCreatedBy());		
			
			logger.debug("Created new version for form. Seqid: " + formSeqid);
			
			formdto.setFormIdseq(formSeqid);
			form.setFormSeqId(formSeqid);

			int res = formV2Dao.updateFormComponent(formdto);
			if (res != 1) {
				logger.error("Error!! Failed to update form");
				return null;
			}
			
			createFormInstructions(form, formdto);
			processFormdetails(form, xmlPathName, form.getIndex());
			createModulesInForm(form, formdto);
			
			return form.getFormSeqId();
			
		} catch (DMLException dbe) {
			logger.error(dbe.getMessage());
			form.addMessage(dbe.getMessage());
			formSeqid = null;
			form.setVersion("0.0");
			form.setLoadStatus(FormDescriptor.STATUS_LOAD_FAILED);
		}
		
		return formSeqid;
	}

	
	@Transactional
	public void unloadForm(FormDescriptor form) {
		
		if (form == null) {
			logger.warn("Form object is null. Unable to proceed with unload");
			return;
		}
			 
		String seqid = form.getFormSeqId();
		if (seqid == null || seqid.length() == 0) {
			logger.debug("Form's seqid is invalid");
			form.addMessage("Form's seqid is invalid. Unable to unload form");
			form.setLoadStatus(FormDescriptor.STATUS_UNLOAD_FAILED);
			return;
		}
		
		if (DomainObjectTranslator.WORKFLOW_STATUS_UNLOADED.endsWith(form.getWorkflowStatusName())) {
			form.addMessage("Form [" + form.getFormIdString() + "] had been unloaded previously.");
			form.setLoadStatus(FormDescriptor.STATUS_UNLOAD_FAILED);
			return;
		}
		
		logger.debug("Start unloading form: " + seqid + "|" + form.getFormSeqId());
		int res = formV2Dao.updateWorkflowStatus(seqid, DomainObjectTranslator.WORKFLOW_STATUS_UNLOADED, 
				DomainObjectTranslator.FORM_LOADER_DB_USER, form.getChangeNote());
		if (res <= 0) {
			form.addMessage("Failed to update form's workflow status in database. Reason unknown");
			form.setLoadStatus(FormDescriptor.STATUS_UNLOAD_FAILED);
		} else {
			form.setLoadStatus(FormDescriptor.STATUS_UNLOADED);
			manageFormVersionPostUnload(form);
		}
		
		FormV2TransferObject formdto = formV2Dao.getFormHeadersBySeqid(seqid);
		form.setModifiedBy(formdto.getModifiedBy());
		form.setModifiedDate(formdto.getDateModified());
		form.setWorkflowStatusName(formdto.getAslName());
		form.setLoadStatus(FormDescriptor.STATUS_UNLOADED);
		
		logger.debug("Done unloading form: " + seqid + "|" + form.getFormSeqId());
	}
	
	
	
	@Transactional
	protected void createFormInstructions(FormDescriptor form, FormV2TransferObject formdto) {
		logger.debug("Creating instructions for form");
		String instructString = form.getHeaderInstruction();
		if (instructString == null || instructString.length() == 0)
			return;
		
		InstructionTransferObject formInstruction = createInstructionDto(formdto, instructString);
		if (formInstruction != null)
			formInstructionV2Dao.createInstruction(formInstruction, formdto.getFormIdseq());
		
		instructString = form.getFooterInstruction();
		formInstruction = createInstructionDto(formdto, instructString);
		if (formInstruction != null)
			formInstructionV2Dao.createFooterInstruction(formInstruction, formdto.getFormIdseq());
		
		logger.debug("Done creating instructions for form");
	}
	
	/**
	 * Create the instruction dto for the parent admin component dto.
	 * 
	 * @param compdto parent admin component that the instruction belongs to
	 * @param instructionString instruction string
	 * 
	 * @return InstructionTransferObject
	 */
	protected InstructionTransferObject createInstructionDto(AdminComponentTransferObject compdto, String instructionString) {
			
		InstructionTransferObject instruction = null;
		if (instructionString != null && instructionString.length() > 0) {
			instruction = new InstructionTransferObject();
			instruction.setLongName(compdto.getLongName());
			instruction.setPreferredDefinition(instructionString);
			instruction.setContext(compdto.getContext());
			instruction.setAslName("DRAFT NEW");
			instruction.setVersion(new Float(1.0));
			instruction.setCreatedBy(compdto.getCreatedBy());
			instruction.setDisplayOrder(1);
		}
		
		return instruction;
	}
	
	/**
	 * Process designations, refdocs, definitions and contact communications for a form
	 * @param form
	 * @param xmlPathName
	 * @param currFormIdx
	 */
	@Transactional
	protected void processFormdetails(FormDescriptor form, String xmlPathName, int currFormIdx) {
		logger.debug("Processing protocols, designations, refdocs and definitions for form");
		
		List<ProtocolTransferObjectExt> protos = form.getProtocols();
		processProtocols(form, protos);
		
		List<DesignationTransferObjectExt> designations = form.getDesignations();
		processDesignations(form, designations);
		
		List<RefdocTransferObjectExt> refdocs = form.getRefdocs();
		processRefdocs(form, refdocs);
		
		List<DefinitionTransferObjectExt> definitions = form.getDefinitions();
		processDefinitions(form, definitions);
		
		processContactCommnunications(form);
		
		processClassifications(form);
		
		logger.debug("Done processing protocols, designations, refdocs and definitions for form");
	}
	
	
	
	/**
	 * 
	 * @param form
	 * @param refdocs
	 */
	@Transactional
	protected void processContactCommnunications(FormDescriptor form) {
		List<ContactCommunicationV2TransferObject> contacts = form.getContactCommnunications();
		
		if (contacts == null || contacts.size() == 0) {
			logger.debug("Form " + form.getPublicId() + " has no contact. Do nothing");
			return;
		}
		
		String formSeqid = form.getFormSeqId();
		for (ContactCommunicationV2TransferObject contact : contacts) {
			String org_idseq = this.contactCommV2Dao.getOrganizationIdseqByName(contact.getOrganizationName());
			contact.setCreatedBy(form.getCreatedBy());
			this.contactCommV2Dao.createContactCommnunicationForComponent(formSeqid, org_idseq, contact);
		}
	}
	
	/**
	 * 
	 * @param form
	 * @param refdocs
	 */
	@Transactional
	protected void processRefdocs(FormDescriptor form, List<RefdocTransferObjectExt> refdocs) {
		if (refdocs == null) {
			logger.error("Null refdocs list passed in to processRefdocs(). Do nothing");
			return;
		}
		
		String formSeqid = form.getFormSeqId();
		String contextSeqId = this.getContextSeqIdByName(form.getContext());
		List existings = null;
		
		if (!form.getLoadType().equals(FormDescriptor.LOAD_TYPE_NEW)) {
			//2nd arg is not used in the actual query.
			existings = this.formV2Dao.getAllReferenceDocuments(form.getFormSeqId(), null);
		}
		
		//create ref docs
		int idx = 0;
		for (RefdocTransferObjectExt refdoc : refdocs) {
			String contextName = refdoc.getContextName();
			String refdocContextSeqid = this.getContextSeqIdByName(contextName);
			if (refdocContextSeqid == null || refdocContextSeqid.length() == 0) {
				refdocContextSeqid = contextSeqId;
				contextName = form.getContext();
			}

			ContextTransferObject con = new ContextTransferObject(contextName);
			con.setConteIdseq(refdocContextSeqid);
			refdoc.setContext(con);
			refdoc.setDisplayOrder(idx++);
			if (!this.refdocTypeExists(refdoc.getDocType())) {
				form.addMessage("Refdoc type [" + refdoc.getDocType() + "] is invalid. Use default type [REFERENCE]");
				refdoc.setDocType(DEFAULT_REFDOC_TYPE);
			}
			if (existings != null && isExistingRefdoc(refdoc, existings)) {
				referenceDocV2Dao.updateReferenceDocument(refdoc);
			} else {
				this.referenceDocV2Dao.createReferenceDoc(refdoc, formSeqid);
			}
		}
		
		removeExtraRefdocsIfAny(existings);
		
	}
	
	/**
	 * Check a list of ref doc object that are previously marked and delete from database those are not marked as "KEEP"
	 * 
	 * @param refdocs
	 */
	@Transactional
	protected void removeExtraRefdocsIfAny(List<ReferenceDocumentTransferObject> refdocs) {
		if (refdocs == null) {
			return;
		}
		
		for (ReferenceDocumentTransferObject refdoc : refdocs) {
			if (refdoc.getDisplayOrder() == MARK_TO_KEEP_IN_UPDATE)
				continue;
			
			logger.debug("Deleting refdoc [" + refdoc.getDocName() + "|" + refdoc.getDocType());
			referenceDocV2Dao.deleteReferenceDocument(refdoc.getDocIdSeq());
		}
	}

	/**
	 * For new version and update form, if designation doesn't already exist, designate it to the context. For new form,
	 * designate the form to the context.
	 * @param form
	 * @param designations
	 */
	@Transactional
	protected void processDesignations(FormDescriptor form, List<DesignationTransferObjectExt> designations) {
		
		if (designations == null) {
			logger.debug("Null designation list passed in to processDesignations(). Do nothing");
			return;
		}
		
		for (DesignationTransferObjectExt desig : designations) {
			if (form.getLoadType().equals(FormDescriptor.LOAD_TYPE_NEW) ||
					form.getLoadType().equals(FormDescriptor.LOAD_TYPE_NEW_VERSION)) {
				createFormDesignation(form, form.getCreatedBy(), desig);
			} else {
				
				//TODO: this block is for update form only
				
//				
//				//Check context name in designation elem from xml. If that's not valid, use form's
//				String desigContext = desig.getContextName();
//				String desgContextSeqid = this.getContextSeqIdByName(desigContext);
//				if (desgContextSeqid == null || desgContextSeqid.length() == 0)
//					desgContextSeqid = contextSeqId;
//				
//				//validate the type, use default if neccessary
//				String desigType = desig.getType();
//				if (!this.designationTypeExists(desigType)) {
//					form.addMessage("Designation type [" + desigType + "] is invalid. Use default type [Form Loader]");
//					desig.setType(DEFAULT_DESIGNATION_TYPE);
//				}
//				
//				List<DesignationTransferObject> existing = formV2Dao.getDesignationsForForm(
//						formSeqid, desig.getName(), desig.getType(), desig.getLanguage());
//				if (existing == null || existing.size() == 0) {
//					designateForm(formSeqid, contextSeqId, form.getCreatedBy(), desig);
//				}
			} 
		}
	}
	
	@Transactional
	protected void processDefinitions(FormDescriptor form, List<DefinitionTransferObjectExt> definitions) {
		if (definitions == null) {
			logger.debug("Null definiion list passed in to processDefinitions(). Do nothing");
			return;
		}
		
		String formSeqid = form.getFormSeqId();
		//String contextSeqId = this.getContextSeqIdByName(d)
		//		this.getContextSeqIdByName(form.getContext());
		
		for (DefinitionTransferObjectExt desig : definitions) {
			if (form.getLoadType().equals(FormDescriptor.LOAD_TYPE_NEW) ||
					form.getLoadType().equals(FormDescriptor.LOAD_TYPE_NEW_VERSION)) {
				createFormDefinition(form, form.getCreatedBy(), desig);
			} 
		}
	}
	
	@Transactional
	protected void processClassifications(FormDescriptor form) {
		List<ClassificationTransferObject> classifications = form.getClassifications();
		
		if (classifications == null || classifications.size() == 0) {
			logger.debug("Form " + form.getPublicId() + " has no classifications to be loaded.");
		}
		else
		{
			String formSeqid = form.getFormSeqId();
			List<String> csCsiIdSeqList = new ArrayList<String>();
			for (ClassificationTransferObject classification : classifications)
			{
				if (classification.getCsCsiIdSeq() != null && classification.getCsCsiIdSeq().length() > 0)
				{
					if (!csCsiIdSeqList.contains(classification.getCsCsiIdSeq()))
					{
						csCsiIdSeqList.add(classification.getCsCsiIdSeq());
						this.formV2Dao.assignClassification(formSeqid, classification.getCsCsiIdSeq());
					}
					else
						logger.info("Classification Scheme Item: " + classification.getCsiPublicID() + " is dupilcaed. Will be added only once.");
				}
			}
		}
	}

	/**
	 * Deprecated.
	 * @param formSeqid
	 * @param contextSeqid
	 * @param createdby
	 * @param desig
	 * 
	 * @deprecated
	 * @return
	 */
	protected int designateForm(String formSeqid, String contextSeqid, String createdby,
			DesignationTransferObjectExt desig) {
		List<String> ac_seqids = new ArrayList<String>();
		ac_seqids.add(formSeqid);
		return formV2Dao.designate(contextSeqid, ac_seqids, createdby);
	}
	
	@Transactional
	protected int createFormDesignation(FormDescriptor form, String createdBy,
			DesignationTransferObjectExt desig) {
		
		if (desig == null)
			return 0;
		
		String formSeqid = form.getFormSeqId();
		String contextSeqId = this.getContextSeqIdByName(desig.getContextName());
		if (contextSeqId == null || contextSeqId.length() == 0)
			contextSeqId = this.getContextSeqIdByName(form.getContext());
		return this.designationDao.createDesignationForComponent(formSeqid, contextSeqId, createdBy, desig);
	}
	
	@Transactional
	protected int createFormDefinition(FormDescriptor form, String createdBy,
			DefinitionTransferObjectExt def) {
		
		if (def == null)
			return 0;
		
		String contextSeqid = this.getContextSeqIdByName(def.getContextName());
		if (contextSeqid == null || contextSeqid.length() == 0)
			contextSeqid = this.getContextSeqIdByName(form.getContext());
		return this.definitionDao.createDefinitionForComponent(form.getFormSeqId(), contextSeqid, createdBy, def);
	}
	
	/**
	 * First, check if a protoId is valid (has a match in db)
	 * 
	 * Second, for those that are valid, do the following:
	 * 
	 * 1. New form and new version form: add all listed protocols from xml to form.
	 * 2. update form: replace what's in db for the form with the set from xml.
	 * 
	 * @param form
	 * @param protoIds
	 */
	@Transactional
	protected void processProtocols(FormDescriptor form, List<ProtocolTransferObjectExt> protos) {
		
		if (protos == null || protos.size() == 0)
			return; 
		
		String formSeqid = form.getFormSeqId();
		
		for (ProtocolTransferObjectExt proto :protos) {
			if (FormDescriptor.LOAD_TYPE_NEW.equals(form.getLoadType()) || FormDescriptor.LOAD_TYPE_NEW_VERSION.equals(form.getLoadType())) {
				if (proto.getIdseq() != null)
					formV2Dao.addFormProtocol(formSeqid, proto.getIdseq(), form.getCreatedBy());
			} else  {
				
				//Update form will be handle for v4.2
				
				//TODO: requirement changed. Need work here 1/17/2014
				//TODO: requirement changed. Need work here 1/17/2014
//				if (!formV2Dao.formProtocolExists(formSeqid, protoSeqid))
//					formV2Dao.addFormProtocol(formSeqid, protoSeqid, form.getModifiedBy());
				//TODO: requirement changed. Need work here 1/17/2014
				//TODO: Need to remove protocols that exist in db but not in xml
			} 
		}
		
		
	}
	
	protected List<String> markNoMatchProtoIds(FormDescriptor form, List<String> protoIds, HashMap<String, String> protoIdMap) {
		List<String> protoSeqIds = new ArrayList<String>();
		String nomatchids = "";
		
		int cnt = 0;
		for (String protoId : protoIds) {
			String seqid = protoIdMap.get(protoId);
			if (seqid == null) {
				nomatchids = (nomatchids.length() > 0) ? "," + protoId : protoId;
				cnt++;
			}
			else
				protoSeqIds.add(seqid);
			
		}
		
		if (cnt == 1)
			form.addMessage("Protocol " + nomatchids + " has not registered in caDSR. Not loaded");
		else if (cnt > 1)
			form.addMessage("Protocols " + nomatchids + " have not registered in caDSR. Not loaded");
		return protoSeqIds;
	}
	
	@Transactional
	protected void createModulesInForm(FormDescriptor form, FormV2TransferObject formdto) {
		logger.debug("Start creating modules for form");
		List<ModuleDescriptor> modules = form.getModules();
		
		//modules = FormLoaderHelper.handleModuleRepeat(modules);  //JR366 not needed
		
		/*
		 * Denise:
		 * For a module and its questions in a form
			1) If it's a new form or new version, use the form's createdBy
			2) If it's an update form, check what's with module element in xml. 
				a. If the module's createBy is valid, use it and apply that to all questions.
				b. If the module's createdBy is not valid, use form's createdBy and apply to all questions.
		 */
		int displayOrder = 0;
		for (ModuleDescriptor module : modules) {
			ModuleTransferObject moduledto = DomainObjectTranslator.translateIntoModuleDTO(module, form, formdto);
			moduledto.setDisplayOrder(displayOrder++);
			
			String moduleSeqid = moduleV2Dao.createModuleComponent(moduledto);
			logger.debug("Created a module with seqid: " + moduleSeqid);
			
			module.setModuleSeqId(moduleSeqid);
			moduledto.setIdseq(moduleSeqid);
			moduledto.setContext(formdto.getContext());
			
			//do we need to go back to db to get module's public id?
		
			logger.info("LoadServiceRepositoryImpl.java#createModulesInForm before FormLoaderHelper.populateQuestionsPV");
			ValueHolder vh = FormLoaderHelper.populateQuestionsPV(form, this);
			logger.info("LoadServiceRepositoryImpl.java#createModulesInForm after FormLoaderHelper.populateQuestionsPV");
			HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos = null;
			List<DataElementTransferObject> cdeDtos = null;	//repository.getCDEsByPublicIds(questCdePublicIds);
			try {
				List data = (ArrayList) vh.getValue();
				pvDtos = (HashMap<String, List<PermissibleValueV2TransferObject>>) data.get(QuestionsPVLoader.PV_INDEX);
				cdeDtos = (List<DataElementTransferObject>) data.get(QuestionsPVLoader.CDE_INDEX);
				logger.info("LoadServiceRepositoryImpl.java#createModulesInForm before createQuestionsInModule");
			} catch(Exception e) {
				e.printStackTrace();
			}
			//Now, onto questions
			createQuestionsInModule(module, moduledto, form, formdto, pvDtos, cdeDtos);	//JR417 new pvDtos param //JR423 added second new param

			getQrdao().updateModuleRepeatCount(moduledto.getModuleIdseq(), moduledto.getNumberOfRepeats(), moduledto.getModifiedBy());	//JR366 new!

			logger.info("LoadServiceRepositoryImpl.java#createModulesInForm after createQuestionsInModule");
		}
		
		logger.debug("Done creating modules for form");
	}	
	
	//FORMBUILD-529 get the correct CDE to be associated with a question
	protected DataElementTransferObject getMatchingDataElement(QuestionDescriptor question, List<DataElementTransferObject> cdeDtos)
	{	
		String cdePublicId = question.getCdePublicId();
		String cdeVersion = question.getCdeVersion();
		DataElementTransferObject matchedCde = null;
		String strPublicId = question.getCdePublicId();
		
		if (strPublicId == null) {
			logger.info("getMatchingDataElement: question.getCdePublicId returns null");
			return matchedCde;
		}
		else if (! StringUtils.isInteger(strPublicId)) {
			logger.error("getMatchingDataElement: question.getCdePublicId is not numeric");
			return matchedCde;
		}
		try
		{
			int cdePubIdNum = Integer.parseInt(cdePublicId);
			float cdeVerNum = Float.parseFloat(cdeVersion);

			if (cdeDtos != null && cdeDtos.size() > 0)
			{
				for (DataElementTransferObject cde : cdeDtos)
				{
					if (cde.getPublicId() == cdePubIdNum && cdeVerNum == cde.getVersion().floatValue())
						matchedCde = cde;
				}
			}
		} catch (NumberFormatException ne) {
			logger.error("Error in formatting CDE Public ID: " + cdePublicId + "v" + cdeVersion);
		}	
		return matchedCde;
	}
	
	@Transactional
	protected void createQuestionsInModule(ModuleDescriptor module, ModuleTransferObject moduledto, 
			FormDescriptor form, FormV2TransferObject formdto, HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos, List<DataElementTransferObject> cdeDtos) {
		if (module == null) return;
		logger.debug("Creating questions for module: " + module.getLongName());
		List<QuestionDescriptor> questions = module.getQuestions();
		if ((questions == null) || (questions.size() == 0)) {
			logger.debug("Module has no question");
			return;
		}
		else {
			logger.info("Amount of module questions: " + (questions.size()));
		}
		
		int idx = 0;
		for (QuestionDescriptor question : questions) {
			if (question == null) continue;
			if (question.isSkip()) continue;
			logger.trace("question number: " + idx);
			
			QuestionTransferObject questdto = DomainObjectTranslator.translateIntoQuestionDTO(question, form);

			questdto.setDisplayOrder(idx++);
			questdto.setContext(formdto.getContext());
			questdto.setModule(moduledto);
			
			//FORMBUILD-529 associate the correct CDE and then use that CDE for setting Question Text
			DataElementTransferObject matchingCde = getMatchingDataElement(question, cdeDtos);
			
			QuestionHelper.handleEmptyQuestionText(questdto, matchingCde);

			//better to call createQuestionComponents, which is not implement.
			QuestionTransferObject newQuestdto = (QuestionTransferObject)this.questionV2Dao.createQuestionComponent(questdto);
			String seqid = newQuestdto.getQuesIdseq();
			
			logger.debug("Created a question: " + seqid);
			question.setQuestionSeqId(seqid);
			retrievePublicIdForQuestion(seqid, question, questdto);
			
			createQuestionInstruction(newQuestdto, moduledto, question.getInstruction());
			
			createQuestionValidValues(question, form, newQuestdto, moduledto, formdto, pvDtos);		//JR417 entry point
		}
		logger.debug("Done creating questions for module");
	}
	
	/**
	 * Create valid value for a newly created question.
	 * 
	 * @param question question generated from xml
	 * @param form	form generated from xml
	 * @param newQuestdto new question dto that just got created in db, with new seqid and public id
	 * @param moduledto
	 */
	@Transactional
	protected void createQuestionValidValues(QuestionDescriptor question, FormDescriptor form, QuestionTransferObject newQuestdto, 
			ModuleTransferObject moduledto, FormV2TransferObject formdto, HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos) {
		
		List<QuestionDescriptor.ValidValue>  validValues = question.getValidValues();
		
		int idx = 0;
		logger.debug("LoadServiceRepositoryImpl.java#createQuestionValidValues");
		for (QuestionDescriptor.ValidValue vValue : validValues) {
			if (vValue.isSkip()) { 
				logger.debug("LoadServiceRepositoryImpl.java#createQuestionValidValues vValue " + vValue.getMeaningText() + " vpIdSeq [" + vValue.getVdPermissibleValueSeqid() + "] skipped!");
				continue;
			}
			
			idx++;
			
			//FORMBUILD-424, 425 : Following block of code to be done in validation step and get the correct PV and set values for Meaning Text and Description
			//JR417 begin
			//get the correct vv's pvdto and set vv's vdPermissibleValueSeqid
			/*PermissibleValueV2TransferObject pv = null;
			 * try {
			 pv = FormLoaderHelper.getValidValuePV(vValue, pvDtos, repository);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(pv != null) {
				String vdPermissibleValueSeqid = pv.getIdseq();	//c.f. FormLoaderHelper.populateQuestionsPV(form, repository)
				vValue.setVdPermissibleValueSeqid(vdPermissibleValueSeqid);  //set the vdpvIdseq!
//				pv.setIdseq(rs.getString("VP_IDSEQ"));	//JR448 TODO do I need to set this? JR417 this is THE vdPvIdSeq! Not sure why is it populated during content validation but it is what it is!
				ValueMeaningV2 pvVM = pv.getValueMeaningV2();
				
				//FORMBUILD-448 following is redundant code as the PV already has the correct VMs associated with it. 
				if(pvVM != null) {
					vValue.setPreferredName(pvVM.getPublicId() + "v" + pvVM.getVersion());
					System.out.println("*********** pv value[" + pv.getValue() + "] vValue getPreferredName[" + vValue.getPreferredName() + "] ***********");
					ValueMeaningV2TransferObject vm = new ValueMeaningV2TransferObject();
					vm.setPublicId(pvVM.getPublicId()); //this is also the preferred name for some reason
					vm.setVersion(pvVM.getVersion());
					vm.setPreferredName(pvVM.getPreferredName());
					vm.setPreferredDefinition(pv.getValue());	//or pvVM's preferedDefinition/vValue's meaningText
					vm.setLongName(pvVM.getLongName());
					vm.setDescription(pvVM.getPreferredDefinition());
					vm.setPreferredDefinition(pvVM.getPreferredDefinition());
					vm.setIdseq(pvVM.getIdseq());
					pv.setValueMeaningV2(vm);
				}
			} //what happend if it is null? do we need to check?
			//JR417 end
			*/
			FormValidValueTransferObject fvv = translateIntoValidValueDto(vValue, newQuestdto, moduledto, formdto, idx);	 //JR417 vValue's vdpvseqid / vp_idseq is NOT empty anymore (fixed in this ticket)
			
			fvv.setDisplayOrder(idx);
			
			if (fvv.getPreferredDefinition() == null || fvv.getPreferredDefinition().length() == 0) {
				String stop = "debug";
				stop = "stop";	//I have no clue what this does, if you do, please let me know
			}
			
			String vvSeqid = formValidValueV2Dao.createValidValue(fvv,newQuestdto.getQuesIdseq(),moduledto.getCreatedBy());	//JR417 the correct place to emulate EJB method
			
			
			if (vvSeqid != null && vvSeqid.length() > 0) {
				int count = formValidValueV2Dao.createValidValueAttributes(vvSeqid, vValue.getMeaningText(), vValue.getDescription(), moduledto.getCreatedBy());
				//vValue.setPreferredName("JAMES_PREFEREDNAME_123");   //JR417
				formValidValueV2Dao.updateValueMeaning(vvSeqid, vValue.getMeaningText(), vValue.getDescription(), moduledto.getCreatedBy());	//JR417 already called by createFormValidValueComponent (see below)

				//FORMBUILD-448 commenting out the following duplicate call to insert valid value. Does not help with resolving tracker 417.
				/*if(count == 1) {	//assuming that only one match!
					formValidValueV2Dao.createFormValidValueComponent(fvv,  vvSeqid, moduledto.getCreatedBy());	//JR417 new call!

					//JR417 TBD not sure if the following should be in formValidValueV2Dao.createFormValidValueComponent or outside!!!
//				    createNewValidValues(formValidValueV2Dao, formValidValueInstructionV2Dao,
//				    		fvv.getNewValidValues(),
//				            formVVChanges.getQuestionId());
//				   updateValidValues(fvvDao, fvvInstrDao,
//				                     formVVChanges.getUpdatedValidValues());
//				   deleteValidValues(fvvDao, fvvInstrDao,
//				                     formVVChanges.getDeletedValidValues());
				}*/
				
				String instr = vValue.getInstruction();
				if (instr != null && instr.length() > 0) {
					InstructionTransferObject instrdto = createInstructionDto(fvv, instr);
					formValidValueInstructionV2Dao.createInstruction(instrdto, vvSeqid);
				}
			}
			
			logger.debug("Created new valid value #: " + idx + ", MeaningText: "+ vValue.getMeaningText());
		}
	}
	
	
	
	
	
	/**
	 * Create new instruction for a question.
	 * 
	 * Do nothing is instruction string is null or empty. 
	 * 
	 * @param newQuestdto
	 * @param moduledto
	 * @param instString
	 */
	@Transactional
	protected void createQuestionInstruction(QuestionTransferObject newQuestdto, 
			ModuleTransferObject moduledto, String instString) {
		
		if (instString == null || instString.length() == 0)
			return; //Nothing to do
		
		String sizedUpInstr = instString;

		/* (Snippet copied from FormModuleEditAction (FB)
		 * 
		 * Truncate instruction string to fit in LONG_NAME field (255 characters)
		 * Refer to GF# 12379 for guidance on this
		 */

		if (sizedUpInstr.length() > MAX_LONG_NAME_LENGTH) { 
			sizedUpInstr = sizedUpInstr.substring(0, MAX_LONG_NAME_LENGTH);
		}

		Instruction instr = new InstructionTransferObject();
		instr.setLongName(sizedUpInstr);
		instr.setDisplayOrder(0);
		instr.setVersion(new Float(1));
		instr.setAslName("DRAFT NEW");
		instr.setContext(moduledto.getContext());
		instr.setPreferredDefinition(instString);
		instr.setCreatedBy(moduledto.getCreatedBy());
		newQuestdto.setInstruction(instr);
		
		questInstructionV2Dao.createInstruction(instr, newQuestdto.getQuesIdseq());
         
	}
	
	/**
	 * Check if question has existing instruction. If instruction found in db, update it. Otherwise, create new.
	 * 
	 * Do nothing if instruction string is null or empty.
	 * 
	 * @param questdto
	 * @param moduledto
	 * @param instString
	 */
	@Transactional
	protected void updateQuestionInstruction(QuestionTransferObject questdto, 
			ModuleTransferObject moduledto, String instString) {
		if (instString == null || instString.length() == 0)
			return; //Nothing to do
		
		List existings = questInstructionV2Dao.getInstructions(questdto.getQuesIdseq());
		if (existings != null && existings.size() > 0) {
			String sizedUpInstr = instString;
			InstructionTransferObject existing = (InstructionTransferObject)existings.get(0);

			if (sizedUpInstr.length() > MAX_LONG_NAME_LENGTH) { 
				sizedUpInstr = sizedUpInstr.substring(0, MAX_LONG_NAME_LENGTH);
			}

			Instruction instr = new InstructionTransferObject();
			instr.setLongName(sizedUpInstr);
			instr.setPreferredDefinition(instString);
			instr.setModifiedBy(moduledto.getCreatedBy());
			instr.setIdseq(existing.getIdseq());
			questInstructionV2Dao.updateInstruction(instr);
		} else {
			createQuestionInstruction(questdto, moduledto, instString);
		}
	}
	
	
	
	
	
	
	/**
	 * Check to see if a new module has a "similar" existing module: no match with public id and version but has the same long name. If found,
	 * add an instruction to the module: 
	 * 		"New Module with public id 2963886 and version 1.0 was created by Form Loader because the XML module public id value of 
	 * 		2962822 and version 1.0 did not match an existing module. Please review modules and delete any unnecessary module, 
	 * 		this module may have been intended to replace an existing module." 
	 * 
	 * This applies to update form ONLY
	 * 
	 * @param currModule
	 * @param existingModuledtos
	 */
	@Transactional(readOnly=true)
	protected void addModuleInstructionIfNecessary(ModuleTransferObject currModule, 
			List<ModuleTransferObject> existingModuledtos) {
		
		for (ModuleTransferObject moduledto : existingModuledtos) {
			//These are the previously marked as "matched" in isExistingModule
			if (MARK_TO_KEEP_IN_UPDATE == moduledto.getDisplayOrder())
				continue;
			
			if (currModule.getLongName().equalsIgnoreCase(moduledto.getLongName())) {
				//get the newly added module's public id and version
				ModuleTransferObject mod = moduleV2Dao.getModulePublicIdVersionBySeqid(currModule.getIdseq());
				
				String instr = "New Module with public id " + mod.getPublicId() + " and version " + mod.getVersion() + 
						" was created by Form Loader because the XML module public id value of " +
						currModule.getPublicId() + " and version " + currModule.getVersion() + " did not match an existing module. " +
						"Please review modules and delete any unnecessary module, this module may have been intended to " +
						"replace an existing module.";
				
				InstructionTransferObject instdto = createInstructionDto(currModule, instr);
				moduleInstructionV2Dao.createInstruction(instdto, currModule.getIdseq());
				
				break;
			}
			
		
		}
	}
	
	protected boolean isExistingModule(ModuleTransferObject currModule, List<ModuleTransferObject> existingModuledtos) {
		
		for (ModuleTransferObject moduledto : existingModuledtos) {
			if ((moduledto.getPublicId() == currModule.getPublicId()) &&
					(moduledto.getVersion().floatValue() == currModule.getVersion().floatValue())) {
				currModule.setIdseq(moduledto.getIdseq());
				currModule.setModuleIdseq(moduledto.getModuleIdseq()); //don't know how these are used
				moduledto.setDisplayOrder(MARK_TO_KEEP_IN_UPDATE); //use this to indicate module's taken
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Check the list to see if any is marked with MARK_FOR_DELETE. Delete the marked ones.
	 * @param existingmoduledtos
	 */
	@Transactional
	protected void removeExtraModulesIfAny(List<ModuleTransferObject> existingmoduledtos) {
		for (ModuleTransferObject moduledto : existingmoduledtos) {
			if (moduledto.getDisplayOrder() != MARK_TO_KEEP_IN_UPDATE) {
				logger.debug("Found a module to delete: [" + moduledto.getPublicId() + "|" + moduledto.getVersion() + "]");
				this.moduleV2Dao.deleteModule(moduledto.getModuleIdseq());
			}
		}
	}
	
	/**
	 * Check a list of pre-processed existing questions from db. Delete those NOT marked as MARK_TO_KEEP_IN_UPDATE
	 * @param questiondtos
	 */
	@Transactional
	protected void removeExtraQuestionsIfAny(List<QuestionTransferObject> questiondtos) {
		//Method copied from FormBuilderEJB
		
		for (QuestionTransferObject qdto : questiondtos) {
			if (qdto.getDisplayOrder() == MARK_TO_KEEP_IN_UPDATE)
				continue;
			
			String qSeqid = qdto.getQuesIdseq();
			logger.debug("Found a question to delete: [" + qdto.getPublicId() + "|" + qdto.getVersion() + "|" + qSeqid);
			
			//delete question
			questionV2Dao.deleteQuestion(qdto.getQuesIdseq());
			logger.debug("Done deleting question");
			
			//delete instructions
			List instructions = this.questInstructionV2Dao.getInstructions(qSeqid);
			if (instructions != null && instructions.size() > 0) {
				logger.debug("Deleting instructions for question...");
				for (int i = 0; i < instructions.size(); i++) {
					InstructionTransferObject instr = (InstructionTransferObject)instructions.get(i);
					this.questInstructionV2Dao.deleteInstruction(instr.getIdseq());
					logger.debug("Deleted an instruction for question");
				}
			}
			
			//delete valid values
			List<String> vvIds = this.formValidValueV2Dao.getValidValueSeqidsByQuestionSeqid(qSeqid);
			if (vvIds != null) {
				for (String vvid : vvIds) {
					//This calls remove_value sp which takes care of vv and instruction
					formValidValueV2Dao.deleteFormValidValue(vvid);
				}
			}
			
		}
	}
	
	/**
	 * Reset all question versions to 1.0 in a module, mostly because we'll insert these questions
	 * as new.
	 * @param module
	 */
	protected void resetQeustionVersionInModule(ModuleDescriptor module) {
		List<QuestionDescriptor> questions = module.getQuestions();
		for (QuestionDescriptor question : questions) {
			question.setVersion("1.0");
		}
	}
	
	
	/**
	 * Determine if it's an existing question based on public id and version
	 * @param currQuestiondto
	 * @param existingQuestiondtos
	 * @return
	 */
	protected QuestionTransferObject getExistingQuestionDto(QuestionTransferObject currQuestdto, List<QuestionTransferObject> existingQuestiondtos) {
		for (QuestionTransferObject quest : existingQuestiondtos) {
			if (currQuestdto.getPublicId() == quest.getPublicId() && 
					currQuestdto.getVersion().floatValue() == quest.getVersion().floatValue()) {
				currQuestdto.setQuesIdseq(quest.getQuesIdseq());
				currQuestdto.setIdseq(quest.getIdseq());
				//Mark this as being update so we don't collect it to delete later.
				quest.setDisplayOrder(MARK_TO_KEEP_IN_UPDATE);
				return quest;
			}
		}
		
		return null;
	}
	
	
	protected boolean isExistingValidValue(FormValidValueTransferObject currVV, List<FormValidValueTransferObject> existingVVs) {
		for (FormValidValueTransferObject vv : existingVVs) {
			if (currVV.getLongName().equalsIgnoreCase((vv.getLongName())) 
					&& currVV.getPreferredDefinition().equalsIgnoreCase(vv.getPreferredDefinition())) {
				currVV.setIdseq(vv.getIdseq());
				currVV.setValueIdseq(vv.getValueIdseq());
				return true;
			}
		}
		
		return false;
	}
	
	
	
	@Transactional
	public String createFormCollectionRecords(FormCollection coll) {
		
		String collSeqid = collectionDao.createCollectionRecord(coll.getName(), coll.getDescription(), 
				coll.getXmlFileName(), coll.getXmlPathOnServer(), coll.getCreatedBy(), coll.getNameRepeatNum());
		
		List<FormDescriptor> forms = coll.getForms();		
		for (FormDescriptor form : forms) {
			
			int publicId = (form.getPublicId() == null || form.getPublicId().length() == 0) ? 0 : Integer.parseInt(form.getPublicId());
			float version = (form.getVersion() == null || form.getVersion().length() == 0) ? 0 : Float.parseFloat(form.getVersion());
			Date loadDate = form.getModifiedDate();
			if (loadDate == null)
				loadDate = new Date();
			int res = collectionDao.createCollectionFormMappingRecord(collSeqid, form.getFormSeqId(),
					publicId, version, form.getLoadType(),
					form.getLoadStatus(), form.getLongName(), form.getPreviousLatestVersion(), loadDate);
			
			int loatStatus = (res > 0) ? FormDescriptor.STATUS_LOADED : FormDescriptor.STATUS_LOAD_FAILED;
		}
		
		return collSeqid;
	}
	
	
	public void updateFormInCollectionRecord(FormCollection coll, FormDescriptor form) {
		if (coll == null || form == null) {
			logger.error("Invalid input on FormCollection obj or FormDescriptor obj");
			return;
		}
		
		this.collectionDao.updateCollectionFormMappingRecord(coll.getId(), form.getFormSeqId(), 
			 form.getLoadType(), form.getLoadStatus());
	}
	
	/**
	 * Post unload tasks:
	 * 
	 * If the form had been loaded as "New Version" AND if it was marked as the latest version prior to unload, we
	 *    need to 1) set latest version indicator to "no" and 2) restore the previous latest version 
	 *    (latest version form before the form was loaded).
	 *    
	 * @param form
	 */
	protected void manageFormVersionPostUnload(FormDescriptor form) {
		String seqid = form.getFormSeqId();

		logger.debug("Form: " + seqid + "|" + form.getFormSeqId() + " unloaded");

		if (!formV2Dao.isLatestVersionForForm(seqid) ||
				!FormDescriptor.LOAD_TYPE_NEW_VERSION.equals(form.getLoadType().trim()))
			return; 

		formV2Dao.updateLatestVersionIndicator(seqid, "No", DomainObjectTranslator.FORM_LOADER_DB_USER);

		float prevVersion = form.getPreviousLatestVersion();
		logger.debug("Form: " + seqid + "|" + form.getFormSeqId() + " unloaded. Previous Lastest version was " + prevVersion);
		if (prevVersion > 0) {
			int r = formV2Dao.updateLatestVersionIndicatorByPublicIdAndVersion(Integer.parseInt(form.getPublicId()), 
					prevVersion, "Yes", DomainObjectTranslator.FORM_LOADER_DB_USER);
			form.addMessage("After unload, the latest version for the form with pulbic id [" + form.getPublicId() +
					"] is reset to " + prevVersion);
			logger.debug("Previous Lastest version was restored: " + prevVersion + ". Response: " + r);
		} else {
			form.addMessage("No valid previous latest version with form. Unable to restore previous latest " +
					"version after unloading a new version form");
			logger.debug("No valid previous latest version with form");
		}

	}
	
	
}
