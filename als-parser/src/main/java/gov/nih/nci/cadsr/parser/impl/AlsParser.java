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
	private static List<String> controlTypes = Arrays.asList("CheckBox","DateTime","DropDownList","Dynamic SearchList","Text","FileUpload","LongText","RadioButton","SearchList"); 

	
	private static String err_msg_1 = "RAVE Protocol Name is missing in the ALS file.";
	private static String err_msg_2 = "RAVE Protocol Number is missing in the ALS file.";
	private static String err_msg_3 = "FORM OID missing.";
	private static String err_msg_4 = "Ordinal of the form missing";	
	private static String err_msg_5 = "Draft Form name of the form missing";	
	private static String err_msg_6 = "Form OID missing in the Fields sheet.";
	private static String err_msg_7 = "Field OID missing in the Fields sheet.";
	private static String err_msg_8 = "Field Ordinal missing in Fields sheet.";
	private static String err_msg_9 = "Draft Field Name missing in Fields sheet.";	
	private static String err_msg_10 = "Data Format missing in Fields sheet.";
	private static String err_msg_11 = "Data Dictionary Name missing in Fields sheet.";
	private static String err_msg_12 = "Control Type missing in Fields sheet.";
	private static String err_msg_13 = "Pretext missing in Fields sheet.";
	private static String err_msg_14 = "Fixed Unit missing in Fields sheet.";		
	private static String err_msg_15 = "Data Dictionary Name is missing in Data Dictionary Entries sheet.";
	private static String err_msg_16 = "Coded Data is missing in Data Dictionary Entries sheet.";
	private static String err_msg_17 = "Ordinal is missing in Data Dictionary Entries sheet.";
	private static String err_msg_18 = "User Data String is missing in Data Dictionary Entries sheet.";
	private static String err_msg_19 = "Specify is missing in Data Dictionary Entries sheet."; // TODO May not be needed if we leave out Specify. If so, remove.
	private static String err_msg_20 = "Unit Dictionary Name missing in Unit Dictionary Entries sheet.";
	private static String err_msg_21 = "Question doesn't contain a CDE public id and version.";	
	private static String err_msg_22 = "This is an unknown control type.";		
	private static String err_msg_23 = "CDE public id and version should be numeric.";	
	private static String err_msg_24 = "Ordinal should be numeric.";	

	/**
	 * Parsing an ALS input file into data objects for validating against the
	 * database
	 * 
	 */
	public ALSData parse (String INPUT_XLSX_FILE_PATH, String fileName) throws IOException, InvalidFormatException, NullPointerException {
		CCCError cccError = getErrorObject();
		ALSData alsData = getAlsDataInstance();
		alsData.setFileName(fileName);
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
					cccError.addAlsError(alsError); 
				}					
				workbook.close();
		} catch (IOException ioe) {
			alsError = getErrorInstance();					
			alsError.setErrorDesc(ioe.getMessage());
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
					alsError.setErrorDesc(err_msg_1+" Sheet: "+sheet.getSheetName()+" Row: "+newRow.getRowNum()+" Cell: "+cell_crfDraftProjectName+".");
					alsError.setErrorSeverity(errorSeverity_error);
					cccError.addAlsError(alsError);
				}
			else {
				Cell newCell = newRow.getCell(cell_crfDraftProjectName);
				String cellValue = dataFormatter.formatCellValue(newCell);
				crfDraft.setProjectName(cellValue); 
				}
			if (newRow.getCell(cell_crfDraftPrimaryFormOid)== null || newRow.getCell(cell_crfDraftPrimaryFormOid).equals("")) {
					alsError = getErrorInstance();			
					alsError.setErrorDesc(err_msg_2+" Sheet: "+sheet.getSheetName()+" Row: "+newRow.getRowNum()+" Cell: "+cell_crfDraftPrimaryFormOid+".");
					alsError.setErrorSeverity(errorSeverity_error);
					cccError.addAlsError(alsError); 
				}				
			else {
				Cell newCell = newRow.getCell(cell_crfDraftPrimaryFormOid);
				String cellValue = dataFormatter.formatCellValue(newCell);
				crfDraft.setPrimaryFormOid(cellValue); 
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
					form.setFormOId(dataFormatter.formatCellValue(row.getCell(cell_formOid)));
					if (row.getCell(cell_formOrdinal) != null)	
						form.setOrdinal(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(cell_formOrdinal))));
					else {
							alsError = getErrorInstance();
							alsError.setErrorDesc((err_msg_4)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_formOrdinal+".");
							alsError.setErrorSeverity(errorSeverity_warn);
							cccError.addAlsError(alsError);
						}
					if (row.getCell(cell_formDraftName) != null)
						form.setDraftFormName(dataFormatter.formatCellValue(row.getCell(cell_formDraftName)));
					else  {
							alsError = getErrorInstance();
							alsError.setErrorDesc((err_msg_5)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_formDraftName+".");
							alsError.setErrorSeverity(errorSeverity_warn);							
							cccError.addAlsError(alsError);
						}					
				} else {
						alsError = getErrorInstance();
						alsError.setErrorDesc((err_msg_3)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_formOid+".");
						alsError.setErrorSeverity(errorSeverity_error);
						cccError.addAlsError(alsError);
					}
				if (cccError.getAlsErrors().size() > 0) {
						alsData.setCccError(cccError);	
					} else { 					
						forms.add(form);
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
				if (row.getCell(cell_field_formOid) != null) {
					field.setFormOid(dataFormatter.formatCellValue(row.getCell(cell_field_formOid)));
					if (row.getCell(cell_fieldOid)!=null)
						field.setFieldOid(dataFormatter.formatCellValue(row.getCell(cell_fieldOid)));
					else 
					{
						alsError = getErrorInstance();
						alsError.setErrorDesc((err_msg_7)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_fieldOid+".");
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
							alsError.setErrorDesc(err_msg_24+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_fieldOrdinal+".");
							alsError.setErrorSeverity(errorSeverity_warn);
							alsData.getCccError().addAlsError(alsError);				    		
					    }						
					}
					if (row.getCell(cell_draftFieldName)!=null) {
						String draftFieldName = dataFormatter.formatCellValue(row.getCell(cell_draftFieldName));
						String idVersion = "";
						field.setDraftFieldName(draftFieldName);
						if (!(draftFieldName.indexOf("PID") > -1 && draftFieldName.indexOf("_V") > -1)) {
							alsError = getErrorInstance();
							alsError.setErrorDesc(err_msg_21+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_draftFieldName+".");
							alsError.setErrorSeverity(errorSeverity_warn);
							alsData.getCccError().addAlsError(alsError);							
						} else {
							try {
								idVersion = draftFieldName.substring(draftFieldName.indexOf("PID"), draftFieldName.length());
								String id = idVersion.substring(3, idVersion.indexOf("_"));
								String version = (idVersion.substring(idVersion.indexOf("_V") + 2, idVersion.length()));
						        Integer.parseInt(id.trim());
						        String[] versionTokens = version.split("\\_");
						        Integer.parseInt(versionTokens[0]);
						        Integer.parseInt(versionTokens[1]);
						        version = versionTokens[0] + "." + versionTokens[1];						        
						    }
						    catch (NumberFormatException e) {
								alsError = getErrorInstance();
								alsError.setErrorDesc(err_msg_23+ " " + idVersion +" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_draftFieldName+".");
								alsError.setErrorSeverity(errorSeverity_error);
								alsData.getCccError().addAlsError(alsError);				    		
						    }
						}
					}
					else
					{
						alsError = getErrorInstance();
						alsError.setErrorDesc((err_msg_9)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_draftFieldName+".");
						alsError.setErrorSeverity(errorSeverity_error);						
						cccError.addAlsError(alsError);
					} 
					if (row.getCell(cell_fieldDataFormat)!=null)
						field.setDataFormat(dataFormatter.formatCellValue(row.getCell(cell_fieldDataFormat)));
					if (row.getCell(cell_fieldDataDictionaryName)!=null)
						field.setDataDictionaryName(dataFormatter.formatCellValue(row.getCell(cell_fieldDataDictionaryName)));
					if (row.getCell(cell_fieldUnitDictionaryName)!=null)
						field.setUnitDictionaryName(dataFormatter.formatCellValue(row.getCell(cell_fieldUnitDictionaryName)));
					if (row.getCell(cell_fieldControlType)!=null) {
						String controlType = dataFormatter.formatCellValue(row.getCell(cell_fieldControlType));
						field.setControlType(controlType);
							if (!controlTypes.contains(controlType)) {
								alsError = getErrorInstance();
								alsError.setErrorDesc(err_msg_22+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_fieldControlType+".");
								alsError.setErrorSeverity(errorSeverity_warn);
								alsData.getCccError().addAlsError(alsError);				
							}
						}
					else
					{
						alsError = getErrorInstance();
						alsError.setErrorDesc((err_msg_12)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_fieldControlType+".");
						alsError.setErrorSeverity(errorSeverity_error);
						cccError.addAlsError(alsError);
					} 
					if (row.getCell(cell_fieldPreText)!=null)
						field.setPreText(dataFormatter.formatCellValue(row.getCell(cell_fieldPreText)));
					if (row.getCell(cell_fieldFixedUnit)!=null)
						field.setFixedUnit(dataFormatter.formatCellValue(row.getCell(cell_fieldFixedUnit)));
					for (ALSForm form : alsData.getForms()) {
						if (field.getFormOid().equalsIgnoreCase(form.getFormOId())) {
							form.getFields().add(field);
						}
					}
					fields.add(field);
				} else {
					alsError = getErrorInstance();
					alsError.setErrorDesc((err_msg_6)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_field_formOid+".");
					alsError.setErrorSeverity(errorSeverity_error);
					cccError.addAlsError(alsError);	
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
							alsError.setErrorDesc((err_msg_16)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_ddeCodedData+".");
							alsError.setErrorSeverity(errorSeverity_error);
							cccError.addAlsError(alsError);	
						}
					if (row.getCell(cell_ddeOrdinal)!=null)
						ordinal.add(Integer.parseInt(dataFormatter.formatCellValue(row.getCell(cell_ddeOrdinal))));
					else
						{
							alsError = getErrorInstance();
							alsError.setErrorDesc((err_msg_17)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_ddeOrdinal+".");
							alsError.setErrorSeverity(errorSeverity_warn);
							cccError.addAlsError(alsError);	
						}
					if (row.getCell(cell_ddeUserDataString)!=null)
						uds.add(dataFormatter.formatCellValue(row.getCell(cell_ddeUserDataString)));
					else
						{
							alsError = getErrorInstance();
							alsError.setErrorDesc((err_msg_18)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_ddeUserDataString+".");
							alsError.setErrorSeverity(errorSeverity_error);
							cccError.addAlsError(alsError);	
						}
					if (row.getCell(cell_ddeSpecify)!=null)
						specify.add(Boolean.valueOf(dataFormatter.formatCellValue(row.getCell(cell_ddeSpecify))));
					else
					{
						alsError = getErrorInstance();
						alsError.setErrorDesc((err_msg_19)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_ddeSpecify+".");
						alsError.setErrorSeverity(errorSeverity_warn);
						cccError.addAlsError(alsError);	
					}
				} else {
					alsError = getErrorInstance();
					alsError.setErrorDesc((err_msg_15)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_ddeDataDictionaryName+".");
					alsError.setErrorSeverity(errorSeverity_error);
					cccError.addAlsError(alsError);
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
				alsError = getErrorInstance();
				alsError.setErrorDesc((err_msg_20)+" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_udName+".");
				alsError.setErrorSeverity(errorSeverity_error);
				cccError.addAlsError(alsError);
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
