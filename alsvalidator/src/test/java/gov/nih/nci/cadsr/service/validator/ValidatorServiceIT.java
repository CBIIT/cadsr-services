package gov.nih.nci.cadsr.service.validator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.DataElement;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.DataElementDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.OtherVersion;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.ReferenceDocument;

/**
 * Integration Tests for ValidateService
 *
 */
public class ValidatorServiceIT {
	
	private static final String retiredArchivedStatus = "RETIRED ARCHIVED";
	private static final String retiredPhasedOutStatus = "RETIRED PHASED OUT";
	private static final String retiredWithdrawnStatus = "RETIRED WITHDRAWN";
	private static final String errMsg1 = "CDE has been retired.";
	private static final String errMsg2 = "Newer version of CDE exists: {%.1f}.";	
	private static final String errMsg3 = "Value domain Max Length too short. PVs MaxLength is {%d} , caDSR MaxLength is {%d}.";
	CCCQuestion question;
	CdeDetails cdeDetails;
	DataElement de;
	DataElementDetails deDetails; 	
	ReferenceDocument rd;

	public void init() {
		question = new CCCQuestion();
		cdeDetails = new CdeDetails();
		de = new DataElement();
		deDetails = new DataElementDetails();
	}

	
	/**
	 * Checking if the given CDE is one of the three Retired Statuses
	 * @param errMsg
	 * @param status
	 */
	public void invokeCheckCdeRetiredStatus(String errMsg, String status) {
		deDetails.setWorkflowStatus(status);
		de.setDataElementDetails(deDetails);
		cdeDetails.setDataElement(de);
		CCCQuestion actualResult = ValidatorService.checkCdeRetired(cdeDetails, question);
		assertEquals(errMsg, actualResult.getMessage());
	}	
		
	/**
	 * Testing for CDE RETIRED ARCHIVED status
	 */	
	@Test
	public void testCheckCdeRetiredArchived () {
		init();
		invokeCheckCdeRetiredStatus(errMsg1, retiredArchivedStatus);
	}	
	
	/**
	 * Testing for CDE RETIRED PHASED OUT status
	 */	
	@Test
	public void testCheckCdeRetiredPhasedOut () {
		init();
		invokeCheckCdeRetiredStatus(errMsg1, retiredPhasedOutStatus);
	}		
	
	/**
	 * Testing for CDE RETIRED WITHDRAWN status
	 */		
	@Test
	public void testCheckCdeRetiredWithdrawn () {
		init();
		invokeCheckCdeRetiredStatus(errMsg1, retiredWithdrawnStatus);
	}
	
	/**
	 * Testing comparison of CDE versions - where the ALS doesn't have the latest version of the CDE
	 */				
	@Test
	public void testCheckCdeVersions1() {
		init();
		Float latestVersion = new Float("5.0");
		question.setCdeVersion("4.0");
		OtherVersion otherVersion = new OtherVersion();
		otherVersion.setVersion(latestVersion);
		List<OtherVersion> otherVersions = new ArrayList<OtherVersion>();
		otherVersions.add(otherVersion);
		de.setOtherVersions(otherVersions);
		cdeDetails.setDataElement(de);
		CCCQuestion actualResult = ValidatorService.checkCdeVersions(cdeDetails, question);
		assertEquals(String.format(errMsg2, latestVersion), actualResult.getMessage());
	}
	
	/**
	 * Testing comparison of CDE versions - where the ALS has the latest version of the CDE
	 */	
	@Test
	public void testCheckCdeVersions2() {
		init();
		Float latestVersion = new Float("1.0");
		question.setCdeVersion("1.0");
		OtherVersion otherVersion = new OtherVersion();
		otherVersion.setVersion(latestVersion);
		List<OtherVersion> otherVersions = new ArrayList<OtherVersion>();
		otherVersions.add(otherVersion);
		de.setOtherVersions(otherVersions);
		cdeDetails.setDataElement(de);
		CCCQuestion actualResult = ValidatorService.checkCdeVersions(cdeDetails, question);
		assertEquals(null, actualResult.getMessage());
	}
	
	/**
 	 *	Compare to ALS.Fields.FixedUnit, and compare to Value Domain PermissibleValue.Value.  
 	 *	If the caDSR VD MaximumLengthNumber is less than the longest PermissibleValue.Value, 
 	 *	report "caDSR Max Length too short. "PVs MaxLength X, caDSR MaxLength X" 
	 * 
	 * Testing checking of CDE max length - PV length is more than VD max length
	 */		
	@Test
	public void testCheckCdeMaxLength1() {
		init();
		List<Object> errorVal = new ArrayList<Object>();
		errorVal.add(10);
		errorVal.add(5);
		String expectedMessage = String.format(errMsg3, errorVal.toArray());
		CCCQuestion actualResult = ValidatorService.checkCdeMaxLength(question, 10, 5, 10);
		assertEquals (expectedMessage, actualResult.getMessage());
	}		
	
	/**
 	 *	Compare to ALS.Fields.FixedUnit, and compare to Value Domain PermissibleValue.Value.  
 	 *	If the caDSR VD MaximumLengthNumber is less than the longest PermissibleValue.Value, 
 	 *	report "caDSR Max Length too short. "PVs MaxLength X, caDSR MaxLength X" 
	 * 
	 * Testing checking of CDE max length - PV length is within range of VD max length
	 */		
	@Test
	public void testCheckCdeMaxLength2() {
		init();
		String expectedMessage = null;
		CCCQuestion actualResult = ValidatorService.checkCdeMaxLength(question, 5, 10, 10);
		assertEquals (expectedMessage, actualResult.getMessage());
	}
	
	/**
	 * Setting up mock data for testing Rave Field Result
	 */
	public void setupRaveFieldResultData() {
		List<ReferenceDocument> rdList = new ArrayList<ReferenceDocument>();
		rdList.add(setupDocText("Test1", "Preferred Question Text"));
		rdList.add(setupDocText("Test2", "Alternate Question Text"));
		de.setQuestionTextReferenceDocuments(rdList);
		cdeDetails.setDataElement(de);
	}

	/**
	 * adding mock ReferenceDocument based on the inputs
	 */	
	public ReferenceDocument setupDocText(String docText, String docType) {
		rd = new ReferenceDocument();
		rd.setDocumentText(docText);
		rd.setDocumentType(docType);
		return rd;
	}		
	
	/**
	 * Testing the Rave Field Label Result deducing method - MATCH scenario
	 */
	
	@Test
	public void testSetRaveFieldLabelResultMatch() {
		init();
		setupRaveFieldResultData();
		question.setRaveFieldLabel("Test2");
		String expectedResult = "MATCH";
		CCCQuestion actualResult = ValidatorService.setRaveFieldLabelResult(cdeDetails, question);
		assertEquals(expectedResult, actualResult.getRaveFieldLabelResult());
	}
	
	/**
	 * Testing the Rave Field Label Result deducing method - NON-MATCH scenario
	 */	
	@Test
	public void testSetRaveFieldLabelResultError() {
		init();
		setupRaveFieldResultData();
		question.setRaveFieldLabel("Not-Test");
		String expectedResult = "ERROR";
		CCCQuestion actualResult = ValidatorService.setRaveFieldLabelResult(cdeDetails, question);
		assertEquals(expectedResult, actualResult.getRaveFieldLabelResult());
	}	
	
	/**
	 * Testing the setting of CDE permitted choices which will be set into the question
	 */		
	@Test
	public void testSetRaveFieldLabelResultCdePermChoices() {
		init();
		setupRaveFieldResultData();
		String expectedResult = "Test1|Test2";
		CCCQuestion actualResult = ValidatorService.setRaveFieldLabelResult(cdeDetails, question);
		assertEquals(expectedResult, actualResult.getCdePermitQuestionTextChoices());
	}

	/*
	 * The following Control Type checker result tests implement the below requirement.
	 *  
	 *  Compare Rave ControlType to caDSR Value Domain Type
	If caDSR VD is non-enumerated, and the Rave ControlType is "Text", it is valid.  
	If Value Domain is non-enumerated and ControlType is not "Text", then check the Rave Datatype. 
	If Rave Datatype matches caDSR Value Domain datatype, then result is "Match" otherwise 
	it's an error. (we will provide the team with a list of the mappings between the Rave Datatypes 
	and caDSR datatypes ,the names are not the same). */ 	
	/**
	 * Control Type Result - MATCH 
	 */
	@Test
	public void testSetRaveControlTypeResultMatch() {
		init();
		String expectedresult = "MATCH";
		question.setRaveControlType("TEXT");
		CCCQuestion actualResult = ValidatorService.setRaveControlTypeResult("N", "CHARACTER", "$12", question);
		assertEquals(expectedresult, actualResult.getControlTypeResult());
	}
	
	/**
	 * Control Type Result - ERROR 
	 */	
	@Test
	public void testSetRaveControlTypeResultError() {
		init();
		String expectedresult = "ERROR";
		question.setRaveControlType("LONGTEXT");
		CCCQuestion actualResult = ValidatorService.setRaveControlTypeResult("E", "NUMBER", "50", question);
		assertEquals(expectedresult, actualResult.getControlTypeResult());
	}	

	/* 
	 * The following PV checker result tests implement the below requirement.
	 *  
	 * Compare to PV.ValueMeaning.longName and the matched PV.Value. 
	 The UserDataString must match the PermissibleValues.ValueMeaning.LongName 
	 for the matched CodedData PV Value OR it can be the same as the matched 
	 PV Value (which means the UserDataString =  CodedData ). 
	 It is also compared to all the ValueMeaning Alternate Names.
	 Exceptions: If it does not match the PV Value MEaning or the PV for the CodedData, 
	 or one of the ValueMeaning Alternate Names, "ERROR" */	
	
	/**
	 * PV Checker result - MATCH
	 */
	@Test
	public void testSetPvCheckerResultMatch() {
		init();
		String expectedResult = "MATCH";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("LA10610-6");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("LA10610-6");
		pvVmList.add("Black or African American");
		pvVmList.add("BLACK OR AFRICAN AMERICAN");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}
	
	/**
	 * PV Checker result - ERROR (NON-MATCH)
	 */	
	@Test
	public void testSetPvCheckerResultError() {
		init();
		String expectedResult = "ERROR";
		question.getRaveCodedData().add("F");
		question.getRaveUserString().add("M");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Female");
		pvVmList.add("F");
		pvVmMap.put("F", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}	

	/**
	 * Creating Allowable CDE values as a concatenated string, in case of a NON-MATCH scenario
	 */	
	@Test
	public void testSetPvCheckerResultAllowableCdeValues() {
		init();
		String expectedResult = "No|Not a Serious Adverse Event|LA32-8|Exception|1 - No";
		question.getRaveCodedData().add("No");
		question.getRaveUserString().add("NO");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("No");
		pvVmList.add("Not a Serious Adverse Event");
		pvVmList.add("LA32-8");
		pvVmList.add("Exception");
		pvVmList.add("1 - No");				
		pvVmMap.put("No", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question);
		assertEquals(expectedResult, actualResult.getAllowableCdeTextChoices().get(0));
	}	
	
}
