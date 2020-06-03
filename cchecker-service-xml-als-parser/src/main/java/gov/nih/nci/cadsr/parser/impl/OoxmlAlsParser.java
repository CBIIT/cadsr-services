/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.parser.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.nih.nci.cadsr.data.ALSCrfDraft;
import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.ALSUnitDictionaryEntry;
import gov.nih.nci.cadsr.data.CCCError;
import gov.nih.nci.cadsr.data.XmlRow;
import gov.nih.nci.cadsr.microservices.SAXHandler;
import gov.nih.nci.cadsr.parser.Parser;

public class OoxmlAlsParser {

	private static final Logger logger = LoggerFactory.getLogger(OoxmlAlsParser.class);
	private static String reportDateFormat = "MM/dd/yyyy";
	private static String crfDraftSheetName = "CRFDraft";
	public static final String formsSheetName = "Forms";
	private static String fieldsSheetName = "Fields";
	private static String dataDictionarySheetName = "DataDictionaryEntries";
	private static String unitDictionarySheetName = "UnitDictionaryEntries";
	private static String errorSheetMissing = "Sheet missing in the ALS input file";
	private static int crfDraftStartRow = 1;
	private static int cell_crfDraftName = 0;
	private static int cell_crfDraftProjectName = 2;
	private static int cell_crfDraftPrimaryFormOid = 4;
	private static int cell_formOid = 0;
	private static int cell_formOrdinal = 1;
	private static int cell_formDraftName = 2;
	private static int cell_field_formOid = 0;
	private static int cell_fieldOid = 1;
	private static int cell_fieldOrdinal = 2;
	private static int cell_draftFieldName = 4;
	private static int cell_fieldDataFormat = 7;
	private static int cell_fieldDataDictionaryName = 8;
	private static int cell_fieldUnitDictionaryName = 9;
	private static int cell_fieldControlType = 11;
	private static int cell_fieldPreText = 14;
	private static int cell_fieldFixedUnit = 15;
	private static int cell_fieldDefaultValue = 20;
	private static int cell_ddeDataDictionaryName = 0;
	private static int cell_ddeCodedData = 1;
	private static int cell_ddeOrdinal = 2;
	private static int cell_ddeUserDataString = 3;
	private static int cell_ddeSpecify = 4;
	private static int cell_udName = 0;
	private static int cell_udCodedUnit = 1;
	private static int cell_udOrdinal = 2;
	private static int cell_udConstantA = 3;
	private static int cell_udConstantB = 4;
	private static int cell_udConstantC = 5;
	private static int cell_udConstantK = 6;
	private static int cell_udUnitString = 7;
	public static final String errorSeverity_fatal = "FATAL";
	public static final String errorSeverity_error = "ERROR";
	public static final String errorSeverity_warn = "WARNING";
	private static List<String> controlTypes = Arrays.asList("CheckBox", "DateTime", "DropDownList",
			"Dynamic SearchList", "Text", "FileUpload", "File Upload", "LongText", "RadioButton", "SearchList");
	private static final String formOId_als = "FORM_OID";
	private static final String formOId_name = "FORM OID";
	private static final String draftFormName = "Draft Form Name";
	private static final String ordinal_str = "Ordinal";
	private static final String draftFieldName_str = "Draft Field Name";
	private static final String controlType_str = "Control Type";
	private static final String unitDictionary_str = "Unit Dictionary Name";
	private static final String crfDraftProjName_str = "CRF Draft Project Name";
	private static final String crfDraftPrimaryFormOID_str = "CRF Draft Primary Form OID";
	private static final String codedData_str = "Coded Data";
	private static final String userDataString_str = "User Data String";
	private static final String specify_str = "Specify";
	private static String err_msg_1 = "RAVE Protocol Name is missing in the ALS file.";
	private static String err_msg_2 = "RAVE Protocol Number is missing in the ALS file.";
	private static String err_msg_15 = "Data Dictionary Name is empty.";
	private static String err_msg_16 = "Coded Data is empty.";
	private static String err_msg_17 = "Ordinal is empty.";
	private static String err_msg_18 = "User Data String is empty.";
	private static String err_msg_19 = "Specify is empty.";	
	private static String err_msg_21 = "Question doesn't contain a CDE public id and version";
	private static String err_msg_22 = "%s is an unknown control type.";
	private static String err_msg_23 = "CDE public id and version should be numeric.";
	private static String err_msg_24 = "Ordinal should be numeric.";
	public final static String err_msg_25 = "FORM OID duplicated: %s.";
	public final static String err_msg_26 = "Draft Form Name duplicated: %s.";
	private static String publicid_prefix = "PID";
	private static String version_prefix = "_V";
	private static String invalidHdrSign = "Invalid header signature";
	private static String invalidFileUploadMsg = "Invalid file format, please check if the uploaded file is in Excel XLSX format.";
	private static String err_msg_empty = "%s is empty.";	

	/**
	 * Parsing an ALS input file into data objects for validating against the
	 * database
	 * 
	 */
	public ALSData parseXml(SAXHandler handler) throws IOException, NullPointerException {
		CCCError cccError = getErrorObject();
		ALSData alsData = getAlsDataInstance();
		logger.debug(" IN parsing XML method");
		ALSError alsError;
		try {
			
			logger.debug(" Handler rows count: "+handler.xmlRowList.size());
			logger.debug(" Sheets: "+handler.sheetsList);
			
			for (String sheet : handler.sheetsList) {
				if ("CRFDraft".equalsIgnoreCase(sheet)) { 
					alsData = getCrfDraft(handler.sheetRowsMap.get("CRFDraft"), alsData); 
				} else if ("Forms".equalsIgnoreCase(sheet)) {
					alsData = getForms(handler.sheetRowsMap.get("Forms"), alsData);
				} else if ("Fields".equalsIgnoreCase(sheet)) { 
					alsData = getFields(handler.sheetRowsMap.get("Fields"), alsData);											
				} else {
					logger.debug("Sheet name: "+sheet);
				}
			}
		} catch (NullPointerException npe) {
			cccError = addError(npe.getMessage(), errorSeverity_fatal, cccError);
		}			
			
			
			
			// Create a new Workbook out of the uploaded XLSX file
			/*Workbook workbook = WorkbookFactory.create(new File());
			List<String> requiredSheets = Arrays.asList(crfDraftSheetName, formsSheetName, fieldsSheetName,
					dataDictionarySheetName);
			for (int i = 0; i < requiredSheets.size(); i++) {
				Sheet sheet = workbook.getSheet(requiredSheets.get(i));
				if (sheet != null) {
					if (crfDraftSheetName.equalsIgnoreCase(sheet.getSheetName())) {
						// Parse CRF Draft Sheet(Summary info) from the ALS file
						alsData = getCrfDraft(sheet, alsData, cccError);
					} else if (formsSheetName.equalsIgnoreCase(sheet.getSheetName())) {
						// Parse Forms sheet from the ALS file
						alsData = getForms(sheet, alsData, cccError);
					} else if (fieldsSheetName.equalsIgnoreCase(sheet.getSheetName())) {
						// Parse Fields sheet from the ALS file
						alsData = getFields(sheet, alsData, cccError);
					} else if (dataDictionarySheetName.equalsIgnoreCase(sheet.getSheetName())) {
						// Parse Data Dictionary Entries sheet from the ALS file
						alsData = getDataDictionaryEntries(sheet, alsData, cccError);
					} else if (unitDictionarySheetName.equalsIgnoreCase(sheet.getSheetName())) {
						// Parse Unit Dictionary Entries sheet from the ALS file
						alsData = getUnitDictionaryEntries(sheet, alsData, cccError);
					} else {
						continue;
					}
				} else {
					// Create an error object that can be added to the errorlist
					alsError = getErrorInstance();
					alsError.setErrorDesc(errorSheetMissing + " - " + requiredSheets.get(i));
					alsError.setSheetName(requiredSheets.get(i));
					if (dataDictionarySheetName.equalsIgnoreCase(requiredSheets.get(i))
							|| unitDictionarySheetName.equalsIgnoreCase(requiredSheets.get(i))) {
						alsError.setErrorSeverity(errorSeverity_warn);
					} else {
						alsError.setErrorSeverity(errorSeverity_fatal);
					}
					cccError.addAlsError(alsError);
				}
			}
			workbook.close();
		} catch (IOException ioe) {
			// Any non-office document formats
			alsError = getErrorInstance();
			if (ioe.getMessage().indexOf(invalidHdrSign) > -1) {
				alsError.setErrorDesc(invalidFileUploadMsg);
			} else {
				alsError.setErrorDesc(ioe.getMessage());
			}
			alsError.setErrorSeverity(errorSeverity_fatal);
			cccError.addAlsError(alsError);
		} catch (NullPointerException npe) {
			cccError = addError(npe.getMessage(), errorSeverity_fatal, cccError);
		}
		if (cccError.getAlsErrors().size() > 0)
			alsData.setCccError(cccError);*/ 
		logger.debug("Parsing done ");
		return alsData;
	}
	
	protected static ALSData getCrfDraft (List<XmlRow> xmlRowList, ALSData alsData) {
		for (XmlRow xmlRow : xmlRowList) {
		    if (xmlRow.cellList.size() > 10) {
		    	if ("DraftName".equalsIgnoreCase(xmlRow.cellList.get(0))) {
		        	continue;
		        } else {
			    	ALSCrfDraft crfDraft = getAlsCrfDraftInstance();		    	
					crfDraft.setDraftName(xmlRow.cellList.get(0) != null ? xmlRow.cellList.get(0) : null);
					crfDraft.setProjectName(xmlRow.cellList.get(2) != null ? xmlRow.cellList.get(2) : null);
					crfDraft.setPrimaryFormOid(xmlRow.cellList.get(4) != null ? xmlRow.cellList.get(4) : null);
			        String crfDraftName = xmlRow.cellList.get(0);					        
			        String projectName = xmlRow.cellList.get(2);
			        String primaryFormOid = xmlRow.cellList.get(4);
			        logger.debug("CRF DRAFT NAME: '" + crfDraftName + "', projectName: '" + projectName + "', primaryFormOid: '" + primaryFormOid + "'");
			        alsData.setCrfDraft(crfDraft);
		        }
		    }
		}
		return alsData;
	}

	/**
	 * @param Sheet
	 * @param ALSData
	 * @return List ALSCrfDraft Populates a collection of ALSCrfDraft objects
	 *         parsed out of the ALS input file
	 * 
	 */

	/*protected static ALSData getCrfDraft(Sheet sheet, ALSData alsData, CCCError cccError) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat(reportDateFormat);
		Date date = new Date();
		alsData.setReportDate(dateFormat.format(date));
		Row row = sheet.getRow(crfDraftStartRow);
		ALSCrfDraft crfDraft = getAlsCrfDraftInstance();
		DataFormatter dataFormatter = new DataFormatter();
		// Parse out the following values from the CRFDraft sheet
		// DraftName, ProjectName & PrimaryFormOID
		crfDraft.setDraftName(row.getCell(cell_crfDraftName) != null ? dataFormatter.formatCellValue(row.getCell(cell_crfDraftName)) : null);
		crfDraft.setProjectName(row.getCell(cell_crfDraftProjectName) != null ? dataFormatter.formatCellValue(row.getCell(cell_crfDraftProjectName)) : null);
		if (crfDraft.getProjectName() == null) {
			cccError = addParsingValidationMsg(cccError, crfDraftProjName_str, crfDraftSheetName, row.getRowNum() + 1,
					cell_crfDraftProjectName, errorSeverity_error, err_msg_1,
					null, null, null, null, null);			
		} else {
			cccError.setRaveProtocolName(crfDraft.getProjectName());
		}		
		crfDraft.setPrimaryFormOid(row.getCell(cell_crfDraftPrimaryFormOid) != null ? dataFormatter.formatCellValue(row.getCell(cell_crfDraftPrimaryFormOid)) : null);
		if (crfDraft.getPrimaryFormOid() == null) {
			cccError = addParsingValidationMsg(cccError, crfDraftPrimaryFormOID_str, crfDraftSheetName, row.getRowNum() + 1,
					cell_crfDraftPrimaryFormOid, errorSeverity_error, err_msg_2, null, null, null, null, null);			
		} else {
			cccError.setRaveProtocolNumber(crfDraft.getPrimaryFormOid());
		}
		alsData.setCrfDraft(crfDraft);
		if (cccError.getAlsErrors().size() > 0)
			alsData.setCccError(cccError);
		return alsData;
	}*/
	
	
	
	protected static ALSData getForms(List<XmlRow> xmlRowList, ALSData alsData) throws IOException {
		List<ALSForm> forms = new ArrayList<ALSForm>();
		for (XmlRow xmlRow : xmlRowList) {
		    if (xmlRow.cellList.size() > 1) {
		    	if ("DraftFormName".equalsIgnoreCase(xmlRow.cellList.get(2)) && "OID".equalsIgnoreCase(xmlRow.cellList.get(0))) {
		        	continue;
		        } else {
			    	ALSForm form = getAlsFormInstance();
			        String formOid = xmlRow.cellList.get(0);
			        form.setFormOid(xmlRow.cellList.get(0) != null ? xmlRow.cellList.get(0) : null);
			        String draftFormName = xmlRow.cellList.get(2);
			        form.setDraftFormName(xmlRow.cellList.get(2) != null ? xmlRow.cellList.get(2) : null);
			        logger.debug("Forms Form OID: '" + formOid + "', Form Name: '" + draftFormName);
			        forms.add(form);
		        }
		    }
		} 
		alsData.setForms(forms);
		return alsData;
	}
	

	/**
	 * @param Sheet
	 * @return List ALSForm Populates a collection of (all) Form objects parsed
	 *         out of the ALS input file
	 * 
	 */
	/*protected static ALSData getForms(Sheet sheet, ALSData alsData, CCCError cccError) throws IOException {
		List<ALSForm> forms = new ArrayList<ALSForm>();
		DataFormatter dataFormatter = new DataFormatter();
		Iterator<Row> rowIterator = sheet.rowIterator();
		Row row = rowIterator.next();
		while (rowIterator.hasNext()) {
			row = rowIterator.next();
			ALSForm form = getAlsFormInstance();

			// Parse out the following values from the Forms sheet
			// OID, Ordinal & DraftFormName

			form.setFormOid(row.getCell(cell_formOid) != null ? dataFormatter.formatCellValue(row.getCell(cell_formOid)) : null);
			form.setOrdinal(row.getCell(cell_formOrdinal) != null ? dataFormatter.formatCellValue(row.getCell(cell_formOrdinal)) : null);
			form.setDraftFormName(row.getCell(cell_formDraftName) != null ? dataFormatter.formatCellValue(row.getCell(cell_formDraftName)) : null);			
			Map<String, Integer> errFieldNames = new HashMap<String, Integer>();			
			if (form.getFormOid() == null) 
				errFieldNames.put(formOId_name, cell_formOid);
			if (form.getDraftFormName() == null)
				errFieldNames.put(draftFormName, cell_formDraftName);
			if (form.getOrdinal() == null)
				errFieldNames.put(ordinal_str, cell_formOrdinal);
			for (String fieldName : errFieldNames.keySet()) {
				cccError = addParsingValidationMsg(cccError, fieldName, formsSheetName, row.getRowNum() + 1,
						errFieldNames.get(fieldName), errorSeverity_error, err_msg_empty,
						dataFormatter.formatCellValue(row.getCell(cell_formOid)), null, null, null, null);
			}
			if (cccError.getAlsErrors().size() > 0) {
				alsData.setCccError(cccError);
			} else {
				if (row.getCell(cell_formOid) != null && row.getCell(cell_formOrdinal) != null
						&& row.getCell(cell_formDraftName) != null) {
					forms.add(form);
				}
			}
		}
		alsData.setForms(forms);
		return alsData;
	}*/
	
	
	protected static ALSData getFields(List<XmlRow> xmlRowList, ALSData alsData) throws NullPointerException {
		DataFormatter dataFormatter = new DataFormatter();
		List<ALSField> fields = new ArrayList<ALSField>();		
		int sequence = 0;		
		for (XmlRow xmlRow : xmlRowList) {
		    if (xmlRow.cellList.size() > 10) {
		    	if ("Ordinal".equalsIgnoreCase(xmlRow.cellList.get(2)) && "FormOID".equalsIgnoreCase(xmlRow.cellList.get(0)) 
		    			&& "FieldOID".equalsIgnoreCase(xmlRow.cellList.get(1))) {
		        	continue;
		        } else {		    	
			    	ALSField field = getAlsFieldInstance();
			        String formOid = xmlRow.cellList.get(0);
			        String fieldOid = xmlRow.cellList.get(1);
			        String ordinal = xmlRow.cellList.get(2);
			        String draftFieldName = xmlRow.cellList.get(3);
			        String dataFormat = xmlRow.cellList.get(6);
			        field.setFormOid(xmlRow.cellList.get(0) != null ? xmlRow.cellList.get(0) : null);
			        field.setFieldOid(xmlRow.cellList.get(1) != null ? xmlRow.cellList.get(1) : null);
			        field.setOrdinal(xmlRow.cellList.get(2) != null ? xmlRow.cellList.get(2) : null);
			        field.setDraftFieldName(xmlRow.cellList.get(3) != null ? xmlRow.cellList.get(3) : null);
			        field.setDataFormat(xmlRow.cellList.get(6) != null ? xmlRow.cellList.get(6) : null);
					//field.setDataDictionaryName(row.getCell(cell_fieldDataDictionaryName) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldDataDictionaryName)) : null);
					//field.setUnitDictionaryName(row.getCell(cell_fieldUnitDictionaryName) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldUnitDictionaryName)) : null);					
					//field.setDefaultValue();
					field.setSequenceNumber(sequence);
					//String controlType = row.getCell(cell_fieldControlType) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldControlType)) : null;
					//field.setControlType(controlType);
					//field.setPreText(row.getCell(cell_fieldPreText) != null ? stripHtml(dataFormatter.formatCellValue((row.getCell(cell_fieldPreText)))) : null);
					//field.setFixedUnit(row.getCell(cell_fieldFixedUnit) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldFixedUnit)) : null);
					for (ALSForm form : alsData.getForms()) {
						if (field.getFormOid()!= null && field.getFormOid().equalsIgnoreCase(form.getFormOid())) {
							form.getFields().add(field);
						}
					}
					fields.add(field);
					// FORMBUILD-652
					sequence++;
			        logger.debug("Fields Form OID: '" + formOid + "', fieldOid: '" + fieldOid + "', ordinal: '" + ordinal + "', draftFieldName: '" + draftFieldName + "', dataFormat: '" + dataFormat + "'");
		        }
		    }
		}		
		alsData.setFields(fields);
		return alsData;
	}

	/**
	 * @param Sheet
	 * @return List ALSField Populates a collection of Field (Question) objects
	 *         parsed out of the ALS input file
	 * 
	 */
	/*protected static ALSData getFields(Sheet sheet, ALSData alsData, CCCError cccError) throws NullPointerException {
		DataFormatter dataFormatter = new DataFormatter();
		List<ALSField> fields = new ArrayList<ALSField>();
		ALSField field;
		Iterator<Row> rowIterator = sheet.rowIterator();
		Row row = rowIterator.next();
		int sequence = 0;
		String formOid = null;
		while (rowIterator.hasNext()) {
			row = rowIterator.next();
			field = getAlsFieldInstance();
			
			// Parse out the following values from the Fields sheet
			// FormOID, FieldOID, Ordinal, DraftFieldName, DataFormat,
			// DataDictionaryName,
			// UnitDictionaryName, ControlType, PreText, FixedUnit &
			// DefaultValue
			
			field.setFormOid(row.getCell(cell_field_formOid) != null ? dataFormatter.formatCellValue(row.getCell(cell_field_formOid)) : null);
			if (field.getFormOid()!=null && !(field.getFormOid().isEmpty())) { // Avoiding blank rows
				field.setDataDictionaryName(row.getCell(cell_fieldDataDictionaryName) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldDataDictionaryName)) : null);
				field.setUnitDictionaryName(row.getCell(cell_fieldUnitDictionaryName) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldUnitDictionaryName)) : null);
				field.setFieldOid(row.getCell(cell_fieldOid) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldOid)) : null);
				if (formOId_als.equalsIgnoreCase(field.getFieldOid())) {
					field.setDefaultValue(row.getCell(cell_fieldDefaultValue) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldDefaultValue)) : (row.getCell(cell_draftFieldName) != null 
							? dataFormatter.formatCellValue(row.getCell(cell_draftFieldName)) : null));
					if (field.getDefaultValue() != null) {
						String[] splitFormId = extractIdVersion(field.getDefaultValue());
						// Split form oid and version
						if (NumberUtils.isCreatable(splitFormId[0]) && NumberUtils.isCreatable(splitFormId[1])
								&& NumberUtils.isCreatable(splitFormId[2])) {
								field.setFormPublicId(splitFormId[0]);
								field.setVersion(splitFormId[1] + "." + splitFormId[2]);
						}
					}
				}
				// FORMBUILD-652
				if (formOid == null) {//the first form
					formOid = field.getFormOid();
				} else {// Resetting the question sequence for each form 
					if (!formOid.equals(field.getFormOid())) {
						if (formOId_als.equalsIgnoreCase(field.getFieldOid())) {
							sequence = 0; // FORM OID row becomes question 0
						} else {
							sequence = 1; // Resetting question sequence to 1 for those forms without Form OID row 
						}
						formOid = field.getFormOid();
					}
				}
				// FORMBUILD-652
				field.setSequenceNumber(sequence);
				field.setOrdinal(row.getCell(cell_fieldOrdinal) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldOrdinal)) : null);
				try {
					Integer.parseInt(field.getOrdinal());
				} catch (NumberFormatException e) {
					cccError = addParsingValidationMsg(cccError, ordinal_str, fieldsSheetName, row.getRowNum() + 1, cell_fieldOrdinal, errorSeverity_warn, err_msg_24, 
							field.getFormOid(), field.getFieldOid(), field.getDataDictionaryName(), field.getUnitDictionaryName(), field.getDraftFieldName());
				}
				field.setDraftFieldName(row.getCell(cell_draftFieldName) != null ? dataFormatter.formatCellValue(row.getCell(cell_draftFieldName)) : null);
				if (field.getDraftFieldName()!=null) {
				if (!(field.getDraftFieldName().indexOf(publicid_prefix) > -1
						&& field.getDraftFieldName().indexOf(version_prefix) > -1)) {
					cccError = addParsingValidationMsg(cccError, draftFieldName_str, fieldsSheetName, row.getRowNum() + 1, cell_draftFieldName, errorSeverity_warn, err_msg_21, 
							field.getFormOid(), field.getFieldOid(), field.getDataDictionaryName(), field.getUnitDictionaryName(), field.getDraftFieldName());
					} else {
							String[] splitCdeIdVersion = extractIdVersion(field.getDraftFieldName());
							// Split cde public ID and version
							if (!(NumberUtils.isCreatable(splitCdeIdVersion[0]) && NumberUtils.isCreatable(splitCdeIdVersion[1])
									&& NumberUtils.isCreatable(splitCdeIdVersion[2]))) {
									cccError = addParsingValidationMsg(cccError, draftFieldName_str, fieldsSheetName, row.getRowNum() + 1, cell_draftFieldName, errorSeverity_error, err_msg_23, 
										field.getFormOid(), field.getFieldOid(), field.getDataDictionaryName(), field.getUnitDictionaryName(), field.getDraftFieldName());
								}
				}
				}
				field.setDataFormat(row.getCell(cell_fieldDataFormat) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldDataFormat)) : null);
				String controlType = row.getCell(cell_fieldControlType) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldControlType)) : null;
				field.setControlType(controlType);
				field.setPreText(row.getCell(cell_fieldPreText) != null ? stripHtml(dataFormatter.formatCellValue((row.getCell(cell_fieldPreText)))) : null);
				field.setFixedUnit(row.getCell(cell_fieldFixedUnit) != null ? dataFormatter.formatCellValue(row.getCell(cell_fieldFixedUnit)) : null);
				for (ALSForm form : alsData.getForms()) {
					if (field.getFormOid()!= null && field.getFormOid().equalsIgnoreCase(form.getFormOid())) {
						form.getFields().add(field);
					}
				}
				fields.add(field);
				// FORMBUILD-652
				sequence++;
			}
		}
		if (cccError.getAlsErrors().size() > 0) {
			alsData.setCccError(cccError);
		}
		alsData.setFields(fields);
		return alsData;
	} */
	
	/**
	 * @param Sheet
	 * @return List ALSDataDictionaryEntry Populates a collection of Data
	 *         Dictionary Entries parsed out of the ALS input file
	 * 
	 */
	protected static ALSData getDataDictionaryEntries(Sheet sheet, ALSData alsData, CCCError cccError)
			throws NullPointerException {
		DataFormatter dataFormatter = new DataFormatter();
		Map<String, ALSDataDictionaryEntry> ddeMap = new HashMap<String, ALSDataDictionaryEntry>();
		ALSDataDictionaryEntry dde = new ALSDataDictionaryEntry();
		final String regex_space = "[\\p{Z}\\s]";// To identify and remove
													// "'\u00A0', '\u2007',
													// '\u202F'" characters
		List<String> ordinal = new ArrayList<String>();
		List<String> cd = new ArrayList<String>();
		List<String> uds = new ArrayList<String>();
		List<Boolean> specify = new ArrayList<Boolean>();
		String ddName = "";
		Iterator<Row> rowIterator = sheet.rowIterator();
		Row row = rowIterator.next();

		// Parse out the following values from the Data Dictionary Entries sheet
		// DataDictionaryName, CodedData & UserDataString

		while (rowIterator.hasNext()) {
			row = rowIterator.next();
			if (row.getCell(cell_ddeDataDictionaryName) != null) {
				if (ddName.equals("")) {
					ddName = dataFormatter.formatCellValue(row.getCell(cell_ddeDataDictionaryName));
				}
				if (!ddName.equals(dataFormatter.formatCellValue(row.getCell(cell_ddeDataDictionaryName)))) {
					dde.setCodedData(cd);
					dde.setOrdinal(ordinal);
					dde.setUserDataString(uds);
					dde.setDataDictionaryName(ddName);
					if (!(ddeMap.containsKey(dde.getDataDictionaryName()))) {
						ddeMap.put(dde.getDataDictionaryName(), dde);
					}
					ddName = dataFormatter.formatCellValue(row.getCell(cell_ddeDataDictionaryName));
					dde = new ALSDataDictionaryEntry();
					dde.setDataDictionaryName(ddName);
					ordinal = new ArrayList<String>();
					cd = new ArrayList<String>();
					uds = new ArrayList<String>();
					specify = new ArrayList<Boolean>();
				}
				if (row.getCell(cell_ddeCodedData) != null)
					cd.add(dataFormatter.formatCellValue(row.getCell(cell_ddeCodedData)));
				else {
					cccError = addParsingValidationMsg(cccError, codedData_str, dataDictionarySheetName, row.getRowNum() + 1,
							cell_ddeCodedData, errorSeverity_error, err_msg_16,
							null, null, ddName, null, null);
				}
				if (row.getCell(cell_ddeOrdinal) != null)
					ordinal.add(dataFormatter.formatCellValue(row.getCell(cell_ddeOrdinal)));
				else {
					cccError = addParsingValidationMsg(cccError, ordinal_str, dataDictionarySheetName, row.getRowNum() + 1,
							cell_ddeOrdinal, errorSeverity_warn, err_msg_17, null, null, ddName, null, null);
				}
				if (row.getCell(cell_ddeUserDataString) != null) {
					String udsStr = dataFormatter.formatCellValue(row.getCell(cell_ddeUserDataString));
					udsStr = udsStr.replaceAll(regex_space, " ");
					uds.add(udsStr);
				} else {
					cccError = addParsingValidationMsg(cccError, userDataString_str, dataDictionarySheetName, row.getRowNum() + 1,
							cell_ddeUserDataString, errorSeverity_error,
							err_msg_18, null, null, ddName, null, null);
				}
				if (row.getCell(cell_ddeSpecify) != null)
					specify.add(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(cell_ddeSpecify))));
				else {
					cccError = addParsingValidationMsg(cccError, specify_str, dataDictionarySheetName, row.getRowNum() + 1,
							cell_ddeSpecify, errorSeverity_warn, err_msg_19, null,
							null, ddName, null, null);
				}
			} else {
				if (row.getCell(cell_ddeCodedData) != null && row.getCell(cell_ddeOrdinal) != null
						&& row.getCell(cell_ddeUserDataString) != null && row.getCell(cell_ddeSpecify) != null) {
					cccError = addParsingValidationMsg(cccError, codedData_str, dataDictionarySheetName, row.getRowNum() + 1,
							cell_ddeDataDictionaryName, errorSeverity_error,
							err_msg_15, null, null, ddName, null, null);
				}
			}
		}
		dde.setCodedData(cd);
		dde.setOrdinal(ordinal);
		dde.setUserDataString(uds);
		dde.setDataDictionaryName(ddName);
		if (!(ddeMap.containsKey(dde.getDataDictionaryName()))) {
			ddeMap.put(dde.getDataDictionaryName(), dde);
		}
		if (cccError.getAlsErrors().size() > 0) {
			alsData.setCccError(cccError);
		}
		alsData.setDataDictionaryEntries(ddeMap);
		return alsData;
	}	
	
	
	
	
	/**
	 * @param Sheet
	 * @return List ALSUnitDictionaryEntry Populates a collection of Data
	 *         Dictionary Entries parsed out of the ALS input file
	 * 
	 */
	protected static ALSData getUnitDictionaryEntries(Sheet sheet, ALSData alsData, CCCError cccError)
			throws NullPointerException {
		DataFormatter dataFormatter = new DataFormatter();
		List<ALSUnitDictionaryEntry> udeList = new ArrayList<ALSUnitDictionaryEntry>();
		ALSUnitDictionaryEntry ude;
		Iterator<Row> rowIterator = sheet.rowIterator();
		Row row = rowIterator.next();

		// Parse out the following values from the Unit Dictionary Entries sheet
		// UnitDictionaryName, CodedUnit, Ordinal, ConstantA, ConstantB,
		// ConstantC, ConstantK & UnitString

		while (rowIterator.hasNext()) {
			row = rowIterator.next();
			ude = getUnitDictionaryInstance();
			ude.setUnitDictionaryName(row.getCell(cell_udName) != null ? dataFormatter.formatCellValue(row.getCell(cell_udName)) : null);
			ude.setCodedUnit(row.getCell(cell_udCodedUnit) != null ? dataFormatter.formatCellValue(row.getCell(cell_udCodedUnit)) : null);
			ude.setOrdinal(row.getCell(cell_udOrdinal) != null ? dataFormatter.formatCellValue(row.getCell(cell_udOrdinal)) : null);
			ude.setConstantA(row.getCell(cell_udConstantA) != null ? dataFormatter.formatCellValue(row.getCell(cell_udConstantA)) : null);
			ude.setConstantB(row.getCell(cell_udConstantB) != null ? dataFormatter.formatCellValue(row.getCell(cell_udConstantB)) : null);
			ude.setConstantC(row.getCell(cell_udConstantC) != null ? dataFormatter.formatCellValue(row.getCell(cell_udConstantC)) : null);
			ude.setConstantK(row.getCell(cell_udConstantK) != null ? dataFormatter.formatCellValue(row.getCell(cell_udConstantK)) : null);
			ude.setUnitString(row.getCell(cell_udUnitString) != null ? dataFormatter.formatCellValue(row.getCell(cell_udUnitString)) : null);
			if (ude.getUnitDictionaryName().isEmpty() ||  ude.getCodedUnit().isEmpty() || ude.getUnitString().isEmpty()) {
				cccError = addParsingValidationMsg(cccError, unitDictionary_str, unitDictionarySheetName, row.getRowNum() + 1, cell_udName, errorSeverity_warn, err_msg_empty,
						null, null, null, null, null);
			} else {
				udeList.add(ude); 
			}
		}
		if (cccError.getAlsErrors().size() > 0) {
			alsData.setCccError(cccError);
		}
		alsData.setUnitDictionaryEntries(udeList);
		return alsData;
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

	protected static ALSUnitDictionaryEntry getUnitDictionaryInstance() {
		ALSUnitDictionaryEntry ude = new ALSUnitDictionaryEntry();
		return ude;
	}

	protected static CCCError getErrorObject() {
		CCCError cccError = new CCCError();
		return cccError;
	}

	protected static ALSError getErrorInstance() {
		ALSError alsError = new ALSError();
		return alsError;
	}

	/**
	 * Returns the html stripped String using Jsoup (3rd party lib)
	 * 
	 * @param html
	 * @return String
	 */
	public static String stripHtml(String html) {
		return Jsoup.parse(html).text();
	}

	/**
	 * Returns the html stripped String with String replaceAll
	 * 
	 * @param html
	 * @return String
	 */
	public static String stripHtmlV2(String html) {
		return html.toString().replaceAll("\\<.*?>", "");
	}

	/**
	 * Adds a system error to the list of errors
	 * 
	 * @param errorMessage
	 * @param errorSeverity
	 * @param cccError
	 * @return CCCError
	 */	
	protected static CCCError addError(String errorMessage, String errorSeverity, CCCError cccError) {
		ALSError alsError = getErrorInstance();
		alsError.setErrorDesc(errorMessage);
		alsError.setErrorSeverity(errorSeverity);
		cccError.addAlsError(alsError);
		return cccError;
	}

	/**
	 * Adds a parsing validation message to the list of error messages
	 * that will be displayed in the forms list page.
	 * 
	 * @param cccError
	 * @param fieldName
	 * @param sheetName
	 * @param rowNum
	 * @param colIdx
	 * @param errorSeverity
	 * @param validationMsg
	 * @param formOid 
	 * @param fieldOid
	 * @param ddName 
	 * @param udName 
	 * @return CCCError
	 */
	protected static CCCError addParsingValidationMsg (CCCError cccError, String fieldName, String sheetName, int rowNum, int colIdx, String errorSeverity,
			String validationMsg, String formOid, String fieldOid, String ddName, String udName, String cellValue) {
		ALSError alsError = getErrorInstance();
		alsError.setErrorDesc(String.format(validationMsg, fieldName));
		// Setting Cell value in the parse error object for display on the forms list screen
		if (cellValue!=null)
			alsError.setCellValue(cellValue);
		alsError.setSheetName(sheetName);
		alsError.setRowNumber(rowNum);
		alsError.setColIdx(CellReference.convertNumToColString(colIdx));
		if (formOid != null)
			alsError.setFormOid(formOid);
		if (fieldOid != null)
			alsError.setFieldOid(fieldOid);
		if (ddName != null)
			alsError.setDataDictionaryName(ddName);
		if (udName != null)
			alsError.setUnitDictionaryName(udName);
		alsError.setErrorSeverity(errorSeverity);
		cccError.addAlsError(alsError);
		return cccError;
	}
	
	
	/**
	 * Splits & extracts the Public ID and Version from a string of format [PIDXXXXX_Vxx_x]
	 * @param draftFieldName
	 * @return String[]
	 */
	protected static String[] extractIdVersion (String draftFieldName) {
		String[] idVersionSplitElements = new String[3];
		if (draftFieldName.indexOf(publicid_prefix) > -1 && draftFieldName.indexOf(version_prefix) > -1) {
			String idVn = draftFieldName.substring(draftFieldName.indexOf(publicid_prefix),
					draftFieldName.length());
			String id = idVn.substring(3, idVn.indexOf("_"));
			String version = (idVn.substring(idVn.indexOf(version_prefix) + 2, idVn.length()));
			id = id.trim();
			String[] versionTokens = version.split("\\_");
			version = versionTokens[0] + "." + versionTokens[1];
			idVersionSplitElements[0] = id;
			idVersionSplitElements[1] = versionTokens[0];
			idVersionSplitElements[2] = versionTokens[1];
		}
		return idVersionSplitElements; 		
	}

}
