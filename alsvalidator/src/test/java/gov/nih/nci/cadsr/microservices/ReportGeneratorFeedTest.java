package gov.nih.nci.cadsr.microservices;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CdeMissing;
import gov.nih.nci.cadsr.data.CdeStdCrfData;
import gov.nih.nci.cadsr.data.NrdsCde;
import gov.nih.nci.cadsr.data.StandardCrfCde;
import gov.nih.nci.cadsr.report.CdeFormInfo;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.DataElement;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.DataElementDetails;

public class ReportGeneratorFeedTest {
	
	ReportGeneratorFeed repGenFeed = new ReportGeneratorFeed(); 
	ALSData alsData;
	CCCForm form = new CCCForm();
	List<NrdsCde> nrdsCdeList;
	List<NrdsCde> missingNciList;
	List<StandardCrfCde> matchingStdCrfCdeList;
	List<StandardCrfCde> standardCrfCdeList;
	List<CCCQuestion> questionsList;
	List<CCCQuestion> congQuestionsList;
	List<NrdsCde> missingNrdsCdesList;
	List<NrdsCde> reportCdeList;
	List<CdeMissing> missingCdashCdesList;
	List<CdeMissing> missingSdtmCdesList;	
	List<StandardCrfCde> missingStdCrfCdeList;
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
		selForms = Arrays.asList("Patient Eligibility");
		checkUom = false;
		checkStdCrfCde = false;
		displayExceptionDetails = false;
		
		nrdsCdeList = new ArrayList<NrdsCde>();
		missingNciList = new ArrayList<NrdsCde>();
		matchingStdCrfCdeList = new ArrayList<StandardCrfCde>();
		standardCrfCdeList = new ArrayList<StandardCrfCde>();
		questionsList = new ArrayList<CCCQuestion>();
		congQuestionsList = new ArrayList<CCCQuestion>();
		missingNrdsCdesList = new ArrayList<NrdsCde>();
		reportCdeList = new ArrayList<NrdsCde>();
		missingCdashCdesList = new ArrayList<>();
		missingSdtmCdesList = new ArrayList<>();		
		missingStdCrfCdeList = new ArrayList<StandardCrfCde>();		
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
	
	@Test
	public void testAssignCdeIdVersionToQuestion() {
		String[] expectedIdversionArr = {"2003316", "4.0"};
		String[] actualIdVer = new String[2];
		CCCQuestion question = new CCCQuestion();
		question = ReportGeneratorFeed.assignCdeIdVersionToQuestion(question, unsplit_cdeIdVersion);
		actualIdVer[0] = question.getCdePublicId();
		actualIdVer[1] = question.getCdeVersion();
		assertArrayEquals(expectedIdversionArr, actualIdVer);
	}	
	
	@Test
	public void testParsePublicId() {
		Integer expectedId = 3298542;
		Integer actualId = ReportGeneratorFeed.parsePublicId(cdePublicId);
		assertEquals(expectedId, actualId);
	}
	
	@Test
	public void testParseVersion() {
		Float expectedVer = Float.valueOf("3.2");
		Float actualVer = ReportGeneratorFeed.parseVersion(cdeVersion);
		assertEquals(expectedVer, actualVer);
	}	

	@Test
	public void testFindFormNameByFormOid() {
		List<ALSForm> formsList = new ArrayList<ALSForm>();
		ALSForm alsForm = new ALSForm();
		alsForm.setFormOid("Form1_OID");
		alsForm.setDraftFormName("Form1");
		formsList.add(alsForm);		
		alsForm = new ALSForm();
		alsForm.setFormOid("Form2_OID");
		alsForm.setDraftFormName("Form2");		
		formsList.add(alsForm);
		String expectedFormName = "Form1";
		String actualFormName = ReportGeneratorFeed.findFormNameByFormOid("Form1_OID", formsList);
		assertEquals(expectedFormName, actualFormName);
	}	
	
	@Test
	public void testBuildCodedData() {
		String[] expectedCD = {"F", "M", "Female", "Male"};
		CCCQuestion question = new CCCQuestion();
		ALSField alsField = new ALSField();
		alsField.setDataDictionaryName("DDE1");
		question = ReportGeneratorFeed.buildCodedData(alsField, question, ddeMap);
		String[] actualCD = new String[4];
		actualCD[0] = question.getRaveCodedData().get(0);
		actualCD[1] = question.getRaveCodedData().get(1);
		actualCD[2] = question.getRaveUserString().get(0);
		actualCD[3] = question.getRaveUserString().get(1);		
		assertArrayEquals(expectedCD, actualCD);
	}
	
	@Test
	public void testUpdateNciCategoryNrds() {
		CdeDetails cdeDetails = new CdeDetails();
		String expectedCategory = "NRDS";
		CCCQuestion question = new CCCQuestion();
		CdeStdCrfData cdeStdCrf = new CdeStdCrfData();
		cdeStdCrf.setNciCategory("NRDS");
		question = ReportGeneratorFeed.updateNciCategory(true, cdeStdCrf, question, cdeDetails);
		String actualCategory = question.getNciCategory();
		assertEquals(expectedCategory, actualCategory);
	}
	
	@Test
	public void testUpdateNciCategoryMand() {
		CdeDetails cdeDetails = new CdeDetails();
		String expectedCategory = "Mandatory";
		CCCQuestion question = new CCCQuestion();
		CdeStdCrfData cdeStdCrf = new CdeStdCrfData();
		cdeStdCrf.setNciCategory("Mandatory");
		question = ReportGeneratorFeed.updateNciCategory(true, cdeStdCrf, question, cdeDetails);
		String actualCategory = question.getNciCategory();
		assertEquals(expectedCategory, actualCategory);
	}
	
	@Test
	public void testUpdateNciCategoryCond() {
		CdeDetails cdeDetails = new CdeDetails();
		String expectedCategory = "Conditional";
		CCCQuestion question = new CCCQuestion();
		CdeStdCrfData cdeStdCrf = new CdeStdCrfData();
		cdeStdCrf.setNciCategory("Conditional");
		question = ReportGeneratorFeed.updateNciCategory(true, cdeStdCrf, question, cdeDetails);
		String actualCategory = question.getNciCategory();
		assertEquals(expectedCategory, actualCategory);
	}
	
	@Test
	public void testUpdateNciCategoryOpt() {
		CdeDetails cdeDetails = new CdeDetails();
		String expectedCategory = "Optional";
		CCCQuestion question = new CCCQuestion();
		CdeStdCrfData cdeStdCrf = new CdeStdCrfData();
		cdeStdCrf.setNciCategory("Optional");
		question = ReportGeneratorFeed.updateNciCategory(true, cdeStdCrf, question, cdeDetails);
		String actualCategory = question.getNciCategory();
		assertEquals(expectedCategory, actualCategory);
	}	
	
	//@Test
	public void testgetNrdsCdeList() {
		CdeDetails cdeDetails = new CdeDetails();

		DataElement de = new DataElement();
		DataElementDetails dde = new DataElementDetails();
		dde.setLongName("Test data");
		de.setDataElementDetails(dde);
		cdeDetails.setDataElement(de);
		CCCQuestion question = new CCCQuestion();		
		question.setRaveFormOId("AB12345");
		question.setCdePublicId("325454");
		question.setCdeVersion("2.1");
		question.setRaveFieldLabel("Test Label");
		question.setFieldOrder("1");
		question.setQuestionCongruencyStatus("ERROR");
		question.setMessage("Error messasge 1");
		question.setNciCategory("NRDS");
		
		
		List<NrdsCde> expectedList = new ArrayList<NrdsCde>();
		//expectedList.add(new NrdsCde());
		List<NrdsCde> actualList = ReportGeneratorFeed.getNrdsCdeList(true, question, cdeDetails, nrdsCdeList);
		
		
		dde.setLongName("Test data 2");
		de.setDataElementDetails(dde);
		cdeDetails.setDataElement(de);
		
		question.setRaveFormOId("AB12345 2");
		question.setCdePublicId("2 325454");
		question.setCdeVersion("3.1");
		question.setRaveFieldLabel("2 Test Label");
		question.setFieldOrder("12 1");
		question.setQuestionCongruencyStatus("WARN");
		question.setMessage("Error messasge 2");
		question.setNciCategory("StdCrf");		
		
		
		
		expectedList = ReportGeneratorFeed.getNrdsCdeList(true, question, cdeDetails, nrdsCdeList);
		
		//assertEquals(expectedList.size(), actualList.size());
		//expectedList.add(actualList.get(0));
		//assertSame(expectedList.get(0), actualList.get(0));
	}
	
	
//	@Test
	public void testGetStdCrfCdeList() {
		// TODO
	}
	
//	@Test
	public void testCalculateCdiscReportTotals() {
		// TODO
	}
	
//	@Test
	public void testQuestionBelongsTo() {
		// TODO
	}
	
	
	@Test
	public void testBuildNrdsCde() {
		NrdsCde expectedNrdsCde = new NrdsCde();
		expectedNrdsCde.setRaveFormOid("AB12345");
		expectedNrdsCde.setCdeIdVersion("325454v2.1");
		expectedNrdsCde.setCdeName("PERSON");
		expectedNrdsCde.setRaveFieldLabel("Test Label");
		expectedNrdsCde.setRaveFieldOrder("1");
		expectedNrdsCde.setResult("ERROR");
		expectedNrdsCde.setMessage("Error message 1");
		expectedNrdsCde.setType("NRDS");
		
		CCCQuestion question = getQuestionPopulated();
		question.setNciCategory("NRDS");
		NrdsCde actualNrdsCde = ReportGeneratorFeed.buildNrdsCde(question, "PERSON");
		
		assertEquals(expectedNrdsCde.getMessage(), actualNrdsCde.getMessage());
		assertEquals(expectedNrdsCde.getRaveFormOid(), actualNrdsCde.getRaveFormOid());
		assertEquals(expectedNrdsCde.getCdeIdVersion(), actualNrdsCde.getCdeIdVersion());
		assertEquals(expectedNrdsCde.getCdeName(), actualNrdsCde.getCdeName());
		assertEquals(expectedNrdsCde.getRaveFieldLabel(), actualNrdsCde.getRaveFieldLabel());
		assertEquals(expectedNrdsCde.getRaveFieldOrder(), actualNrdsCde.getRaveFieldOrder());
		assertEquals(expectedNrdsCde.getResult(), actualNrdsCde.getResult());
		assertEquals(expectedNrdsCde.getType(), actualNrdsCde.getType());
	}	
	
	
	@Test
	public void testBuildStdCrfCde() {
		StandardCrfCde expectedStdCrfCde = new StandardCrfCde();
		expectedStdCrfCde.setRaveFormOid("AB12345");
		expectedStdCrfCde.setCdeIdVersion("325454v2.1");
		expectedStdCrfCde.setCdeName("Patient");
		expectedStdCrfCde.setRaveFieldLabel("Test Label");
		expectedStdCrfCde.setRaveFieldOrder("1");
		expectedStdCrfCde.setResult("ERROR");
		expectedStdCrfCde.setMessage("Error message 1");
		
		CCCQuestion question = getQuestionPopulated();
		StandardCrfCde actualStdCrfCde = ReportGeneratorFeed.buildStdCrfCde(question, "Patient");
		
		assertEquals(expectedStdCrfCde.getMessage(), actualStdCrfCde.getMessage());
		assertEquals(expectedStdCrfCde.getRaveFormOid(), actualStdCrfCde.getRaveFormOid());
		assertEquals(expectedStdCrfCde.getCdeIdVersion(), actualStdCrfCde.getCdeIdVersion());
		assertEquals(expectedStdCrfCde.getCdeName(), actualStdCrfCde.getCdeName());
		assertEquals(expectedStdCrfCde.getRaveFieldLabel(), actualStdCrfCde.getRaveFieldLabel());
		assertEquals(expectedStdCrfCde.getRaveFieldOrder(), actualStdCrfCde.getRaveFieldOrder());
		assertEquals(expectedStdCrfCde.getResult(), actualStdCrfCde.getResult());
		
	}
	
//	@Test
	public void testgetMissingNciCdeList() {
		// TODO
	}
	
//	@Test
	public void testgetStdManCrfCdeList() {
		// TODO
	}
	
//	@Test
	public void testaddToReportCdeList() {
		// TODO
	}
	
//	@Test
	public void testgetStdCrfCdeList() {
		// TODO
	}	
	
	@Test
	public void testPickFieldErrors() {
		ALSField field = new ALSField();
		field.setFormOid("NEW FORM");
		List<ALSError> errors = new ArrayList<ALSError>();
		ALSError alsError = new ALSError();
		alsError.setFieldOid("SYST_BP_VAL");
		field.setFieldOid("SYST_BP_VAL");
		alsError.setErrorSeverity("WARNING");
		alsError.setErrorDesc("MISSING FIELD");
		alsError.setFormOid("NEW FORM");
		errors.add(alsError);
		Map<String, String> errorMap = ReportGeneratorFeed.pickFieldErrors(field, errors);
		String expectedMsg = "MISSING FIELD";
		String actualMsg = errorMap.get("WARNINGS").toString();
		assertEquals(expectedMsg, actualMsg);
	}
	
	public CCCQuestion getQuestionPopulated() {
		CCCQuestion question = new CCCQuestion();		
		question.setRaveFormOId("AB12345");
		question.setCdePublicId("325454");
		question.setCdeVersion("2.1");
		question.setRaveFieldLabel("Test Label");
		question.setFieldOrder("1");
		question.setQuestionCongruencyStatus("ERROR");
		question.setMessage("Error message 1");
		question.setNciCategory("NRDS");		
		return question;
	}
	

}
