package gov.nih.nci.cadsr.parser;

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

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.nih.nci.cadsr.data.ALSCrfDraft;
import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.CCCError;
import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;

public class AlsParser {

	private static final Logger logger = Logger.getLogger(AlsParser.class);
	private static CCCReport cccReport;
	private static DataFormatter dataFormatter = new DataFormatter();
	private static String reportDateFormat = "MM/dd/yyyy";
	private static String formHeader_1 = "VIEW OF EXPANDED RESULTS FOR ";
	private static String formHeader_2 = " FORM";	
	private static String summaryFormsHeader = "Report Summary - Click on Form Name to expand results";
	private static String summaryFormsValidResult = "Validation Result";
	private static int summaryFormsValidResultColNum = 11;
	private static String checkerReportOwnerLbl = "CDE Congruency Checker Report for ";
	private static String raveProtocolNameLbl = "Rave Protocol name ";
	private static String raveProtocolNumLbl = "Rave Protocol number ";
	private static String reportDateLbl = "Date Validated ";
	private static String formCountLbl = "# Forms in protocol ";
	private static String totalCountFormLbl = "# Total Forms Congruent ";
	private static String totalQuestCheckLbl = "# Total Questions Checked ";
	private static String totalQuestWarnLbl = "# Total Questions with Warnings ";
	private static String totalQuestErrorLbl = "# Total Questions with Errors ";
	private static String totalunassociatedQuestLbl = "# Total Questions without associated CDE ";
	private static String reqQuestMissLbl = "# Required NRDS Questions missing ";
	private static String reqNrdsQuestCongLbl = "# Required NRDS Questions Congruent ";
	private static String reqNrdsQuestWarnLbl = "# Required NRDS Questions With Warnings ";
	private static String reqNrdsQuestErrorLbl = "# Required NRDS Questions With Errors ";
	private static String nciStdTempQuestLbl = "# NCI Standard Template Mandatory Modules Questions not used in Protocol ";
	private static String nciStdTempCongLbl = "# NCI Standard Template Mandatory Modules Questions Congruent ";
	private static String nciStdTempErrorLbl = "# NCI Standard Template Mandatory Modules Questions With Errors ";
	private static String nciStdTempWarnLbl = "# NCI Standard Template Mandatory Modules Questions With Warnings ";	
	private static String crfDraftSheetName = "CRFDraft";
	private static String formsSheetName = "Forms";
	private static String fieldsSheetName = "Fields";
	private static String dataDictionarySheetName = "DataDictionaryEntries";	
	private static String errorSheetMissing = "Sheet missing in the ALS input file";
	private static int formStartRow = 4;
	private static int formStartColumn = 4;	
	private static int allowableCdeValueCol = 19;
	private static int codedDataColStart = 16;
	private static int crfDraftStartRow = 1;

	public static void main(String[] args) {

		CCCReport cccReport = getCCCReport();
		logger.debug("CCC report protocol - "+cccReport.getRaveProtocolName()+cccReport.getRaveProtocolNumber() + "Forms : "+cccReport.getCccForms().size());
	}
	
	
	// Method to expose as service
	public static CCCReport getCCCReport() {
		CCCReport cccReport = new CCCReport();
		CCCError cccError = getErrorObject();
		Properties prop = new Properties();
		InputStream input = null;
		String filename = "config.properties";		
		try {
		input = AlsParser.class.getClassLoader().getResourceAsStream(filename);
		prop.load(input);
		String INPUT_XLSX_FILE_PATH = "target/classes/" + prop.getProperty("ALS-INPUT-FILE");
		String OUTPUT_XLSX_FILE_PATH = "target/" + prop.getProperty("VALIDATOR-OUTPUT-FILE");

		// Parsing the ALS file in Excel format (XLSX). If this file has an XML
		// extension
		// then it needs to be converted to an XLSX file before being provided
		// as the input to the parser.

		ALSData alsData = parseExcel(INPUT_XLSX_FILE_PATH);
		alsData = buildAls(alsData);
		// Validating (Non-DB) & producing the final output
		cccReport  = getOutputForReport(alsData);
		// Writing the output in excel format
		writeExcel(OUTPUT_XLSX_FILE_PATH, alsData);
		} catch (IOException ioe) {
			ioe.printStackTrace();			
			cccError.setErrorDescription(ioe.getMessage());
		} catch (InvalidFormatException ife) {
			ife.printStackTrace();
			cccError.setErrorDescription(ife.getMessage());
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			cccError.setErrorDescription(npe.getMessage());			
		} finally {
			if (cccError.getErrorDescription()!=null) {
				logger.debug("Error Occurred: "+cccError.getErrorDescription());
				cccReport.setCccError(cccError);
			}				
		}		
		
		return cccReport;
	}

	/**
	 * Parsing an ALS input file into data objects for validating against the
	 * database
	 * 
	 */
	private static ALSData parseExcel(String INPUT_XLSX_FILE_PATH) throws IOException, InvalidFormatException, NullPointerException {
		Workbook workbook = WorkbookFactory.create(new File(INPUT_XLSX_FILE_PATH));
		CCCError cccError = getErrorObject();
		ALSData alsData = getAlsDataInstance();
		Sheet sheet = workbook.getSheet(crfDraftSheetName);
		if (sheet!=null)
			alsData = getCrfDraft(sheet, alsData);
		else
			cccError.setErrorDescription(errorSheetMissing+" - "+crfDraftSheetName);
		sheet = workbook.getSheet(formsSheetName);
		if (sheet!=null)
			alsData.setForms(getForms(sheet));
		else
			cccError.setErrorDescription(errorSheetMissing+" - "+formsSheetName);
		sheet = workbook.getSheet(fieldsSheetName);
		if (sheet!=null)
			alsData.setFields(getFields(sheet));
		else
			cccError.setErrorDescription(errorSheetMissing+" - "+fieldsSheetName);
		sheet = workbook.getSheet(dataDictionarySheetName);
		if (sheet!=null)
			alsData.setDataDictionaryEntries(getDataDictionaryEntries(sheet));
		else 		
			cccError.setErrorDescription(errorSheetMissing+" - "+dataDictionarySheetName);		
		workbook.close();
		if (cccError.getErrorDescription()!=null)
			alsData.setCccError(cccError);	
		return alsData;
	}	
	
	/**
	 * @param Sheet
	 * @param ALSData 
	 * @return List ALSCrfDraft Populates a collection of ALSCrfDraft objects
	 *         parsed out of the ALS input file
	 * 
	 */

	private static ALSData getCrfDraft(Sheet sheet, ALSData alsData) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat(reportDateFormat);
		Date date = new Date();
		List<ALSCrfDraft> crfDrafts = new ArrayList<ALSCrfDraft>();
			alsData.setReportDate(dateFormat.format(date));
			Row newRow = sheet.getRow(crfDraftStartRow);
			Cell newCell = newRow.getCell(2);
			String cellValue = dataFormatter.formatCellValue(newCell);
			logger.debug("Rave Protocol Name - " + cellValue);
			alsData.setRaveProtocolName(cellValue);
			newCell = newRow.getCell(4);
			cellValue = dataFormatter.formatCellValue(newCell);
			logger.debug("Rave Protocol Number - " + cellValue);
			alsData.setRaveProtocolNumber(cellValue);
			ALSCrfDraft crfDraft = getAlsCrfDraftInstance();
			crfDraft.setDraftName(dataFormatter.formatCellValue(newRow.getCell(0)));
			crfDraft.setProjectName(dataFormatter.formatCellValue(newRow.getCell(2)));
			crfDraft.setPrimaryFormOid(dataFormatter.formatCellValue(newRow.getCell(4)));
			crfDrafts.add(crfDraft);
			alsData.setCrfDrafts(crfDrafts);
			return alsData;
	}

	/**
	 * @param Sheet
	 * @return List ALSForm Populates a collection of Form objects parsed out of
	 *         the ALS input file
	 * 
	 */
	private static List<ALSForm> getForms(Sheet sheet) throws IOException {
		List<ALSForm> forms = new ArrayList<ALSForm>();
			Iterator<Row> rowIterator = sheet.rowIterator();
			Row row = rowIterator.next();
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				ALSForm form = getAlsFormInstance();
				if (row.getCell(0) != null) {
					form.setFormOId(dataFormatter.formatCellValue(row.getCell(0)));
					form.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(1))));
					form.setDraftFormName(dataFormatter.formatCellValue(row.getCell(2)));
					forms.add(form);
				}
			}
		return forms;
	}

	/**
	 * @param Sheet
	 * @return List ALSField Populates a collection of Field (Question) objects
	 *         parsed out of the ALS input file
	 * 
	 */
	private static List<ALSField> getFields(Sheet sheet) throws NullPointerException {
		List<ALSField> fields = new ArrayList<ALSField>();
		if (sheet.getSheetName().equalsIgnoreCase("Fields")) {
			Iterator<Row> rowIterator = sheet.rowIterator();
			Row row = rowIterator.next();
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				ALSField field = getAlsFieldInstance();
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
	private static Map<String, ALSDataDictionaryEntry> getDataDictionaryEntries(Sheet sheet) throws NullPointerException {
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
	 * @param
	 * @return Populates the output object for the report after initial
	 *         validation and parsing of data
	 * 
	 */
	private static CCCReport getOutputForReport(ALSData alsData) throws NullPointerException {
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
					//TODO remove any excessive decimals - as of now this still allows more than one decimal in version
					question.setCdeVersion((idVersion.substring(idVersion.indexOf("_V") + 2, idVersion.length()))  
							.replaceAll("_", "."));
					question.setNciCategory("NRDS"); // "NRDS" "Mandatory Module: {CRF ID/V}", "Optional Module {CRF ID/V}", "Conditional Module: {CRF ID/V}"
					question.setQuestionCongruencyStatus("MATCH");// Valid results are "ERROR"/"Match"
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
							//question.setCodedDataResult("CHECK"); // Will be replaced with the caDSR db validation result							
						}
					}
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
		return cccReport;
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
	private static ALSData buildAls(ALSData alsData) {
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
		}
		return alsData;
	}

	/**
	 * @param
	 * @return Writing the final output report object into an excel
	 * 
	 */
	private static void writeExcel(String OUTPUT_XLSX_FILE_PATH, ALSData alsData) throws IOException, InvalidFormatException, NullPointerException {

		String fileName = OUTPUT_XLSX_FILE_PATH;
		Row row;
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Summary");
		Map<String, String> summaryLabels = new LinkedHashMap<String, String>();
		summaryLabels.put(checkerReportOwnerLbl, cccReport.getReportOwner());
		summaryLabels.put(raveProtocolNameLbl, cccReport.getRaveProtocolName());
		summaryLabels.put(raveProtocolNumLbl, cccReport.getRaveProtocolNumber());
		summaryLabels.put(reportDateLbl, cccReport.getReportDate());
		summaryLabels.put(formCountLbl, String.valueOf(cccReport.getCccForms().size()));
		summaryLabels.put(totalCountFormLbl, String.valueOf(cccReport.getCccForms().size()));
		summaryLabels.put(totalQuestCheckLbl, String.valueOf(alsData.getFields().size()));
		summaryLabels.put(totalQuestWarnLbl, "");
		summaryLabels.put(totalQuestErrorLbl, "");
		summaryLabels.put(totalunassociatedQuestLbl, "");
		summaryLabels.put(reqQuestMissLbl, "");
		summaryLabels.put(reqNrdsQuestCongLbl, "");
		summaryLabels.put(reqNrdsQuestWarnLbl, "");
		summaryLabels.put(reqNrdsQuestErrorLbl, "");
		summaryLabels.put(nciStdTempQuestLbl, "");
		summaryLabels.put(nciStdTempCongLbl, "");
		summaryLabels.put(nciStdTempErrorLbl, "");
		summaryLabels.put(nciStdTempWarnLbl, "");
		
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
		newCell = row.createCell(summaryFormsValidResultColNum);
		newCell.setCellValue(summaryFormsValidResult);
		List<CCCForm> forms = cccReport.getCccForms(); 
		for (CCCForm form : forms) {
			row = sheet.createRow(rowNum++);
			int colNum = 0;
			Cell cell = row.createCell(colNum++);
			cell.setCellValue(form.getRaveFormOId());
			cell = row.createCell(summaryFormsValidResultColNum);
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
			rowNum = 0;
			row = sheet2.createRow(rowNum++);
			newCell = row.createCell(0);
			newCell.setCellValue(formHeader_1 + forms.get(i).getRaveFormOId() + formHeader_2);
			row = sheet2.createRow(rowNum++);
			int colNum = 0;
			// Print row headers in the form sheet
			for (String rowHeader : rowHeaders) {
				newCell = row.createCell(colNum++);
				newCell.setCellValue(rowHeader);
			}
			colNum = 0;
			row = sheet2.createRow(rowNum++);
			newCell = row.createCell(0);
			newCell.setCellValue(forms.get(i).getRaveFormOId());
			colNum = colNum+3;
			newCell = row.createCell(3);
			newCell.setCellValue(forms.get(i).getQuestions().size());
			for (int j = 0; j < forms.get(i).getQuestions().size(); j++) {
				int colNum2 = formStartColumn;
				CCCQuestion question = forms.get(i).getQuestions().get(j);
				row = sheet2.createRow(rowNum++);
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getFieldOrder());
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getCdePublicId());				
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getCdeVersion());
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getNciCategory());
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getQuestionCongruencyStatus());
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getMessage());
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getRaveFieldLabel());
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getRaveFieldLabelResult());
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getCdePermitQuestionTextChoices());
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getRaveControlType());				
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getControlTypeResult());								
				newCell = row.createCell(colNum2++);
				newCell.setCellValue(question.getCdeValueDomainType());
				List<String> raveCodedData = question.getRaveCodedData();
				List<String> raveUserString = question.getRaveUserString();
				Row rowBeforeCD = row;
				for (int m = 0; m < raveCodedData.size(); m++)	{
					int colNum3 = codedDataColStart;
					newCell = row.createCell(colNum3++);
					newCell.setCellValue(raveCodedData.get(m));					
					newCell = row.createCell(colNum3++);
					newCell.setCellValue("CHECK"); // TODO - needs to get actual values from caDSR DB validation result 
					newCell = row.createCell(colNum3);
					newCell.setCellValue(raveCodedData.get(m).replaceAll("-", ", ")); // substituting pv values from ALS for now TODO - needs to get actual values from caDSR db			
					if (m != raveCodedData.size()-1)
						row = sheet2.createRow(rowNum++);
				}
				int rowNumAfterCD = rowNum;
				row = rowBeforeCD;
				int newColNum = allowableCdeValueCol;
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getAllowableCdeValue());
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getPvResult());				
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getAllowableCdeTextChoices());
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getRaveFieldDataType());				
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getDatatypeResult());								
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getCdeDataType());
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getRaveUOM());
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getUomResult());																				
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getCdeUOM());																								
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getRaveLength());
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getLengthResult());				
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getRaveDisplayFormat());								
				newCell = row.createCell(newColNum++);
				newCell.setCellValue(question.getFormatResult());												
				newCell = row.createCell(newColNum);
				newCell.setCellValue(question.getCdeDisplayFormat());
				if (rowNumAfterCD > rowNum)
					rowNum = rowNumAfterCD;
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
	}
	
	private static ALSData getAlsDataInstance() {
		ALSData alsData = new ALSData();
		return alsData;
	}

	private static ALSCrfDraft getAlsCrfDraftInstance() {
		ALSCrfDraft alsCrfData = new ALSCrfDraft();
		return alsCrfData;
	}
	
	private static ALSForm getAlsFormInstance() {
		ALSForm alsForm = new ALSForm();
		return alsForm;
	}
		
	private static ALSField getAlsFieldInstance() {
		ALSField alsField = new ALSField();
		return alsField;
	}
	
	private static CCCError getErrorObject() {
		CCCError cccError = new CCCError();
		return cccError;
	}

}
