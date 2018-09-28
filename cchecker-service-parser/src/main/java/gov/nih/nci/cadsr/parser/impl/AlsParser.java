/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.parser.impl;

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
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.ALSUnitDictionaryEntry;
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
	private static String errorSeverity_fatal = "FATAL";
	private static String errorSeverity_error = "ERROR";
	private static String errorSeverity_warn = "WARNING";
	private static List<String> controlTypes = Arrays.asList("CheckBox","DateTime","DropDownList","Dynamic SearchList","Text","FileUpload", "File Upload","LongText","RadioButton","SearchList"); 

	
	private static String err_msg_1 = "RAVE Protocol Name is missing in the ALS file.";
	private static String err_msg_2 = "RAVE Protocol Number is missing in the ALS file.";
	private static String err_msg_3 = "FORM OID is empty.";
	private static String err_msg_4 = "Ordinal of the form is empty";	
	private static String err_msg_5 = "Draft Form name of the form is empty";	
	private static String err_msg_6 = "Form OID is empty.";
	private static String err_msg_7 = "Field OID is empty.";
	private static String err_msg_8 = "Field Ordinal is empty.";
	private static String err_msg_9 = "Draft Field Name is empty.";	
	private static String err_msg_10 = "Data Format is empty.";
	private static String err_msg_11 = "Data Dictionary Name is empty.";
	private static String err_msg_12 = "Control Type is empty.";
	private static String err_msg_13 = "Pretext is empty.";
	private static String err_msg_14 = "Fixed Unit is empty.";		
	private static String err_msg_15 = "Data Dictionary Name is empty.";
	private static String err_msg_16 = "Coded Data is empty.";
	private static String err_msg_17 = "Ordinal is empty.";
	private static String err_msg_18 = "User Data String is empty.";
	private static String err_msg_19 = "Specify is empty.";
	private static String err_msg_20 = "Unit Dictionary Name is empty.";
	private static String err_msg_21 = "Question doesn't contain a CDE public id and version";	
	private static String err_msg_22 = "This is an unknown control type.";		
	private static String err_msg_23 = "CDE public id and version should be numeric.";	
	private static String err_msg_24 = "Ordinal should be numeric.";	
	private static String publicid_prefix = "PID";
	private static String version_prefix = "_V";	

	/**
	 * Parsing an ALS input file into data objects for validating against the
	 * database
	 * 
	 */
	public ALSData parse (String INPUT_XLSX_FILE_PATH) throws IOException, InvalidFormatException, NullPointerException {
		CCCError cccError = getErrorObject();
		ALSData alsData = getAlsDataInstance();
		alsData.setFilePath(INPUT_XLSX_FILE_PATH);
		ALSError alsError;		
		try {
				Workbook workbook = WorkbookFactory.create(new File(INPUT_XLSX_FILE_PATH));
				Sheet sheet = workbook.getSheet(crfDraftSheetName);
				if (sheet!=null) 
					alsData = getCrfDraft(sheet, alsData, cccError);
				else
					{
						alsError = getErrorInstance();
						alsError.setErrorDesc(errorSheetMissing+" - "+crfDraftSheetName);
						alsError.setSheetName(crfDraftSheetName);
						alsError.setErrorSeverity(errorSeverity_fatal);						
						cccError.addAlsError(alsError); 
					}
				sheet = workbook.getSheet(formsSheetName);
				if (alsData.getCccError()!=null)
					cccError = alsData.getCccError();
				if (sheet!=null)
					alsData = getForms(sheet,alsData, cccError);
				else
				{
					alsError = getErrorInstance();					
					alsError.setErrorDesc(errorSheetMissing+" - "+formsSheetName);
					alsError.setSheetName(formsSheetName);
					alsError.setErrorSeverity(errorSeverity_fatal);					
					cccError.addAlsError(alsError); 
				}					
				sheet = workbook.getSheet(fieldsSheetName);
				if (alsData.getCccError()!=null)
					cccError = alsData.getCccError();		
				if (sheet!=null)
					alsData = getFields(sheet,alsData, cccError);
				else
				{
					alsError = getErrorInstance();					
					alsError.setErrorDesc(errorSheetMissing+" - "+fieldsSheetName);
					alsError.setSheetName(fieldsSheetName);
					alsError.setErrorSeverity(errorSeverity_fatal);					
					cccError.addAlsError(alsError); 
				}										
				sheet = workbook.getSheet(dataDictionarySheetName);
				if (alsData.getCccError()!=null)
					cccError = alsData.getCccError();	
				if (sheet!=null)
					alsData = getDataDictionaryEntries(sheet, alsData, cccError);
				else 		
				{
					alsError = getErrorInstance();					
					alsError.setErrorDesc(errorSheetMissing+" - "+dataDictionarySheetName);
					alsError.setSheetName(dataDictionarySheetName);
					alsError.setErrorSeverity(errorSeverity_warn);					
					cccError.addAlsError(alsError); 
				}
				sheet = workbook.getSheet(unitDictionarySheetName);
				if (sheet!=null)
					alsData = getUnitDictionaryEntries(sheet, alsData, cccError);
				
				workbook.close();
		} catch (IOException ioe) {
			alsError = getErrorInstance();
			if (ioe.getMessage().indexOf("Invalid header signature") > -1) {
				logger.debug(ioe.getMessage());
				alsError.setErrorDesc("Invalid file format, please check if the uploaded file is in XLS format.");
			} else {
				alsError.setErrorDesc(ioe.getMessage());				
			}
			alsError.setErrorSeverity(errorSeverity_fatal);
			cccError.addAlsError(alsError);
		} catch (InvalidFormatException ife) {
			alsError = getErrorInstance();
			alsError.setErrorDesc(ife.getMessage());
			alsError.setErrorSeverity(errorSeverity_fatal);
			cccError.addAlsError(alsError);
		} catch (NullPointerException npe) {
			alsError = getErrorInstance();
			alsError.setErrorDesc(npe.getMessage());
			alsError.setErrorSeverity(errorSeverity_fatal);
			cccError.addAlsError(alsError);
		}
		if (cccError.getAlsErrors().size() > 0)
			alsData.setCccError(cccError);
		logger.debug("Parsing done ");
		return alsData;
	}	
	
	/**
	 * @param Sheet
	 * @param ALSData 
	 * @return List ALSCrfDraft Populates a collection of ALSCrfDraft objects
	 *         parsed out of the ALS input file
	 * 
	 */

	protected static ALSData getCrfDraft(Sheet sheet, ALSData alsData, CCCError cccError) throws IOException {
		DateFormat dateFormat = new SimpleDateFormat(reportDateFormat);
		Date date = new Date();
		ALSError alsError;		
			alsData.setReportDate(dateFormat.format(date));
			Row newRow = sheet.getRow(crfDraftStartRow);
			ALSCrfDraft crfDraft = getAlsCrfDraftInstance();
			if (newRow.getCell(cell_crfDraftName)!=null && !newRow.getCell(cell_crfDraftName).equals(""))
				crfDraft.setDraftName(dataFormatter.formatCellValue(newRow.getCell(cell_crfDraftName)));
			if (newRow.getCell(cell_crfDraftProjectName) == null || newRow.getCell(cell_crfDraftProjectName).equals("")) {
					alsError = getErrorInstance();
					alsError.setErrorDesc(err_msg_1);
					alsError.setSheetName(sheet.getSheetName());
					alsError.setRowNumber(newRow.getRowNum());
					alsError.setColNumber(cell_crfDraftProjectName);
					alsError.setErrorSeverity(errorSeverity_error);
					cccError.addAlsError(alsError);
				}
			else {
				Cell newCell = newRow.getCell(cell_crfDraftProjectName);
				String cellValue = dataFormatter.formatCellValue(newCell);
				crfDraft.setProjectName(cellValue); 
				cccError.setRaveProtocolName(cellValue);
				}
			if (newRow.getCell(cell_crfDraftPrimaryFormOid)== null || newRow.getCell(cell_crfDraftPrimaryFormOid).equals("")) {
					alsError = getErrorInstance();			
					alsError.setErrorDesc(err_msg_2);
					alsError.setSheetName(sheet.getSheetName());
					alsError.setRowNumber(newRow.getRowNum());
					alsError.setColNumber(cell_crfDraftPrimaryFormOid);
					alsError.setErrorSeverity(errorSeverity_error);
					cccError.addAlsError(alsError); 
				}				
			else {
				Cell newCell = newRow.getCell(cell_crfDraftPrimaryFormOid);
				String cellValue = dataFormatter.formatCellValue(newCell);
				crfDraft.setPrimaryFormOid(cellValue);
				cccError.setRaveProtocolNumber(cellValue);
				}
			if (crfDraft.getPrimaryFormOid()!=null && crfDraft.getProjectName()!=null) {
				alsData.setCrfDraft(crfDraft);
			}
			if (cccError.getAlsErrors().size() > 0)
				alsData.setCccError(cccError);	
			return alsData;
	}

	/**
	 * @param Sheet
	 * @return List ALSForm Populates a collection of (all) Form objects parsed out of
	 *         the ALS input file
	 * 
	 */
	protected static ALSData getForms(Sheet sheet, ALSData alsData, CCCError cccError) throws IOException {
		List<ALSForm> forms = new ArrayList<ALSForm>();
		ALSError alsError;		
			Iterator<Row> rowIterator = sheet.rowIterator();
			Row row = rowIterator.next();
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				ALSForm form = getAlsFormInstance();
				if (row.getCell(cell_formOid) != null) {
					form.setFormOid(dataFormatter.formatCellValue(row.getCell(cell_formOid)));
					if (row.getCell(cell_formOrdinal) != null)	
						form.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(cell_formOrdinal))));
					else {
							alsError = getErrorInstance();
							alsError.setErrorDesc(err_msg_4);
							alsError.setSheetName(sheet.getSheetName());
							alsError.setRowNumber(row.getRowNum());
							alsError.setColNumber(cell_formOrdinal);
							alsError.setFormOid(dataFormatter.formatCellValue(row.getCell(cell_formOid)));
							alsError.setErrorSeverity(errorSeverity_warn);
							cccError.addAlsError(alsError);
						}
					if (row.getCell(cell_formDraftName) != null)
						form.setDraftFormName(dataFormatter.formatCellValue(row.getCell(cell_formDraftName)));
					else  {
							alsError = getErrorInstance();
							alsError.setErrorDesc(err_msg_5);
							alsError.setSheetName(sheet.getSheetName());
							alsError.setRowNumber(row.getRowNum());
							alsError.setColNumber(cell_formDraftName);
							alsError.setFormOid(dataFormatter.formatCellValue(row.getCell(cell_formOid)));							
							alsError.setErrorSeverity(errorSeverity_warn);							
							cccError.addAlsError(alsError);
						}		
				} else {
					if (row.getCell(cell_formOrdinal) != null && row.getCell(cell_formDraftName) != null) {
							alsError = getErrorInstance();
							alsError.setErrorDesc(err_msg_3);
							alsError.setSheetName(sheet.getSheetName());
							alsError.setRowNumber(row.getRowNum());
							alsError.setColNumber(cell_formOid);
							alsError.setErrorSeverity(errorSeverity_error);
							cccError.addAlsError(alsError); 
						}
					}
				if (cccError.getAlsErrors().size() > 0) {
						alsData.setCccError(cccError);	
					} else { 			
						if (row.getCell(cell_formOid) != null && row.getCell(cell_formOrdinal) != null && row.getCell(cell_formDraftName) != null) {
							forms.add(form);
						}
				}
			}
		alsData.setForms(forms); 			
		return alsData;
	}

	/**
	 * @param Sheet
	 * @return List ALSField Populates a collection of Field (Question) objects
	 *         parsed out of the ALS input file
	 * 
	 */
	protected static ALSData getFields(Sheet sheet, ALSData alsData, CCCError cccError) throws NullPointerException {
		List<ALSField> fields = new ArrayList<ALSField>();
		ALSField field;		
		ALSError alsError;		
			Iterator<Row> rowIterator = sheet.rowIterator();
			Row row = rowIterator.next();
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				field = getAlsFieldInstance();
				String formOid = null;
				String fieldOid = null;
				String dataDictionaryName = null;
				String unitDictionaryName = null;
				if (row.getCell(cell_field_formOid) != null) {
					formOid = dataFormatter.formatCellValue(row.getCell(cell_field_formOid));					
					field.setFormOid(formOid);
					if (row.getCell(cell_fieldDataDictionaryName)!=null) {
							dataDictionaryName = dataFormatter.formatCellValue(row.getCell(cell_fieldDataDictionaryName));
							field.setDataDictionaryName(dataDictionaryName);
						}
					if (row.getCell(cell_fieldUnitDictionaryName)!=null) {
							unitDictionaryName = dataFormatter.formatCellValue(row.getCell(cell_fieldUnitDictionaryName));
							field.setUnitDictionaryName(unitDictionaryName); 
						}			
					
					if (row.getCell(cell_fieldOid)!=null) {
							fieldOid = dataFormatter.formatCellValue(row.getCell(cell_fieldOid));						
							field.setFieldOid(fieldOid); 
					} else {
						alsError = getErrorInstance();
						alsError.setErrorDesc(err_msg_7);
						alsError.setSheetName(sheet.getSheetName());
						alsError.setRowNumber(row.getRowNum());
						alsError.setColNumber(cell_fieldOid);			
						alsError.setFormOid(dataFormatter.formatCellValue(row.getCell(cell_field_formOid)));
						if (formOid!=null)
							alsError.setFormOid(formOid);
						if (dataDictionaryName!=null)
							alsError.setDataDictionaryName(dataDictionaryName);
						if (unitDictionaryName!=null)
							alsError.setUnitDictionaryName(unitDictionaryName);
						alsError.setErrorSeverity(errorSeverity_error);
						cccError.addAlsError(alsError);
					}
					
					if (row.getCell(cell_fieldOrdinal)!=null)	{
						field.setOrdinal(dataFormatter.formatCellValue(row.getCell(cell_fieldOrdinal)));
						try {
					        Integer.parseInt(field.getOrdinal());
					    }
					    catch (NumberFormatException e) {
							alsError = getErrorInstance();
							alsError.setErrorDesc(err_msg_24);
							alsError.setSheetName(sheet.getSheetName());
							alsError.setRowNumber(row.getRowNum());
							alsError.setColNumber(cell_fieldOrdinal);		
							if (formOid!=null)
								alsError.setFormOid(formOid);
							if (fieldOid!=null)
								alsError.setFieldOid(fieldOid);
							if (dataDictionaryName!=null)
								alsError.setDataDictionaryName(dataDictionaryName);
							if (unitDictionaryName!=null)
								alsError.setUnitDictionaryName(unitDictionaryName);
							alsError.setErrorSeverity(errorSeverity_warn);
							alsData.getCccError().addAlsError(alsError);				    		
					    }						
					}
					if (row.getCell(cell_draftFieldName)!=null) {
						String draftFieldName = dataFormatter.formatCellValue(row.getCell(cell_draftFieldName));
						String idVersion = "";
						field.setDraftFieldName(draftFieldName);
						if ("FORM_OID".equalsIgnoreCase(draftFieldName)) {
							if (row.getCell(cell_fieldDefaultValue)!=null) { 
									field.setDefaultValue(dataFormatter.formatCellValue(row.getCell(cell_fieldDefaultValue)));
										String defaultValue = field.getDefaultValue();
										if (defaultValue.indexOf(publicid_prefix) > -1 && defaultValue.indexOf(version_prefix) > -1) {
										String idVn = defaultValue.substring(defaultValue.indexOf(publicid_prefix),defaultValue.length());
										String id = idVn.substring(3, idVn.indexOf("_"));
										String version = (idVn.substring(idVn.indexOf(version_prefix) + 2, idVn.length()));
										id = id.trim();
										String[] versionTokens = version.split("\\_");
										version = versionTokens[0] + "." + versionTokens[1];
										field.setFormPublicId(id.trim());
										field.setVersion(version);
									}
								} 
						}						
						if (!(draftFieldName.indexOf(publicid_prefix) > -1 && draftFieldName.indexOf(version_prefix) > -1)) {
							alsError = getErrorInstance();
							alsError.setErrorDesc(err_msg_21);
							alsError.setCellValue(draftFieldName);
							alsError.setSheetName(sheet.getSheetName());
							alsError.setRowNumber(row.getRowNum());
							alsError.setColNumber(cell_draftFieldName);
							if (formOid!=null)
								alsError.setFormOid(formOid);
							if (fieldOid!=null)
								alsError.setFieldOid(fieldOid);
							if (dataDictionaryName!=null)
								alsError.setDataDictionaryName(dataDictionaryName);
							if (unitDictionaryName!=null)
								alsError.setUnitDictionaryName(unitDictionaryName);							
							alsError.setErrorSeverity(errorSeverity_warn);
							alsData.getCccError().addAlsError(alsError);							
						} else {
								idVersion = draftFieldName.substring(draftFieldName.indexOf(publicid_prefix), draftFieldName.length());
								String id = idVersion.substring(3, idVersion.indexOf("_"));
								String version = (idVersion.substring(idVersion.indexOf(version_prefix) + 2, idVersion.length()));
						        id = id.trim();
						        String[] versionTokens = version.split("\\_");
						        if (NumberUtils.isNumber(id) && NumberUtils.isNumber(versionTokens[0]) && NumberUtils.isNumber(versionTokens[1])) {
							        Integer.parseInt(versionTokens[0]);
							        Integer.parseInt(versionTokens[1]);
							        version = versionTokens[0] + "." + versionTokens[1];						        							        	
						        } else {
									alsError = getErrorInstance();
									alsError.setErrorDesc(err_msg_23);
									alsError.setCellValue(idVersion);
									alsError.setSheetName(sheet.getSheetName());
									alsError.setRowNumber(row.getRowNum());
									alsError.setColNumber(cell_draftFieldName);
									if (formOid!=null)
										alsError.setFormOid(formOid);
									if (fieldOid!=null)
										alsError.setFieldOid(fieldOid);
									if (dataDictionaryName!=null)
										alsError.setDataDictionaryName(dataDictionaryName);
									if (unitDictionaryName!=null)
										alsError.setUnitDictionaryName(unitDictionaryName);								
									alsError.setErrorSeverity(errorSeverity_error);
									alsData.getCccError().addAlsError(alsError);
						        }
						}
					}
					else
					{
						alsError = getErrorInstance();
						alsError.setErrorDesc(err_msg_9);
						alsError.setSheetName(sheet.getSheetName());
						alsError.setRowNumber(row.getRowNum());
						alsError.setColNumber(cell_draftFieldName);
						if (formOid!=null)
							alsError.setFormOid(formOid);
						if (fieldOid!=null)
							alsError.setFieldOid(fieldOid);
						if (dataDictionaryName!=null)
							alsError.setDataDictionaryName(dataDictionaryName);
						if (unitDictionaryName!=null)
							alsError.setUnitDictionaryName(unitDictionaryName);						
						alsError.setErrorSeverity(errorSeverity_error);
						cccError.addAlsError(alsError);
					} 
					if (row.getCell(cell_fieldDataFormat)!=null)
						field.setDataFormat(dataFormatter.formatCellValue(row.getCell(cell_fieldDataFormat)));
					if (row.getCell(cell_fieldControlType)!=null) {
						String controlType = dataFormatter.formatCellValue(row.getCell(cell_fieldControlType));
						field.setControlType(controlType);
							if (!controlTypes.contains(controlType)) {
								alsError = getErrorInstance();
								alsError.setErrorDesc(err_msg_22);
								alsError.setSheetName(sheet.getSheetName());
								alsError.setRowNumber(row.getRowNum());
								alsError.setColNumber(cell_fieldControlType);
								if (formOid!=null)
									alsError.setFormOid(formOid);
								if (fieldOid!=null)
									alsError.setFieldOid(fieldOid);
								if (dataDictionaryName!=null)
									alsError.setDataDictionaryName(dataDictionaryName);
								if (unitDictionaryName!=null)
									alsError.setUnitDictionaryName(unitDictionaryName);								
								alsError.setErrorSeverity(errorSeverity_warn);
								alsData.getCccError().addAlsError(alsError);				
							}
						}
					else
					{
						alsError = getErrorInstance();
						alsError.setErrorDesc(err_msg_12);
						alsError.setSheetName(sheet.getSheetName());
						alsError.setRowNumber(row.getRowNum());
						alsError.setColNumber(cell_fieldControlType);	
						if (formOid!=null)
							alsError.setFormOid(formOid);
						if (fieldOid!=null)
							alsError.setFieldOid(fieldOid);
						if (dataDictionaryName!=null)
							alsError.setDataDictionaryName(dataDictionaryName);
						if (unitDictionaryName!=null)
							alsError.setUnitDictionaryName(unitDictionaryName);						
						alsError.setErrorSeverity(errorSeverity_error);
						cccError.addAlsError(alsError);
					} 
					if (row.getCell(cell_fieldPreText)!=null)
						field.setPreText(dataFormatter.formatCellValue(row.getCell(cell_fieldPreText)));
					if (row.getCell(cell_fieldFixedUnit)!=null)
						field.setFixedUnit(dataFormatter.formatCellValue(row.getCell(cell_fieldFixedUnit)));
					for (ALSForm form : alsData.getForms()) {
						if (field.getFormOid().equalsIgnoreCase(form.getFormOid())) {
							form.getFields().add(field);
						}
					}
					fields.add(field);
				} else {
					if (row.getCell(cell_fieldOrdinal)!=null && row.getCell(cell_draftFieldName)!=null && row.getCell(cell_fieldControlType)!=null) {
						alsError = getErrorInstance();
						alsError.setErrorDesc(err_msg_6);
						alsError.setSheetName(sheet.getSheetName());
						alsError.setRowNumber(row.getRowNum());
						alsError.setColNumber(cell_field_formOid);						
						alsError.setErrorSeverity(errorSeverity_error);
						cccError.addAlsError(alsError);
					}	
				}
			}
			if (cccError.getAlsErrors().size() > 0) {
				alsData.setCccError(cccError);	
			}
			alsData.setFields(fields);				
		return alsData;
	}

	/**
	 * @param Sheet
	 * @return List ALSDataDictionaryEntry Populates a collection of Data
	 *         Dictionary Entries parsed out of the ALS input file
	 * 
	 */
	protected static ALSData getDataDictionaryEntries(Sheet sheet, ALSData alsData, CCCError cccError) throws NullPointerException {
		Map<String, ALSDataDictionaryEntry> ddeMap = new HashMap<String, ALSDataDictionaryEntry>();
			ALSDataDictionaryEntry dde = new ALSDataDictionaryEntry();
			ALSError alsError;
			final String regex_space = "[\\p{Z}\\s]";//"'\u00A0', '\u2007', '\u202F'";
			List<Integer> ordinal = new ArrayList<Integer>();
			List<String> cd = new ArrayList<String>();
			List<String> uds = new ArrayList<String>();
			List<Boolean> specify = new ArrayList<Boolean>();
			String ddName = "";
			Iterator<Row> rowIterator = sheet.rowIterator();
			Row row = rowIterator.next();
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
						ordinal = new ArrayList<Integer>();
						cd = new ArrayList<String>();
						uds = new ArrayList<String>();
						specify = new ArrayList<Boolean>();
					}
					if (row.getCell(cell_ddeCodedData)!=null)
						cd.add(dataFormatter.formatCellValue(row.getCell(cell_ddeCodedData)));
					else
						{
							alsError = getErrorInstance();
							alsError.setErrorDesc(err_msg_16);
							alsError.setSheetName(sheet.getSheetName());
							alsError.setRowNumber(row.getRowNum());
							alsError.setColNumber(cell_ddeCodedData);
							if (!ddName.equals(""))
								alsError.setDataDictionaryName(ddName);
							alsError.setErrorSeverity(errorSeverity_error);
							cccError.addAlsError(alsError);	
						}
					if (row.getCell(cell_ddeOrdinal)!=null)
						ordinal.add(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(cell_ddeOrdinal))));
					else
						{
							alsError = getErrorInstance();
							alsError.setErrorDesc(err_msg_17);
							alsError.setSheetName(sheet.getSheetName());
							alsError.setRowNumber(row.getRowNum());
							alsError.setColNumber(cell_ddeOrdinal);		
							if (!ddName.equals(""))
								alsError.setDataDictionaryName(ddName);							
							alsError.setErrorSeverity(errorSeverity_warn);
							cccError.addAlsError(alsError);	
						}
					if (row.getCell(cell_ddeUserDataString)!=null) {
						String udsStr = dataFormatter.formatCellValue(row.getCell(cell_ddeUserDataString));
						udsStr = udsStr.replaceAll(regex_space," ");
						uds.add(udsStr);							
					}	else
						{
							alsError = getErrorInstance();
							alsError.setErrorDesc(err_msg_18);
							alsError.setSheetName(sheet.getSheetName());
							alsError.setRowNumber(row.getRowNum());
							alsError.setColNumber(cell_ddeUserDataString);		
							if (!ddName.equals(""))
								alsError.setDataDictionaryName(ddName);							
							alsError.setErrorSeverity(errorSeverity_error);
							cccError.addAlsError(alsError);	
						}
					if (row.getCell(cell_ddeSpecify)!=null)
						specify.add(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(cell_ddeSpecify))));
					else
					{
						alsError = getErrorInstance();
						alsError.setErrorDesc(err_msg_19);
						alsError.setSheetName(sheet.getSheetName());
						alsError.setRowNumber(row.getRowNum());
						alsError.setColNumber(cell_ddeSpecify);						
						if (!ddName.equals(""))
							alsError.setDataDictionaryName(ddName);	
						alsError.setErrorSeverity(errorSeverity_warn);
						cccError.addAlsError(alsError);	
					}
				} else {
					if (row.getCell(cell_ddeCodedData) != null && row.getCell(cell_ddeOrdinal) != null && row.getCell(cell_ddeUserDataString) != null && row.getCell(cell_ddeSpecify) != null) {
						alsError = getErrorInstance();
						alsError.setErrorDesc(err_msg_15);
						alsError.setSheetName(sheet.getSheetName());
						alsError.setRowNumber(row.getRowNum());
						alsError.setColNumber(cell_ddeDataDictionaryName);																		
						alsError.setErrorSeverity(errorSeverity_error);
						cccError.addAlsError(alsError);
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
	protected static ALSData getUnitDictionaryEntries(Sheet sheet, ALSData alsData, CCCError cccError) throws NullPointerException {
		List <ALSUnitDictionaryEntry> udeList = new ArrayList<ALSUnitDictionaryEntry>();
		ALSUnitDictionaryEntry ude;
		ALSError alsError;			
		Iterator<Row> rowIterator = sheet.rowIterator();
		Row row = rowIterator.next();
		while (rowIterator.hasNext()) {
			row = rowIterator.next();
			ude = getUnitDictionaryInstance();
			if (row.getCell(cell_udName) != null) {
				ude.setUnitDictionaryName(dataFormatter.formatCellValue(row.getCell(cell_udName)));
				if (row.getCell(cell_udCodedUnit) != null)	
					ude.setCodedUnit(dataFormatter.formatCellValue(row.getCell(cell_udCodedUnit)));				
				if (row.getCell(cell_udOrdinal) != null)	
					ude.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(cell_udOrdinal))));
				if (row.getCell(cell_udConstantA) != null)	
					ude.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(cell_udConstantA))));
				if (row.getCell(cell_udConstantB) != null)	
					ude.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(cell_udConstantB))));
				if (row.getCell(cell_udConstantC) != null)	
					ude.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(cell_udConstantC))));				
				if (row.getCell(cell_udConstantK) != null)	
					ude.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(cell_udConstantK))));								
				if (row.getCell(cell_udUnitString) != null) {
					ude.setUnitString(dataFormatter.formatCellValue(row.getCell(cell_udUnitString)));
			} 
		}	else {
			if (row.getCell(cell_udCodedUnit) != null && row.getCell(cell_udOrdinal) != null && row.getCell(cell_udConstantA) != null && row.getCell(cell_udConstantB) != null && row.getCell(cell_udUnitString) != null) {
					alsError = getErrorInstance();
					alsError.setErrorDesc(err_msg_20);
					alsError.setSheetName(sheet.getSheetName());
					alsError.setRowNumber(row.getRowNum());
					alsError.setColNumber(cell_udName);																						
					alsError.setErrorSeverity(errorSeverity_error);
					cccError.addAlsError(alsError);
				}
		}			
			udeList.add(ude);
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

}
