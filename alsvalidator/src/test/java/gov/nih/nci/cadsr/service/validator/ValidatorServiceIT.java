package gov.nih.nci.cadsr.service.validator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
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

	@Before
	public void setup() {
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
		CCCQuestion actualResult = ValidatorService.checkCdeRetired(status, question);
		assertEquals(errMsg, actualResult.getMessage());
	}	
		
	/**
	 * Testing for CDE RETIRED ARCHIVED status
	 */	
	@Test
	public void testCheckCdeRetiredArchived () {		
		invokeCheckCdeRetiredStatus(errMsg1, retiredArchivedStatus);
	}	
	
	/**
	 * Testing for CDE RETIRED PHASED OUT status
	 */	
	@Test
	public void testCheckCdeRetiredPhasedOut () {		
		invokeCheckCdeRetiredStatus(errMsg1, retiredPhasedOutStatus);
	}		
	
	/**
	 * Testing for CDE RETIRED WITHDRAWN status
	 */		
	@Test
	public void testCheckCdeRetiredWithdrawn () {	
		invokeCheckCdeRetiredStatus(errMsg1, retiredWithdrawnStatus);
	}
	
	/**
	 * Testing comparison of CDE versions - where the ALS doesn't have the latest version of the CDE
	 */				
	@Test
	public void testCheckCdeVersions1() {		
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
		String expectedResult = "MATCH";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("LA10610-6");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("LA10610-6");
		pvVmList.add("Black or African American");
		pvVmList.add("BLACK OR AFRICAN AMERICAN");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}
	
	/**
	 * PV Checker result - MATCH - Mixed case (upper and lower)
	 */
	@Test
	public void testSetPvCheckerResultMatch1() {		
		String expectedResult = "MATCH";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("LA10610-6");
		pvVmList.add("Black or African American");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}
	
	/**
	 * PV Checker result - MATCH - Null CD vs Null PV
	 */	
	@Test
	public void testSetPvCheckerResultMatch2() {
		String expectedResult = "MATCH";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add(null);
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add(null);
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}
	
	/**
	 * PV Checker result - MATCH - ALL UPPER CASE
	 */	
	@Test
	public void testSetPvCheckerResultMatch3() {
		String expectedResult = "MATCH";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("HISPANIC");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("HISPANIC");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}	
	
	/**
	 * PV Checker result - MATCH - No strings to compare - <BLANK> for both strings
	 */	
	@Test
	public void testSetPvCheckerResultMatch4() {
		String expectedResult = "MATCH";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}		
	
	/**
	 * PV Checker result - MATCH - Null vs <BLANK>
	 */	
	@Test
	public void testSetPvCheckerResultMatch5() {
		String expectedResult = "MATCH";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add(null);
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}			
	
	
	/**
	 * PV Checker result - MATCH - ALL LOWER CASE
	 */	
	@Test
	public void testSetPvCheckerResultMatch6() {
		String expectedResult = "MATCH";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("asian");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("asian");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}		
	
	/**
	 * PV Checker result - WARNING - Ignoring case differences
	 */	
	@Test
	public void testSetPvCheckerResultCaseInsensitiveWarn1() {
		String expectedResult = "WARNING";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("BLACK OR AFRICAN AMERICAN");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}	
	
	/**
	 * PV Checker result - WARNING - Ignoring case differences
	 */	
	@Test
	public void testSetPvCheckerResultCaseInsensitiveWarn2() {
		String expectedResult = "WARNING";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("BLACK OR African American");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}	
	
	
	/**
	 * PV Checker result - WARNING - Ignoring punctuation differences
	 */	
	@Test
	public void testSetPvCheckerResultIgnorePunctWarn1() {
		String expectedResult = "WARNING";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Black or African American,");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}		
	
	/**
	 * PV Checker result - WARNING - Ignoring punctuation differences
	 */	
	@Test
	public void testSetPvCheckerResultIgnorePunctWarn2() {
		String expectedResult = "WARNING";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Black- or African American");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}			
	
	/**
	 * PV Checker result - WARNING - Ignoring punctuation differences
	 */	
	@Test
	public void testSetPvCheckerResultIgnorePunctWarn3() {
		String expectedResult = "WARNING";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Black or African. American");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}		
	
	/**
	 * PV Checker result - WARNING - Ignoring punctuation differences
	 */	
	@Test
	public void testSetPvCheckerResultIgnorePunctWarn4() {
		String expectedResult = "WARNING";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Black or; African American");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}				
	
	/**
	 * PV Checker result - WARNING - Ignoring punctuation differences
	 */	
	@Test
	public void testSetPvCheckerResultIgnorePunctWarn5() {
		String expectedResult = "WARNING";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("?Black or African American");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}		
	
	/**
	 * PV Checker result - WARNING - Ignoring punctuation differences
	 */	
	@Test
	public void testSetPvCheckerResultIgnorePunctWarn6() {
		String expectedResult = "WARNING";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Black \"or African American");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}			
	
	/**
	 * PV Checker result - WARNING - Ignoring punctuation differences
	 */	
	@Test
	public void testSetPvCheckerResultIgnorePunctWarn7() {
		String expectedResult = "WARNING";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Black or African' American");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}		
	
	/**
	 * PV Checker result - WARNING - Ignoring punctuation differences
	 */	
	@Test
	public void testSetPvCheckerResultIgnorePunctWarn8() {
		String expectedResult = "WARNING";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("(Black) or African American");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}					
	
	
	/**
	 * PV Checker result - ERROR (NON-MATCH)
	 */		
	@Test
	public void testSetPvCheckerResulErrorNoCodedData() {
		String expectedResult = "ERROR";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("LA10610-6");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Not valid VM");
		pvVmMap.put("Unmatched VM", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}	
	/**
	 * PV Checker result - ERROR (NON-MATCH)
	 */	
	@Test
	public void testSetPvCheckerResultError1() {		
		String expectedResult = "ERROR";
		question.getRaveCodedData().add("F");
		question.getRaveUserString().add("M");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Female");
		pvVmList.add("F");
		pvVmMap.put("F", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}	
	
	/**
	 * PV Checker result - ERROR (NON-MATCH) - Null user data string vs Non-null pv list
	 */	
	@Test
	public void testSetPvCheckerResultError2() {		
		String expectedResult = "ERROR";
		question.getRaveCodedData().add("F");
		question.getRaveUserString().add(null);
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Female");
		pvVmList.add("F");
		pvVmMap.put("F", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}	
	
	/**
	 * PV Checker result - ERROR (NON-MATCH) - Null pv list vs Non-null user data string
	 */	
	@Test
	public void testSetPvCheckerResultError3() {		
		String expectedResult = "ERROR";
		question.getRaveCodedData().add("F");
		question.getRaveUserString().add("M");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add(null);
		pvVmMap.put("F", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}		
	
	/**
	 * PV Checker result - ERROR (NON-MATCH) - <BLANK> pv list against user data string
	 */	
	@Test
	public void testSetPvCheckerResultError4() {		
		String expectedResult = "ERROR";
		question.getRaveCodedData().add("F");
		question.getRaveUserString().add("M");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("");
		pvVmMap.put("F", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}		
	
	/**
	 * PV Checker result - ERROR (NON-MATCH) - <BLANK> user data string against pv list 
	 */	
	@Test
	public void testSetPvCheckerResultError5() {		
		String expectedResult = "ERROR";
		question.getRaveCodedData().add("F");
		question.getRaveUserString().add("");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("M");
		pvVmMap.put("F", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}			
	
	
	/**
	 * PV Checker result - ERROR - Excluded punctuation mismatch
	 */	
	@Test
	public void testSetPvCheckerResultError6() {
		String expectedResult = "ERROR";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("{Black or African American");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}						
	
	/**
	 * PV Checker result - ERROR - Excluded punctuation mismatch
	 */	
	@Test
	public void testSetPvCheckerResultError7() {
		String expectedResult = "ERROR";
		question.getRaveCodedData().add("LA10610-6");
		question.getRaveUserString().add("Black or |African American");
		Map pvVmMap = new HashMap<String, List<String>>();
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Black or African American");
		pvVmMap.put("LA10610-6", pvVmList);
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, false);
		assertEquals(expectedResult, actualResult.getPvResults().get(0));
	}							
	

	/**
	 * Creating Allowable CDE values as a concatenated string, in case of a NON-MATCH scenario
	 */	
	@Test
	public void testSetPvCheckerResultAllowableCdeValues() {		
		String expectedResult = "1 - No|Exception|LA32-8|No|Not a Serious Adverse Event";
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
		CCCQuestion actualResult = ValidatorService.setPvCheckerResult(pvVmMap, question, true);
		assertEquals(expectedResult, actualResult.getAllowableCdeTextChoices().get(0));
	}	
	
	/*
	 * The following tests verify the Coded Data Checker result implementing the below requirement.
	 * 
	 * Compare each CodedData value to all of the Value Domain's PermissibleValue.value
		Exceptions: If it does not match one of the CDEs PV Value, "ERROR"
	 * 
	 * */
	
	
	/**
	 *  Test coded data checker result - Mixed case Match
	 */
	@Test
	public void testSetCodedDataCheckerResultMatch1() {		
		String expectedResult = "MATCH";
		List codedDataList = Arrays.asList("Fresh Tissue", "Blood", "Bone Marrow", "Buccal Cell Sample");
		question.setRaveCodedData(codedDataList);
		List pvList = Arrays.asList("Fresh Tissue", "Frozen Tissue");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}
	
	/**
	 *  Test coded data checker result - <BLANK> coded data vs <BLANK> pv
	 */
	@Test
	public void testSetCodedDataCheckerResultMatch2() {		
		String expectedResult = "MATCH";
		List codedDataList = Arrays.asList("");
		question.setRaveCodedData(codedDataList);
		List pvList = Arrays.asList("");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}
	
	/**
	 *  Test coded data checker result - Match - All upper case
	 */
	@Test
	public void testSetCodedDataCheckerResultMatch3() {
		String expectedResult = "MATCH";
		List codedDataList = Arrays.asList("FRESH TISSUE", "Blood", "Bone Marrow", "Buccal Cell Sample");
		question.setRaveCodedData(codedDataList);
		List pvList = Arrays.asList("FRESH TISSUE");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}
	
	/**
	 *  Test coded data checker result - Match - All lower case
	 */
	@Test
	public void testSetCodedDataCheckerResultMatch4() {
		String expectedResult = "MATCH";
		List codedDataList = Arrays.asList("fresh tissue", "Blood", "Bone Marrow", "Buccal Cell Sample");
		question.setRaveCodedData(codedDataList);
		List pvList = Arrays.asList("fresh tissue");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}	
	
	
	/**
	 *  Test coded data checker result - <BLANK> coded data against Null PV
	 */
	@Test
	public void testSetCodedDataCheckerResultMatch5() {		
		String expectedResult = "MATCH";
		List codedDataList = Arrays.asList("");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add(null);
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}	
	
	/**
	 *  Test coded data checker result - Null coded data against Null PV
	 */
	@Test
	public void testSetCodedDataCheckerResultMatch6() {		
		String expectedResult = "MATCH";
		List codedDataList = new ArrayList<String>();
		codedDataList.add(null);
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add(null);
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}		
	
	
	/**
	 *  Test coded data checker result - Case Insensitive - Different cases
	 */
	@Test
	public void testSetCodedDataCheckerResultCaseInsensitiveWarn1() {		
		String expectedResult = "WARNING";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("fresh tissue");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}			
	
	/**
	 *  Test coded data checker result - Partial case differences
	 */
	@Test
	public void testSetCodedDataCheckerResultCaseInsensitiveWarn2() {
		String expectedResult = "WARNING";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("FRESH tissue");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}
	
	
	/**
	 * Coded data checker result - WARNING - Ignoring punctuation differences
	 */		
	@Test
	public void testSetCodedDataCheckerResultIgnorePunct1() {
		String expectedResult = "WARNING";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("FRESH- TISSUE");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}	
	
	/**
	 * Coded data checker result - WARNING - Ignoring punctuation differences
	 */		
	@Test
	public void testSetCodedDataCheckerResultIgnorePunct2() {
		String expectedResult = "WARNING";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("FRESH TISSUE;");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}		
	
	/**
	 * Coded data checker result - WARNING - Ignoring punctuation differences
	 */		
	@Test
	public void testSetCodedDataCheckerResultIgnorePunct3() {
		String expectedResult = "WARNING";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("FRESH (TISSUE)");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}		
	
	/**
	 * Coded data checker result - WARNING - Ignoring punctuation differences
	 */		
	@Test
	public void testSetCodedDataCheckerResultIgnorePunct4() {
		String expectedResult = "WARNING";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("?FRESH TISSUE");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}
	
	/**
	 * Coded data checker result - WARNING - Ignoring punctuation differences
	 */		
	@Test
	public void testSetCodedDataCheckerResultIgnorePunct5() {
		String expectedResult = "WARNING";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FreshTissue");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("Fresh.Tissue");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}	
	
	/**
	 * Coded data checker result - WARNING - Ignoring punctuation differences
	 */		
	@Test
	public void testSetCodedDataCheckerResultIgnorePunct6() {
		String expectedResult = "WARNING";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("FRESH\" TISSUE");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}		
	
	/**
	 * Coded data checker result - WARNING - Ignoring punctuation differences
	 */		
	@Test
	public void testSetCodedDataCheckerResultIgnorePunct7() {
		String expectedResult = "WARNING";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("FRESH TISSUE'");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}		
	
	/**
	 * Coded data checker result - WARNING - Ignoring punctuation differences
	 */		
	@Test
	public void testSetCodedDataCheckerResultIgnorePunct8() {
		String expectedResult = "WARNING";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add(":FRESH TISSUE");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}		
		
	/**
	 *  Test coded data checker result - Error - No coded data
	 */	
	@Test
	public void testSetCodedDataCheckerResultError1() {		
		String expectedResult = "ERROR";
		List codedDataList = Arrays.asList("Blood", "Bone Marrow", "Buccal Cell Sample", "Fresh Tissue");
		question.setRaveCodedData(codedDataList);
		List pvList = Arrays.asList("Frozen Tissue", "Formalin Fixed Tissue");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}	
	
	/**
	 *  Test coded data checker result - ERROR (NON-MATCH) - Excluded punctuation - Coded data
	 */	
	@Test
	public void testSetCodedDataCheckerResultError2() {		
		String expectedResult = "ERROR";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH |TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("FRESH TISSUE");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}		
	
	/**
	 *  Test coded data checker result - ERROR (NON-MATCH) - Excluded punctuation - pv list
	 */	
	@Test
	public void testSetCodedDataCheckerResultError3() {		
		String expectedResult = "ERROR";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("|FRESH TISSUE");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}
	
	/**
	 *  Test coded data checker result - ERROR (NON-MATCH) - <BLANK> coded data vs Non-null pv list
	 */	
	@Test
	public void testSetCodedDataCheckerResultError4() {		
		String expectedResult = "ERROR";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("FRESH TISSUE");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}		
	
	/**
	 *  Test coded data checker result - ERROR (NON-MATCH) - <BLANK> pv list vs Non-null coded data
	 */	
	@Test
	public void testSetCodedDataCheckerResultError5() {
		String expectedResult = "ERROR";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}	
	
	/**
	 *  Test coded data checker result - ERROR (NON-MATCH) - Null coded data vs Non-null pv list
	 */	
	@Test
	public void testSetCodedDataCheckerResultError6() {		
		String expectedResult = "ERROR";
		List codedDataList = new ArrayList<String>();
		codedDataList.add(null);
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("FRESH TISSUE");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}		
	
	/**
	 *  Test coded data checker result - ERROR (NON-MATCH) - Null pv list vs Non-null coded data
	 */	
	@Test
	public void testSetCodedDataCheckerResultError7() {
		String expectedResult = "ERROR";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("FRESH TISSUE");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add(null);
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}		
	
	/**
	 *  Test coded data checker result - ERROR (NON-MATCH) - Null pv list vs Non-null coded data
	 */	
	@Test
	public void testSetCodedDataCheckerResultError8() {
		String expectedResult = "ERROR";
		List codedDataList = new ArrayList<String>();
		codedDataList.add("Fresh_Tissue");
		question.setRaveCodedData(codedDataList);
		List pvList = new ArrayList<String>();
		pvList.add("Fresh-Tissue");
		CCCQuestion actualResult = ValidatorService.setCodedDataCheckerResult(pvList, question, false);
		assertEquals(expectedResult, actualResult.getCodedDataResult().get(0));
	}			
	

	/*The following tests verify the Data type checker result implementing the below requirement.
	 * 
	 * Compare caDSR Value Domain's Data Type to ALS.Fields.DataFormat. 
	DataFormat value starting with $ equates to caDSR ALPHANUMERIC, CHARACTER, javal.lang.String, java.lang.Character, xsd.string. 
	DataFormat value starting with a numeral (no $) equates to caDSR Integer, NUMBER, java.lang.Integer, xsd:integer. 
	DataFormat value starting with dd equates to caDSR DATE, xsd:date; and to caDSR FormatName
	DataFormat value starting with HH, hh, equates to caDSR TIME, xsd:time. 
	*/
	
	/**
	 *  Test Data Type checker result - Match
	 */	
	@Test
	public void testCheckDataTypeCheckerResultMatch() {		
		String expectedResult = "MATCH";
		CCCQuestion actualResult = ValidatorService.checkDataTypeCheckerResult(question, "$12", "Character");
		assertEquals (expectedResult, actualResult.getDatatypeCheckerResult());
	}
	
	/**
	 *  Test Data Type checker result - Non-Match
	 */		
	@Test
	public void testCheckDataTypeCheckerResultError() {
		
		String expectedResult = "ERROR";
		CCCQuestion actualResult = ValidatorService.checkDataTypeCheckerResult(question, "dd-mm-yyyy", "Time");
		assertEquals (expectedResult, actualResult.getDatatypeCheckerResult());
	}	
	
	/**
	 *  Test Data Type checker result - Not Checked
	 */			
	@Test
	public void testCheckDataTypeCheckerResultNotChecked() {		
		String expectedResult = "NOT CHECKED";
		CCCQuestion actualResult = ValidatorService.checkDataTypeCheckerResult(question, "dd-mm-yyyy", "DateTime");
		assertEquals (expectedResult, actualResult.getDatatypeCheckerResult());
	}		

	/*
	 * The following tests verify the Length checker result implementing the below requirement.
	 * 
	 *  If caDSR VD maxlengthNumber does not match FixedUnit number of characters, 
	display both Rave value and caDSR value and result  "WARNING" */	
	
	/**
	 *  Test Length checker result - Match
	 */				
	@Test
	public void testSetLengthCheckerResultMatch() {		
		String expectedResult = "MATCH";
		question.setRaveLength("10 Characters");
		CCCQuestion actualResult = ValidatorService.setLengthCheckerResult(question, 10);
		assertEquals(expectedResult, actualResult.getLengthCheckerResult());
	}
	
	/**
	 *  Test Length checker result - Non-match
	 */				
	@Test
	public void testSetLengthCheckerResultWarning() {		
		String expectedResult = "WARNING";
		question.setRaveLength("12 Characters");
		CCCQuestion actualResult = ValidatorService.setLengthCheckerResult(question, 5);
		assertEquals(expectedResult, actualResult.getLengthCheckerResult());
	}	
	
	
	/*
	 * The following tests verify the format checker result implementing the following requirement.
	 * 
	 * Comparing the ALS RAVE Data Format with caDSR Value Domain Display Format
	 * */	
	
	/**
	 *  Test Format checker result - Match
	 */				
	@Test
	public void testSetFormatCheckerResultMatch() {		
		String expectedResult = "MATCH";
		CCCQuestion actualResult = ValidatorService.checkFormatCheckerResult(question, "dd-mm-yyyy", "dd-mm-yyyy");
		assertEquals(expectedResult, actualResult.getFormatCheckerResult());
	}	
	
	/**
	 *  Test Format checker result - Non-match
	 */				
	@Test
	public void testSetFormatCheckerResultWarning() {		
		String expectedResult = "WARNING";
		CCCQuestion actualResult = ValidatorService.checkFormatCheckerResult(question, "dd-mm-yyyy", "MON-DY-YEAR");
		assertEquals(expectedResult, actualResult.getFormatCheckerResult());
	}		
	
	/*
	 * The following tests verify the UOM checker result implementing the following requirement.
	 * 
		If the Value Domain Unit of Measure in not null/blank, then check to see 
		If there is a matching value in FixedUnit or CodedUnit.
		If it does not match, then display the Rave UOM and the Value Domain UOM and result "WARNING".
	 * */	
	
	/**
	 *  Test UOM checker result - Match
	 */				
	@Test
	public void testSetUomCheckerResultMatch() {		
		String expectedResult = "MATCH";
		question.setRaveUOM("m2");
		CCCQuestion actualResult = ValidatorService.setUomCheckerResult(question, "m2");
		assertEquals(expectedResult, actualResult.getUomCheckerResult());
	}	
	
	/**
	 *  Test UOM checker result - Non-match
	 */				
	@Test
	public void testSetUomCheckerResultWarning() {		
		String expectedResult = "WARNING";
		question.setRaveUOM("ddd.dd");
		CCCQuestion actualResult = ValidatorService.setUomCheckerResult(question, "m2");
		assertEquals(expectedResult, actualResult.getUomCheckerResult());
	}
	

}
