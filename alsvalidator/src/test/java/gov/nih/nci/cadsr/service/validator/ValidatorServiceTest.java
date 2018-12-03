package gov.nih.nci.cadsr.service.validator;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ValidatorServiceTest {

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
	public void testCompareDataTypeString () {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.compareDataType("$8", "ALPHANUMERIC");
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
	
	
}
