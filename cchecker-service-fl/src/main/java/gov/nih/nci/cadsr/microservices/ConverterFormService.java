/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.domain.ModuleDescriptor;
import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor;
import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor.ValidValue;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObjectExt;
/**
 * Create FL form from ALS Form Service.
 * 
 * @author asafievan
 *
 */
@Service
public class ConverterFormService {
	public FormDescriptor convertAlsToCadsr(ALSForm alsForm, ALSData alsData, FormDescriptor formDesc) {
		formDesc.setCollectionName(alsData.getCrfDraft().getPrimaryFormOid());
		formDesc.setLoadType(FormDescriptor.LOAD_TYPE_NEW);
		formDesc.setLongName(alsForm.getDraftFormName());
		formDesc.setType("CRF");		
		formDesc.setPreferredDefinition(alsForm.getFormOid());
		List<ModuleDescriptor> modules = new ArrayList<ModuleDescriptor>();
		modules.add(addModule(alsForm, alsData));
		formDesc.setModules(modules);
		return formDesc;
	}
	
	
	/**
	 * Return a Module
	 * @param alsForm
	 * @param alsData
	 * @return ModuleDescriptor
	 */
	private ModuleDescriptor addModule(ALSForm alsForm, ALSData alsData) {
		ModuleDescriptor defaultModule = new ModuleDescriptor();
		defaultModule.setLongName("Default Module");
		defaultModule.setPreferredDefinition("No Definition");
		defaultModule.setQuestions(addQuestions(alsForm, alsData));
		return defaultModule;
	}
	
	
	/**
	 * Return a list of questions
	 * @param alsForm
	 * @param alsData
	 * @return List<QuestionDescriptor>
	 */	
	private List<QuestionDescriptor> addQuestions (ALSForm alsForm, ALSData alsData) {
		List<QuestionDescriptor> questions = new ArrayList<QuestionDescriptor>();
		String formOid = null;
		for (ALSField alsField : alsData.getFields()) {
			formOid = alsField.getFormOid();
			if (formOid != null) {
				if (!"FORM_OID".equals(alsField.getFieldOid())) {
					if (formOid.equals(alsForm.getFormOid())) {
						questions.add(addSingleQuestion(alsField, alsData));
						formOid = null;
					}
				}
			}
		}
		return questions;
	}
	
	
	/**
	 * Return a single question and its valid values filled up
	 * @param alsForm
	 * @param alsData
	 * @return QuestionDescriptor
	 */		
	private QuestionDescriptor addSingleQuestion (ALSField alsField, ALSData alsData) {
		QuestionDescriptor question = new QuestionDescriptor();
		//question.setDisplayOrder(alsField.getOrdinal()); // Not present at the moment
		if (alsField.getPreText()!=null && (!alsField.getPreText().isEmpty())) {
			question.setQuestionText(alsField.getPreText()); 
		}
		String[] idVersion = extractIdVersion(alsField.getDraftFieldName());
		if (idVersion[0] != null) {
			if (NumberUtils.isCreatable(idVersion[0]) && NumberUtils.isCreatable(idVersion[1])
					&& NumberUtils.isCreatable(idVersion[2])) {
				question.setCdePublicId(idVersion[0]);
				question.setCdeVersion(idVersion[1] + "." + idVersion[2]);
			}
		}
		if (alsField.getDataDictionaryName()!=null) {
			question.setValidValues(addValidValues(alsField.getDataDictionaryName(), alsData.getDataDictionaryEntries(), question));
		}
		return question;
	}
	
	
	/**
	 * Return a list of Valid Values for the question
	 * @param alsForm
	 * @param alsData
	 * @return List<ValidValue>
	 */			
	private List<ValidValue> addValidValues (String ddeName, Map<String, ALSDataDictionaryEntry> ddeMap, QuestionDescriptor question) {
		List<ValidValue> validValues = new ArrayList<ValidValue>();
		for (String key : ddeMap.keySet()) {
			if (key.equals(ddeName)) {
				for (int i = 0; i < ddeMap.get(key).getCodedData().size(); i++) {
					ValidValue validVal = question.new ValidValue();
					//validVal.setDisplayOrder(ddeMap.get(key).getOrdinal()); // Not present at the moment 
					validVal.setValue(ddeMap.get(key).getCodedData().get(i));
					validVal.setMeaningText(ddeMap.get(key).getUserDataString().get(i));
					validVal.setPreferredDefinition("No definition");				
					validValues.add(validVal);
				}
			}							
		}			
		return validValues;
	}
	
	
	/**
	 * Splits & extracts the Public ID and Version from a string of format [PIDXXXXX_Vxx_x]
	 * @param draftFieldName
	 * @return String[]
	 */
	protected static String[] extractIdVersion (String draftFieldName) {
		String publicid_prefix = "PID";
		String version_prefix = "_V";		
		String[] idVersionSplitElements = new String[3];
		if (draftFieldName.indexOf(publicid_prefix) > -1 && draftFieldName.indexOf(version_prefix) > -1) {
			String idVn = draftFieldName.substring(draftFieldName.indexOf(publicid_prefix),
					draftFieldName.length());
			String id = idVn.substring(3, idVn.indexOf("_"));
			String version = (idVn.substring(idVn.indexOf(version_prefix) + 2, idVn.length()));
			id = id.trim();
			String[] versionTokens = version.split("\\_");
			version = versionTokens[0] + "." + versionTokens[1];
			idVersionSplitElements[0] = id;
			idVersionSplitElements[1] = versionTokens[0];
			idVersionSplitElements[2] = versionTokens[1];
		}
		return idVersionSplitElements;
	}	
	
	
}
