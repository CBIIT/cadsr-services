package gov.nih.nci.cadsr.microservices;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import gov.nih.nci.cadsr.data.ALSCrfDraft;
import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.ALSUnitDictionaryEntry;
import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.report.CdeFormInfo;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;

public class ReportGeneratorFeedIT {
	
	ReportGeneratorFeed repGenFeed = new ReportGeneratorFeed(); 
	Map<String, ALSDataDictionaryEntry> ddeMap;
	Map<String, List<CdeFormInfo>> cdeFormInfoMap;
	List<CdeFormInfo> cdeFormInfoList;
	Map<CdeFormInfo, CdeDetails> formCdeDetailsMap;
	
	String idseq;
	List<String> selForms = new ArrayList<String>();
	Boolean checkUom;
	Boolean checkStdCrfCde;
	Boolean displayExceptionDetails;
	String cdePublicId;
	String cdeVersion;
	String unsplit_cdeIdVersion;
	
	@Before
	public void init() {
		idseq = "9BCD3278-84DB-423E-8905-1A08F3E1CFAC";
		cdePublicId = "3298542";
		cdeVersion = "3.2";
		unsplit_cdeIdVersion = "PID2003316_V4_0";
		selForms = Arrays.asList("Patient Eligibility", "Enrollment");
		checkUom = false;
		checkStdCrfCde = false;
		displayExceptionDetails = false;
		
		ddeMap = new HashMap<String, ALSDataDictionaryEntry>();
		//cdeFormInfoMap = buildFormCdeList(alsData, selForms);
		formCdeDetailsMap = new HashMap<>();	
		
		ALSDataDictionaryEntry dde = new ALSDataDictionaryEntry();
		List<String> ordinal = new ArrayList<String>();
		ordinal.add("1");
		ordinal.add("2");
		List<String> cd = new ArrayList<String>();
		cd.add("F");
		cd.add("M");
		List<String> uds = new ArrayList<String>();
		uds.add("Female");
		uds.add("Male");
		dde.setCodedData(cd);
		dde.setOrdinal(ordinal);
		dde.setUserDataString(uds);
		dde.setDataDictionaryName("DDE1");
		ddeMap.put("DDE1", dde);		
	}	
	
	public ALSData buildAlsData() {
		ALSData alsData = new ALSData();
		alsData.setReportOwner("TEST USER");
		alsData.setFileName("TEST ALS FILE");
		alsData.setReportDate("02/19/2020");		
		alsData = getCrfDraft(alsData);
		alsData = getForms(alsData);
		alsData = getFields(alsData);
		alsData = getDataDictionaryEntries(alsData);		
		alsData = getUnitDictionaryEntries(alsData);
		return alsData;
	}
	
	public ALSData getCrfDraft(ALSData alsData) {
		ALSCrfDraft crfDraft = new ALSCrfDraft();
		crfDraft.setDraftName("20-FEB-2019 MAM");
		crfDraft.setPrimaryFormOid("SUBJECT_ENROLLMENT");
		crfDraft.setProjectName("10231");
		alsData.setCrfDraft(crfDraft);
		return alsData;
		
	}
	
	public ALSData getForms(ALSData alsData) {
		List<ALSForm> forms = new ArrayList<ALSForm>();
		ALSForm testFormA = new ALSForm();
		testFormA.setFormOid("PATIENT_ELIGIBILITY");
		testFormA.setDraftFormName("Patient Eligibility");
		testFormA.setOrdinal("1");
		forms.add(testFormA);
		ALSForm testFormB = new ALSForm();
		testFormB.setFormOid("ENROLLMENT");
		testFormB.setDraftFormName("Enrollment");
		testFormB.setOrdinal("2");
		forms.add(testFormB);
		alsData.setForms(forms);
		return alsData;
		
	}
	
	public ALSData getFields(ALSData alsData) {
		List<ALSField> fields = new ArrayList<ALSField>();
		ALSField testFieldA = new ALSField();
		ALSField testFieldB = new ALSField();
		ALSField testFieldC = new ALSField();
		testFieldA = getSingleField(testFieldA, "1", "PATIENT_ELIGIBILITY", "YES_NO_TEXT_CODE_INDICA_PID2017337_V1_0", "TimeZone", 
				"FORM_OID", "PID4379511_V1_0", "PID4379511_V1_0", "4379511", "1.0", "$200", "LongText", 
				"FORM_OID", "(12 characters)");
		testFieldB = getSingleField(testFieldB, "2", "PATIENT_ELIGIBILITY", "YES_NO_TEXT_CODE_INDICA_PID2017337_V1_0", "TimeZone", 
				"PROT_CHKLST_NUM", "", "Checklist Number PID2003387_V3_0", "2003387", "3.0", "$2", "Text", 
				"Checklist #", "(24 characters)");
		testFieldC = getSingleField(testFieldC, "3", "PATIENT_ELIGIBILITY", "YES_NO_TEXT_CODE_INDICA_PID2017337_V1_0", "TimeZone", 
				"PROT_VER_DT", "PID4379511_V1_0", "Protocol Version Date PID2188522_V1_0", "2188522", "1.0", "dd MMM yyyy", "DateTime", 
				"Protocol Date", "(8 characters)");

		fields.add(testFieldA);
		fields.add(testFieldB);
		fields.add(testFieldC);
		alsData.setFields(fields);
		return alsData;		
	}
	
	public ALSField getSingleField(ALSField testField, String ordinal, String formOid, String ddeName, String udeName, 
			String fieldOid, String defaultValue, String draftFieldName, String publicId, String version, String dataFormat, String controlType, 
			String preText, String fixedUnit) {
		testField.setOrdinal(ordinal);
		testField.setFormOid(formOid);
		testField.setDataDictionaryName(ddeName);
		testField.setUnitDictionaryName(udeName);
		testField.setFieldOid(fieldOid);
		testField.setDefaultValue(defaultValue);
		testField.setDraftFieldName(draftFieldName);
		testField.setFormPublicId(publicId);
		testField.setVersion(version);
		testField.setDataFormat(dataFormat);
		testField.setControlType(controlType);
		testField.setPreText(preText);
		testField.setFixedUnit(fixedUnit);
		return testField;		
	}
	
	public ALSData getDataDictionaryEntries(ALSData alsData) {
		Map<String, ALSDataDictionaryEntry> ddeMap = new HashMap<String, ALSDataDictionaryEntry>();
		ALSDataDictionaryEntry dde = new ALSDataDictionaryEntry();
		List<String> ordinal = new ArrayList<String>();
		List<String> cd = new ArrayList<String>();
		List<String> uds = new ArrayList<String>();		
		cd.add("N");
		cd.add("Y");
		uds.add("No");
		uds.add("Yes");
		ordinal.add("1");
		ordinal.add("2");
		dde.setCodedData(cd);
		dde.setOrdinal(ordinal);
		dde.setUserDataString(uds);
		dde.setDataDictionaryName("YES_NO_TEXT_CODE_INDICA_PID2017337_V1_0");		
		ddeMap.put(dde.getDataDictionaryName(), dde);
		alsData.setDataDictionaryEntries(ddeMap);
		return alsData;
		
	}	
	
	public ALSData getUnitDictionaryEntries(ALSData alsData) {
		List<ALSUnitDictionaryEntry> udeList = new ArrayList<ALSUnitDictionaryEntry>();
		ALSUnitDictionaryEntry ude = new ALSUnitDictionaryEntry();
		ude.setUnitDictionaryName("TimeZone");
		ude.setCodedUnit("EST");
		ude.setOrdinal("1");
		ude.setConstantA("1");
		ude.setConstantB("1");
		ude.setConstantC("0");
		ude.setConstantK("0");
		ude.setUnitString("EST");		
		udeList.add(ude); 
		alsData.setUnitDictionaryEntries(udeList);
		return alsData;
		
	}
	
	
	public CCCReport buildReportObject() {
		CCCReport cccReport = new CCCReport();
		List<CCCForm> formsList = new ArrayList<CCCForm>();
		CCCForm form = new CCCForm();		
		cccReport.setReportOwner("TEST USER");
		cccReport.setFileName("TEST ALS FILE");
		cccReport.setReportDate("02/19/2020");
		cccReport.setRaveProtocolName("10231");
		cccReport.setRaveProtocolNumber("SUBJECT_ENROLLMENT");
		cccReport.setTotalFormsCount(2);
		List<CCCQuestion> questionsList = new ArrayList<CCCQuestion>();
		CCCQuestion question = new CCCQuestion();
		question.setSequenceNumber(2);
		question.setFieldOrder("3");
		question.setRaveFormOId("PATIENT_ELIGIBILITY");
		question.setQuestionCongruencyStatus("WARNINGS");
		question.setMessage("No CDE provided : {protocol_header}.");
		question.setRaveFieldLabel("Note that the Protocol Date is the date of the version of the protocol that the patient's eligibility is based on.");
		questionsList.add(question);
		form.setQuestions(questionsList);
		form.setCountTotalQuestions(4);
		form.setTotalQuestionsChecked(4);
		form.setRaveFormOid("PATIENT_ELIGIBILITY");
		form.setCongruencyStatus("WARNINGS");
		formsList.add(form);
		CCCForm form2 = new CCCForm();
		form2.setCongruencyStatus("CONGRUENT");
		form2.setRaveFormOid("ENROLLMENT");
		form2.setQuestions(new ArrayList<CCCQuestion>());
		form2.setCountTotalQuestions(4);
		form2.setTotalQuestionsChecked(4);		
		formsList.add(form2);
		cccReport.setCccForms(formsList);
		cccReport.setTotalFormsCong(1);
		cccReport.setSelectedFormsCount(1);
		cccReport.setCountNrdsMissing(47);
		return cccReport;		
	}
	
	@Test
	public void testGetFinalReportData() {
		CCCReport expectedReport = buildReportObject();
		CCCReport actualReport = repGenFeed.getFinalReportData(idseq, buildAlsData(), selForms, checkUom, checkStdCrfCde, displayExceptionDetails);
		assertEquals(expectedReport.getReportOwner(), actualReport.getReportOwner());
		assertEquals(expectedReport.getReportDate(), actualReport.getReportDate());
		assertEquals(expectedReport.getFileName(), actualReport.getFileName());
		assertEquals(expectedReport.getCountQuestionsWithoutCde(), actualReport.getCountQuestionsWithoutCde());
		assertEquals(expectedReport.getCountNrdsCongruent(), actualReport.getCountNrdsCongruent());
		assertEquals(expectedReport.getCountNrdsWithErrors(), actualReport.getCountNrdsWithErrors());
		assertEquals(expectedReport.getCountNrdsWithWarnings(), actualReport.getCountNrdsWithWarnings());
		assertEquals(expectedReport.getCountNciCongruent(), actualReport.getCountNciCongruent());
		assertEquals(expectedReport.getCountNrdsMissing(), actualReport.getCountNrdsMissing());		
		assertEquals(expectedReport.getCountQuestionsChecked(), actualReport.getCountQuestionsChecked());		
		assertEquals(expectedReport.getTotalFormsCong(), actualReport.getTotalFormsCong());
		assertEquals(expectedReport.getSelectedFormsCount(), actualReport.getSelectedFormsCount());
	}	
	

}
