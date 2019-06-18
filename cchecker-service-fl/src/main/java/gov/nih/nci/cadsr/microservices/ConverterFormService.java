/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.domain.ModuleDescriptor;
import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor;
import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor.ValidValue;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObjectExt;
/**
 * Create FL form from ALS Form Service.
 * 
 * @author asafievan
 *
 */
@Service
public class ConverterFormService {
	public FormDescriptor convertAlsToCadsr(ALSForm alsForm, ALSData alsData) {
		FormDescriptor formDesc = new FormDescriptor();
		formDesc.setCollectionName(alsData.getCrfDraft().getPrimaryFormOid());
		formDesc.setContext("context"); // Retrieve from caDSR DB for the protocol in the ALS file 
		formDesc.setLongName(alsForm.getDraftFormName());
		List<ModuleDescriptor> modules = new ArrayList<ModuleDescriptor>();
		modules.add(addModule(alsForm, alsData));
		formDesc.setModules(modules);
		List<ProtocolTransferObjectExt> protocols = new ArrayList<ProtocolTransferObjectExt>();
		formDesc.setProtocols(protocols);
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
				if (formOid.equals(alsForm.getFormOid())) {
					questions.add(addSingleQuestion(alsField, alsData));
					formOid = null;
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
		question.setQuestionText(alsField.getPreText());
		question.setCdePublicId(alsField.getDraftFieldName());
		question.setCdeVersion(alsField.getDraftFieldName());
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
				ValidValue validVal = question.new ValidValue();
				//validVal.setDisplayOrder(ddeMap.get(key).getOrdinal()); // Not present at the moment 
				validVal.setValue(ddeMap.get(key).getCodedData().toString());
				validVal.setMeaningText(ddeMap.get(key).getUserDataString().toString());
				validValues.add(validVal);
			}							
		}			
		return validValues;
	}
	
	
}
