package gov.nih.nci.cadsr.parser.impl;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import gov.nih.nci.cadsr.data.ALSCrfDraft;
import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.CCCError;
import gov.nih.nci.cadsr.parser.Parser;

public class AlsParser implements Parser{

	private static final Logger logger = Logger.getLogger(AlsParser.class);
	private static DataFormatter dataFormatter = new DataFormatter();
	private static String reportDateFormat = "MM/dd/yyyy";
	private static String crfDraftSheetName = "CRFDraft";
	private static String formsSheetName = "Forms";
	private static String fieldsSheetName = "Fields";
	private static String dataDictionarySheetName = "DataDictionaryEntries";	
	private static String errorSheetMissing = "Sheet missing in the ALS input file";
	private static int crfDraftStartRow = 1;


	/**
	 * Parsing an ALS input file into data objects for validating against the
	 * database
	 * 
	 */
	public ALSData parse (String INPUT_XLSX_FILE_PATH) throws IOException, InvalidFormatException, NullPointerException {
		Workbook workbook = WorkbookFactory.create(new File(INPUT_XLSX_FILE_PATH));
		CCCError cccError = getErrorObject();
		ALSData alsData = getAlsDataInstance();
		Sheet sheet = workbook.getSheet(crfDraftSheetName);
		if (sheet!=null) {
			alsData = getCrfDraft(sheet, alsData);
		}
		else
			cccError.getErrors().add(errorSheetMissing+" - "+crfDraftSheetName);
		sheet = workbook.getSheet(formsSheetName);
		if (sheet!=null)
			alsData.setForms(getForms(sheet));
		else
			cccError.getErrors().add(errorSheetMissing+" - "+formsSheetName);
		sheet = workbook.getSheet(fieldsSheetName);
		if (sheet!=null)
			alsData.setFields(getFields(sheet));
		else
			cccError.getErrors().add(errorSheetMissing+" - "+fieldsSheetName);
		sheet = workbook.getSheet(dataDictionarySheetName);
		if (sheet!=null)
			alsData.setDataDictionaryEntries(getDataDictionaryEntries(sheet));
		else 		
			cccError.getErrors().add(errorSheetMissing+" - "+dataDictionarySheetName);		
		workbook.close();
		if (cccError.getErrors().size() > 0)
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

	protected static ALSData getCrfDraft(Sheet sheet, ALSData alsData) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat(reportDateFormat);
		Date date = new Date();
		CCCError cccError = getErrorObject();
			alsData.setReportDate(dateFormat.format(date));
			Row newRow = sheet.getRow(crfDraftStartRow);
			ALSCrfDraft crfDraft = getAlsCrfDraftInstance();
			if (!newRow.getCell(0).equals(""))
				crfDraft.setDraftName(dataFormatter.formatCellValue(newRow.getCell(0)));
			if (newRow.getCell(2).equals("")) {
					cccError.getErrors().add("RAVE Protocol Name is missing in the ALS file."); 
				}
			else {
				Cell newCell = newRow.getCell(2);
				String cellValue = dataFormatter.formatCellValue(newCell);
				crfDraft.setProjectName(cellValue); 
				}
			if (newRow.getCell(4).equals("")) {
				cccError.getErrors().add("RAVE Protocol Number is missing in the ALS file."); 
				}				
			else {
				Cell newCell = newRow.getCell(4);
				String cellValue = dataFormatter.formatCellValue(newCell);
				crfDraft.setPrimaryFormOid(cellValue); 
				}
			if (crfDraft.getPrimaryFormOid()!=null && crfDraft.getProjectName()!=null) {
				alsData.setCrfDraft(crfDraft);
			}
			if (cccError.getErrors().size() > 0)
				alsData.setCccError(cccError);	
			return alsData;
	}

	/**
	 * @param Sheet
	 * @return List ALSForm Populates a collection of (all) Form objects parsed out of
	 *         the ALS input file
	 * 
	 */
	protected static List<ALSForm> getForms(Sheet sheet) throws IOException {
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
	protected static List<ALSField> getFields(Sheet sheet) throws NullPointerException {
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
	protected static Map<String, ALSDataDictionaryEntry> getDataDictionaryEntries(Sheet sheet) throws NullPointerException {
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

	
	protected static ALSData getAlsDataInstance() {
		ALSData alsData = new ALSData();
		return alsData;
	}

	protected static ALSCrfDraft getAlsCrfDraftInstance() {
		ALSCrfDraft alsCrfData = new ALSCrfDraft();
		return alsCrfData;
	}
	
	protected static ALSForm getAlsFormInstance() {
		ALSForm alsForm = new ALSForm();
		return alsForm;
	}
		
	protected static ALSField getAlsFieldInstance() {
		ALSField alsField = new ALSField();
		return alsField;
	}
	
	protected static CCCError getErrorObject() {
		CCCError cccError = new CCCError();
		return cccError;
	}

}
