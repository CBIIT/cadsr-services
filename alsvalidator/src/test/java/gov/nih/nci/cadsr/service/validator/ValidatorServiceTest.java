package gov.nih.nci.cadsr.service.validator;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.DataElement;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.DataElementDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.OtherVersion;

public class ValidatorServiceTest {
	
	CCCQuestion question;
	CdeDetails cdeDetails;
	DataElement de;
	DataElementDetails deDetails; 	
	private static final String retiredArchivedStatus = "RETIRED ARCHIVED";
	private static final String retiredPhasedOutStatus = "RETIRED PHASED OUT";
	private static final String retiredWithdrawnStatus = "RETIRED WITHDRAWN";	
	private static final String errMsg1 = "CDE has been retired.";
	private static final String errMsg2 = "Newer version of CDE exists: {%.1f}.";	
	
	public void init() {
		question = new CCCQuestion();
		cdeDetails = new CdeDetails();
		de = new DataElement();
		deDetails = new DataElementDetails();
	}

	public void testReplacePattern(String stringWithPattern, String patternToReplace, String replacement,
			String expectedResult) {
		String replacedText = ValidatorService.replacePattern(stringWithPattern, patternToReplace, replacement);
		assertEquals(expectedResult, replacedText);
	}

	@Test
	public void testReplacePatternAt() {
		String at_str = "@@";
		String comma_str = ",";
		testReplacePattern("Cancer Antigen 15-3 (CA15-3)@@ Serum", at_str, comma_str,
				"Cancer Antigen 15-3 (CA15-3), Serum");
	}
	
	@Test
	public void testReplacePatternHash() {
		String hash_str = "##";
		String semicolon_str = ";";
		testReplacePattern("Cancer Antigen 15-3 (CA15-3)## Serum", hash_str, semicolon_str,
				"Cancer Antigen 15-3 (CA15-3); Serum");
	}	

	@Test
	public void testCreateAllowableTextChoices() {
		String expectedResult = "PvValue1|PvValue2|PvValue3|PvValue4";
		List<String> pvVmList = Arrays.asList("PvValue1", "PvValue2", "PvValue3", "PvValue4");
		String allowableTextChoices = ValidatorService.createAllowableTextChoices(pvVmList);
		assertEquals(expectedResult, allowableTextChoices);
	}

	@Test
	public void testCleanStringforNbsp() {
		String expectedResult = "Patient Eligibility";
		// The space present between 'Patient' & 'Eligibility' is a Non-breaking space.
		String cleanString = ValidatorService.cleanStringforNbsp("Patient Eligibility"); 
		assertEquals(expectedResult, cleanString);
	}

	@Test
	public void testAssignQuestionErrorMessage() {
		String expectedResult = "Error Message1.\nError Message2.\nError Message3.";
		String newMessage1 = "Error Message1.";
		String newMessage2 = "Error Message2.";
		String newMessage3 = "Error Message3.";
		String actualResult = ValidatorService.assignQuestionErrorMessage(newMessage1, newMessage2);
		actualResult = ValidatorService.assignQuestionErrorMessage(actualResult, newMessage3);
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void testComputeRaveLengthFromText() {
		int expectedResult = 12;
		int actualResult = ValidatorService.computeRaveLength("(12 characters)");
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void testComputeRaveLengthFromCharPattern() {
		int expectedResult = 6;
		int actualResult = ValidatorService.computeRaveLength("(dddd.dd)");
		assertEquals(expectedResult, actualResult);
	}	
	
	@Test
	public void testComputeRaveLengthFromNumericPattern() {
		int expectedResult = 5;
		int actualResult = ValidatorService.computeRaveLength("(999.99)");
		assertEquals(expectedResult, actualResult);
	}		
	
	@Test
	public void testCompareDataTypeString1 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("$8", "ALPHANUMERIC");
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void testCompareDataTypeString2 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("$700", "character");
		assertEquals(expectedResult, actualResult);
	}	

	@Test
	public void testCompareDataTypeDate1 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("mon dd yyyy", "DATE");
		assertEquals(expectedResult, actualResult);
	}				
	
	@Test
	public void testCompareDataTypeDate2 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("MM DD YYYY", "DATE");
		assertEquals(expectedResult, actualResult);
	}					

	@Test
	public void testCompareDataTypeDate3 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("yy mm dd", "DATE");
		assertEquals(expectedResult, actualResult);
	}					
	
	@Test
	public void testCompareDataTypeDate4 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("yyyy mm dd", "DATE");
		assertEquals(expectedResult, actualResult);
	}						
	
	@Test
	public void testCompareDataTypeDate5 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("DD MM YY", "DATE");
		assertEquals(expectedResult, actualResult);
	}							
	
	@Test
	public void testCompareDataTypeDate6 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("DY MTH YR", "DATE");
		assertEquals(expectedResult, actualResult);
	}								
	
	@Test
	public void testCompareDataTypeTime1 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("HH:nn", "TIME");
		assertEquals(expectedResult, actualResult);
	}									
	
	@Test
	public void testCompareDataTypeTime2 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("hh:mm:ss", "TIME");
		assertEquals(expectedResult, actualResult);
	}										
	
	@Test
	public void testCompareDataTypeTime3 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("TIME (HR(24):MN)", "TIME");
		assertEquals(expectedResult, actualResult);
	}
	
	@Test
	public void testCompareDataTypeNumeric1 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("1024", "NUMBER");
		assertEquals(expectedResult, actualResult);
	}	
	
	@Test
	public void testCompareDataTypeNumeric2 () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("5.2", "JAVA.LANG.LONG");
		assertEquals(expectedResult, actualResult);
	}		
	
	public void invokeCheckCdeRetiredStatus(String errMsg, String status) {
		deDetails.setWorkflowStatus(status);
		de.setDataElementDetails(deDetails);
		cdeDetails.setDataElement(de);
		CCCQuestion actualResult = ValidatorService.checkCdeRetired(cdeDetails, question);
		assertEquals(errMsg, actualResult.getMessage());
	}	
		
	@Test
	public void testCheckCdeRetiredArchived () {
		init();
		invokeCheckCdeRetiredStatus(errMsg1, retiredArchivedStatus);
	}	
	
	@Test
	public void testCheckCdeRetiredPhasedOut () {
		init();
		invokeCheckCdeRetiredStatus(errMsg1, retiredPhasedOutStatus);
	}		
	
	@Test
	public void testCheckCdeRetiredWithdrawn () {
		init();
		invokeCheckCdeRetiredStatus(errMsg1, retiredWithdrawnStatus);
	}
	
	@Test
	public void testCheckCdeVersions() {
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
	
}
