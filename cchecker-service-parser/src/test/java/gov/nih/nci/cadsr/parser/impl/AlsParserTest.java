/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.parser.impl;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Before;
import org.junit.Test;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.ALSUnitDictionaryEntry;
import gov.nih.nci.cadsr.data.CCCError;

public class AlsParserTest {
	Workbook workbook;
	Sheet sheet;
	Row row;
	Cell cell;
	ALSData alsData;
	private static int crfDraftStartRow = 1;
	private static int cell_zero = 0;
	private static int cell_one = 1;
	private static int cell_two = 2;
	private static int cell_three = 3;	
	private static int cell_four = 4;
	private static int cell_five = 5;
	private static int cell_six = 6;	
	private static int cell_seven = 7;	
	private static int cell_eight = 8;
	private static int cell_nine = 9;		
	private static int cell_eleven = 11;
	private static int cell_fourteen = 14;	
	private static int cell_fifteen = 15;
	private static int cell_twenty = 20;	
	
	@Before
	  public void init() throws IOException {
		alsData = new ALSData();
		workbook = WorkbookFactory.create(true);
	  }
	
	/**
	 * Creates a Sheet named CRFDraft in the Excel
	 * @throws IOException
	 */
	public void createCrfDraftInExcel () throws IOException {
	    sheet = workbook.createSheet("CRFDraft");
	    row = sheet.createRow(crfDraftStartRow);
	    cell = row.createCell(cell_zero);
	    cell.setCellValue("14-JUN-2017 NS");
	    cell = row.createCell(cell_two);	    
	    cell.setCellValue("10057");	    
	    cell = row.createCell(cell_four);	    
	    cell.setCellValue("SUBJECT_ENROLLMENT");
	}
	
	/**
	 * Creates a Sheet named Forms in the Excel
	 * @throws IOException
	 */	
	public void createFormsSheetInExcel () throws IOException {
		String[] formsList = {"Form1", "Form2", "Form3"};		
	    sheet = workbook.createSheet("Forms");
	    row = sheet.createRow(0);	    
	    for (int i = 0; i < formsList.length; i++) {
		    row = sheet.createRow(i+1);
		    cell = row.createCell(cell_zero);
		    cell.setCellValue("Form_Oid_"+(i+1));
		    cell = row.createCell(cell_one);
		    cell.setCellValue(i+1);
		    cell = row.createCell(cell_two);	    
		    cell.setCellValue(formsList[i]);
	    }

	}
	
	/**
	 * Creates a Sheet named Fields in the Excel
	 * @throws IOException
	 */	
	public void createFieldsSheetInExcel (String publicIdToggle) throws IOException {
	    sheet = workbook.createSheet("Fields");
	    row = sheet.createRow(0);
	    row = sheet.createRow(1);
	    cell = row.createCell(cell_zero);
	    cell.setCellValue("ENROLLMENT"); // Form OID
	    cell = row.createCell(cell_one);
	    if ("FORM_OID".equalsIgnoreCase(publicIdToggle)) {
		    cell.setCellValue("FORM_OID"); // Field OID
	    } else {
		    cell.setCellValue("PRSN_GENDER_CD"); // Field OID
	    }
	    cell = row.createCell(cell_two);
	    cell.setCellValue("5"); // Ordinal
	    cell = row.createCell(cell_four);
	    if ("FORM_OID".equalsIgnoreCase(publicIdToggle)) {
		    cell.setCellValue("FORM_OID"); // Draft Field Name
	    } else {
		    cell.setCellValue("Gender PID2721620_V1_0"); // Draft Field Name
	    }
	    cell = row.createCell(cell_seven);
	    cell.setCellValue("$1"); // Data Format
	    cell = row.createCell(cell_eight);
	    cell.setCellValue("PERSON_GENDER_C_PID2721618_V1_0"); // Data Dictionary Name
	    cell = row.createCell(cell_nine);
	    cell.setCellValue("No Unit"); // Unit Dictionary Name
	    cell = row.createCell(cell_eleven);
	    cell.setCellValue("Text"); // Control Type
	    cell = row.createCell(cell_fourteen);
	    cell.setCellValue("Gender"); // Pre Text
	    cell = row.createCell(cell_fifteen);
	    cell.setCellValue("(12 characters)"); // Fixed Unit
	    cell = row.createCell(cell_twenty);
	    cell.setCellValue("PID3292959_V1_0"); // Default Value
	}	
	
	
	/**
	 * Creates a Sheet named Data Dictionary Entries in the Excel
	 * @throws IOException
	 */	
	public void createDDESheetInExcel () throws IOException {
	    sheet = workbook.createSheet("DataDictionaryEntries");
	    row = sheet.createRow(0);
	    row = sheet.createRow(1);
	    cell = row.createCell(cell_zero);
	    cell.setCellValue("Adverse Event Outcome"); // DataDictionaryName
	    cell = row.createCell(cell_one);
	    cell.setCellValue("1"); // CodedData
	    cell = row.createCell(cell_two);
	    cell.setCellValue("1"); // Ordinal
	    cell = row.createCell(cell_three);
	    cell.setCellValue("Recovered"); // UserDataString
	    cell = row.createCell(cell_four);
	    cell.setCellValue("FALSE"); // Specify
	}	
	
	
	/**
	 * Creates a ALS Data Dictionary Entry object
	 */		
	@SuppressWarnings({ "unchecked" })
	public ALSDataDictionaryEntry createDDETestDataToVerify() {
		ALSDataDictionaryEntry ddeExpected = new ALSDataDictionaryEntry();
		ddeExpected.setDataDictionaryName("Adverse Event Outcome");
		List<String> ordinalNum = new ArrayList<String>();
		ordinalNum.add("1"); // Adding a number for Ordinal		
		List<String> cd = new ArrayList<String>();
		cd.add("1"); // Adding a String - Coded Data
		List<String> userString = new ArrayList<String>();
		userString.add("Recovered");
		ddeExpected.setOrdinal(ordinalNum);
		ddeExpected.setCodedData(cd);
		ddeExpected.setUserDataString(userString);	
		return ddeExpected;
	}
	
	/**
	 * Creates a Sheet named Unit Dictionary Entries in the Excel
	 * @throws IOException
	 */	
	public void createUDESheetInExcel () throws IOException {
	    sheet = workbook.createSheet("UnitDictionaryEntries");
	    row = sheet.createRow(0);
	    row = sheet.createRow(1);
	    cell = row.createCell(cell_zero);
	    cell.setCellValue("TimeZone"); // UnitDictionaryName
	    cell = row.createCell(cell_one);
	    cell.setCellValue("EST"); // CodedUnit
	    cell = row.createCell(cell_two);
	    cell.setCellValue("1"); // Ordinal
	    cell = row.createCell(cell_three);
	    cell.setCellValue("1"); // ConstantA
	    cell = row.createCell(cell_four);
	    cell.setCellValue("0"); // ConstantB
	    cell = row.createCell(cell_five);
	    cell.setCellValue("0"); // ConstantC
	    cell = row.createCell(cell_six);
	    cell.setCellValue("0"); // ConstantK
	    cell = row.createCell(cell_seven);	    
	    cell.setCellValue("DST"); // UnitString
	}	
	
	
	/**
	 * HTML stripping from a string using JSOUP (3rd party lib) 
	 */	
	public void testHtmlStripJsoupMethod(String htmlText, String expectedResult) {
		String htmlStrippedText = AlsParser.stripHtml(htmlText);
		assertEquals(expectedResult, htmlStrippedText);
	}
	
	/**
	 * HTML stripping from a string using String replace 
	 */		
	public void testHtmlStripReplaceMethod(String htmlText, String expectedResult) {
		String htmlStrippedText = AlsParser.stripHtmlV2(htmlText);
		assertEquals(expectedResult, htmlStrippedText);
	}

	/**
	 * HTML stripping from a string using JSOUP (3rd party lib) 
	 */		
	@Test
	public void testHtmlStripPatternsJsoup1() {
		String htmlText1 = "<font color=\"red\">Start Date</font>";
		String strippedText1 = "Start Date";
		testHtmlStripJsoupMethod(htmlText1, strippedText1);
	}
	
	/**
	 * HTML stripping from a string using String replace 
	 */		
	@Test
	public void testHtmlStripPatternsReplace1() {
		String htmlText1 = "<font color=\"red\">Start Date</font>";
		String strippedText1 = "Start Date";
		testHtmlStripReplaceMethod(htmlText1, strippedText1);
	}
	
	/**
	 * HTML stripping from a string using JSOUP (3rd party lib) 
	 */		
	@Test
	public void testHtmlStripPatternsJsoup2() {
		String htmlText2 = "<H10 style=\"color:red;\">***Any solid or nodular lesion greater than or equal to 1 cm in diameter with evidence of deep infiltration in the skin "
				+ "and/or vertical growth.</H10>";
		String strippedText2 = "***Any solid or nodular lesion greater than or equal to 1 cm in diameter with evidence of deep infiltration in the skin and/or vertical growth.";		
		testHtmlStripJsoupMethod(htmlText2, strippedText2);
	}
	
	/**
	 * HTML stripping from a string using JSOUP (3rd party lib) 
	 */		
	@Test
	public void testHtmlStripPatternsReplace2() {
		String htmlText2 = "<H10 style=\"color:red;\">***Any solid or nodular lesion greater than or equal to 1 cm in diameter with evidence of deep infiltration in the skin "
				+ "and/or vertical growth.</H10>";
		String strippedText2 = "***Any solid or nodular lesion greater than or equal to 1 cm in diameter with evidence of deep infiltration in the skin and/or vertical growth.";
		testHtmlStripReplaceMethod(htmlText2, strippedText2);
	}	
	
	
	/**
	 * HTML stripping from a string using JSOUP (3rd party lib)
	 */		
	@Test
	public void teststripHtmlMath() {
		String expectedResult = "Start Date x < 2 and x > 1";
		String htmlText1 = "<font color=\"red\"><b>" + expectedResult + "</b></font>";

		String actual = AlsParser.stripHtml(htmlText1);
		assertEquals(expectedResult, actual);
	}
	
	/**
	 * stripHtmlV2 cannot deal with '<'
	 */
	@Test
	public void testStripHtmlV2MathWrongResult() {
		String expectedResult = "Start Date x < 2 and x > 1";
		String expectedWrongResult = "Start Date x  1";
		String htmlText1 = "<font color=\"red\"><b>" + expectedResult + "</b></font>";

		String actual = AlsParser.stripHtmlV2(htmlText1);
		assertEquals(expectedWrongResult, actual);
	}

	/**
	 * HTML stripping from a string using JSOUP (3rd party lib) 
	 */		
	@Test
	public void teststripHtmlMath2() {
		String expectedResult = "Start Date x < 2";
		String htmlText1 = "<font color=\"red\"><b>" + expectedResult + "</b></font>";

		String actual = AlsParser.stripHtml(htmlText1);
		assertEquals(expectedResult, actual);
	}
	
	/**
	 * stripHtmlV2 cannot deal with '<'
	 */
	@Test
	public void testStripHtmlV2Math2WrongResult() {
		String expectedResult = "Start Date x < 2";
		String expectedWrongResult = "Start Date x ";
		String htmlText1 = "<font color=\"red\"><b>" + expectedResult + "</b></font>";

		String actual = AlsParser.stripHtmlV2(htmlText1);
		assertEquals(expectedWrongResult, actual);
	}
		
	/**
	 * Testing retrieval of CRF Project name from CRFDraft sheet 
	 */		
	@Test
	public void testGetCrfDraftProjectName() throws IOException {
		createCrfDraftInExcel();		
		String expectedResult = "10057";
		CCCError cccError = new CCCError();
		alsData = AlsParser.getCrfDraft(sheet, alsData, cccError);
		String actualResult = alsData.getCrfDraft().getProjectName();
		assertEquals(expectedResult, actualResult);
	}
	
	/**
	 * Testing retrieval of CRF Primary Form OID from CRFDraft sheet 
	 */			
	@Test
	public void testGetCrfDraftPrimaryFormOid() throws IOException {
		createCrfDraftInExcel();		
		String expectedResult = "SUBJECT_ENROLLMENT";
		CCCError cccError = new CCCError();
		alsData = AlsParser.getCrfDraft(sheet, alsData, cccError);
		String actualResult = alsData.getCrfDraft().getPrimaryFormOid();
		assertEquals(expectedResult, actualResult);
	}
	
	/**
	 * Testing retrieval of Form Names from Forms sheet 
	 */			
	@Test
	public void testGetForms_FormNames() throws IOException {
		String[] expecteds = {"Form1", "Form2", "Form3"};
		createFormsSheetInExcel();
		List<ALSForm> formsList = AlsParser.getForms(sheet, alsData, new CCCError()).getForms();
		String[] actuals = new String[3];
		for (int i = 0; i < formsList.size(); i++) {
			actuals[i] = formsList.get(i).getDraftFormName();
		}
		assertArrayEquals(expecteds, actuals);
	}
	
	/**
	 * Testing retrieval of Ordinals from Forms sheet 
	 */	
	@Test
	public void testGetForms_Ordinals() throws IOException {
		String[] expecteds = {"1", "2", "3"};
		createFormsSheetInExcel();
		List<ALSForm> formsList = AlsParser.getForms(sheet, alsData, new CCCError()).getForms();
		String[] actuals = new String[3];
		for (int i = 0; i < formsList.size(); i++) {
			actuals[i] = formsList.get(i).getOrdinal();
		}
		assertArrayEquals(expecteds, actuals);
	}
	
	/**
	 * Testing retrieval of Form OIDs from Forms sheet 
	 */	
	@Test
	public void testGetForms_FormOids() throws IOException {
		String[] expecteds = {"Form_Oid_1", "Form_Oid_2", "Form_Oid_3"};
		createFormsSheetInExcel();
		List<ALSForm> formsList = AlsParser.getForms(sheet, alsData, new CCCError()).getForms();
		String[] actuals = new String[3];
		for (int i = 0; i < formsList.size(); i++) {
			actuals[i] = formsList.get(i).getFormOid();
		}
		assertArrayEquals(expecteds, actuals);
	}	
	
	/**
	 * Testing retrieval of columns from Fields sheet - CDE
	 */	
	@Test
	public void testGetFields_getCde() throws IOException {
		String[] expecteds = {"ENROLLMENT", "PRSN_GENDER_CD", "5", "Gender PID2721620_V1_0", "$1", "PERSON_GENDER_C_PID2721618_V1_0", "No Unit", "Text", "Gender", "(12 characters)"};
		createFieldsSheetInExcel("CDE ID");
		List<ALSField> fieldsList = AlsParser.getFields(sheet, alsData, new CCCError()).getFields();
		String[] actuals = new String[10];
		ALSField field = fieldsList.get(0);
		actuals[0] = field.getFormOid();
		actuals[1] = field.getFieldOid();
		actuals[2] = field.getOrdinal();
		actuals[3] = field.getDraftFieldName();
		actuals[4] = field.getDataFormat();
		actuals[5] = field.getDataDictionaryName();
		actuals[6] = field.getUnitDictionaryName();
		actuals[7] = field.getControlType();
		actuals[8] = field.getPreText();
		actuals[9] = field.getFixedUnit();	
		assertArrayEquals(expecteds, actuals);
	}
	
	/**
	 * Testing retrieval of columns from Fields sheet - FORM ID
	 */	
	@Test
	public void testGetFields_getFormId() throws IOException {
		String[] expecteds = {"ENROLLMENT", "FORM_OID", "5", "FORM_OID", "$1", "PERSON_GENDER_C_PID2721618_V1_0", "No Unit", "Text", "Gender", "(12 characters)", "PID3292959_V1_0"};
		createFieldsSheetInExcel("FORM_OID");
		List<ALSField> fieldsList = AlsParser.getFields(sheet, alsData, new CCCError()).getFields();
		String[] actuals = new String[11];
		ALSField field = fieldsList.get(0);
		actuals[0] = field.getFormOid();
		actuals[1] = field.getFieldOid();
		actuals[2] = field.getOrdinal();
		actuals[3] = field.getDraftFieldName();
		actuals[4] = field.getDataFormat();
		actuals[5] = field.getDataDictionaryName();
		actuals[6] = field.getUnitDictionaryName();
		actuals[7] = field.getControlType();
		actuals[8] = field.getPreText();
		actuals[9] = field.getFixedUnit();
		actuals[10] = field.getDefaultValue();
		assertArrayEquals(expecteds, actuals);
	}
	
		
	/**
	 * Testing retrieval of Data Dictionary Entries - Dictionary Name
	 * @throws IOException
	 */
	@Test
	public void testGetDataDictionaryEntries_DictionaryName() throws IOException {
		createDDESheetInExcel();
		Map<String, ALSDataDictionaryEntry> ddeMap = (AlsParser.getDataDictionaryEntries(sheet, alsData, new CCCError())).getDataDictionaryEntries();
		ALSDataDictionaryEntry ddeExpected  = createDDETestDataToVerify();
		ALSDataDictionaryEntry ddeActual = null;
		for (String ddName : ddeMap.keySet()) {
			ddeActual = new ALSDataDictionaryEntry();
			 ddeActual = ddeMap.get(ddName);
			}
		assertEquals(ddeExpected.getDataDictionaryName(), ddeActual.getDataDictionaryName());
	}
	
	/**
	 * Testing retrieval of Data Dictionary Entries - User Data String
	 * @throws IOException
	 */	
	@Test
	public void testGetDataDictionaryEntries_UserString() throws IOException {
		createDDESheetInExcel();
		Map<String, ALSDataDictionaryEntry> ddeMap = (AlsParser.getDataDictionaryEntries(sheet, alsData, new CCCError())).getDataDictionaryEntries();
		ALSDataDictionaryEntry ddeExpected  = createDDETestDataToVerify();
		ALSDataDictionaryEntry ddeActual = null;
		for (String ddName : ddeMap.keySet()) {
			ddeActual = new ALSDataDictionaryEntry();
			 ddeActual = ddeMap.get(ddName);
			}
		assertEquals(ddeExpected.getUserDataString(), ddeActual.getUserDataString());
	}
		
	/**
	 * Testing retrieval of Data Dictionary Entries - Coded Data
	 * @throws IOException
	 */	
	@Test
	public void testGetDataDictionaryEntries_CodedData() throws IOException {
		createDDESheetInExcel();
		Map<String, ALSDataDictionaryEntry> ddeMap = (AlsParser.getDataDictionaryEntries(sheet, alsData, new CCCError())).getDataDictionaryEntries();
		ALSDataDictionaryEntry ddeExpected  = createDDETestDataToVerify();
		ALSDataDictionaryEntry ddeActual = null;
		for (String ddName : ddeMap.keySet()) {
			ddeActual = new ALSDataDictionaryEntry();
			 ddeActual = ddeMap.get(ddName);
			}
		assertArrayEquals(ddeExpected.getCodedData().toArray(), ddeActual.getCodedData().toArray());
	}
	
	/**
	 * Testing retrieval of Data Dictionary Entries - Error with Dictionary Name
	 * @throws IOException
	 */
	@Test
	public void testGetDataDictionaryEntries_DictionaryName_Error() throws IOException {
		createDDESheetInExcel();
		Map<String, ALSDataDictionaryEntry> ddeMap = (AlsParser.getDataDictionaryEntries(sheet, alsData, new CCCError())).getDataDictionaryEntries();
		ALSDataDictionaryEntry ddeExpected  = createDDETestDataToVerify();
		ddeExpected.setDataDictionaryName(null);
		ALSDataDictionaryEntry ddeActual = null;
		for (String ddName : ddeMap.keySet()) {
			ddeActual = new ALSDataDictionaryEntry();
			 ddeActual = ddeMap.get(ddName);
			}
		assertNotEquals(ddeExpected.getDataDictionaryName(), ddeActual.getDataDictionaryName());
	}	

	
	/**
	 * Testing retrieval of Unit Dictionary Entries - Dictionary Name
	 * @throws IOException
	 */
	@Test
	public void testGetUnitDictionaryEntries_DictionaryName() throws IOException {
		createUDESheetInExcel();
		List<ALSUnitDictionaryEntry> udeList = (AlsParser.getUnitDictionaryEntries (sheet, alsData, new CCCError())).getUnitDictionaryEntries();
		ALSUnitDictionaryEntry udeExpected  = new ALSUnitDictionaryEntry();
		udeExpected.setUnitDictionaryName("TimeZone");
		ALSUnitDictionaryEntry udeActual = udeList.get(0);
		assertEquals(udeExpected.getUnitDictionaryName(), udeActual.getUnitDictionaryName());
	}
	
	/**
	 * Testing retrieval of Unit Dictionary Entries - Coded Unit
	 * @throws IOException
	 */
	@Test
	public void testGetUnitDictionaryEntries_CodedUnit() throws IOException {
		createUDESheetInExcel();
		List<ALSUnitDictionaryEntry> udeList = (AlsParser.getUnitDictionaryEntries (sheet, alsData, new CCCError())).getUnitDictionaryEntries();
		ALSUnitDictionaryEntry udeExpected  = new ALSUnitDictionaryEntry();
		udeExpected.setCodedUnit("EST");
		ALSUnitDictionaryEntry udeActual = udeList.get(0);
		assertEquals(udeExpected.getCodedUnit(), udeActual.getCodedUnit());
	}	
	
	
	/**
	 * Testing retrieval of Unit Dictionary Entries - Constants
	 * @throws IOException
	 */
	@Test
	public void testGetUnitDictionaryEntries_Constants() throws IOException {
		createUDESheetInExcel();
		String[] udeExpected  = {"1", "0", "0", "0"};
		List<ALSUnitDictionaryEntry> udeList = (AlsParser.getUnitDictionaryEntries (sheet, alsData, new CCCError())).getUnitDictionaryEntries();
		String[] udeActual  = new String[4];
		udeActual[0] = udeList.get(0).getConstantA();
		udeActual[1] = udeList.get(0).getConstantB();
		udeActual[2] = udeList.get(0).getConstantC();
		udeActual[3] = udeList.get(0).getConstantK();
		assertArrayEquals(udeExpected, udeActual);
	}
	
	
	/**
	 * Testing retrieval of Unit Dictionary Entries - Unit String
	 * @throws IOException
	 */
	@Test
	public void testGetUnitDictionaryEntries_UnitString() throws IOException {
		createUDESheetInExcel();
		List<ALSUnitDictionaryEntry> udeList = (AlsParser.getUnitDictionaryEntries (sheet, alsData, new CCCError())).getUnitDictionaryEntries();
		ALSUnitDictionaryEntry udeExpected  = new ALSUnitDictionaryEntry();
		udeExpected.setUnitString("DST");
		ALSUnitDictionaryEntry udeActual = udeList.get(0);
		assertEquals(udeExpected.getUnitString(), udeActual.getUnitString());
	}		
	
}
