/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

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
 * Test the ALS to Form Loader conversion service.
 * 
 * @author santhanamv
 *
 */

public class ConverterFormServiceTest {

	ConverterFormService converterService = new ConverterFormService();
	FormDescriptor formDescExpected;
	ALSData alsData;
	ALSForm alsForm;
	ALSField alsField; 
	ALSDataDictionaryEntry alsDDE;
	
	
	public FormDescriptor populateFormDescriptor() {
		FormDescriptor fd = new FormDescriptor();
		fd.setContext("NCIP");
		fd.setContextSeqid("7676-KJHG-654H-SD23");
		List<ProtocolTransferObjectExt> protocols = new ArrayList<ProtocolTransferObjectExt>();
		ProtocolTransferObjectExt protocol = new ProtocolTransferObjectExt();
		protocol.setPreferredName("NCIP");
		protocol.setProtoIdseq("8675-SDLK-IWES-SDSD");
		protocols.add(protocol);
		fd.setProtocols(protocols);
		return fd;
	}
	
	
	public FormDescriptor initFormDescriptor() {
		formDescExpected = new FormDescriptor();
		formDescExpected.setCollectionName("Test_Form_ID");
		formDescExpected.setContext("TEST"); // Retrieve from caDSR DB for the protocol in the ALS file
		formDescExpected.setLoadType(FormDescriptor.LOAD_TYPE_NEW);
		formDescExpected.setLongName("TEST FORM NAME");
		formDescExpected.setType("CRF");
		formDescExpected.setPreferredDefinition("Test_Form_ID");
		formDescExpected.setModules(initModules());
		formDescExpected.setProtocols(initProtocols());
		return formDescExpected;
	}	
	
	
	public void initAlsData()
	{
		alsData = new ALSData();
		alsData.getCrfDraft().setPrimaryFormOid("Test_Form_ID");
		alsData.getCrfDraft().setProjectName("NCIP");
		alsForm = new ALSForm();
		alsForm.setDraftFormName("TEST FORM NAME");
		alsForm.setFormOid("Test_Form_ID");
		alsField = new ALSField();
		Map<String, ALSDataDictionaryEntry> ddeMap = new HashMap<String, ALSDataDictionaryEntry>();
		alsField.setDraftFieldName("PID12345678_V2_5");
		alsField.setPreText("Question Pre text");
		alsField.setFormOid("Test_Form_ID");
		alsField.setDdeMap(ddeMap);
		alsField.setDataDictionaryName("Test_DDE");
		List<ALSField> fields = new ArrayList<ALSField>();
		fields.add(alsField);
		alsData.setFields(fields);
		alsDDE = new ALSDataDictionaryEntry();
		alsDDE.setDataDictionaryName("Test_DDE");
		alsDDE.getCodedData().add("A");
		alsDDE.getUserDataString().add("Stage A");
		alsDDE.getOrdinal().add("1");
		alsData.getDataDictionaryEntries().put("Test_DDE", alsDDE);
	}
	
	public List<ModuleDescriptor> initModules() 
	{
		List<ModuleDescriptor> modules = new ArrayList<ModuleDescriptor>();
		ModuleDescriptor module = new ModuleDescriptor();
		module.setLongName("Default Module");
		module.setPreferredDefinition("No Definition");
		module.setQuestions(initQuestions());
		modules.add(module);	
		return modules;
	}
	
	
	public List<QuestionDescriptor> initQuestions() {
		QuestionDescriptor question = new QuestionDescriptor();
		List<QuestionDescriptor> questions = new ArrayList<QuestionDescriptor>();
		question.setQuestionText("Question Pre text");
		question.setCdePublicId("12345678"); 
		question.setCdeVersion("2.5"); 
		question.setValidValues(intiValidValues(question));
		questions.add(question);
		return questions;
	}
	
	
	public List<ValidValue> intiValidValues(QuestionDescriptor question) {
		List<ValidValue> validValues = new ArrayList<ValidValue>();
		ValidValue validVal = question.new ValidValue();
		validVal.setValue("A");
		validVal.setMeaningText("Stage A");
		validValues.add(validVal);	
		return validValues;
	}
	
	
	public List<ProtocolTransferObjectExt> initProtocols() {
		List<ProtocolTransferObjectExt> protocols = new ArrayList<ProtocolTransferObjectExt>();
		ProtocolTransferObjectExt protocol = new ProtocolTransferObjectExt();
		protocol.setPreferredName("NCIP");
		protocol.setIdseq("8675-SDLK-IWES-SDSD");
		protocols.add(protocol);
		return protocols;
	}
	
	
	
	@Test
	public void testQuestions() {
		initFormDescriptor();
		initAlsData();
		ConverterFormService converterService = new ConverterFormService();
		FormDescriptor formDescActual = converterService.convertAlsToCadsr(alsForm, alsData, populateFormDescriptor());
		QuestionDescriptor questActual = formDescActual.getModules().get(0).getQuestions().get(0);
		String[] questActArr =  new String[3];
		questActArr[0] = questActual.getCdePublicId();
		questActArr[1] = questActual.getCdeVersion();
		questActArr[2] = questActual.getQuestionText();

		QuestionDescriptor questExp = formDescExpected.getModules().get(0).getQuestions().get(0);		
		String[] questExpArr =  new String[3];
		questExpArr[0] = questExp.getCdePublicId();
		questExpArr[1] = questExp.getCdeVersion();
		questExpArr[2] = questExp.getQuestionText();		

		assertArrayEquals(questExpArr, questActArr);
	}
	
	@Test
	public void testModules() {
		initFormDescriptor();
		initAlsData();
		FormDescriptor formDescActual = converterService.convertAlsToCadsr(alsForm, alsData, populateFormDescriptor());
		ModuleDescriptor moduleActual = formDescActual.getModules().get(0);
		String[] moduleActArr =  new String[2];
		moduleActArr[0] = moduleActual.getLongName();
		moduleActArr[1] = moduleActual.getPreferredDefinition();
		
		ModuleDescriptor moduleExp = formDescExpected.getModules().get(0);
		String[] moduleExpArr =  new String[2];
		moduleExpArr[0] = moduleExp.getLongName();
		moduleExpArr[1] = moduleExp.getPreferredDefinition();
		
		assertArrayEquals(moduleExpArr, moduleActArr);
		
	}	
	
	
	@Test
	public void testValidValues() {
		initFormDescriptor();
		initAlsData();
		FormDescriptor formDescActual = converterService.convertAlsToCadsr(alsForm, alsData, populateFormDescriptor());
		ValidValue vvActual = formDescActual.getModules().get(0).getQuestions().get(0).getValidValues().get(0);
		ValidValue vvExpected = formDescExpected.getModules().get(0).getQuestions().get(0).getValidValues().get(0);
		String[] vvActArr =  new String[2];
		vvActArr[0] = vvActual.getValue();
		vvActArr[1] = vvActual.getMeaningText();
		String[] vvExpArr =  new String[2];
		vvExpArr[0] = vvExpected.getValue();
		vvExpArr[1] = vvExpected.getMeaningText();
		assertArrayEquals(vvExpArr, vvActArr);
	}	
	
	@Test
	public void testProtocols() {
		initFormDescriptor();
		initAlsData();
		FormDescriptor formDescActual = converterService.convertAlsToCadsr(alsForm, alsData, populateFormDescriptor());
		ProtocolTransferObjectExt actualProtocol = formDescActual.getProtocols().get(0);
		ProtocolTransferObjectExt expectedProtocol = formDescExpected.getProtocols().get(0);
		String[] expProtoArr = new String[2];
		expProtoArr[0] = expectedProtocol.getPreferredName();
		expProtoArr[1] = expectedProtocol.getIdseq();
		String[] actProtoArr = new String[2];
		actProtoArr[0] = actualProtocol.getPreferredName();
		actProtoArr[1] = actualProtocol.getIdseq();
		assertArrayEquals(expProtoArr, actProtoArr);
	}	
	
	
}
