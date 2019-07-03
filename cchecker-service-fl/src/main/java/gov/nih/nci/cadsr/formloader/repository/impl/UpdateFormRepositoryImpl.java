package gov.nih.nci.cadsr.formloader.repository.impl;

import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import gov.nih.nci.cadsr.formloader.domain.DomainObjectTranslator;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.domain.ModuleDescriptor;
import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor;
import gov.nih.nci.ncicb.cadsr.common.dto.FormV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.FormValidValueTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.InstructionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ModuleTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionChangeTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.exception.DMLException;
import gov.nih.nci.ncicb.cadsr.common.resource.Instruction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class UpdateFormRepositoryImpl extends LoadServiceRepositoryImpl {
	private static final Logger logger = LoggerFactory.getLogger(UpdateFormRepositoryImpl.class.getName());
	
	public UpdateFormRepositoryImpl(DataSource dataSource2) {
		super(dataSource2);
		// TODO Auto-generated constructor stub
	}

	@Transactional
	public String updateForm(FormDescriptor form, String loggedinUser, String xmlPathName) {
		
		try {
			FormV2TransferObject formdto = DomainObjectTranslator.translateIntoFormDTO(form);	
			if (formdto == null) {
				logger.error("Error!! Failed to translate xml form into FormTransferObject");
				return null;
			}

			int res = formV2Dao.updateFormComponent(formdto);
			if (res != 1) {
				logger.error("Error!! Failed to update form");
				return null;
			}

			createFormInstructions(form, formdto);

			//designations, refdocs, definitions and contact communications
			processFormdetails(form, xmlPathName, form.getIndex());

			//Onto modules and questions
			updateModulesInForm(form, formdto);
		} catch (DMLException dbe) {
			logger.error(dbe.getMessage());
			form.addMessage(dbe.getMessage());
			form.setFormSeqId("");
			form.setLoadStatus(FormDescriptor.STATUS_LOAD_FAILED);
		}
		
		return form.getFormSeqId();
	}
	
	/**
	 * Update inlcudes adding new modules, updating existing ones and delete extra ones that are not in xml
	 * 
	 * @param form
	 * @param formdto
	 */
	@Transactional
	protected void updateModulesInForm(FormDescriptor form, FormV2TransferObject formdto) {
		
		List<ModuleDescriptor> modules = form.getModules();
		
		List<ModuleTransferObject> existingModuledtos = this.formV2Dao.getModulesInAForm(formdto.getFormIdseq());
		
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
			moduledto.setDisplayOrder(++displayOrder);
			String moduleSeqid = "";
	
			if (isExistingModule(moduledto, existingModuledtos)) {
				//this means sampe public id and version
				int res = moduleV2Dao.updateModuleComponent(moduledto);
				
				updateQuestionsInModule(module, moduledto, form, formdto);
			}
			else {
				moduledto.setContext(formdto.getContext());
				moduleSeqid = moduleV2Dao.createModuleComponent(moduledto);
				module.setModuleSeqId(moduleSeqid);
				moduledto.setIdseq(moduleSeqid);
				
				addModuleInstructionIfNecessary(moduledto, existingModuledtos);
				
				//Now, onto questions. Ignore version from xml. Start from 1.0
				resetQeustionVersionInModule(module); //
				createQuestionsInModule(module, moduledto, form, formdto, new HashMap(), null);	//JR417 4.2 release for PvDtos! Thus it is empty hashmap.

			}
			
		}
		
		//Find the existing modules to be deleted
		removeExtraModulesIfAny(existingModuledtos);
	}
	
	
	/**
	 * Update an existing question's various attributes.
	 * @param question
	 * @param questdto
	 * @param moduledto
	 */
	@Transactional
	protected void updateQuestion(QuestionDescriptor question, QuestionTransferObject questdto, QuestionTransferObject existing,
			ModuleTransferObject moduledto, FormV2TransferObject formdto) {
		
		//What we could update in sbrext.Quest_contents_view_ext
		//display order, long name and asl name
		if (!questdto.getLongName().equals(existing.getLongName()) ||
				questdto.getDisplayOrder() != existing.getDisplayOrder()) {
			int res = questionV2Dao.updateQuestionLongNameDispOrderDeIdseq(questdto);
		}
		
		//What we could update in sbrext.quest_attributes_ext
		//manditory, editable, default value
		QuestionChangeTransferObject qChangedto = new QuestionChangeTransferObject();
		qChangedto.setDefaultValue(question.getDefaultValue());
		qChangedto.setEditable(question.isEditable());
		qChangedto.setMandatory(question.isMandatory());
		qChangedto.setQuestionId(questdto.getQuesIdseq());
		
		qChangedto.setQuestAttrChange(true);
        
		//update isEditable, isMandatory and default value
		questionV2Dao.updateQuestAttr(qChangedto, questdto.getModifiedBy().toUpperCase());
      
		//what to do with isderived?
		
		updateQuestionInstruction(questdto, moduledto, question.getInstruction());
			
		//For valid values, need to first compare what's in db
		updateQuestionValidValues(question, questdto, moduledto, formdto);
		
	}

	@Transactional
	protected void updateQuestionValidValues(QuestionDescriptor question, QuestionTransferObject questdto, 
			ModuleTransferObject moduledto, FormV2TransferObject formdto) {

		List<QuestionDescriptor.ValidValue>  validValues = question.getValidValues();
		List<FormValidValueTransferObject> existingVVs = questionV2Dao.getValidValues(questdto.getQuesIdseq());

		/*
		 * compare vv's long name, preferred definition and display order
		 * 
		 * 
		 */

		int idx = 0;
		for (QuestionDescriptor.ValidValue vValue : validValues) {
			
			if (vValue.isSkip()) continue;
			
			FormValidValueTransferObject fvvdto = translateIntoValidValueDto(vValue, questdto, moduledto,
					formdto, idx);
			
			fvvdto.setDisplayOrder(idx++);
			
			if (isExistingValidValue(fvvdto, existingVVs)) {
				fvvdto.setModifiedBy(moduledto.getModifiedBy());
				this.updateQuestionValidValue(fvvdto, vValue, questdto);
			} else {
				String vvSeqid  = 
						formValidValueV2Dao.createValidValue(fvvdto,questdto.getQuesIdseq(),moduledto.getCreatedBy());
				
				if (vvSeqid != null && vvSeqid.length() > 0) {
					formValidValueV2Dao.createValidValueAttributes(vvSeqid, vValue.getMeaningText(), vValue.getDescription(), moduledto.getCreatedBy());
					
					String instr = vValue.getInstruction();
					if (instr != null && instr.length() > 0) {
						InstructionTransferObject instrdto = createInstructionDto(fvvdto, instr);
						formValidValueInstructionV2Dao.createInstruction(instrdto, vvSeqid);
					}
				}
				
			}
		}
	}
	
	/**
	 * 
	 * @param module
	 * @param moduledto
	 * @param form
	 * @param formdto
	 */
	@Transactional
	protected void updateQuestionsInModule(ModuleDescriptor module, ModuleTransferObject moduledto, 
			FormDescriptor form, FormV2TransferObject formdto) {
		
		List<QuestionTransferObject> existingQuestiondtos = 
				moduleV2Dao.getQuestionsInAModuleV2(moduledto.getModuleIdseq());
		
		List<QuestionDescriptor> questions = module.getQuestions();
		
		int idx = 0;
		for (QuestionDescriptor question : questions) {
			if (question.isSkip()) continue;
			
			QuestionTransferObject questdto = DomainObjectTranslator.translateIntoQuestionDTO(question, form);
	
			questdto.setContext(formdto.getContext());
			questdto.setModule(moduledto);
			//questdto.setModifiedBy(module.getModifiedBy());
			
			//TODO: display order should come from xml?
			questdto.setDisplayOrder(idx++);
			
			QuestionTransferObject existingquestdto = getExistingQuestionDto(questdto, existingQuestiondtos);
			if (existingquestdto != null) {
				//existing question
				updateQuestion(question, questdto, existingquestdto, moduledto, formdto);
				
			} else {
				//better to call createQuestionComponents, which is not implemented.
				QuestionTransferObject newQuestdto = (QuestionTransferObject)this.questionV2Dao.createQuestionComponent(questdto);
				createQuestionInstruction(newQuestdto, moduledto, question.getInstruction());
				//DEBUGG
				createQuestionValidValues(question, form, newQuestdto, moduledto, formdto, new HashMap());	//JR417 4.2 release for PvDtos! Thus it is empty hashmap.
			}
		}
		
		removeExtraQuestionsIfAny(existingQuestiondtos);
	}
	
	@Transactional
	protected void updateQuestionValidValue(FormValidValueTransferObject currVV, 
			QuestionDescriptor.ValidValue vvXml, QuestionTransferObject questdto) {
		
		formValidValueV2Dao.updateDisplayOrder(currVV.getValueIdseq(), 
				currVV.getDisplayOrder(), currVV.getModifiedBy());
		
		String meaningText = vvXml.getMeaningText();
		String description = vvXml.getDescription();
		if (meaningText != null && meaningText.length() > 0
				|| description != null && description.length() > 0)
			formValidValueV2Dao.updateValueMeaning(currVV.getValueIdseq(), meaningText, description, currVV.getModifiedBy());
			
		/*
		 * 
		 */
		String instruct = vvXml.getInstruction();
		if (instruct != null && instruct.length() > 0) {
			Instruction instr = new InstructionTransferObject();
			instr.setLongName(instruct);
			instr.setDisplayOrder(0);
			instr.setVersion(new Float(1));
			instr.setAslName("DRAFT NEW");
			instr.setContext(questdto.getContext());
			instr.setPreferredDefinition(instruct);
			instr.setCreatedBy(questdto.getCreatedBy());
			formValidValueInstructionV2Dao.createInstruction(instr, currVV.getValueIdseq());
		}
	}

}
