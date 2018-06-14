package gov.nih.nci.cadsr.parser;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import gov.nih.nci.cadsr.data.ALSCrfDraft;
import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.ALSUnitDictionaryEntry;
import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;

public class AlsParser {

	public static final Logger logger = Logger.getLogger(AlsParser.class);
	public static ALSData alsData;
	public static CCCReport cccReport;
	public static DataFormatter dataFormatter = new DataFormatter();
	public static String formHeader_1 = "VIEW OF EXPANDED RESULTS FOR ";
	public static String formHeader_2 = " FORM";	
	public static String summaryFormsHeader = "Report Summary - Click on Form Name to expand results";

	public static void main(String[] args) throws IOException, InvalidFormatException {

		Properties prop = new Properties();
		InputStream input = null;

		String filename = "config.properties";
		input = AlsParser.class.getClassLoader().getResourceAsStream(filename);
		prop.load(input);
		String INPUT_XLSX_FILE_PATH = "target/classes/" + prop.getProperty("ALS-INPUT-FILE");
		String OUTPUT_XLSX_FILE_PATH = "target/" + prop.getProperty("VALIDATOR-OUTPUT-FILE");


		// Parsing the ALS file in Excel format (XLSX). If this file has an XML
		// extension
		// then it needs to be converted to an XLSX file before being provided
		// as the input to the parser.
		parseExcel(INPUT_XLSX_FILE_PATH);
		buildAls();
		// Validating (Non-DB) & producing the final output
		getOutputForReport();

		// Writing the output in excel format
		writeExcel(OUTPUT_XLSX_FILE_PATH);

	}

	/**
	 * Parsing an ALS input file into data objects for validating against the
	 * database
	 * 
	 */
	private static void parseExcel(String INPUT_XLSX_FILE_PATH) throws IOException, InvalidFormatException {
		Workbook workbook = WorkbookFactory.create(new File(INPUT_XLSX_FILE_PATH));

		logger.debug("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

		// print all the sheets in the workbook
		/*
		 * for (Sheet sheet : workbook) { logger.debug("Sheet name: "
		 * +sheet.getSheetName()); }
		 */

		alsData = new ALSData();
		alsData.setCrfDrafts(getCrfDrafts(workbook.getSheetAt(0)));
		alsData.setForms(getForms(workbook.getSheetAt(1)));
		alsData.setFields(getFields(workbook.getSheetAt(2)));
		alsData.setDataDictionaryEntries(getDataDictionaryEntries(workbook.getSheetAt(5)));
		alsData.setUnitDictionaryEntries(getUnitDictionaryEntries(workbook.getSheetAt(7)));

		logger.debug("alsData : " + alsData.getRaveProtocolName());
		logger.debug("alsData Primary Form ID: " + alsData.getCrfDrafts().get(0).getPrimaryFormOid());
		logger.debug("CRFData objects: " + (alsData.getCrfDrafts()).size());
		logger.debug("alsData Form ID #9: " + alsData.getForms().get(8).getFormOId());
		logger.debug("alsData Form Name #6: " + alsData.getForms().get(5).getDraftFormName());
		logger.debug("Form objects: " + (alsData.getForms()).size());
		logger.debug("Field objects: " + (alsData.getFields()).size());
		logger.debug("Data dictionary objects: " + (alsData.getDataDictionaryEntries().size()));
		logger.debug("Unit dictionary objects: " + (alsData.getUnitDictionaryEntries()).size());

		workbook.close();
		System.out.println("Done");

	}

	/**
	 * @param Sheet
	 * @return List ALSCrfDraft Populates a collection of ALSCrfDraft objects
	 *         parsed out of the ALS input file
	 * 
	 */

	private static List<ALSCrfDraft> getCrfDrafts(Sheet sheet) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date date = new Date();
		List<ALSCrfDraft> crfDrafts = new ArrayList<ALSCrfDraft>();
		if (sheet.getSheetName().equalsIgnoreCase("CRFDraft")) {
			logger.debug("I.Protocol Report Header ");
			logger.debug("Name of person who ran the Congruence Checker");
			logger.debug("Date of Report - " + dateFormat.format(date));
			alsData.setReportDate(dateFormat.format(date));
			Row newRow = sheet.getRow(1);
			Cell newCell = newRow.getCell(2);
			String cellValue = dataFormatter.formatCellValue(newCell);
			logger.debug("Rave Protocol Name - " + cellValue);
			alsData.setRaveProtocolName(cellValue);
			newCell = newRow.getCell(4);
			cellValue = dataFormatter.formatCellValue(newCell);
			logger.debug("Rave Protocol Number - " + cellValue);
			alsData.setRaveProtocolNumber(cellValue);
			ALSCrfDraft crfDraft = new ALSCrfDraft();
			crfDraft.setDraftName(dataFormatter.formatCellValue(newRow.getCell(0)));
			crfDraft.setProjectName(dataFormatter.formatCellValue(newRow.getCell(2)));
			crfDraft.setPrimaryFormOid(dataFormatter.formatCellValue(newRow.getCell(4)));
			crfDrafts.add(crfDraft);
		} else {
			logger.debug("Incorrect sheet name. Should be CRFDraft");
		}
		return crfDrafts;
	}

	/**
	 * @param Sheet
	 * @return List ALSForm Populates a collection of Form objects parsed out of
	 *         the ALS input file
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
				if (row.getCell(0) != null) {
					form.setFormOId(dataFormatter.formatCellValue(row.getCell(0)));
					form.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(1))));
					form.setDraftFormName(dataFormatter.formatCellValue(row.getCell(2)));
					forms.add(form);
				}
			}

		} else {
			logger.debug("Incorrect sheet name. Should be Forms");
		}
		return forms;
	}

	/**
	 * @param Sheet
	 * @return List ALSField Populates a collection of Field (Question) objects
	 *         parsed out of the ALS input file
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
				if (row.getCell(0) != null) {
					field.setFormOid(dataFormatter.formatCellValue(row.getCell(0)));
					field.setFieldOid(dataFormatter.formatCellValue(row.getCell(1)));
					field.setOrdinal(dataFormatter.formatCellValue(row.getCell(2)));
					field.setDraftFieldName(dataFormatter.formatCellValue(row.getCell(4)));
					field.setDataFormat(dataFormatter.formatCellValue(row.getCell(7)));
					field.setDataDictionaryName(dataFormatter.formatCellValue(row.getCell(8)));
					field.setControlType(dataFormatter.formatCellValue(row.getCell(11)));
					field.setPreText(dataFormatter.formatCellValue(row.getCell(14)));
					field.setFixedUnit(dataFormatter.formatCellValue(row.getCell(15)));
					fields.add(field);
				}
			}

		} else {
			logger.debug("Incorrect sheet name. Should be Fields");
		}

		return fields;
	}

	/**
	 * @param Sheet
	 * @return List ALSDataDictionaryEntry Populates a collection of Data
	 *         Dictionary Entries parsed out of the ALS input file
	 * 
	 */
	private static Map<String, ALSDataDictionaryEntry> getDataDictionaryEntries(Sheet sheet) {
		Map<String, ALSDataDictionaryEntry> ddeMap = new HashMap<String, ALSDataDictionaryEntry>();
		if (sheet.getSheetName().equalsIgnoreCase("DataDictionaryEntries")) {
			ALSDataDictionaryEntry dde = new ALSDataDictionaryEntry();
			List<Integer> ordinal = new ArrayList<Integer>();
			List<String> cd = new ArrayList<String>();
			List<String> uds = new ArrayList<String>();
			List<Boolean> specify = new ArrayList<Boolean>();
			String ddName = "";
			Iterator<Row> rowIterator = sheet.rowIterator();
			Row row = rowIterator.next();
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				if (row.getCell(0) != null) {
					if (ddName.equals("")) {
						ddName = dataFormatter.formatCellValue(row.getCell(0));
					}
					if (!ddName.equals(dataFormatter.formatCellValue(row.getCell(0)))) {
						dde.setCodedData(cd);
						dde.setOrdinal(ordinal);
						dde.setUserDataString(uds);
						dde.setDataDictionaryName(ddName);
						if (!(ddeMap.containsKey(dde.getDataDictionaryName()))) {
							ddeMap.put(dde.getDataDictionaryName(), dde);
						}
						ddName = dataFormatter.formatCellValue(row.getCell(0));
						dde = new ALSDataDictionaryEntry();
						dde.setDataDictionaryName(dataFormatter.formatCellValue(row.getCell(0)));
						ordinal = new ArrayList<Integer>();
						cd = new ArrayList<String>();
						uds = new ArrayList<String>();
						specify = new ArrayList<Boolean>();
					}
					cd.add(dataFormatter.formatCellValue(row.getCell(1)));
					ordinal.add(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(2))));
					uds.add(dataFormatter.formatCellValue(row.getCell(3)));
					specify.add(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(4))));
				}
			}
			dde.setCodedData(cd);
			dde.setOrdinal(ordinal);
			dde.setUserDataString(uds);
			dde.setDataDictionaryName(ddName);
			if (!(ddeMap.containsKey(dde.getDataDictionaryName()))) {
				ddeMap.put(dde.getDataDictionaryName(), dde);
			}
		} else {
			logger.debug("Incorrect sheet name. Should be DataDictionaryEntries");
		}
		return ddeMap;
	}

	/**
	 * @param Sheet
	 * @return List ALSUnitDictionaryEntry Populates a collection of Unit
	 *         Dictionary Entries parsed out of the ALS input file
	 * 
	 */
	private static List<ALSUnitDictionaryEntry> getUnitDictionaryEntries(Sheet sheet) {
		List<ALSUnitDictionaryEntry> dataDictionaryEntries = new ArrayList<ALSUnitDictionaryEntry>();
		if (sheet.getSheetName().equalsIgnoreCase("UnitDictionaryEntries")) {
			Iterator<Row> rowIterator = sheet.rowIterator();
			Row row = rowIterator.next();
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				if (row.getCell(0) != null) {
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
		} else {
			logger.debug("Incorrect sheet name. Should be UnitDictionaryEntries");
		}

		return dataDictionaryEntries;
	}

	/**
	 * @param
	 * @return Populates the output object for the report after initial
	 *         validation and parsing of data
	 * 
	 */
	private static void getOutputForReport() {
		cccReport = new CCCReport();
		cccReport.setReportOwner("<NAME OF PERSON WHO THE REPORT IS FOR>"); // From the user input through the browser
		cccReport.setReportDate(alsData.getReportDate());
		cccReport.setRaveProtocolName(alsData.getRaveProtocolName());
		cccReport.setRaveProtocolNumber(alsData.getRaveProtocolNumber());
		List<CCCForm> formsList = new ArrayList<CCCForm>();
		CCCForm form = new CCCForm();
		String formName = "";
		List<CCCQuestion> questionsList = new ArrayList<CCCQuestion>();
		Map<String, ALSDataDictionaryEntry> ddeMap = alsData.getDataDictionaryEntries();
		for (ALSField alsField : alsData.getFields()) {
			if (formName.equals(""))
				formName = alsField.getFormOid();
			if (!formName.equals("OID")) {
				if (!formName.equals(alsField.getFormOid())) {
					form.setQuestions(questionsList);
					form.setRaveFormOId(formName);
					formsList.add(form);
					formName = alsField.getFormOid();
					form = new CCCForm();
					questionsList = new ArrayList<CCCQuestion>();
				}
				CCCQuestion question = new CCCQuestion();
				question.setFieldOrder(alsField.getOrdinal()); // which sheet is
																// it from -
																// Forms/Fields?
				String draftFieldName = alsField.getDraftFieldName();
				if (draftFieldName.indexOf("PID") > -1 && draftFieldName.indexOf("_V") > -1) {
					String idVersion = draftFieldName.substring(draftFieldName.indexOf("PID"), draftFieldName.length());
					question.setCdePublicId(idVersion.substring(3, idVersion.indexOf("_")));
					question.setCdeVersion((idVersion.substring(idVersion.indexOf("_V") + 2, idVersion.length()))
							.replaceAll("_", "."));
					question.setNciCategory("NRDS"); // "NRDS" "Mandatory Module: {CRF ID/V}", "Optional Module {CRF ID/V}", "Conditional Module: {CRF ID/V}"
					question.setQuestionCongruenceStatus("MATCH");// Valid results are "ERROR"/"Match"
					question.setMessage("Error message"); // Will be replaced with the caDSR db validation result error message, if any.
					question.setRaveFieldLabel(alsField.getPreText());
					question.setRaveFieldLabelResult("Error/Match"); // Will be replaced with the caDSR db validation result
					question.setCdePermitQuestionTextChoices(""); // From the caDSR DB - docText
					question.setRaveControlType(alsField.getControlType());
					question.setControlTypeResult("Match"); // Will be replaced with the caDSR db validation result
					question.setCdeValueDomainType(""); // from caDSR DB - Value Domain Enumerated/NonEnumerated

					for (String key : ddeMap.keySet()) {
						if (key.equals(alsField.getDataDictionaryName())) {
							question.setRaveCodedData(ddeMap.get(key).getCodedData()); // Data dictionary name and its corresponding entries - All the Permissible values
							question.setRaveUserString(ddeMap.get(key).getUserDataString());
						}
					}
					question.setCodedDataResult("Error/Match"); // Will be replaced with the caDSR db validation result
					question.setAllowableCdeValue("");

					question.setPvResult("Error/match"); // Will be replaced with the caDSR db validation result
					question.setAllowableCdeTextChoices("A|B|C|D"); // Test values - will be replaced with the PV value meanings from caDSR db
					questionsList.add(question);
				} else {
					question.setRaveFieldLabel(alsField.getPreText());
					questionsList.add(question);
				}
			}
		}
		form.setQuestions(questionsList);
		form.setRaveFormOId(formName);
		formsList.add(form);
		cccReport.setCccForms(formsList);
		for (CCCForm forms : cccReport.getCccForms()) {
			logger.debug("Form name: " + forms.getRaveFormOId());
			logger.debug("Questions list: " + forms.getQuestions().size());
			for (CCCQuestion question : forms.getQuestions()) {
				if (question.getRaveCodedData() != null && question.getRaveCodedData().size() != 0)
					logger.debug("Question coded data list: " + question.getRaveCodedData().size());
				if (question.getRaveUserString() != null && question.getRaveUserString().size() != 0)
					logger.debug("Questions user string data list: " + question.getRaveUserString().size());
			}
		}
		logger.debug("Output object forms count: " + cccReport.getCccForms().size());

	}

	/**
	 * @param
	 * @return Attempting to build a relationship between the data objects from
	 *         ALS file Form -> Fields -> Data Dictionary Entry This method is
	 *         optional for the parser to work as the straightforward objects of
	 *         Forms, Fields & Data Dictionary Entries, just by themselves will
	 *         work. Having an interconnected data structure might help in
	 *         better processing for validation
	 * 
	 */
	private static void buildAls() {
		for (ALSField field : alsData.getFields()) {
			for (ALSForm form : alsData.getForms()) {
				if (field.getFormOid().equals(form.getFormOId())) {
					form.getFields().add(field);
				}
			}
			for (String key : alsData.getDataDictionaryEntries().keySet()) {
				if (field.getDataDictionaryName().equals(key)) {
					if (!(field.getDdeMap().containsKey(key))) {
						field.getDdeMap().put(key, alsData.getDataDictionaryEntries().get(key));
					}
				}
			}
			logger.debug("DDE Map for " + field.getFieldOid() + " : " + field.getDdeMap().size());
		}
	}

	/**
	 * @param
	 * @return Writing the final output report object into an excel
	 * 
	 */
	private static void writeExcel(String OUTPUT_XLSX_FILE_PATH) {

		// TODO - writing the report output to excel for download
		String fileName = OUTPUT_XLSX_FILE_PATH;
		Row row;
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Summary");
		Map<String, String> summaryLabels = new LinkedHashMap<String, String>();
		summaryLabels.put("CDE Congruency Checker Report for ", cccReport.getReportOwner());
		summaryLabels.put("Rave Protocol name ", cccReport.getRaveProtocolName());
		summaryLabels.put("Rave Protocol number ", cccReport.getRaveProtocolNumber());
		summaryLabels.put("Date Validated ", cccReport.getReportDate());
		summaryLabels.put("# Forms in protocol ", String.valueOf(cccReport.getCccForms().size()));
		summaryLabels.put("# Total Forms Congruent ", String.valueOf(cccReport.getCccForms().size()));
		summaryLabels.put("# Total Questions Checked ", String.valueOf(alsData.getFields().size()));
		summaryLabels.put("# Total Questions with Warnings ", "");
		summaryLabels.put("# Total Questions with Errors ", "");
		summaryLabels.put("# Total Questions without associated CDE ", "");
		summaryLabels.put("# Required NRDS Questions missing ", "");
		summaryLabels.put("# Required NRDS Questions Congruent ", "");
		summaryLabels.put("# Required NRDS Questions With Warnings ", "");
		summaryLabels.put("# Required NRDS Questions With Errors ", "");
		summaryLabels.put("# NCI Standard Template Mandatory Modules Questions not used in Protocol ", "");
		summaryLabels.put("# NCI Standard Template Mandatory Modules Questions Congruent ", "");
		summaryLabels.put("# NCI Standard Template Mandatory Modules Questions With Errors ", "");
		summaryLabels.put("# NCI Standard Template Mandatory Modules Questions With Warnings ", "");

		int rowNum = 0;
		logger.debug("Creating excel");
		for (Map.Entry<String, String> label : summaryLabels.entrySet()) {
			row = sheet.createRow(rowNum++);
			int colNum = 0;
			if ((label.getKey().equals("# Forms in protocol ")))
				row = sheet.createRow(rowNum++);
			Cell cell = row.createCell(colNum++);
			cell.setCellValue((String) label.getKey());
			cell = row.createCell(colNum + 10);
			cell.setCellValue((String) label.getValue());
		}
		row = sheet.createRow(rowNum++);
		row = sheet.createRow(rowNum++);
		Cell newCell = row.createCell(0);
		newCell.setCellValue(summaryFormsHeader);
		newCell = row.createCell(11);
		newCell.setCellValue("Validation Result");
		List<CCCForm> forms = cccReport.getCccForms(); 
		for (CCCForm form : forms) {
			row = sheet.createRow(rowNum++);
			int colNum = 0;
			Cell cell = row.createCell(colNum++);
			cell.setCellValue(form.getRaveFormOId());
			cell = row.createCell(colNum + 10);
			cell.setCellValue("Congruent");
		}

		String[] rowHeaders = { "Rave Form OID", "caDSR Form ID", "Version", "Total Number Of Questions Checked",
				"Field Order", "CDE Public ID", "CDE Version", "NCI Category", "Question Congruency Status", "Message",
				"Rave Field Label", "Rave Field Label Result", "CDE Permitted Question Text Choices",
				"Rave Control Type", "Control Type", "CDE Value Domain Type", "Rave Coded Data", "Coded Data Result",
				"Allowable CDE  Value", "Rave User String", "PV  Result", "Allowable CDE  Value Meaning Text Choices",
				"Rave Field Data Type", "Dataype Result", "CDE Data Type", "Rave UOM", "UOM  Result", "CDE UOM",
				"Rave Length", "Length  Result", "CDE Maximum Length", "Rave Display Format", "Format  Result",
				"CDE Display Format" };
		for (int i = 0; i < 5; i++) {
			XSSFSheet sheet2 = workbook.createSheet(forms.get(i).getRaveFormOId());
			row = sheet2.createRow(0);
			newCell = row.createCell(0);
			newCell.setCellValue(formHeader_1 + forms.get(i).getRaveFormOId() + formHeader_2);
			row = sheet2.createRow(1);
			int colNum = 0;
			// Print row headers in the form sheet
			for (String rowHeader : rowHeaders) {			
				newCell = row.createCell(colNum++);
				newCell.setCellValue(rowHeader);
				//sheet2.autoSizeColumn(colNum);					
			}
			row = sheet2.createRow(2);
			newCell = row.createCell(0);
			newCell.setCellValue(forms.get(i).getRaveFormOId());
			newCell = row.createCell(3);
			newCell.setCellValue(forms.get(i).getQuestions().size());
			
			for (int j = 0; j < forms.get(i).getQuestions().size(); j++) {
				CCCQuestion question = forms.get(i).getQuestions().get(j);
				row = sheet2.createRow(j+4);
				newCell = row.createCell(4);
				newCell.setCellValue(question.getFieldOrder());
				newCell = row.createCell(5);
				newCell.setCellValue(question.getCdePublicId());				
				newCell = row.createCell(6);
				newCell.setCellValue(question.getCdeVersion());								
			}
		}

		try {
			FileOutputStream outputStream = new FileOutputStream(fileName);
			workbook.write(outputStream);
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("File Writing Done");

	}

}
