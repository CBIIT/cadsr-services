package gov.nih.nci.cadsr.formloader.domain;

import gov.nih.nci.cadsr.formloader.service.common.FormLoaderHelper;
import gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.DataElementTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.FormV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ModuleTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.resource.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class DomainObjectTranslator {
	
	private static Logger logger = Logger.getLogger(DomainObjectTranslator.class.getName());
	
	public static final	String FORM_LOADER_DB_USER = "FORMLOADER";
	public static final	String WORKFLOW_STATUS_UNLOADED = "RETIRED UNLOADED"; 
	
	/**
	 * A form from database may have 2 rows because each additional protocol will trigger an additional form row added
	 * to database. This method will combine multiple rows for the same form into one form object, with comma separated
	 * protocol names.
	 * @param formdtos
	 * @return
	 */
	public static List<FormDescriptor> translateIntoFormDescriptors(FormCollection aColl, List<FormV2TransferObject> formdtos) {
		
		List<FormDescriptor> forms = new ArrayList<FormDescriptor>();
		
		if (formdtos == null) {
			logger.debug("Form dtos is null. Can't translater list into FormDescriptors");
			return forms;
		}
		
		HashMap<String, FormDescriptor> processedForms = new HashMap<String, FormDescriptor>();
		
		FormDescriptor form = null;
		for (FormV2TransferObject dto : formdtos) {
			if (processedForms.containsKey(dto.getFormIdseq())) {
				form = processedForms.get(dto.getFormIdseq());
				form.setProtocolName(dto.getProtocolLongName());
			}
			else {
				form  = new FormDescriptor();
				form.setFormSeqId(dto.getFormIdseq());
				form.setLongName(dto.getLongName());
				form.setContext(dto.getContextName());
				form.setModifiedBy(dto.getModifiedBy());
				form.setModifiedDate(dto.getDateModified());
				form.setCreatedBy(dto.getCreatedBy());
				form.setProtocolName(dto.getProtocolLongName());
				form.setPublicId(String.valueOf(dto.getPublicId()));
				form.setVersion(FormLoaderHelper.formatVersion(dto.getVersion()));
				form.setType(dto.getFormType());
				form.setWorkflowStatusName(dto.getAslName());
				form.setCollectionSeqid(aColl.getId());
				form.setCollectionName(aColl.getNameWithRepeatIndicator());
				forms.add(form);
				processedForms.put(dto.getFormIdseq(), form);
			}
		}
		
		return forms;
	}
	
	public static FormV2TransferObject translateIntoFormDTO(FormDescriptor form) {
		
		FormV2TransferObject formdto = new FormV2TransferObject();
		
		//User FORMLOADER for forms, modules, questions, etc.
		formdto.setModifiedBy(FORM_LOADER_DB_USER);
		form.setModifiedBy(FORM_LOADER_DB_USER);
		
		formdto.setCreatedBy(form.getCreatedBy());
		formdto.setChangeNote(form.getChangeNote());
		
		String loadType = form.getLoadType(); 
		if (FormDescriptor.LOAD_TYPE_NEW_VERSION.equals(loadType)) {
			formdto.setVersion(Float.parseFloat(form.getVersion()));
			form.setVersion(String.valueOf(formdto.getVersion()));
			formdto.setPublicId(Integer.parseInt(form.getPublicId()));
			formdto.setFormIdseq(form.getFormSeqId());
			formdto.setAslName("DRAFT NEW"); 
		} else if (FormDescriptor.LOAD_TYPE_NEW.equals(loadType)) {
			formdto.setVersion(Float.valueOf("1.0")); 
			formdto.setAslName("DRAFT NEW");
		} else if (FormDescriptor.LOAD_TYPE_UPDATE_FORM.equals(loadType)) {
			formdto.setVersion(Float.valueOf(form.getVersion()));
			formdto.setAslName(form.getWorkflowStatusName()); 
			String seqid = form.getFormSeqId(); //we got this when we queried on form public id
			//This should not happen
			if (seqid == null || seqid.length() == 0) {
				String msg = "Update form doesn't have a valid seqid. Unable to load.";
				form.addMessage(msg);
				form.setLoadStatus(FormDescriptor.STATUS_LOAD_FAILED);
				logger.error("Error with [" + form.getFormIdString() + "]: " + msg);
				return null;
			}
			
			formdto.setFormIdseq(seqid);
		}
		
		formdto.setLongName(form.getLongName());
		formdto.setPreferredName(form.getLongName());
		formdto.setFormType(form.getType());
		formdto.setFormCategory(form.getCategoryName());
		formdto.setPreferredDefinition(form.getPreferredDefinition());
		
		Context context = new ContextTransferObject();
	    context.setConteIdseq(form.getContextSeqid());
	    formdto.setContext(context);
		formdto.setConteIdseq(form.getContextSeqid());
		
		return formdto;
	}
	
	public static ModuleTransferObject translateIntoModuleDTO(ModuleDescriptor module, FormDescriptor form, FormV2TransferObject formdto) {
		ModuleTransferObject moduleDto = new ModuleTransferObject();
		
		moduleDto.setForm(formdto);
		moduleDto.setAslName("DRAFT NEW");
		moduleDto.setLongName(module.getLongName());
		
		moduleDto.setCreatedBy(formdto.getCreatedBy());
		moduleDto.setModifiedBy(FORM_LOADER_DB_USER);
		
		if (FormDescriptor.LOAD_TYPE_NEW.equals(form.getLoadType())) {
				moduleDto.setVersion(Float.valueOf("1.0")); 
		} else if (FormDescriptor.LOAD_TYPE_NEW_VERSION.equals(form.getLoadType())) {
			moduleDto.setVersion(formdto.getVersion());
		} else {
			if(StringUtils.isNumeric(module.getPublicId())) {	//JR366 not related to ticket
				moduleDto.setPublicId(Integer.parseInt((module.getPublicId())));
			}
			if(StringUtils.isNumeric(module.getVersion())) {	//JR366 not related to ticket
				moduleDto.setVersion(Float.parseFloat(module.getVersion()));
			}
		}
		
		moduleDto.setContext(formdto.getContext());
		moduleDto.setPreferredDefinition(module.getPreferredDefinition());
		if(StringUtils.isNumeric(module.maximumModuleRepeat)) {
			moduleDto.setNumberOfRepeats(Integer.valueOf(module.maximumModuleRepeat));	//JR366 new
		}
		return moduleDto;
	}
	
	public static QuestionTransferObject translateIntoQuestionDTO(QuestionDescriptor question, FormDescriptor form) {
		QuestionTransferObject questdto = new QuestionTransferObject();
		
		questdto.setCreatedBy(form.getCreatedBy());
		questdto.setModifiedBy(FORM_LOADER_DB_USER);
		
		String deSeqid = question.getCdeSeqId();
		if (deSeqid != null && deSeqid.length() > 0) {
			DataElementTransferObject de = new DataElementTransferObject();
			de.setDeIdseq(deSeqid);
			questdto.setDataElement(de);
		}
			
		if (FormDescriptor.LOAD_TYPE_NEW.equals(form.getLoadType())) {
			questdto.setVersion(Float.valueOf("1.0")); 
		} else if (FormDescriptor.LOAD_TYPE_NEW_VERSION.equals(form.getLoadType())) {
			questdto.setVersion(Float.valueOf(form.getVersion()));
		} else {
			questdto.setVersion(Float.valueOf(question.getVersion())); 
		}
		
		String pid = question.getPublicId();
		if (pid != null && pid.length() > 0)
			questdto.setPublicId(Integer.parseInt(pid));
		
		questdto.setEditable(question.isEditable());
		questdto.setMandatory(question.isMandatory());
		questdto.setDefaultValue(question.getDefaultValue());
		questdto.setAslName("DRAFT NEW");
		
		questdto.setLongName(question.getQuestionText());  
		
		//TODO: xsd doesn't have preferred def use question text now 
		//Denise: if preferred def of data element if it's not null. Otherwise, use long name of data element. if No CDE, use question text
		questdto.setPreferredDefinition(question.getQuestionText());

		return questdto;
	}
	
	

}
