package gov.nih.nci.cadsr.parser;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import gov.nih.nci.cadsr.data.ALSCrfDraft;
import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.ALSUnitDictionaryEntry;
import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;

import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class AlsParser {
	
	public static final Logger logger = Logger.getLogger(AlsParser.class);
	public static final String INPUT_XLSX_FILE_PATH = "/Users/santhanamv/Documents/FORMBUILD-595/June4/FORMBUILD-595/From-PeterZipFile-RAVE-ALS-10057-VS.xlsx";
	public static ALSData alsData;
	public static CCCReport cccReport;
	public static DataFormatter dataFormatter = new DataFormatter();		

	public static void main(String[] args) throws IOException, InvalidFormatException {
		
		// Parsing the ALS file in Excel format (XLS). If this file has a XML extension
		// It needs to be converted to an XLSX file before being parsed.
		parseExcel();
		
		//Validating (Non-DB) & producing the final output
		getOutput();
		

	}

	
	/**
	 * Parsing an ALS input file into data objects for validating against the database
	 * 
	 */	
	private static void parseExcel() throws IOException, InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(new File(INPUT_XLSX_FILE_PATH));
		
        logger.debug("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");
        
        // print all the sheets in the workbook
        /*for (Sheet sheet : workbook) {
        	logger.debug("Sheet name: "+sheet.getSheetName());
        }*/

        alsData = new ALSData();
        alsData.setCrfDrafts(getCrfDrafts(workbook.getSheetAt(0)));
        alsData.setForms(getForms(workbook.getSheetAt(1)));
        alsData.setFields(getFields(workbook.getSheetAt(2)));
        alsData.setDataDictionaryEntries(getDataDictionaryEntries(workbook.getSheetAt(5)));
        alsData.setUnitDictionaryEntries(getUnitDictionaryEntries(workbook.getSheetAt(7)));
        
        
        logger.debug("alsData : "+alsData.getRaveProtocolName());
        logger.debug("alsData Primary Form ID: "+alsData.getCrfDrafts().get(0).getPrimaryFormOid());
        logger.debug("CRFData objects: "+(alsData.getCrfDrafts()).size());
        logger.debug("alsData Form ID: "+alsData.getForms().get(8).getFormOId());
        logger.debug("alsData Form ID: "+alsData.getForms().get(5).getDraftFormName());        
        logger.debug("alsData Draft Form Active : "+alsData.getForms().get(4).getDraftFormActive());
        logger.debug("Form objects: "+(alsData.getForms()).size());
        logger.debug("Field objects: "+(alsData.getFields()).size());
        logger.debug("Data dictionary objects: "+(alsData.getDataDictionaryEntries()).size());
        logger.debug("Unit dictionary objects: "+(alsData.getUnitDictionaryEntries()).size());

        workbook.close();		

	}
	
	/**
	 * @param Sheet 
	 * @return List ALSCrfDraft
	 * Populates a collection of ALSCrfDraft objects parsed out of the ALS input file
	 * 
	 */
	
	private static List<ALSCrfDraft> getCrfDrafts(Sheet sheet) throws IOException {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    	Date date = new Date();    	
		List<ALSCrfDraft> crfDrafts = new ArrayList<ALSCrfDraft>();
		if (sheet.getSheetName().equalsIgnoreCase("CRFDraft")) {
            logger.debug("I.Protocol Report Header ");
            logger.debug("Name of person who ran the Congruence Checker");
            logger.debug("Date of Report - "+dateFormat.format(date));
            alsData.setReportDate(dateFormat.format(date));
        	Row newRow = sheet.getRow(1);
        	Cell newCell = newRow.getCell(2);
        	String cellValue = dataFormatter.formatCellValue(newCell);
        	logger.debug("Rave Protocol Name - "+cellValue);
        	alsData.setRaveProtocolName(cellValue);
        	newCell = newRow.getCell(4);
        	cellValue = dataFormatter.formatCellValue(newCell);
        	logger.debug("Rave Protocol Number - "+cellValue);
        	alsData.setRaveProtocolNumber(cellValue);
        	ALSCrfDraft crfDraft = new ALSCrfDraft();
        	crfDraft.setDraftName(dataFormatter.formatCellValue(newRow.getCell(0)));
        	crfDraft.setDeleteExisting(Boolean.valueOf((dataFormatter.formatCellValue(newRow.getCell(1)))));
        	crfDraft.setProjectName(dataFormatter.formatCellValue(newRow.getCell(2)));
        	crfDraft.setProjectType(dataFormatter.formatCellValue(newRow.getCell(3)));        	
        	crfDraft.setPrimaryFormOid(dataFormatter.formatCellValue(newRow.getCell(4)));
        	crfDraft.setDefaultMatrixOid(dataFormatter.formatCellValue(newRow.getCell(5)));
        	crfDraft.setConfirmationMessage(dataFormatter.formatCellValue(newRow.getCell(6)));
        	crfDraft.setSignPrompt(dataFormatter.formatCellValue(newRow.getCell(7)));        	        	
        	crfDraft.setLabStandardGroup(dataFormatter.formatCellValue(newRow.getCell(8)));
        	crfDraft.setReferenceLabs(dataFormatter.formatCellValue(newRow.getCell(9)));
        	crfDraft.setAlertLabs(dataFormatter.formatCellValue(newRow.getCell(10)));        	
        	crfDraft.setSyncOidProject(dataFormatter.formatCellValue(newRow.getCell(11)));
        	crfDraft.setSyncOidDraft(dataFormatter.formatCellValue(newRow.getCell(12)));
        	crfDraft.setSyncOidProjectType(dataFormatter.formatCellValue(newRow.getCell(13)));
        	crfDraft.setSyncOidOriginalVersion(Boolean.valueOf((dataFormatter.formatCellValue(newRow.getCell(14)))));
        	crfDrafts.add(crfDraft);        	
		}	else {
			logger.debug("Incorrect sheet name. Should be CRFDraft");
		}	
		return crfDrafts;
	}			
	
	/**
	 * @param Sheet 
	 * @return List ALSForm
	 * Populates a collection of Form objects parsed out of the ALS input file
	 * 
	 */	
	private static List<ALSForm> getForms(Sheet sheet) throws IOException {
		List<ALSForm> forms = new ArrayList<ALSForm>();
		if (sheet.getSheetName().equalsIgnoreCase("Forms")) {			
        	Iterator<Row> rowIterator = sheet.rowIterator();
        	Row row = rowIterator.next();        	
            while (rowIterator.hasNext()) {
            	row = rowIterator.next();
    			ALSForm form = new ALSForm(); 
    			if (row.getCell(0)!=null) {    			
    			form.setFormOId(dataFormatter.formatCellValue(row.getCell(0)));
    			form.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(1))));    			
    			form.setDraftFormName(dataFormatter.formatCellValue(row.getCell(2)));
    			form.setDraftFormActive(dataFormatter.formatCellValue(row.getCell(3)));
    			form.setHelpText(dataFormatter.formatCellValue(row.getCell(4)));
    			form.setIsTemplate(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(5))));
    			form.setIsSignRequired(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(6))));
    			form.setIsEproForm(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(7))));
    			form.setViewRestrictions(dataFormatter.formatCellValue(row.getCell(8)));
    			form.setEntryRestrictions(dataFormatter.formatCellValue(row.getCell(9)));
    			form.setLogDirection(dataFormatter.formatCellValue(row.getCell(10)));
    			form.setDdeOption(dataFormatter.formatCellValue(row.getCell(11)));
    			form.setConfirmationStyle(dataFormatter.formatCellValue(row.getCell(12)));
    			form.setLinkFolderOid(dataFormatter.formatCellValue(row.getCell(13)));
    			form.setLinkFormOid(dataFormatter.formatCellValue(row.getCell(14)));    			
    			forms.add(form);
    			}
            }
			
		}	else {
			logger.debug("Incorrect sheet name. Should be Forms");
		}			
		return forms;
	}
	
	/**
	 * @param Sheet 
	 * @return List ALSField
	 * Populates a collection of Field (Question) objects parsed out of the ALS input file
	 * 
	 */		
	private static List<ALSField> getFields(Sheet sheet) throws IOException {
		List<ALSField> fields = new ArrayList<ALSField>();
		if (sheet.getSheetName().equalsIgnoreCase("Fields")) {
        	Iterator<Row> rowIterator = sheet.rowIterator();
        	Row row = rowIterator.next();        	
            while (rowIterator.hasNext()) {
            	row = rowIterator.next();
            	ALSField field = new ALSField();
            	if (row.getCell(0)!=null) {
            	field.setFormOid(dataFormatter.formatCellValue(row.getCell(0)));
            	field.setFieldOid(dataFormatter.formatCellValue(row.getCell(1)));
            	field.setOrdinal(dataFormatter.formatCellValue(row.getCell(2)));
            	field.setDraftFieldNumber(dataFormatter.formatCellValue(row.getCell(3)));
            	field.setDraftFieldName(dataFormatter.formatCellValue(row.getCell(4)));
            	field.setDraftFieldActive(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(5))));
            	field.setVariableOid(dataFormatter.formatCellValue(row.getCell(6)));
            	field.setDataFormat(dataFormatter.formatCellValue(row.getCell(7)));
            	field.setDataDictionaryName(dataFormatter.formatCellValue(row.getCell(8)));
            	field.setUnitDictionaryName(dataFormatter.formatCellValue(row.getCell(9)));
            	field.setCodingDictionary(dataFormatter.formatCellValue(row.getCell(10)));
            	field.setControlType(dataFormatter.formatCellValue(row.getCell(11)));
            	field.setAcceptableFileExtensions(dataFormatter.formatCellValue(row.getCell(12)));
            	field.setIndentlevel(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(13))));
            	field.setPreText(dataFormatter.formatCellValue(row.getCell(14)));
            	field.setFixedUnit(dataFormatter.formatCellValue(row.getCell(15)));
            	field.setHeaderText(dataFormatter.formatCellValue(row.getCell(16)));
            	field.setHelpText(dataFormatter.formatCellValue(row.getCell(17)));
            	field.setSourceDocument(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(18))));
            	field.setIsLog(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(19))));
            	field.setDefaultValue(dataFormatter.formatCellValue(row.getCell(20)));
            	field.setSasLabel(dataFormatter.formatCellValue(row.getCell(21)));
            	field.setSasFormat(dataFormatter.formatCellValue(row.getCell(22)));
            	field.setEproFormat(dataFormatter.formatCellValue(row.getCell(23)));
            	field.setIsRequired(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(24))));
            	field.setQueryFutureDate(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(25))));
            	field.setIsVisible(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(26))));
            	field.setIsTranslationRequired(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(27))));
            	field.setAnalyteName(dataFormatter.formatCellValue(row.getCell(28)));
            	field.setIsClinicalSignificance(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(29))));
            	field.setQueryNonConformance(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(30))));
            	field.setOtherVisits(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(31))));
            	field.setCanSetRecordDate(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(32))));
            	field.setCanSetDataPageDate(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(33))));
            	field.setCanSetInstanceDate(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(34))));
            	field.setCanSetSubjectDate(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(35))));
            	field.setDoesNotBreakSignature(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(36))));
            	field.setLowerRange(dataFormatter.formatCellValue(row.getCell(37)));
            	field.setUpperRange(dataFormatter.formatCellValue(row.getCell(38)));
            	field.setNcLowerRange(dataFormatter.formatCellValue(row.getCell(39)));
            	field.setNcUpperRange(dataFormatter.formatCellValue(row.getCell(40)));
            	field.setViewRestrictions(dataFormatter.formatCellValue(row.getCell(41)));
            	field.setEntryRestrictions(dataFormatter.formatCellValue(row.getCell(42)));
            	field.setReviewGroups(dataFormatter.formatCellValue(row.getCell(43)));
            	field.setIsVisualVerify(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(44))));
            	fields.add(field);
              }
            }			
			
		}	else {
			logger.debug("Incorrect sheet name. Should be Fields");
		}					
		
		return fields;
	}		
	
	/**
	 * @param Sheet 
	 * @return List ALSDataDictionaryEntry
	 * Populates a collection of Data Dictionary Entries parsed out of the ALS input file
	 * 
	 */		
	private static List<ALSDataDictionaryEntry> getDataDictionaryEntries(Sheet sheet) {
		List<ALSDataDictionaryEntry> dataDictionaryEntries = new ArrayList<ALSDataDictionaryEntry>();
		if (sheet.getSheetName().equalsIgnoreCase("DataDictionaryEntries")) {
        	Iterator<Row> rowIterator = sheet.rowIterator();
        	Row row = rowIterator.next();        	
            while (rowIterator.hasNext()) {            	
            	row = rowIterator.next();
            	if (row.getCell(0)!=null) {	
		            	ALSDataDictionaryEntry dde = new ALSDataDictionaryEntry();
		            	dde.setDataDictionaryName(dataFormatter.formatCellValue(row.getCell(0)));
		            	dde.setCodedData(dataFormatter.formatCellValue(row.getCell(1)));
		            	dde.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(2))));
		            	dde.setUserDataString(dataFormatter.formatCellValue(row.getCell(3)));
		            	dde.setSpecify(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(4))));
		            	dataDictionaryEntries.add(dde);
            		}
            	}					
		}	else {
			logger.debug("Incorrect sheet name. Should be DataDictionaryEntries");
		}							
		
		return dataDictionaryEntries;
	}			
	
	/**
	 * @param Sheet 
	 * @return List ALSUnitDictionaryEntry
	 * Populates a collection of Unit Dictionary Entries parsed out of the ALS input file
	 * 
	 */		
	private static List<ALSUnitDictionaryEntry> getUnitDictionaryEntries(Sheet sheet) {
		List<ALSUnitDictionaryEntry> dataDictionaryEntries = new ArrayList<ALSUnitDictionaryEntry>();
		if (sheet.getSheetName().equalsIgnoreCase("UnitDictionaryEntries")) {
        	Iterator<Row> rowIterator = sheet.rowIterator();
        	Row row = rowIterator.next();        	
            while (rowIterator.hasNext()) {            	
            	row = rowIterator.next();
            	if (row.getCell(0)!=null) {	
            			ALSUnitDictionaryEntry ude = new ALSUnitDictionaryEntry();
		            	ude.setUnitDictionaryName(dataFormatter.formatCellValue(row.getCell(0)));
		            	ude.setCodedUnit(dataFormatter.formatCellValue(row.getCell(1)));
		            	ude.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(2))));
		            	ude.setConstantA(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(3))));
		            	ude.setConstantB(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(4))));
		            	ude.setConstantC(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(5))));
		            	ude.setConstantK(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(6))));
		            	ude.setUnitString(dataFormatter.formatCellValue(row.getCell(7)));
		            	dataDictionaryEntries.add(ude);
            		}
            	}					
		}	else {
			logger.debug("Incorrect sheet name. Should be UnitDictionaryEntries");
		}							
		
		return dataDictionaryEntries;
	}	
	
	/**
	 * @param  
	 * @return 
	 * Populates the output object after initial validation and parsing of data
	 * 
	 */		
	private static void getOutput() {
		cccReport = new CCCReport();
		cccReport.setReportOwner("VS");
		cccReport.setReportDate(alsData.getReportDate());
		cccReport.setRaveProtocolName(alsData.getRaveProtocolName());
		cccReport.setRaveProtocolNumber(alsData.getRaveProtocolNumber());
		List<CCCForm> formsList = new ArrayList<CCCForm>();
		CCCForm form  = new CCCForm();
		String formName = "";
		List<CCCQuestion> questionsList = new ArrayList<CCCQuestion>();		
		ALSDataDictionaryEntry dde = new ALSDataDictionaryEntry();
		for (ALSField alsField : alsData.getFields()) {
			if (formName == "" || !formName.equals(alsField.getFormOid())) {
				form.setQuestions(questionsList);				
				formName = alsField.getFormOid();
				form = new CCCForm();
				form.setRaveFormOId(formName);
				questionsList = new ArrayList<CCCQuestion>();
			}
			CCCQuestion question = new CCCQuestion();
			question.setFieldOrder(alsField.getOrdinal()); // which tab is it from - Forms/Fields?
			String draftFieldName = alsField.getDraftFieldName();
			if (draftFieldName.indexOf("PID") > -1 && draftFieldName.indexOf("_V") > -1) {
				String idVersion = draftFieldName.substring(draftFieldName.indexOf("PID"), draftFieldName.length());
				question.setCdePublicId(idVersion.substring(3, idVersion.indexOf("_")));
				question.setCdeVersion((idVersion.substring(idVersion.indexOf("_V")+2, idVersion.length())).replaceAll("_", "."));
				question.setNciCategory("NRDS"); //"NRDS" "Mandatory Module: {CRF ID/V}", "Optional Module {CRF ID/V}", "Conditional Module: {CRF ID/V}"
				question.setQuestionCongruenceStatus("MATCH");//Valid results are "ERROR" "Match"
				question.setMessage("Error message"); // Will be replaced with the caDSR db validation result error message, if any.
				question.setRaveFieldLabel(alsField.getPreText());
				question.setRaveFieldLabelResult("Error/Match"); // Will be replaced with the caDSR db validation result
				question.setCdePermitQuestionTextChoices(""); // From the caDSR DB - docText
				question.setRaveControlType(alsField.getControlType());
				question.setControlTypeResult("Match"); // Will be replaced with the caDSR db validation result
				question.setCdeValueDomainType(""); // from caDSR DB - Value Domain	Enumerated/NonEnumerated
				List<String> pvList = new ArrayList<String>();
				if (alsField.getDataDictionaryName().equals(dde.getDataDictionaryName()))
					pvList.add(dde.getUserDataString());
				question.setRaveCodedData(pvList); // Data dictionary name and its corresponding entries - All the Permissible values
				question.setCodedDataResult("Error/Match");  // Will be replaced with the caDSR db validation result
				question.setAllowableCdeValue("");
				question.setRaveUserString(dde.getUserDataString());
				question.setPvResult("Error/match"); // Will be replaced with the caDSR db validation result
				question.setAllowableCdeTextChoices("A|B|C|D"); // Test values - will be replaced with the PV value meanings from caDSR db
				questionsList.add(question);
			}

		}
		formsList.add(form);
		cccReport.setCccForms(formsList);		
	}	
}
