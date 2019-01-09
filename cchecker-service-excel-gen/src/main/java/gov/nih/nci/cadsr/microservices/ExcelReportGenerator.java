package gov.nih.nci.cadsr.microservices;
/*
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.NrdsCde;
import gov.nih.nci.cadsr.data.StandardCrfCde;

public class ExcelReportGenerator {

		private static final Logger logger = LoggerFactory.getLogger(ExcelReportGenerator.class);
		private static String formHeader_1 = "View of Expanded Results for ";
		private static String formHeader_2 = " form";	
		private static String summaryFormsHeader = "Report Summary - Click on Form Name to expand results";
		private static String summaryFormsValidResult = "Validation Result";
		private static int summaryFormsValidResultColNum = 1;
		private static String checkerReportOwnerLbl = "CDE Congruency Checker Report for ";
		private static String raveProtocolNameLbl = "Rave Protocol name ";
		private static String raveProtocolNumLbl = "Rave Protocol number ";
		private static String reportDateLbl = "Date Validated ";
		private static String formCountLbl = "# Forms in protocol ";
		private static String totalQuestCongLbl = "# Total Questions Congruent ";
		private static String totalQuestCheckLbl = "# Total Questions Checked ";
		private static String totalQuestWarnLbl = "# Total Questions with Warnings ";
		private static String totalQuestErrorLbl = "# Total Questions with Errors ";
		private static String totalunassociatedQuestLbl = "# Total Questions without associated CDE ";
		private static String reqQuestMissLbl = "# Required NRDS Questions missing ";
		private static String reqNrdsQuestCongLbl = "# Required NRDS Questions Congruent ";
		private static String reqNrdsQuestWarnLbl = "# Required NRDS Questions With Warnings ";
		private static String reqNrdsQuestErrorLbl = "# Required NRDS Questions With Errors ";
		private static String nciStdManQuestLbl = "# NCI Standard Template Mandatory Modules Questions missing from Protocol ";
		private static String nciStdManCongLbl = "# NCI Standard Template Mandatory Modules Questions Congruent ";
		private static String nciStdManErrorLbl = "# NCI Standard Template Mandatory Modules Questions With Errors ";
		private static String nciStdManWarnLbl = "# NCI Standard Template Mandatory Modules Questions With Warnings ";	
		private static String nciStdCondQuestLbl = "# NCI Standard Template Conditional Modules Questions missing from Protocol ";
		private static String nciStdCondCongLbl = "# NCI Standard Template Conditional Modules Questions Congruent ";
		private static String nciStdCondErrorLbl = "# NCI Standard Template Conditional Modules Questions With Errors ";
		private static String nciStdCondWarnLbl = "# NCI Standard Template Conditional Modules Questions With Warnings ";	
		private static String nciStdOptQuestLbl = "# NCI Standard Template Optional Modules Questions missing from Protocol ";
		private static String nciStdOptCongLbl = "# NCI Standard Template Optional Modules Questions Congruent ";
		private static String nciStdOptErrorLbl = "# NCI Standard Template Optional Modules Questions With Errors ";
		private static String nciStdOptWarnLbl = "# NCI Standard Template Optional Modules Questions With Warnings ";		
		private static int formStartColumn = 4;	
		private static int raveFieldDataTypeCol = 22;
		private static int codedDataColStart = 16;
		private static String matching_nrds_cdes_tab_name = "NRDS CDEs in ALS";
		private static String nrds_missing_cde_tab_name = "NRDS CDEs missing";
		private static String nrds_missing_cde_header = "NRDS CDEs missing from the ALS file";
		private static String matching_nrds_cdes_header = "NRDS CDEs included in Protocol Forms with Warnings or Errors";
		private static String congStatus_Congruent = "CONGRUENT";
		private static int cell_max_limit = 32767;
		private static int cell_write_limit = 32700;
		private static final int idxSummaryLbl = 0;
		private static final int idxSummaryVal = 1;
		private static final int widthSummaryVal = 12800;
		private static final int widthFormColsShort = 5120;
		private static final int widthFormColsLong = 25600;			
		private static Integer[] shortColumnsforForm = {0,1,2,3,4,5,6,7,8,11,13,14,15,17,20,22,23,24,25,26,27,28,29,30,31,32,33,34};
		private static Integer[] longColumnsforForm = { 9,10,12,16,18,19,21 };
		private static String croppedStringText = "// CONTENT CROPPED TO 32,700 CHARACTERS. // \n ";
		private static String[] rowHeaders = { "Rave Form OID", "caDSR Form ID", "Version", "Total Number Of Questions Checked",
				"Field Order", "CDE Public ID", "CDE Version", "NCI Category", "Question Congruency Status", "Message",
				"Rave Field Label", "Rave Field Label Result", "CDE Permitted Question Text Choices",
				"Rave Control Type", "Control Type Checker Result", "CDE Value Domain Type", "Rave Coded Data", "Coded Data Result",
				"Allowable CDE  Value", "Rave User String", "PV Result", "Allowable CDE  Value Meaning Text Choices",
				"Rave Field Data Type", "Data Type Checker Result", "CDE Data Type", "Rave UOM", "UOM Checker Result", "CDE UOM",
				"Rave Length", "Length Checker Result", "CDE Maximum Length", "Rave Display Format", "Format Checker Result",
				"CDE Display Format" };

		/**
		 * @param
		 * @return Implementation for Writing the final output report object into an excel (as a feasibility check)
		 * 
		 */
		public static void writeExcel(String OUTPUT_XLSX_FILE_PATH, CCCReport cccReport) throws IOException, InvalidFormatException, NullPointerException {

			Row row;
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Summary");
			CreationHelper createHelper = workbook.getCreationHelper();
			
			//cell style for hyperlinks
            //by default hyperlinks are blue and underlined
            CellStyle hlink_style = workbook.createCellStyle();
            Font hlink_font = workbook.createFont();
            hlink_font.setUnderline(Font.U_SINGLE);
            hlink_font.setColor(IndexedColors.BLACK.getIndex());
            hlink_style.setFont(hlink_font);			
			
			// Adding labels for each value (row) displayed in the summary page of the report			
			Map<String, String> summaryLabels = returnSummaryLabelsMap(cccReport);
			
			int rowNum = 0;
			
			// Setting a fixed column width for values in the Summary page (sheet)
			sheet.setColumnWidth(idxSummaryLbl, widthFormColsLong);
			
			// Setting a fixed column width for labels in the Summary page (sheet)
			sheet.setColumnWidth(idxSummaryVal, widthSummaryVal);	
			
			logger.debug("Creating excel");
			
			// Printing the Summary labels and their corresponding values next to them			
			for (Map.Entry<String, String> label : summaryLabels.entrySet()) {
				row = sheet.createRow(rowNum++);
				int colNum = 0;
				if ((formCountLbl.equals(label.getKey())))
					row = sheet.createRow(rowNum++);
				Cell cell = row.createCell(colNum++);
				cell.setCellValue((String) label.getKey());
				cell = row.createCell(colNum++);
				cell.setCellValue((String) label.getValue());
			}
			row = sheet.createRow(rowNum++);
			row = sheet.createRow(rowNum++);
			Cell newCell = row.createCell(0);
			
			// Printing the Report Summary [2nd half - Summary sheet] with forms' congruency status
			newCell.setCellValue(summaryFormsHeader);
			newCell = row.createCell(summaryFormsValidResultColNum);
			newCell.setCellValue(summaryFormsValidResult);
			List<CCCForm> forms = cccReport.getCccForms();			
			// Iterating through the forms list in the report to display their name and their congruency status
			for (CCCForm form : forms) {
				row = sheet.createRow(rowNum++);
				int colNum = 0;
				Cell cell = row.createCell(colNum++);
				cell.setCellValue(form.getRaveFormOid());
				//Creating the link for Form to open the corresponding sheet
				Hyperlink link = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
				String linkText = "'"+form.getRaveFormOid()+"'!E1";//"'Target Sheet'!A1"
				link.setAddress(linkText);
				cell.setHyperlink(link);
				cell.setCellStyle(hlink_style);				
				cell = row.createCell(summaryFormsValidResultColNum);
				cell.setCellValue(form.getCongruencyStatus());
			}
			
			// Iterating through the forms list to create sheets for each one
			for (CCCForm cccForm : forms) {
				if (cccForm.getCongruencyStatus()!=null) {
					if (congStatus_Congruent.equalsIgnoreCase(cccForm.getCongruencyStatus())) {
						continue;
					} else {
				Sheet sheet2 = workbook.createSheet(cccForm.getRaveFormOid());
				// Setting width for columns that are expected to be Short
				setColumnWidth(sheet2, shortColumnsforForm, widthFormColsShort);
				// Setting width for columns that are expected to be Long
				setColumnWidth(sheet2, longColumnsforForm, widthFormColsLong);
				rowNum = 0;
				row = sheet2.createRow(rowNum++);
				newCell = row.createCell(0);
				newCell.setCellValue(formHeader_1 + cccForm.getRaveFormOid() + formHeader_2);
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
				newCell.setCellValue(cccForm.getRaveFormOid());	
				newCell = row.createCell(1);
				newCell.setCellValue(cccForm.getFormPublicId());
				newCell = row.createCell(2);
				newCell.setCellValue(cccForm.getFormVersion());
				newCell = row.createCell(3);
				newCell.setCellValue(cccForm.getTotalQuestionsChecked());
	            CellStyle cellStyle = workbook.createCellStyle(); //Create new style
	            cellStyle.setWrapText(true);
				for (int j = 0; j < cccForm.getQuestions().size(); j++) {
					int colNum2 = formStartColumn;
					CCCQuestion question = cccForm.getQuestions().get(j);
					row = sheet2.createRow(rowNum++);
					// Printing columns before Coded data column					
					Map<String, String> formFields1 = returnFormFieldsPart1(question);
					row = returnFilledRow(formFields1, row, colNum2, cellStyle);
					Row rowBeforeCD = row;
					
					// Nested loop processing for Coded data and User data string for a CDE
					Map<String, Integer> rowAndColNumbers = prepareCodedDataResultsforReport(question, row, cellStyle, rowNum, sheet2);
					int rowNumAfterCD = rowAndColNumbers.get("RowNum");
					colNum2 = rowAndColNumbers.get("ColNum");
					row = rowBeforeCD;
					int newColNum = raveFieldDataTypeCol;
					// Printing columns after Coded data column
					Map<String, String> formFields2 = returnFormFieldsPart2(question);					
					row = returnFilledRow(formFields2, row, newColNum, null);
					if (rowNumAfterCD > rowNum)
						rowNum = rowNumAfterCD;
					}
				   }
			    }
			}
			buildNrdsTab(workbook, cccReport.getNrdsCdeList());
			buildMissingNrdsCdesTab(workbook, cccReport.getMissingNrdsCdeList());
			if (cccReport.getIsCheckStdCrfCdeChecked()) {
				buildStdCrfMissingTabs(workbook, cccReport.getMissingStandardCrfCdeList());
			}			
			FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(OUTPUT_XLSX_FILE_PATH);
				logger.debug("..outputStream created for " + OUTPUT_XLSX_FILE_PATH);
				workbook.write(outputStream);
				logger.debug("...workbook.write done");
				workbook.close();
				logger.debug("...workbook.close done");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				logger.error("outputStream FileNotFoundException " + OUTPUT_XLSX_FILE_PATH + ", FileNotFoundException: "+ e);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Exception in Excel generation on file " + OUTPUT_XLSX_FILE_PATH + ", getMessage: " + e.getMessage() + e);
			}
			finally {
				if (outputStream != null) {
					outputStream.flush();
					outputStream.close();
					logger.debug("File is closed: " + OUTPUT_XLSX_FILE_PATH);
				}
			}
		}
		
		
		/**
		 * Populate a map with the labels and their values, for Summary sheet (1st half)
		 * @param cccReport
		 * @return Map<String, String>
		 */
		public static Map<String, String> returnSummaryLabelsMap(CCCReport cccReport) {
			Map<String, String> summaryLabels = new LinkedHashMap<String, String>();
			summaryLabels.put(checkerReportOwnerLbl, cccReport.getReportOwner());
			summaryLabels.put(raveProtocolNameLbl, cccReport.getRaveProtocolName());
			summaryLabels.put(raveProtocolNumLbl, cccReport.getRaveProtocolNumber());
			summaryLabels.put(reportDateLbl, cccReport.getReportDate());
			summaryLabels.put(formCountLbl, String.valueOf(cccReport.getTotalFormsCount()));
			summaryLabels.put(totalQuestCheckLbl, String.valueOf(cccReport.getCountQuestionsChecked()));
			summaryLabels.put(totalQuestCongLbl, String.valueOf(cccReport.getCountCongruentQuestions()));			
			summaryLabels.put(totalQuestWarnLbl, String.valueOf(cccReport.getCountQuestionsWithWarnings()));
			summaryLabels.put(totalQuestErrorLbl, String.valueOf(cccReport.getCountQuestionsWithErrors()));
			summaryLabels.put(totalunassociatedQuestLbl, String.valueOf(cccReport.getCountQuestionsWithoutCde()));
			summaryLabels.put(reqQuestMissLbl, String.valueOf(cccReport.getCountNrdsMissing()));
			summaryLabels.put(reqNrdsQuestCongLbl, String.valueOf(cccReport.getCountNrdsCongruent()));
			summaryLabels.put(reqNrdsQuestWarnLbl, String.valueOf(cccReport.getCountNrdsWithWarnings()));
			summaryLabels.put(reqNrdsQuestErrorLbl, String.valueOf(cccReport.getCountNrdsWithErrors()));
			summaryLabels.put(nciStdManQuestLbl, String.valueOf(cccReport.getCountManCrfMissing()));
			summaryLabels.put(nciStdManCongLbl, String.valueOf(cccReport.getCountManCrfCongruent()));
			summaryLabels.put(nciStdManErrorLbl, String.valueOf(cccReport.getCountManCrfWithErrors()));
			summaryLabels.put(nciStdManWarnLbl, String.valueOf(cccReport.getCountManCrfwWithWarnings()));
			summaryLabels.put(nciStdCondQuestLbl, String.valueOf(cccReport.getCountCondCrfMissing()));
			summaryLabels.put(nciStdCondCongLbl, String.valueOf(cccReport.getCountCondCrfCongruent()));
			summaryLabels.put(nciStdCondErrorLbl, String.valueOf(cccReport.getCountCondCrfWithErrors()));
			summaryLabels.put(nciStdCondWarnLbl, String.valueOf(cccReport.getCountCondCrfwWithWarnings()));
			summaryLabels.put(nciStdOptQuestLbl, String.valueOf(cccReport.getCountOptCrfMissing()));
			summaryLabels.put(nciStdOptCongLbl, String.valueOf(cccReport.getCountOptCrfCongruent()));
			summaryLabels.put(nciStdOptErrorLbl, String.valueOf(cccReport.getCountOptCrfWithErrors()));
			summaryLabels.put(nciStdOptWarnLbl, String.valueOf(cccReport.getCountOptCrfwWithWarnings()));			
			return summaryLabels;
		}
		
		
		
		/**
		 * Populate a map with the report values before Coded data column, for individual forms
		 * @param cccReport
		 * @return Map<String, String>
		 */
		public static Map<String, String> returnFormFieldsPart1(CCCQuestion question) {
			Map<String, String> formFields = new LinkedHashMap<String, String>();
			formFields.put("Field Order", question.getFieldOrder());
			formFields.put("CDE Public ID", question.getCdePublicId());
			formFields.put("CDE Version", question.getCdeVersion());
			formFields.put("NCI Category", question.getNciCategory());
			formFields.put("Question Congruency Status", question.getQuestionCongruencyStatus());
			String message = question.getMessage();
			// Checking for the length of the string for max limit before writing to cell
			if (message!=null && message.length() > cell_max_limit){
				message = croppedStringText+message.substring(0, cell_write_limit);
			}			
			formFields.put("Message", message);
			formFields.put("RAVE Field Label ", question.getRaveFieldLabel());
			formFields.put("RAVE Field Label Result", question.getRaveFieldLabelResult());
			formFields.put("CDE Permitted Question Text Choices", question.getCdePermitQuestionTextChoices());
			formFields.put("RAVE Control Type", question.getRaveControlType());				
			formFields.put("RAVE Control Type Result", question.getControlTypeResult());								
			formFields.put("CDE Value Domain Type", question.getCdeValueDomainType());
			return formFields;
			}		
		
		/**
		 * Populate a map with the report values after Coded data column, for individual forms
		 * @param cccReport
		 * @return Map<String, String>
		 */		
		public static Map<String, String> returnFormFieldsPart2(CCCQuestion question) {
			Map<String, String> formFields = new LinkedHashMap<String, String>();
			formFields.put("RAVE Field Data Type", question.getRaveFieldDataType());				
			formFields.put("Data Type Checker Result", question.getDatatypeCheckerResult());								
			formFields.put("CDE Data Type", question.getCdeDataType());
			formFields.put("RAVE UOM", question.getRaveUOM());
			formFields.put("UOM Checker Result", question.getUomCheckerResult());
			formFields.put("CDE UOM", question.getCdeUOM());
			formFields.put("RAVE Length", question.getRaveLength());
			formFields.put("Length Checker Result", question.getLengthCheckerResult());
			formFields.put("CDE Max Length", String.valueOf(question.getCdeMaxLength()));
			formFields.put("RAVE Display Format", question.getRaveDisplayFormat());
			formFields.put("Format Checker Result", question.getFormatCheckerResult());
			formFields.put("CDE Display Format", question.getCdeDisplayFormat());
			return formFields;
			}				
		
		
		/**
		 * Fills up a row with the form fields, in the appropriate columns
		 * @param formFields
		 * @param row
		 * @param colNum
		 * @param cellStyle
		 * @return Row
		 */
		public static Row returnFilledRow (Map<String, String> formFields, Row row, int colNum, CellStyle cellStyle) {
			Cell newCell;
			// Iterating through the Map of report fields to print them into the excel sheet
			for (Map.Entry<String, String> formField : formFields.entrySet()) {						
				newCell = row.createCell(colNum++);
				newCell.setCellValue(formField.getValue());
				if (cellStyle!=null)
					newCell.setCellStyle(cellStyle);
			}
			return row;
		}
		
		/**
		 * Print Coded data, User data string and Coded data result (PV checker result) in the report 
		 * @param question
		 * @param row
		 * @param cellStyle
		 * @param rowNum
		 * @param sheet
		 * @return Map<String, Integer>
		 */
		public static Map<String, Integer> prepareCodedDataResultsforReport(CCCQuestion question, Row row, CellStyle cellStyle, int rowNum, Sheet sheet) {
			Cell newCell;
			Map<String, Integer> colNumbers = new LinkedHashMap<String, Integer>();
			List<String> raveCodedData = question.getRaveCodedData();
			List<String> raveUserString = question.getRaveUserString();
			List<String> codedDataResult = question.getCodedDataResult();
			int colNum = 0;
			
			// Nested loop processing for Coded data and User data string for a CDE
			for (int m = 0; m < raveCodedData.size(); m++)	{
				colNum = codedDataColStart;
				newCell = row.createCell(colNum++);
				newCell.setCellValue(raveCodedData.get(m));					
				newCell = row.createCell(colNum++);
				if (codedDataResult.isEmpty())
					newCell.setCellValue("Cell empty");
				else	
					newCell.setCellValue(codedDataResult.get(m)); 
				newCell = row.createCell(colNum++);
				if (m != 0) {
						newCell.setCellValue(""); 
					} else {
						String allowableCdeVal = question.getAllowableCdeValue();
						// Checking for the length of the string for max limit before writing to cell								
						if (allowableCdeVal!=null && allowableCdeVal.length() > cell_max_limit){
							allowableCdeVal = croppedStringText+allowableCdeVal.substring(0, cell_write_limit);
						}
						newCell.setCellValue(allowableCdeVal);
					} 
				newCell = row.createCell(colNum++);
				newCell.setCellValue(raveUserString.get(m));
				newCell = row.createCell(colNum++);
				if (question.getPvResults()!=null && !question.getPvResults().isEmpty()) {
					newCell.setCellValue(question.getPvResults().get(m)); 
				} else {
					newCell.setCellValue("");
				}
				newCell = row.createCell(colNum++);
				newCell.setCellStyle(cellStyle);
				// Adding Allowable CDE text choices (in case of not match)
				if (question.getAllowableCdeTextChoices() != null && !question.getAllowableCdeTextChoices().isEmpty()) {
				String allowabeCdeTextChoices = question.getAllowableCdeTextChoices().get(m);
				// Checking for the length of the string for max limit before writing to cell						
					if (allowabeCdeTextChoices!=null && allowabeCdeTextChoices.length() > cell_max_limit){
						allowabeCdeTextChoices = croppedStringText+allowabeCdeTextChoices.substring(0, cell_write_limit);
					}
					newCell.setCellValue(allowabeCdeTextChoices);
				} else {
					newCell.setCellValue("");
				}						
				if (m != raveCodedData.size()-1)
					row = sheet.createRow(rowNum++);
			}
			colNumbers.put("ColNum", colNum);
			colNumbers.put("RowNum", rowNum);
			return colNumbers;
		}
		
		
		
		
		
		/**
		 * Builds and returns an excel tab (sheet) with NRDS CDEs
		 * @param workbook
		 * @param nrdsCdeList
		 * @return XSSFWorkbook
		 */
		public static Workbook buildNrdsTab (Workbook workbook, List<NrdsCde> nrdsCdeList) {
			Row row;
			final String[] nrdsRowHeaders = { "Rave Form OID", "RAVE Field Order", "RAVE Field Label", "CDE ID Version", "CDE Name", "Result", "Message"};
			Sheet sheet = workbook.createSheet(matching_nrds_cdes_tab_name);
			int rowNum = 0;
			row = sheet.createRow(rowNum++);
			Cell newCell = row.createCell(0);
			newCell.setCellValue(matching_nrds_cdes_header);
			row = sheet.createRow(rowNum++);
			row = sheet.createRow(rowNum++);
			int colNum = 0;
			// Print row headers in the NRDS sheet
			for (String rowHeader : nrdsRowHeaders) {
				newCell = row.createCell(colNum++);
				newCell.setCellValue(rowHeader);
			}
			CellStyle cellStyle = workbook.createCellStyle();
	        cellStyle.setWrapText(true);
	        colNum = 0;
			// Print the ALS CDEs matching with the NRDS CDEs 	
			for (NrdsCde cde : nrdsCdeList) {
				colNum = 0;			
				row = sheet.createRow(rowNum++);
				newCell = row.createCell(colNum++);
				newCell.setCellValue(cde.getRaveFormOid());
				newCell = row.createCell(colNum++);
				newCell.setCellValue(cde.getRaveFieldOrder());
				newCell = row.createCell(colNum++);
				newCell.setCellStyle(cellStyle);			
				newCell.setCellValue(cde.getRaveFieldLabel());			
				newCell = row.createCell(colNum++);
				newCell.setCellValue(cde.getCdeIdVersion());			
				newCell = row.createCell(colNum++);
				newCell.setCellValue(cde.getCdeName());						
				newCell = row.createCell(colNum++);
				newCell.setCellValue(cde.getResult());
				newCell = row.createCell(colNum++);
				newCell.setCellValue(cde.getMessage());			
			}		
			
			return workbook;
		}
		
		/**
		 * Builds and returns an excel tab (sheet) with NRDS CDEs that are not part of the ALS
		 * @param workbook
		 * @param missingNrdsCdeList
		 * @return XSSFWorkbook
		 */
		public static Workbook buildMissingNrdsCdesTab (Workbook workbook, List<NrdsCde> missingNrdsCdeList) {
			Row row;
			final int idxOfCdeId = 0;//0-based
			final int widthOfCdeId = 8*256;//in characters
			final int idxOfCdeName = 1;
			final int widthOfCdeName = 100*256;
			final String[] nrdsRowHeaders = { "CDE ID Version", "CDE Name"};
			Sheet sheet = workbook.createSheet(nrds_missing_cde_tab_name);
			sheet.setColumnWidth(idxOfCdeId, widthOfCdeId);
			sheet.setColumnWidth(idxOfCdeName, widthOfCdeName);
			int rowNum = 0;
			row = sheet.createRow(rowNum++);
			Cell newCell = row.createCell(0);
			newCell.setCellValue(nrds_missing_cde_header);
			row = sheet.createRow(rowNum++);
			row = sheet.createRow(rowNum++);
			int colNum = 0;
			// Print row headers in the NRDS sheet
			for (String rowHeader : nrdsRowHeaders) {
				newCell = row.createCell(colNum++);
				newCell.setCellValue(rowHeader);
			}
			CellStyle cellStyle = workbook.createCellStyle();
	        cellStyle.setWrapText(true);
	        colNum = 0;
			// Print the missing NRDS CDEs 	
			for (NrdsCde cde : missingNrdsCdeList) {
				colNum = 0;			
				row = sheet.createRow(rowNum++);
				newCell = row.createCell(colNum++);
				newCell.setCellValue(cde.getCdeIdVersion());			
				newCell = row.createCell(colNum++);
				newCell.setCellValue(cde.getCdeName());						
			}		
			
			return workbook;
		}	
		
		
		/**
		 * Build the Standard CRF tabs
		 * @param workbook
		 * @param stdCrfCdeList
		 * @return XSSFWorkbook
		 */
		public static Workbook buildStdCrfMissingTabs (Workbook workbook, List<StandardCrfCde> stdCrfCdeList) {
			final String[] templateTypes = {"Mandatory", "Optional", "Conditional"};
			final String[] tabNames = {"Standard CRF Mandatory Missing", "Standard CRF Optional Missing", "Standard CRF Conditional Missing"};
			int crfTabsCount = 3; // 3 categories of standard CRF CDEs		
			for (int i = 0; i < crfTabsCount; i++ )
				buildCrfTab(workbook.createSheet(tabNames[i]), stdCrfCdeList, templateTypes[i]);	
	 		return workbook;		
		}	
		
		
		/**
		 * Build a CRF tab
		 * @param sheet
		 * @param stdCrfCdeList
		 * @param category
		 * @return XSSFSheet
		 */
		private static Sheet buildCrfTab (Sheet sheet, List<StandardCrfCde> stdCrfCdeList, String category) {
			Row row;
			final String[] crfRowHeaders = { "CDE IDVersion", "CDE Name", "Template Name", "CRF ID Version"};
			final int idxOfCdeId = 0;//0-based
			final int widthOfCdeId = 12*256;//in characters
			final int idxOfCdeName = 1;//0-based
			final int widthOfCdeName = 48*256;
			final int idxOfFormName = 2;
			final int widthOfFormName = 100*256;
			final int idxOfFormId = 3;
			final int widthOfFormId = 20*256;//in characters
			sheet.setColumnWidth(idxOfCdeId, widthOfCdeId);
			sheet.setColumnWidth(idxOfCdeName, widthOfCdeName);
			sheet.setColumnWidth(idxOfFormName, widthOfFormName);
			sheet.setColumnWidth(idxOfFormId, widthOfFormId);
			int colNum = 0;
			int rowNum = 0;
			row = sheet.createRow(rowNum++);
			Cell newCell = row.createCell(0);
			newCell.setCellValue("CDEs in Standard Template \""+category+"\" Modules Not Used");
			row = sheet.createRow(rowNum++);
			row = sheet.createRow(rowNum++);
			// Print row headers in the CRF sheets
			for (String rowHeader : crfRowHeaders) {
				newCell = row.createCell(colNum++);
				newCell.setCellValue(rowHeader);
			}
			
			colNum = 0;
			// Print the Standard CRF CDEs
			for (StandardCrfCde cde : stdCrfCdeList) {
				if (cde.getStdTemplateType().equalsIgnoreCase(category)) {
					colNum = 0;
					row = sheet.createRow(rowNum++);
					newCell = row.createCell(colNum++);
					newCell.setCellValue(cde.getCdeIdVersion());
					newCell = row.createCell(colNum++);
					newCell.setCellValue(cde.getCdeName());
					newCell = row.createCell(colNum++);
					newCell.setCellValue(cde.getTemplateName());
					newCell = row.createCell(colNum++);
					newCell.setCellValue(cde.getIdVersion()); 
				}
			}		
			return sheet;
		} 	
		
		
		/**
		 * Set width for columns in the form tabs
		 * @param sheet
		 * @param columns
		 * @param colWidth
		 * @return XSSFSheet
		 */
		private static Sheet setColumnWidth (Sheet sheet, Integer[] columns, int colWidth) {
			for (Integer colIndx : columns) {
				sheet.setColumnWidth(colIndx, colWidth);
			}
			return sheet;	
		}
		
}
