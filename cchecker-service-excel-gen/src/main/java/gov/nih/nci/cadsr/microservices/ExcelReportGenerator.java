package gov.nih.nci.cadsr.microservices;

/*
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.CdeMissing;
import gov.nih.nci.cadsr.data.NrdsCde;
import gov.nih.nci.cadsr.data.StandardCrfCde;

public class ExcelReportGenerator {

	private static final Logger logger = LoggerFactory.getLogger(ExcelReportGenerator.class);
	private static final String formHeader = "VIEW OF EXPANDED RESULTS for %s form";
	private static final String summaryFormsHeader = "Report Summary - Click on Form Name to expand results";
	private static final String summaryFormsValidResult = "Validation Result";
	private static final int summaryFormsValidResultColNum = 1;
	private static final String checkerReportOwnerLbl = "CDE Congruency Checker Report for";
	private static final String raveProtocolNameLbl = "Rave Protocol name ";
	private static final String raveProtocolNumLbl = "Rave Protocol number ";
	private static final String reportDateLbl = "Date Validated ";
	private static final String formCountLbl = "# Forms in protocol ";
	private static final String formsCheckedLbl = "# Forms in protocol Checked";
	private static final String totalQuestCongLbl = "# Total Questions Congruent ";
	private static final String totalQuestCheckLbl = "# Total Questions Checked in %d forms";
	private static final String totalQuestWarnLbl = "# Total Questions With Warnings ";
	private static final String totalQuestErrorLbl = "# Total Questions With Errors in %d forms";
	private static final String totalunassociatedQuestLbl = "# Total Questions Without Associated CDE ";
	private static final String reqQuestMissLbl = "# Required NRDS Questions Missing ";
	private static final String reqNrdsQuestCongLbl = "# Required NRDS Questions Congruent ";
	private static final String reqNrdsQuestWarnLbl = "# Required NRDS Questions With Warnings ";
	private static final String reqNrdsQuestErrorLbl = "# Required NRDS Questions With Errors ";
	// FORMBUILD-636
	private static final String reqNciQuestMissLbl = "# Required NCI Questions Missing ";
	private static final String reqNciQuestCongLbl = "# Required NCI Questions Congruent ";
	private static final String reqNciQuestWarnLbl = "# Required NCI Questions With Warnings ";
	private static final String reqNciQuestErrorLbl = "# Required NCI Questions With Errors ";	
	
	private static final String nciStdManQuestLbl = "# NCI Standard Template Mandatory Modules Questions missing from Protocol ";
	private static final String nciStdManCongLbl = "# NCI Standard Template Mandatory Modules Questions Congruent ";
	private static final String nciStdManErrorLbl = "# NCI Standard Template Mandatory Modules Questions With Errors ";
	private static final String nciStdManWarnLbl = "# NCI Standard Template Mandatory Modules Questions With Warnings ";
	private static final String nciStdCondQuestLbl = "# NCI Standard Template Conditional Modules Questions missing from Protocol ";
	private static final String nciStdCondCongLbl = "# NCI Standard Template Conditional Modules Questions Congruent ";
	private static final String nciStdCondErrorLbl = "# NCI Standard Template Conditional Modules Questions With Errors ";
	private static final String nciStdCondWarnLbl = "# NCI Standard Template Conditional Modules Questions With Warnings ";
	private static final String nciStdOptQuestLbl = "# NCI Standard Template Optional Modules Questions missing from Protocol ";
	private static final String nciStdOptCongLbl = "# NCI Standard Template Optional Modules Questions Congruent ";
	private static final String nciStdOptErrorLbl = "# NCI Standard Template Optional Modules Questions With Errors ";
	private static final String nciStdOptWarnLbl = "# NCI Standard Template Optional Modules Questions With Warnings ";
	private static final int formStartColumn = 4;
	// FORMBUILD-648
	private static final int borderStartColumn1 = 10;
	private static final int borderStartColumn2 = 13;
	private static final int borderStartColumn3 = 19;
	private static final int raveFieldDataTypeCol = 22;
	private static final int codedDataColStart = 16;
	private static final int borderStartColumn4 = 25;
	private static final int borderStartColumn5 = 28;
	private static final int borderStartColumn6 = 31;
	private static final int borderStartColumn7 = 34;	
	private static final String matching_nrds_cdes_tab_name = "NCI Questions in ALS";
	private static final String nrds_missing_cde_tab_name = "NCI Questions Missing";
	//FORMBUILD-621
	private static final String reqCdashMissLbl = "# Required CDASH x.x Questions Missing ";
	// FORMBUILD-636
	private static final String reqCdashQuestWarnLbl = "# Required CDASH x.x Questions With Warnings ";
	// FORMBUILD-636
	private static final String reqCdashQuestErrorLbl = "# Required CDASH x.x Questions With Errors ";
	private static final String reqSdtmMissLbl = "# Required SDTM x.x Questions Missing ";
	// FORMBUILD-636
	private static final String reqSdtmQuestWarnLbl = "# Required SDTM x.x Questions With Warnings ";
	// FORMBUILD-636
	private static final String reqSdtmQuestErrorLbl = "# Required SDTM x.x Questions With Errors ";	
	private static final String cdash_missing_cde_sheet_name = "CDASH x.x CDEs Missing";
	private static final String sdtm_missing_cde_sheet_name = "SDTM x.x CDEs Missing";
	private static final String cdash_missing_cde_header = "CDASH x.x CDEs missing from the ALS file";
	private static final String sdtm_missing_cde_header = "SDTM x.x CDEs missing from the ALS file";
	private static final String[] classifiedMissingRowHeaders = { "CDE IDVersion", "CDE Long Name", "Preferred Question Text" };
	//
	private static final String nrds_missing_cde_header = "Required NCI Questions missing from the ALS file";
	private static final String matching_nrds_cdes_header = "NCI Questions included in Protocol Forms with Warnings or Errors";
	private static final String congStatus_Congruent = "CONGRUENT";
	private static final int cell_max_limit = 32767;
	private static final int cell_write_limit = 32700;
	private static final short headerLblFontSize = 280;
	private static final short headerLblFontSize2 = 240;
	private static final String headerFontName = "Calibri";
	private static final int idxSummaryLbl = 0;
	private static final int idxSummaryVal = 1;
	private static final int widthSummaryVal = 12800;
	private static final int widthFormColsShort = 5120;
	private static final int widthFormColsLong = 25600;
	private static final Integer[] shortColumnsforForm = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 13, 14, 15, 17, 20, 22, 23,
			24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34 };
	private static final Integer[] longColumnsforForm = { 9, 10, 12, 16, 18, 19, 21 };
	private static final String croppedStringText = "// CONTENT CROPPED TO 32,700 CHARACTERS. // \n ";
	// FORMBUILD-652	
	private static final String[] rowHeaders = { "Rave Form OID", "caDSR Form ID", "Version",
			"Total Number Of Questions Checked", "Sequence #", "ALS Ordinal #", "CDE PID", "NCI Category",
			"Question Congruency Status", "Message", "Rave Field Label", "Rave Field Label Result",
			"CDE Permitted Question Text Choices", "Rave Control Type", "Control Type Checker Result",
			"CDE Value Domain Type", "Rave Coded Data", "Coded Data Result", "Allowable CDE  Value", "Rave User String",
			"PV Result", "Allowable CDE  Value Meaning Text Choices", "Rave Field Data Type",
			"Data Type Checker Result", "CDE Data Type", "Rave UOM", "UOM Checker Result", "CDE UOM", "Rave Length",
			"Length Checker Result", "CDE Maximum Length", "Rave Display Format", "Format Checker Result",
			"CDE Display Format" };
	private static final String[] templateTypes = { "Mandatory", "Optional", "Conditional" };
	private static final String stdCrfManMiss_tab_name = "Std CRF Mandatory Missing";
	private static final String stdCrfOptMiss_tab_name = "Std CRF Optional Missing";
	private static final String stdCrfCondMiss_tab_name = "Std CRF Conditional Missing";
	private static final String[] tabNames = { stdCrfManMiss_tab_name, stdCrfOptMiss_tab_name,
			stdCrfCondMiss_tab_name };
	private static final String cdeStdCrfMissingmsg = "CDEs in Standard Template \"%s\" Modules Not Used";
	private static final String[] crfRowHeaders = { "CDE IDVersion", "CDE Long Name", "Preferred Question Text", "Type", "Template Name", "CRF ID Version" };
	private static final String[] nrdsRowHeaders = { "Rave Form OID", "RAVE Field Order", "RAVE Field Label",
			"CDE ID Version", "CDE Long Name", "Result", "Message" };
	// FORMBUILD-652	
	private static final String seqNumLbl = "Sequence #";
	private static final String fieldOrderLbl = "ALS Ordinal #";
	private static final String cdeIdLbl = "CDE Public ID";
	private static final String cdeVersion = "CDE Version";
	// FORMBUILD-652	
	private static final String cdePidVerLbl = "CDE PID";
	private static final String nciCategoryLbl = "NCI Category";
	private static final String questionCongStatusLbl = "Question Congruency Status";
	private static final String msgLbl = "Message";
	private static final String raveFieldLbl = "RAVE Field Label";
	private static final String raveFieldLblResult = "RAVE Field Label Result";
	private static final String cdePermittedQuestChoicesLbl = "CDE Permitted Question Text Choices";
	private static final String controlTypeLbl = "RAVE Control Type";
	private static final String controlTypeResultLbl = "RAVE Control Type Result";
	private static final String cdeVDTypeLbl = "CDE Value Domain Type";
	private static final String raveFieldDataTypeLbl = "RAVE Field Data Type";
	private static final String dataTypeCheckerResultLbl = "Data Type Checker Result";
	private static final String cdeDataTypeLbl = "CDE Data Type";
	private static final String raveUomLbl = "RAVE UOM";
	private static final String raveUomCheckerResultLbl = "UOM Checker Result";
	private static final String cdeUomLbl = "CDE UOM";
	private static final String raveLengthLbl = "RAVE Length";
	private static final String lengthCheckerResultLbl = "Length Checker Result";
	private static final String cdeMaxLengthLbl = "CDE Max Length";
	private static final String raveDisplayFormatLbl = "RAVE Display Format";
	private static final String formatCheckerResultLbl = "Format Checker Result";
	private static final String cdeDisplayFormatLbl = "CDE Display Format";
	private static Map<String, String> formOidSheetNamesMap;
	private static Set<String> formOids;
	private static Map<String, Integer> formOidDupes;
	private static final int truncSheetNumSingleDigit = 3;
	private static final int truncSheetNumDoubleDigit = 4;
	private static final int truncSheetNumTripleDigit = 5;
	private static final String backToSummary = "Click here to go back to Summary sheet.";
	private static CellStyle hlink_style;
	private static CellStyle header_lbl_style;
	private static CellStyle header_lbl_style_2;
	private static CellStyle header_lbl_style_leftB;
	private static CellStyle cell_leftBorder_Style;
	private static CellStyle cell_Style_wrap;	
	private static CellStyle cell_rightBorder_Style;
	private static Font hlink_font;
	private static Font header_lbl_font;
	private static Font header_lbl_font_2;
	private static final String summaryLbl = "Summary";
	private static CreationHelper createHelper;

	/**
	 * Writing the final output report object into an Excel for download
	 * @param OUTPUT_XLSX_FILE_PATH
	 * @param cccReport
	 */
	public void writeExcel(String OUTPUT_XLSX_FILE_PATH, CCCReport cccReport)
			throws IOException, InvalidFormatException, NullPointerException {

		Row row;
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet(summaryLbl);
		createHelper = workbook.getCreationHelper();
		formOidSheetNamesMap = new HashMap<String, String>();
		formOids = new HashSet<String>();
		formOidDupes = new HashMap<String, Integer>();

		// cell style for hyperlinks
		// by default hyperlinks are blue and underlined
		hlink_style = workbook.createCellStyle();
		hlink_font = workbook.createFont();
		hlink_font.setUnderline(Font.U_SINGLE);
		hlink_font.setColor(IndexedColors.BLACK.getIndex());
		hlink_style.setFont(hlink_font);

		// Bold headers
		header_lbl_style = workbook.createCellStyle();
		header_lbl_font = workbook.createFont();
		header_lbl_font.setBold(true);
		header_lbl_font.setFontName(headerFontName);
		header_lbl_font.setFontHeight(headerLblFontSize);
		header_lbl_style.setFont(header_lbl_font);

		header_lbl_style_2 = workbook.createCellStyle();
		header_lbl_font_2 = workbook.createFont();
		header_lbl_font_2.setBold(true);
		header_lbl_font_2.setFontName(headerFontName);
		header_lbl_font_2.setFontHeight(headerLblFontSize2);
		header_lbl_style_2.setFont(header_lbl_font_2);
		
		header_lbl_style_leftB = workbook.createCellStyle();
		header_lbl_style_leftB.cloneStyleFrom(header_lbl_style_2);
		header_lbl_style_leftB.setBorderLeft(BorderStyle.THIN);	

		
		cell_leftBorder_Style = workbook.createCellStyle();
		cell_rightBorder_Style = workbook.createCellStyle();
		cell_leftBorder_Style.setBorderLeft(BorderStyle.THIN);
		cell_rightBorder_Style.setBorderRight(BorderStyle.THIN);
		cell_Style_wrap = workbook.createCellStyle();
		cell_Style_wrap.setWrapText(true);

		// Adding labels for each value (row) displayed in the summary page of
		// the report
		Map<String, String> summaryLabels = returnSummaryLabelsMap(cccReport);

		int rowNum = 0;

		// Setting a fixed column width for values in the Summary page (sheet)
		sheet.setColumnWidth(idxSummaryLbl, widthFormColsLong);

		// Setting a fixed column width for labels in the Summary page (sheet)
		sheet.setColumnWidth(idxSummaryVal, widthSummaryVal);

		logger.debug("Creating excel");

		// Printing the Summary labels and their corresponding values next to
		// them
		for (Map.Entry<String, String> label : summaryLabels.entrySet()) {
			row = sheet.createRow(rowNum++);
			int colNum = 0;
			String lblKey = label.getKey();
			if ((formCountLbl.equals(lblKey))) {
				row = sheet.createRow(rowNum++);
			}
			Cell cell = row.createCell(colNum++);

			// Print the top header labels in Bold - Report for, Rave Protocol
			// Name, Number & Date
			boolean isCheckStdCrfCdeChecked = cccReport.getIsCheckStdCrfCdeChecked() != null ? 
					cccReport.getIsCheckStdCrfCdeChecked() : false;//this is to avoid NullPointerException
			if (checkerReportOwnerLbl.equals(lblKey) || raveProtocolNameLbl.equals(lblKey)
					|| raveProtocolNumLbl.equals(lblKey) || reportDateLbl.equals(lblKey)) {
				cell.setCellValue((String) lblKey);
				cell.setCellStyle(header_lbl_style);
			// FORMBUILD-636					
			/*} else if (reqQuestMissLbl.equals(lblKey)) {
				// Link to the NRDS missing CDEs sheet
				linkToSheet(cell, (String) lblKey, nrds_missing_cde_tab_name);
			} else if (reqNrdsQuestCongLbl.equals(lblKey) || reqNrdsQuestErrorLbl.equals(lblKey)
					|| reqNrdsQuestWarnLbl.equals(lblKey)) {
				// Link to the NRDS ALS matching CDEs sheet
				linkToSheet(cell, (String) lblKey, matching_nrds_cdes_tab_name);*/
				} else if (reqNciQuestMissLbl.equals(lblKey)) {
				// Link to the NRDS & NCI Std CRF missing CDEs sheet
				linkToSheet(cell, (String) lblKey, nrds_missing_cde_tab_name);
			} else if (reqNciQuestCongLbl.equals(lblKey) || reqNciQuestErrorLbl.equals(lblKey)
					|| reqNciQuestWarnLbl.equals(lblKey)) {
				// Link to the NRDS ALS matching CDEs sheet
				linkToSheet(cell, (String) lblKey, matching_nrds_cdes_tab_name);				
			//FORMBUILD-621
			} else if (reqCdashMissLbl.equals(lblKey)) {
					// Link to the CDASH missing CDEs sheet
					linkToSheet(cell, (String) lblKey, cdash_missing_cde_sheet_name);
			// FORMBUILD-636		
			} else if (reqCdashQuestWarnLbl.equals(lblKey)) {
				// Link to the CDASH missing CDEs sheet
				linkToSheet(cell, (String) lblKey, cdash_missing_cde_sheet_name);
			// FORMBUILD-636				
			} else if (reqCdashQuestErrorLbl.equals(lblKey)) {
				// Link to the CDASH missing CDEs sheet
				linkToSheet(cell, (String) lblKey, cdash_missing_cde_sheet_name);					
			} else if (reqSdtmMissLbl.equals(lblKey)) {
				// Link to the SDTM missing CDEs sheet
				linkToSheet(cell, (String) lblKey, sdtm_missing_cde_sheet_name);
			// FORMBUILD-636				
			} else if (reqSdtmQuestWarnLbl.equals(lblKey)) {
				// Link to the SDTM missing CDEs sheet
				linkToSheet(cell, (String) lblKey, sdtm_missing_cde_sheet_name);
			// FORMBUILD-636				
			} else if (reqSdtmQuestErrorLbl.equals(lblKey)) {
				// Link to the SDTM missing CDEs sheet
				linkToSheet(cell, (String) lblKey, sdtm_missing_cde_sheet_name);				
			} else if ((nciStdCondCongLbl.equals(lblKey) || nciStdCondErrorLbl.equals(lblKey)
					|| nciStdCondQuestLbl.equals(lblKey) || nciStdCondWarnLbl.equals(lblKey))
					&& isCheckStdCrfCdeChecked) {
				// Link to the Standard CRF Conditional CDEs sheet (provided check CDE flag is checked)
				linkToSheet(cell, (String) lblKey, stdCrfCondMiss_tab_name);
			} else if ((nciStdOptCongLbl.equals(lblKey) || nciStdOptErrorLbl.equals(lblKey)
					|| nciStdOptQuestLbl.equals(lblKey) || nciStdOptWarnLbl.equals(lblKey))
					&& isCheckStdCrfCdeChecked) {
				// Link to the Standard CRF Optional CDEs sheet (provided check CDE flag is checked)
				linkToSheet(cell, (String) lblKey, stdCrfOptMiss_tab_name);
			} else if ((nciStdManCongLbl.equals(lblKey) || nciStdManErrorLbl.equals(lblKey)
					|| nciStdManQuestLbl.equals(lblKey) || nciStdManWarnLbl.equals(lblKey))
					&& isCheckStdCrfCdeChecked) {
				// Link to the Standard CRF Mandatory CDEs sheet (provided check CDE flag is checked)				
				linkToSheet(cell, (String) lblKey, stdCrfManMiss_tab_name);
			} else {
				// Print without a link
				cell.setCellValue((String) lblKey);
			}
			cell = row.createCell(colNum++);
			cell.setCellValue((String) label.getValue());
			if (checkerReportOwnerLbl.equals(lblKey) || raveProtocolNameLbl.equals(lblKey)
					|| raveProtocolNumLbl.equals(lblKey) || reportDateLbl.equals(lblKey)) {
				cell.setCellStyle(header_lbl_style);
			}
		}
		row = sheet.createRow(rowNum++);
		row = sheet.createRow(rowNum++);
		Cell newCell = row.createCell(0);

		// Printing the Report Summary [2nd half - Summary sheet] with forms'
		// congruency status
		newCell.setCellValue(summaryFormsHeader);
		newCell.setCellStyle(header_lbl_style_2);
		newCell = row.createCell(summaryFormsValidResultColNum);
		newCell.setCellValue(summaryFormsValidResult);
		newCell.setCellStyle(header_lbl_style_2);
		List<CCCForm> forms = cccReport.getCccForms();
		// Iterating through the forms list in the report to display their name
		// and their congruency status
		for (CCCForm form : forms) {
			row = sheet.createRow(rowNum++);
			int colNum = 0;
			Cell cell = row.createCell(colNum++);
			cell.setCellValue(form.getRaveFormOid());
			String worksheetName = form.getRaveFormOid();
			if (!congStatus_Congruent.equalsIgnoreCase(form.getCongruencyStatus())) {
				// Creating the link for another sheet in the document
				Hyperlink newlink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
				// Obtaining a cropped version of the form name in case it's
				// longer than 31
				worksheetName = cropFormNameForExcel(worksheetName);
				String linkText = "'" + worksheetName + "'!E1";// "'Target Sheet'!A1"
				newlink.setAddress(linkText);
				cell.setHyperlink(newlink);
				cell.setCellStyle(hlink_style);
			}
			formOidSheetNamesMap.put(form.getRaveFormOid(), worksheetName);
			cell = row.createCell(summaryFormsValidResultColNum);
			cell.setCellValue(form.getCongruencyStatus());
		}

		// Iterating through the forms list to create sheets for each one
		for (CCCForm cccForm : forms) {
			if (cccForm.getCongruencyStatus() != null) {
				if (congStatus_Congruent.equalsIgnoreCase(cccForm.getCongruencyStatus())) {
					continue;
				} else {
					String sheetName = formOidSheetNamesMap.get(cccForm.getRaveFormOid());
					Sheet sheet2 = workbook.createSheet(sheetName);
					// Setting width for columns that are expected to be Short
					setColumnWidth(sheet2, shortColumnsforForm, widthFormColsShort);
					// Setting width for columns that are expected to be Long
					setColumnWidth(sheet2, longColumnsforForm, widthFormColsLong);
					rowNum = 0;
					row = sheet2.createRow(rowNum++);
					newCell = row.createCell(0);
					newCell.setCellValue(String.format(formHeader, cccForm.getRaveFormOid()));
					newCell.setCellStyle(header_lbl_style_2);
					newCell = row.createCell(6);
					linkToSheet(newCell, backToSummary, summaryLbl);
					groupTripletCellsWithBorders(row);					
					row = sheet2.createRow(rowNum++);
					int colNum = 0;
					// Print row headers in the form sheet
					for (String rowHeader : rowHeaders) {
						newCell = row.createCell(colNum++);
						newCell.setCellValue(rowHeader);
						// FORMBUILD-648
						if (newCell.getColumnIndex() == borderStartColumn1 || newCell.getColumnIndex() == borderStartColumn2 || newCell.getColumnIndex() == codedDataColStart || newCell.getColumnIndex() == borderStartColumn3 
								|| newCell.getColumnIndex() == raveFieldDataTypeCol || newCell.getColumnIndex() == borderStartColumn4 || newCell.getColumnIndex() == borderStartColumn5 || newCell.getColumnIndex() == borderStartColumn6 ) {
							newCell.setCellStyle(header_lbl_style_leftB);
						} else { 
							newCell.setCellStyle(header_lbl_style_2);							
						}
						// FORMBUILD-648
						row.createCell(borderStartColumn7).setCellStyle(header_lbl_style_leftB);
					}
					colNum = 0;
					row = sheet2.createRow(rowNum++);
					groupTripletCellsWithBorders(row);
					newCell = row.createCell(0);
					newCell.setCellValue(cccForm.getRaveFormOid());
					newCell = row.createCell(1);
					newCell.setCellValue(cccForm.getFormPublicId());
					newCell = row.createCell(2);
					newCell.setCellValue(cccForm.getFormVersion());
					newCell = row.createCell(3);
					newCell.setCellValue(cccForm.getTotalQuestionsChecked());
					CellStyle cellStyle = workbook.createCellStyle(); // Create new style
					CellStyle cellStyle2 = workbook.createCellStyle();
					cellStyle.setWrapText(true);
					for (int j = 0; j < cccForm.getQuestions().size(); j++) {
						int colNum2 = formStartColumn;
						CCCQuestion question = cccForm.getQuestions().get(j);
						row = sheet2.createRow(rowNum++);
						// FORMBUILD-648
						groupTripletCellsWithBorders(row);
						// FORMBUILD-652
						newCell = row.createCell(4);
						newCell.setCellValue("");						
						// Printing columns before Coded data column
						Map<String, String> formFields1 = returnFormFieldsPart1(question);
						row = returnFilledRow(formFields1, row, colNum2, cellStyle);
						Row rowBeforeCD = row;

						// Nested loop processing for Coded data and User data
						// string for a CDE
						Map<String, Integer> rowAndColNumbers = prepareCodedDataResultsforReport(question, row,
								cellStyle, rowNum, sheet2);
						int rowNumAfterCD = rowAndColNumbers.get("RowNum");
						colNum2 = rowAndColNumbers.get("ColNum");
						row = rowBeforeCD;
						int newColNum = raveFieldDataTypeCol;
						// Printing columns after Coded data column
						Map<String, String> formFields2 = returnFormFieldsPart2(question);
						row = returnFilledRow(formFields2, row, newColNum, cell_leftBorder_Style);
						// FORMBUILD-648
						groupTripletCellsWithBorders(row);
						if (rowNumAfterCD > rowNum)
							rowNum = rowNumAfterCD;
					}
				}
			}
		}
		buildNrdsTab(workbook, cccReport.getNrdsCdeList(), cccReport.getStdCrfCdeList());
		// FORMBUILD-635
		boolean isCheckStdCrfCdeChecked = cccReport.getIsCheckStdCrfCdeChecked() != null ? 
		cccReport.getIsCheckStdCrfCdeChecked() : false;//this is to avoid NullPointerException
		buildMissingNrdsCdesTab(workbook, cccReport.getMissingNrdsCdeList(), cccReport.getMissingStandardCrfCdeList(), isCheckStdCrfCdeChecked);
		//FORMBUILD-621
		buildMissingCdesTab(workbook, cccReport.getMissingCdashCdeList(), cdash_missing_cde_sheet_name, cdash_missing_cde_header);
		buildMissingCdesTab(workbook, cccReport.getMissingSdtmCdeList(), sdtm_missing_cde_sheet_name, sdtm_missing_cde_header);
		//
		/*boolean isCheckStdCrfCdeChecked = cccReport.getIsCheckStdCrfCdeChecked() != null ? 
				cccReport.getIsCheckStdCrfCdeChecked() : false;//this is to avoid NullPointerException
		if (isCheckStdCrfCdeChecked) {
			buildStdCrfMissingTabs(workbook, cccReport.getMissingStandardCrfCdeList());
		}*/
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
			logger.error(
					"outputStream FileNotFoundException " + OUTPUT_XLSX_FILE_PATH + ", FileNotFoundException: " + e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in Excel generation on file " + OUTPUT_XLSX_FILE_PATH + ", getMessage: "
					+ e.getMessage() + e);
		} finally {
			if (outputStream != null) {
				outputStream.flush();
				outputStream.close();
				logger.debug("File is closed: " + OUTPUT_XLSX_FILE_PATH);
			}
		}
	}

	/**
	 * Populate a map with the labels and their values, for Summary sheet (1st
	 * half)
	 * 
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
		summaryLabels.put(formsCheckedLbl, String.valueOf(cccReport.getSelectedFormsCount()));
		summaryLabels.put(String.format(totalQuestCheckLbl, cccReport.getSelectedFormsCount()), String.valueOf(cccReport.getCountQuestionsChecked()));
		summaryLabels.put(totalQuestCongLbl, String.valueOf(cccReport.getCountCongruentQuestions()));
		summaryLabels.put(totalQuestWarnLbl, String.valueOf(cccReport.getCountQuestionsWithWarnings()));
		summaryLabels.put(String.format(totalQuestErrorLbl, cccReport.getSelectedFormsCount()), String.valueOf(cccReport.getCountQuestionsWithErrors()));
		summaryLabels.put(totalunassociatedQuestLbl, String.valueOf(cccReport.getCountQuestionsWithoutCde()));
		/*summaryLabels.put(reqQuestMissLbl, String.valueOf(cccReport.getCountNrdsMissing()));
		summaryLabels.put(reqNrdsQuestCongLbl, String.valueOf(cccReport.getCountNrdsCongruent()));
		summaryLabels.put(reqNrdsQuestWarnLbl, String.valueOf(cccReport.getCountNrdsWithWarnings()));
		summaryLabels.put(reqNrdsQuestErrorLbl, String.valueOf(cccReport.getCountNrdsWithErrors()));*/
		// FORMBUILD-636
		summaryLabels.put(reqNciQuestMissLbl, String.valueOf(cccReport.getCountNciMissing()));
		summaryLabels.put(reqNciQuestCongLbl, String.valueOf(cccReport.getCountNciCongruent()));
		summaryLabels.put(reqNciQuestWarnLbl, String.valueOf(cccReport.getCountNciWithWarnings()));
		summaryLabels.put(reqNciQuestErrorLbl, String.valueOf(cccReport.getCountNciWithErrors()));
		
		// FORMBUILD-621
		summaryLabels.put(reqCdashMissLbl, String.valueOf(cccReport.getCountCdashMissing()));
		//FORMBUILD-636
		summaryLabels.put(reqCdashQuestWarnLbl, String.valueOf(cccReport.getCountCdashWithWarnings()));
		//FORMBUILD-636
		summaryLabels.put(reqCdashQuestErrorLbl, String.valueOf(cccReport.getCountCdashWithErrors()));		
		summaryLabels.put(reqSdtmMissLbl, String.valueOf(cccReport.getCountSdtmMissing()));
		//FORMBUILD-636
		summaryLabels.put(reqSdtmQuestWarnLbl, String.valueOf(cccReport.getCountSdtmWithWarnings()));
		//FORMBUILD-636
		summaryLabels.put(reqSdtmQuestErrorLbl, String.valueOf(cccReport.getCountSdtmWithErrors()));		
		/*summaryLabels.put(nciStdManQuestLbl, String.valueOf(cccReport.getCountManCrfMissing()));
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
		summaryLabels.put(nciStdOptWarnLbl, String.valueOf(cccReport.getCountOptCrfwWithWarnings()));*/
		return summaryLabels;
	}

	/**
	 * Populate a map with the report values before Coded data column, for
	 * individual forms
	 * 
	 * @param cccReport
	 * @return Map<String, String>
	 */
	public static Map<String, String> returnFormFieldsPart1(CCCQuestion question) {
		Map<String, String> formFields = new LinkedHashMap<String, String>();
		// FORMBUILD-652		
		formFields.put(seqNumLbl, question.getSequenceNumber().toString());
		formFields.put(fieldOrderLbl, question.getFieldOrder());
		if (question.getCdePublicId()!=null && question.getCdeVersion()!=null)
			formFields.put(cdePidVerLbl, question.getCdePublicId()+"v"+question.getCdeVersion());
		else 
			formFields.put(cdePidVerLbl, "");
		// FORMBUILD-652
		/* formFields.put(cdeIdLbl, question.getCdePublicId());
		 formFields.put(cdeVersion, question.getCdeVersion()); */
		formFields.put(nciCategoryLbl, question.getNciCategory());
		formFields.put(questionCongStatusLbl, question.getQuestionCongruencyStatus());
		String message = question.getMessage();
		// Checking for the length of the string for max limit before writing to cell
		if (message != null && message.length() > cell_max_limit) {
			message = croppedStringText + message.substring(0, cell_write_limit);
		}
		formFields.put(msgLbl, message);
		formFields.put(raveFieldLbl, question.getRaveFieldLabel());
		formFields.put(raveFieldLblResult, question.getRaveFieldLabelResult());
		formFields.put(cdePermittedQuestChoicesLbl, question.getCdePermitQuestionTextChoices());
		formFields.put(controlTypeLbl, question.getRaveControlType());
		formFields.put(controlTypeResultLbl, question.getControlTypeResult());
		formFields.put(cdeVDTypeLbl, question.getCdeValueDomainType());
		return formFields;
	}

	/**
	 * Populate a map with the report values after Coded data column, for
	 * individual forms
	 * 
	 * @param cccReport
	 * @return Map<String, String>
	 */
	public static Map<String, String> returnFormFieldsPart2(CCCQuestion question) {
		Map<String, String> formFields = new LinkedHashMap<String, String>();
		formFields.put(raveFieldDataTypeLbl, question.getRaveFieldDataType());
		formFields.put(dataTypeCheckerResultLbl, question.getDatatypeCheckerResult());
		formFields.put(cdeDataTypeLbl, question.getCdeDataType());
		formFields.put(raveUomLbl, question.getRaveUOM());
		formFields.put(raveUomCheckerResultLbl, question.getUomCheckerResult());
		formFields.put(cdeUomLbl, question.getCdeUOM());
		formFields.put(raveLengthLbl, question.getRaveLength());
		formFields.put(lengthCheckerResultLbl, question.getLengthCheckerResult());
		if (question.getCdeMaxLength()!=null)
			formFields.put(cdeMaxLengthLbl, question.getCdeMaxLength().toString());
		else 
			formFields.put(cdeMaxLengthLbl, "");
		formFields.put(raveDisplayFormatLbl, question.getRaveDisplayFormat());
		formFields.put(formatCheckerResultLbl, question.getFormatCheckerResult());
		formFields.put(cdeDisplayFormatLbl, question.getCdeDisplayFormat());
		return formFields;
	}

	/**
	 * Fills up a row with the form fields, in the appropriate columns
	 * 
	 * @param formFields
	 * @param row
	 * @param colNum
	 * @param cellStyle
	 * @return Row
	 */
	public static Row returnFilledRow(Map<String, String> formFields, Row row, int colNum, CellStyle cellStyle) {
		Cell newCell;
		// Iterating through the Map of report fields to print them into the
		// excel sheet
		for (Map.Entry<String, String> formField : formFields.entrySet()) {
			newCell = row.createCell(colNum++);
			newCell.setCellValue(formField.getValue());			
			// FORMBUILD-648
			if (cellStyle != null) {
				if (newCell.getColumnIndex() == borderStartColumn1 || newCell.getColumnIndex() == borderStartColumn2 || newCell.getColumnIndex() == raveFieldDataTypeCol) {	
						newCell.setCellStyle(cell_leftBorder_Style);
					}
				if (newCell.getColumnIndex() == (borderStartColumn1 - 1) || newCell.getColumnIndex() == (borderStartColumn2 - 1)) {
						newCell.setCellStyle(cell_Style_wrap);
					}
				}

		}
		return row;
	}

	/**
	 * Print Coded data, User data string and Coded data result (PV checker
	 * result) in the report
	 * 
	 * @param question
	 * @param row
	 * @param cellStyle
	 * @param rowNum
	 * @param sheet
	 * @return Map<String, Integer>
	 */
	public static Map<String, Integer> prepareCodedDataResultsforReport(CCCQuestion question, Row row,
			CellStyle cellStyle, int rowNum, Sheet sheet) {
		Cell newCell;
		Map<String, Integer> colNumbers = new LinkedHashMap<String, Integer>();
		List<String> raveCodedData = question.getRaveCodedData();
		List<String> raveUserString = question.getRaveUserString();
		List<String> codedDataResult = question.getCodedDataResult();
		int colNum = 0;

		// Nested loop processing for Coded data and User data string for a CDE
		for (int m = 0; m < raveCodedData.size(); m++) {
			colNum = codedDataColStart;
			newCell = row.createCell(colNum++);
			newCell.setCellValue(raveCodedData.get(m));
			cellStyle.setBorderLeft(BorderStyle.THIN);	
			newCell.setCellStyle(cellStyle);
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
				// Checking for the length of the string for max limit before
				// writing to cell
				if (allowableCdeVal != null && allowableCdeVal.length() > cell_max_limit) {
					allowableCdeVal = croppedStringText + allowableCdeVal.substring(0, cell_write_limit);
				}
				newCell.setCellValue(allowableCdeVal);
			}
			newCell = row.createCell(colNum++);
			newCell.setCellValue(raveUserString.get(m));
			cellStyle.setBorderLeft(BorderStyle.THIN);	
			newCell.setCellStyle(cellStyle);			
			newCell = row.createCell(colNum++);
			if (question.getPvResults() != null && !question.getPvResults().isEmpty()) {
				newCell.setCellValue(question.getPvResults().get(m));
			} else {
				newCell.setCellValue("");
			}
			newCell = row.createCell(colNum++);
			// FORMBUILD-648
			newCell.setCellStyle(cell_rightBorder_Style);
			// Adding Allowable CDE text choices (in case of not match)
			if (question.getAllowableCdeTextChoices() != null && !question.getAllowableCdeTextChoices().isEmpty()) {
				String allowabeCdeTextChoices = question.getAllowableCdeTextChoices().get(m);
				// Checking for the length of the string for max limit before
				// writing to cell
				if (allowabeCdeTextChoices != null && allowabeCdeTextChoices.length() > cell_max_limit) {
					allowabeCdeTextChoices = croppedStringText + allowabeCdeTextChoices.substring(0, cell_write_limit);
				}
				newCell.setCellValue(allowabeCdeTextChoices);
			} else {
				newCell.setCellValue("");
			}			
			if (m != raveCodedData.size() - 1) {
				row = sheet.createRow(rowNum++);
				// FORMBUILD-648
				groupTripletCellsWithBorders(row);
				}
		}
		colNumbers.put("ColNum", colNum);
		colNumbers.put("RowNum", rowNum);
		return colNumbers;
	}

	/**
	 * Builds and returns an excel tab (sheet) with NRDS CDEs
	 * 
	 * @param workbook
	 * @param nrdsCdeList
	 * @return XSSFWorkbook
	 */
	public static Workbook buildNrdsTab(Workbook workbook, List<NrdsCde> nrdsCdeList, List<StandardCrfCde> stdCrfCdeList) {
		Row row;
		Sheet sheet = workbook.createSheet(matching_nrds_cdes_tab_name);
		// Setting fixed column widths for cells
		final int idxOfRaveFormId = 0;// 0-based
		final int widthOfRaveFormId = 36 * 256;// in characters
		final int idxOfRaveFieldOrder = 1;// 0-based
		final int widthOfRaveFieldOrder = 4 * 256;
		final int idxOfFieldLbl = 2;
		final int widthOfFieldLbl = 48 * 256;
		final int idxOfCdeId = 3;
		final int widthOfCdeId = 12 * 256;// in characters
		final int idxOfCdeName = 4;
		final int widthOfCdeName = 48 * 256;// in characters
		final int idxOfResult = 5;
		final int widthOfResult = 36 * 256;// in characters
		final int idxOfMsg = 6;
		final int widthOfMsg = 100 * 256;// in characters
		sheet.setColumnWidth(idxOfRaveFormId, widthOfRaveFormId);
		sheet.setColumnWidth(idxOfRaveFieldOrder, widthOfRaveFieldOrder);
		sheet.setColumnWidth(idxOfFieldLbl, widthOfFieldLbl);
		sheet.setColumnWidth(idxOfCdeId, widthOfCdeId);
		sheet.setColumnWidth(idxOfCdeName, widthOfCdeName);
		sheet.setColumnWidth(idxOfResult, widthOfResult);
		sheet.setColumnWidth(idxOfMsg, widthOfMsg);
		int rowNum = 0;
		row = sheet.createRow(rowNum++);
		Cell newCell = row.createCell(0);
		newCell.setCellValue(matching_nrds_cdes_header);
		newCell.setCellStyle(header_lbl_style);
		newCell = row.createCell(4);
		linkToSheet(newCell, backToSummary, summaryLbl);
		row = sheet.createRow(rowNum++);
		row = sheet.createRow(rowNum++);
		int colNum = 0;
		// Print row headers in the NRDS sheet
		for (String rowHeader : nrdsRowHeaders) {
			newCell = row.createCell(colNum++);
			newCell.setCellValue(rowHeader);
			newCell.setCellStyle(header_lbl_style_2);
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
			// FORMBUILD-636
			newCell = row.createCell(colNum++);
			newCell.setCellValue("NRDS");			
		}
		
		// FORMBUILD-636
		// Print the ALS CDEs matching with the Standard CRF Mandatory CDEs
		for (StandardCrfCde stdCrfCde : stdCrfCdeList) {
			colNum = 0;
			row = sheet.createRow(rowNum++);
			newCell = row.createCell(colNum++);
			newCell.setCellValue(stdCrfCde.getRaveFormOid());
			newCell = row.createCell(colNum++);
			newCell.setCellValue(stdCrfCde.getRaveFieldOrder());
			newCell = row.createCell(colNum++);
			newCell.setCellStyle(cellStyle);
			newCell.setCellValue(stdCrfCde.getRaveFieldLabel());
			newCell = row.createCell(colNum++);
			newCell.setCellValue(stdCrfCde.getCdeIdVersion());
			newCell = row.createCell(colNum++);
			newCell.setCellValue(stdCrfCde.getCdeName());
			newCell = row.createCell(colNum++);
			newCell.setCellValue(stdCrfCde.getResult());
			newCell = row.createCell(colNum++);
			newCell.setCellValue(stdCrfCde.getMessage());
			newCell = row.createCell(colNum++);
			newCell.setCellValue("Std CRF Mandatory");
		}		

		return workbook;
	}

	/**
	 * Builds and returns an excel tab (sheet) with NRDS CDEs that are not part
	 * of the ALS
	 * 
	 * @param workbook
	 * @param missingNrdsCdeList
	 * @return XSSFWorkbook
	 */
	public static Workbook buildMissingNrdsCdesTab(Workbook workbook, List<NrdsCde> missingNrdsCdeList, List<StandardCrfCde> stdCrfCdeList, Boolean IsCrfChecked) {
		final String[] nrdsRowHeaders = crfRowHeaders;
		Sheet sheet = workbook.createSheet(nrds_missing_cde_tab_name);
		Row row;

		// Setting fixed column widths for cells
		final int idxOfCdeId = 0;// 0-based
		final int widthOfCdeId = 12 * 256;// in characters
		final int idxOfCdeName = 1;// 0-based
		final int widthOfCdeName = 100 * 256;
		final int idxOfFormName = 2;
		final int widthOfFormName = 100 * 256;
		final int idxOfFormId = 3;
		final int widthOfFormId = 20 * 256;// in characters
		// FORMBUILD-635
		final int idxOfPqt = 4;
		final int widthOfPqt = 100 * 256;
		final int idxOfType = 5;
		final int widthOfType = 20 * 256;// in characters		
		sheet.setColumnWidth(idxOfCdeId, widthOfCdeId);
		sheet.setColumnWidth(idxOfCdeName, widthOfCdeName);
		sheet.setColumnWidth(idxOfFormName, widthOfFormName);
		sheet.setColumnWidth(idxOfFormId, widthOfFormId);
		sheet.setColumnWidth(idxOfPqt, widthOfPqt);
		sheet.setColumnWidth(idxOfType, widthOfType);		
		int rowNum = 0;
		row = sheet.createRow(rowNum++);
		Cell newCell = row.createCell(0);
		newCell.setCellValue(nrds_missing_cde_header);
		newCell.setCellStyle(header_lbl_style);
		newCell = row.createCell(2);
		linkToSheet(newCell, backToSummary, summaryLbl);
		row = sheet.createRow(rowNum++);
		row = sheet.createRow(rowNum++);
		int colNum = 0;
		// Print row headers in the NRDS sheet
		for (String rowHeader : nrdsRowHeaders) {
			newCell = row.createCell(colNum++);
			newCell.setCellValue(rowHeader);
			newCell.setCellStyle(header_lbl_style_2);
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
			newCell = row.createCell(colNum++);			
			// FORMBUILD-635
			newCell.setCellValue(cde.getPreferredQuestionText());
			newCell = row.createCell(colNum++);
			newCell.setCellValue("NRDS");
			newCell = row.createCell(colNum++);
			newCell = row.createCell(colNum++);			
		}
		// FORMBUILD-635
		// Print the missing Std CRF Mandatory CDEs
		if (IsCrfChecked) {
			for (StandardCrfCde cde : stdCrfCdeList) {
				if (cde.getStdTemplateType().equalsIgnoreCase(templateTypes[0])) {
					colNum = 0;
					row = sheet.createRow(rowNum++);
					newCell = row.createCell(colNum++);
					newCell.setCellValue(cde.getCdeIdVersion());
					newCell = row.createCell(colNum++);
					newCell.setCellValue(cde.getCdeName());
					newCell = row.createCell(colNum++);
					newCell.setCellValue(cde.getPreferredQuestionText());
					newCell = row.createCell(colNum++);
					newCell.setCellValue("Std CRF");
					newCell = row.createCell(colNum++);
					newCell.setCellValue(cde.getTemplateName());
					newCell = row.createCell(colNum++);
					newCell.setCellValue(cde.getIdVersion());					
				}
			}		
		}
		return workbook;
	}
	/**
	 * Builds and returns an excel tab (sheet) with CDE missing from the ALS
	 * 
	 * @param workbook
	 * @param missingNrdsCdeList
	 * @return XSSFWorkbook
	 */
	public static Workbook buildMissingCdesTab(Workbook workbook, List<CdeMissing> missingNrdsCdeList, String sheetName, String header) {
		final String[] nrdsRowHeaders = classifiedMissingRowHeaders;
		Sheet sheet = workbook.createSheet(sheetName);
		Row row;

		// Setting fixed column widths for cells
		final int idxOfCdeId = 0;// 0-based
		final int widthOfCdeId = 12 * 256;// in characters
		final int idxOfCdeName = 1;// 0-based
		final int widthOfCdeName = 100 * 256;
		final int idxOfFormName = 2;
		final int widthOfFormName = 100 * 256;
		final int idxOfFormId = 3;
		final int widthOfFormId = 20 * 256;// in characters
		sheet.setColumnWidth(idxOfCdeId, widthOfCdeId);
		sheet.setColumnWidth(idxOfCdeName, widthOfCdeName);
		sheet.setColumnWidth(idxOfFormName, widthOfFormName);
		sheet.setColumnWidth(idxOfFormId, widthOfFormId);
		int rowNum = 0;
		row = sheet.createRow(rowNum++);
		Cell newCell = row.createCell(0);
		newCell.setCellValue(header);
		newCell.setCellStyle(header_lbl_style);
		newCell = row.createCell(2);
		linkToSheet(newCell, backToSummary, summaryLbl);
		row = sheet.createRow(rowNum++);
		row = sheet.createRow(rowNum++);
		int colNum = 0;
		// Print row headers in the NRDS sheet
		for (String rowHeader : nrdsRowHeaders) {
			newCell = row.createCell(colNum++);
			newCell.setCellValue(rowHeader);
			newCell.setCellStyle(header_lbl_style_2);
		}
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setWrapText(true);
		colNum = 0;
		// Print the missing NRDS CDEs
		for (CdeMissing cde : missingNrdsCdeList) {
			colNum = 0;
			row = sheet.createRow(rowNum++);
			newCell = row.createCell(colNum++);
			newCell.setCellValue(cde.getCdeIdVersion());
			newCell = row.createCell(colNum++);
			newCell.setCellValue(cde.getCdeName());
			newCell = row.createCell(colNum++);
			newCell.setCellValue(cde.getPreQuestionText());
		}

		return workbook;
	}
	/**
	 * Build the Standard CRF tabs
	 * 
	 * @param workbook
	 * @param stdCrfCdeList
	 * @return XSSFWorkbook
	 */
	public static Workbook buildStdCrfMissingTabs(Workbook workbook, List<StandardCrfCde> stdCrfCdeList) {
		int crfTabsCount = 3; // 3 categories of standard CRF CDEs
		for (int i = 0; i < crfTabsCount; i++)
			buildCrfTab(workbook.createSheet(tabNames[i]), stdCrfCdeList, templateTypes[i]);
		return workbook;
	}

	/**
	 * Build a CRF tab
	 * 
	 * @param sheet
	 * @param stdCrfCdeList
	 * @param category
	 * @return XSSFSheet
	 */
	private static Sheet buildCrfTab(Sheet sheet, List<StandardCrfCde> stdCrfCdeList, String category) {
		Row row;
		// Setting fixed column widths for cells
		final int idxOfCdeId = 0;// 0-based
		final int widthOfCdeId = 12 * 256;// in characters
		final int idxOfCdeName = 1;// 0-based
		final int widthOfCdeName = 100 * 256;
		final int idxOfFormName = 2;
		final int widthOfFormName = 100 * 256;
		final int idxOfFormId = 3;
		final int widthOfFormId = 20 * 256;// in characters
		sheet.setColumnWidth(idxOfCdeId, widthOfCdeId);
		sheet.setColumnWidth(idxOfCdeName, widthOfCdeName);
		sheet.setColumnWidth(idxOfFormName, widthOfFormName);
		sheet.setColumnWidth(idxOfFormId, widthOfFormId);
		int colNum = 0;
		int rowNum = 0;
		row = sheet.createRow(rowNum++);
		Cell newCell = row.createCell(0);
		newCell.setCellValue(String.format(cdeStdCrfMissingmsg, category));
		newCell.setCellStyle(header_lbl_style);
		newCell = row.createCell(2);
		linkToSheet(newCell, backToSummary, summaryLbl);
		row = sheet.createRow(rowNum++);
		row = sheet.createRow(rowNum++);
		// Print row headers in the CRF sheets
		for (String rowHeader : crfRowHeaders) {
			newCell = row.createCell(colNum++);
			newCell.setCellValue(rowHeader);
			newCell.setCellStyle(header_lbl_style_2);
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
	 * 
	 * @param sheet
	 * @param columns
	 * @param colWidth
	 * @return XSSFSheet
	 */
	private static Sheet setColumnWidth(Sheet sheet, Integer[] columns, int colWidth) {
		for (Integer colIndx : columns) {
			sheet.setColumnWidth(colIndx, colWidth);
		}
		return sheet;
	}

	/**
	 * Checks and crops the worksheetname to less than 31 for accommodating the
	 * 31 character limit for names of worksheets allowed by excel
	 * 
	 * If a duplicate worksheetname is found, then the last 3 characters of the
	 * worksheetname are truncated and appended with "(n)" to indicate the
	 * number of times the worksheet name is duplicated because of the excel
	 * limit truncation of the worksheet name
	 * 
	 * @param sheet
	 * @param columns
	 * @param colWidth
	 * @return XSSFSheet
	 */
	private static String cropFormNameForExcel(String worksheetName) {
		// Checking and cropping the form OID if it's more than 31 characters
		// long
		if (worksheetName.length() > 31) {// Excel limits 31 character on
											// worksheet name length
			worksheetName = worksheetName.substring(0, 31);
		}
		// count for the duplicates of truncated form sheet name
		int count = 0;
		// Checking the presence of the worksheet to identify duplicates
		if (formOids.contains(worksheetName)) {
			// Get the count of how many times the duplicate formOID is present
			if (formOidDupes.containsKey(worksheetName)) {
				count = formOidDupes.get(worksheetName);
			}
			String longWorksheetName = worksheetName;
			int truncLength = 0;
			// Determining the length of truncation based on the number of duplicates for a form name
			// when they go into double or triple digits - highly unlikely scenario yet not impossible
			if (count < 9) {
				truncLength = truncSheetNumSingleDigit;
			} else if (count == 9 || count > 9) {
				truncLength = truncSheetNumDoubleDigit;
			} else if (count == 99 || count > 99) {
				truncLength = truncSheetNumTripleDigit;
			}
			// Truncating the last set of characters of the worksheet name to
			// append "(n)" where 'n' is the count
			worksheetName = worksheetName.substring(0, worksheetName.length() - truncLength) + "(" + (++count) + ")";
			formOidDupes.put(longWorksheetName, count);
		}
		// Add the worksheet name to the Set for the first time
		else {
			formOids.add(worksheetName);
		}
		return worksheetName;
	}

	/**
	 * Creates a hyperlink to a given Sheet in the final report
	 * 
	 * @param newCell
	 */
	private static void linkToSheet(Cell newCell, String cellText, String sheetName) {
		Hyperlink sheetLink = createHelper.createHyperlink(HyperlinkType.DOCUMENT);
		newCell.setCellValue(cellText);
		String linkText = "'" + sheetName + "'!A1";// "'Target Sheet'!A1"
		sheetLink.setAddress(linkText);
		newCell.setHyperlink(sheetLink);
		newCell.setCellStyle(hlink_style);
	}
	
	// FORMBUILD-648
	/**
	 * Adds left borders to columns that are triplets in terms of the report - [ALS value, Result, caDSR value]
	 * 
	 * @param row
	 * 
	 */	
	private static void groupTripletCellsWithBorders (Row row) {
		Integer[] borderColumns = { borderStartColumn1,  borderStartColumn2, codedDataColStart, borderStartColumn3, raveFieldDataTypeCol, borderStartColumn4, borderStartColumn5, borderStartColumn6, borderStartColumn7};		
		for (Integer colIndx : borderColumns) {
			Cell bCell = row.getCell(colIndx);
			if (bCell == null) {
				bCell = row.createCell(colIndx);
			} 
			bCell.setCellStyle(cell_leftBorder_Style);			
		}
	}	

}
