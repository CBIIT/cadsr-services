package gov.nih.nci.cadsr.service.validator;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.DataElement;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.DataElementDetails;

public class ValidatorServiceTest {
	
	CCCQuestion question;
	CdeDetails cdeDetails;
	DataElement de;
	DataElementDetails deDetails; 	

	private static final String errorString = "ERROR";
	private static final String matchString = "MATCH";
	private static final String notCheckedString = "NOT CHECKED";	
	
	public void init() {
		question = new CCCQuestion();
		cdeDetails = new CdeDetails();
		de = new DataElement();
		deDetails = new DataElementDetails();
	}

	
	/**
	 * Replaces any given pattern identified with a provided replacement pattern
	 * @param stringWithPattern
	 * @param patternToReplace
	 * @param replacement
	 * @param expectedResult
	 */
	public void testReplacePattern(String stringWithPattern, String patternToReplace, String replacement,
			String expectedResult) {
		String replacedText = ValidatorService.replacePattern(stringWithPattern, patternToReplace, replacement);
		assertEquals(expectedResult, replacedText);
	}

	/**
	 * Testing '@@' to be replaced with ","
	 */	
	@Test
	public void testReplacePatternAt() {
		String at_str = "@@";
		String comma_str = ",";
		testReplacePattern("Cancer Antigen 15-3 (CA15-3)@@ Serum", at_str, comma_str,
				"Cancer Antigen 15-3 (CA15-3), Serum");
	}
	
	/**
	 * Testing '##' to be replaced with ";"
	 */		
	@Test
	public void testReplacePatternHash() {
		String hash_str = "##";
		String semicolon_str = ";";
		testReplacePattern("Cancer Antigen 15-3 (CA15-3)## Serum", hash_str, semicolon_str,
				"Cancer Antigen 15-3 (CA15-3); Serum");
	}	

	/**
	 * Testing concatenation of PV VMs into a string (allowableTextChoices)
	 */			
	@Test
	public void testCreateAllowableTextChoices() {
		String expectedResult = "PvValue1|PvValue2|PvValue3|PvValue4";
		List<String> pvVmList = Arrays.asList("PvValue1", "PvValue2", "PvValue3", "PvValue4");
		String allowableTextChoices = ValidatorService.createAllowableTextChoices(pvVmList);
		assertEquals(expectedResult, allowableTextChoices);
	}

	/**
	 * Testing replacement of Non breaking space with a whitespace
	 */	
	@Test
	public void testCleanStringforNbsp() {
		String expectedResult = "Patient Eligibility";
		// The space present between 'Patient' & 'Eligibility' is a Non-breaking space.
		String cleanString = ValidatorService.cleanStringforNbsp("Patient Eligibility"); 
		assertEquals(expectedResult, cleanString);
	}

	/**
	 * Testing a single error message assignment into the Question
	 */		
	@Test
	public void testAssignQuestionErrorMessage1() {
		String expectedResult = "Error Message.";
		String newMessage1 = "Error Message.";
		String actualResult = ValidatorService.assignQuestionErrorMessage(null, newMessage1);
		assertEquals(expectedResult, actualResult);
	}
	
	/**
	 * Testing error message appending when the question already has an error message
	 */	
	@Test
	public void testAssignQuestionErrorMessage2() {
		String expectedResult = "Error Message1.\n\nError Message2.\n\nError Message3.";
		String newMessage1 = "Error Message1.";
		String newMessage2 = "Error Message2.";
		String newMessage3 = "Error Message3.";
		String actualResult = ValidatorService.assignQuestionErrorMessage(newMessage1, newMessage2);
		actualResult = ValidatorService.assignQuestionErrorMessage(actualResult, newMessage3);
		assertEquals(expectedResult, actualResult);
	}	
	
	/**
	 * Testing null error message assignment to the question
	 */		
	@Test
	public void testAssignQuestionErrorMessageNull() {
		String expectedResult = null;
		String newMessage1 = null;
		String actualResult = ValidatorService.assignQuestionErrorMessage(null, newMessage1);
		assertEquals(expectedResult, actualResult);
	}		
	
	/**
	 * Testing rave length computing from 'xx characters' pattern
	 */			
	@Test
	public void testComputeRaveLengthFromText() {
		int expectedResult = 12;
		int actualResult = ValidatorService.computeRaveLength("(12 characters)");
		assertEquals(expectedResult, actualResult);
	}
	
	/**
	 * Testing rave length computing from 'dddd.dd' pattern (multiple 'd's)
	 */				
	@Test
	public void testComputeRaveLengthFromCharPattern() {
		int expectedResult = 6;
		int actualResult = ValidatorService.computeRaveLength("(dddd.dd)");
		assertEquals(expectedResult, actualResult);
	}	
	
	/**
	 * Testing rave length computing from '999.99' pattern (multiple '9's)
	 */					
	@Test
	public void testComputeRaveLengthFromNumericPattern() {
		int expectedResult = 5;
		int actualResult = ValidatorService.computeRaveLength("(999.99)");
		assertEquals(expectedResult, actualResult);
	}		
	
	/**
	 * Testing rave length computing from '$xx' pattern (alphanumeric data type)
	 */						
	@Test
	public void testCompareDataTypeString1 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("$12", "ALPHANUMERIC");
		assertEquals(expectedResult, actualResult);
	}
	
	/**
	 * Testing rave length computing from '$xx' pattern (character data type)
	 */							
	@Test
	public void testCompareDataTypeString2 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("$700", "character");
		assertEquals(expectedResult, actualResult);
	}
	
	/**
	 * Testing data type comparison when ALS data type is only one or more spaces and no other text
	 */									
	@Test
	public void testCompareDataTypeStringWithSpacesOnly () {
		String expectedResult = errorString;
		String actualResult = ValidatorService.compareDataType("  ", "JAVA.LANG.STRING");
		assertEquals(expectedResult, actualResult);
	}	

	/**
	 * Testing data type comparison when ALS data type is null
	 */	
	@Test
	public void testCompareDataTypeStringNullCheck () {
		String expectedResult = errorString;
		String actualResult = ValidatorService.compareDataType(null, "character");
		assertEquals(expectedResult, actualResult);
	}	
	
	/**
	 * Testing data type comparison for not checked ALS data type - example 1 (datetime)
	 */		
	@Test
	public void testCompareDataTypeNotChecked1 () {
		String expectedResult = notCheckedString;
		String actualResult = ValidatorService.compareDataType("dd-mm-yyyy hh:nn:ss", "DATETIME");
		assertEquals(expectedResult, actualResult);
	}	
	
	/**
	 * Testing data type comparison for not checked ALS data type - example 2 (String)
	 */			
	@Test
	public void testCompareDataTypeNotChecked2 () {
		String expectedResult = notCheckedString;
		String actualResult = ValidatorService.compareDataType("NNN TTTT", "java.lang.Object");
		assertEquals(expectedResult, actualResult);
	}		
	
	/**
	 * Testing data type comparison for not checked ALS data type - example 3 (Empty String)
	 */				
	@Test
	public void testCompareDataTypeNotCheckedDTNull () {
		String expectedResult = notCheckedString;
		String actualResult = ValidatorService.compareDataType("", "java.lang.Byte");
		assertEquals(expectedResult, actualResult);
	}
	
	/**
	 * Testing data type comparison for not checked ALS data type - example 4 (null String)
	 */					
	@Test
	public void testCompareDataTypeNotCheckedDTEmpty () {
		String expectedResult = notCheckedString;
		String actualResult = ValidatorService.compareDataType(null, "java.lang.Byte");
		assertEquals(expectedResult, actualResult);
	}	

	/**
	 * Testing data type comparison for a known & checked ALS data type - example 1 (Date)
	 */						
	@Test
	public void testCompareDataTypeDate1 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("mon dd yyyy", "DATE");
		assertEquals(expectedResult, actualResult);
	}				
	
	/**
	 * Testing data type comparison for a known & checked ALS data type with a different date format- example 2 (Date)
	 */							
	@Test
	public void testCompareDataTypeDate2 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("MM DD YYYY", "DATE");
		assertEquals(expectedResult, actualResult);
	}					

	/**
	 * Testing data type comparison for a known & checked ALS data type with a different date format- example 3 (Date)
	 */								
	@Test
	public void testCompareDataTypeDate3 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("yy mm dd", "DATE");
		assertEquals(expectedResult, actualResult);
	}					
	
	/**
	 * Testing data type comparison for a known & checked ALS data type with a different date format- example 4 (Date)
	 */								
	@Test
	public void testCompareDataTypeDate4 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("yyyy mm dd", "DATE");
		assertEquals(expectedResult, actualResult);
	}						
	
	/**
	 * Testing data type comparison for a known & checked ALS data type with a different date format- example 5 (Date)
	 */								
	@Test
	public void testCompareDataTypeDate5 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("DD MM YY", "DATE");
		assertEquals(expectedResult, actualResult);
	}							
	
	/**
	 * Testing data type comparison for a known & checked ALS data type with a different date format- example 6 (Date)
	 */									
	@Test
	public void testCompareDataTypeDate6 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("DY MTH YR", "DATE");
		assertEquals(expectedResult, actualResult);
	}								
	
	/**
	 * Testing data type comparison for a known & checked ALS data type - example 1 (Time)
	 */										
	@Test
	public void testCompareDataTypeTime1 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("HH:nn", "TIME");
		assertEquals(expectedResult, actualResult);
	}									
	
	/**
	 * Testing data type comparison for a known & checked ALS data type with a different time format - example 2 (Time)
	 */											
	@Test
	public void testCompareDataTypeTime2 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("hh:mm:ss", "TIME");
		assertEquals(expectedResult, actualResult);
	}										
	
	/**
	 * Testing data type comparison for a known & checked ALS data type with a different time format - example 3 (Time)
	 */												
	@Test
	public void testCompareDataTypeTime3 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("TIME (HR(24):MN)", "TIME");
		assertEquals(expectedResult, actualResult);
	}
	
	/**
	 * Testing data type comparison for a known & checked ALS data type - example 1 (Numeric)
	 */												
	@Test
	public void testCompareDataTypeNumeric1 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("1024", "NUMBER");
		assertEquals(expectedResult, actualResult);
	}	
	
	/**
	 * Testing data type comparison for a known & checked ALS data type - example 2 (Integer)
	 */													
	@Test
	public void testCompareDataTypeNumeric2 () {
		String expectedResult = matchString;
		String actualResult = ValidatorService.compareDataType("5", "JAVA.LANG.INTEGER");
		assertEquals(expectedResult, actualResult);
	}
	
	/**
	 * Testing a Non enumerated ALS control type - example 1 (Text)
	 */														
	@Test
	public void testIsNonEnumeratedN1() {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.isNonEnumerated("TEXT");
		assertEquals(expectedResult, actualResult);
	}	
	
	/**
	 * Testing a Non enumerated ALS control type - example 2 (LongText)
	 */															
	@Test
	public void testIsNonEnumeratedN2() {
		Boolean expectedResult = true;
		Boolean actualResult = ValidatorService.isNonEnumerated("longtext");
		assertEquals(expectedResult, actualResult);
	}		
	
	/**
	 * Testing an Enumerated ALS control type - example 1 (DropDownList)
	 */																
	@Test
	public void testIsNonEnumeratedE1() {
		Boolean expectedResult = false;
		Boolean actualResult = ValidatorService.isNonEnumerated("DropDownList");
		assertEquals(expectedResult, actualResult);
	}	
	
	/**
	 * Testing an Enumerated ALS control type - example 2 (RadioButton)
	 */																	
	@Test
	public void testIsNonEnumeratedE2() {
		Boolean expectedResult = false;
		Boolean actualResult = ValidatorService.isNonEnumerated("RADIOBUTTON");
		assertEquals(expectedResult, actualResult);
	}
	
	/**
	 * Testing an Enumerated ALS control type - example 3 (Dynamic Searchlist)
	 */												
	@Test
	public void testIsNonEnumeratedE3() {
		Boolean expectedResult = false;
		Boolean actualResult = ValidatorService.isNonEnumerated("dynamic searchlist");
		assertEquals(expectedResult, actualResult);
	}	
	
	/**
	 * Testing anb Enumerated ALS control type - example 4 (Checkbox)
	 */												
	@Test
	public void testIsNonEnumeratedE4() {
		Boolean expectedResult = false;
		Boolean actualResult = ValidatorService.isNonEnumerated("CheckBox");
		assertEquals(expectedResult, actualResult);
	}
	
}
