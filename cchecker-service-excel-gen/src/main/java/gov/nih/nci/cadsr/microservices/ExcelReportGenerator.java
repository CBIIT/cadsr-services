package gov.nih.nci.cadsr.microservices;
/*
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.NrdsCde;
import gov.nih.nci.cadsr.data.StandardCrfCde;

public class ExcelReportGenerator {

		private static final Logger logger = Logger.getLogger(ExcelReportGenerator.class);
		private static String formHeader_1 = "View of Expanded Results for ";
		private static String formHeader_2 = " form";	
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
		private static int formStartColumn = 4;	
		private static int allowableCdeValueCol = 19;
		private static int codedDataColStart = 16;
		private static String matching_nrds_cdes_tab_name = "NRDS CDEs in ALS";
		private static String nrds_missing_cde_tab_name = "NRDS CDEs missing";
		private static String nrds_missing_cde_header = "NRDS CDEs missing from the ALS file";
		private static String matching_nrds_cdes_header = "NRDS CDEs included in Protocol Forms with Warnings or Errors";		

		/**
		 * @param
		 * @return Implementation for Writing the final output report object into an excel (as a feasibility check)
		 * 
		 */
		public static void writeExcel(String OUTPUT_XLSX_FILE_PATH, CCCReport cccReport) throws IOException, InvalidFormatException, NullPointerException {

			Row row;
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("Summary");
			Map<String, String> summaryLabels = new LinkedHashMap<String, String>();
			summaryLabels.put(checkerReportOwnerLbl, cccReport.getReportOwner());
			summaryLabels.put(raveProtocolNameLbl, cccReport.getRaveProtocolName());
			summaryLabels.put(raveProtocolNumLbl, cccReport.getRaveProtocolNumber());
			summaryLabels.put(reportDateLbl, cccReport.getReportDate());
			summaryLabels.put(formCountLbl, String.valueOf(cccReport.getTotalFormsCount()));
			summaryLabels.put(totalCountFormLbl, String.valueOf(cccReport.getTotalFormsCount()));
			summaryLabels.put(totalQuestCheckLbl, String.valueOf(cccReport.getCountQuestionsChecked()));
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
				cell.setCellValue(form.getRaveFormOid());
				cell = row.createCell(summaryFormsValidResultColNum);
				cell.setCellValue(form.getCongruencyStatus());
			}

			String[] rowHeaders = { "Rave Form OID", "caDSR Form ID", "Version", "Total Number Of Questions Checked",
					"Field Order", "CDE Public ID", "CDE Version", "NCI Category", "Question Congruency Status", "Message",
					"Rave Field Label", "Rave Field Label Result", "CDE Permitted Question Text Choices",
					"Rave Control Type", "Control Type Checker Result", "CDE Value Domain Type", "Rave Coded Data", "Coded Data Result",
					"Allowable CDE  Value", "Rave User String", "PV  Result", "Allowable CDE  Value Meaning Text Choices",
					"Rave Field Data Type", "Dataype Checker Result", "CDE Data Type", "Rave UOM", "UOM Checker Result", "CDE UOM",
					"Rave Length", "Length Checker Result", "CDE Maximum Length", "Rave Display Format", "Format Checker Result",
					"CDE Display Format" };
			for (CCCForm cccForm : forms) {
				XSSFSheet sheet2 = workbook.createSheet(cccForm.getRaveFormOid());
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
				colNum = colNum+3;
				newCell = row.createCell(3);
				newCell.setCellValue(cccForm.getCountTotalQuestions());
	            CellStyle cellStyle = workbook.createCellStyle(); //Create new style
	            cellStyle.setWrapText(true);
				for (int j = 0; j < cccForm.getQuestions().size(); j++) {
					int colNum2 = formStartColumn;
					CCCQuestion question = cccForm.getQuestions().get(j);
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
					newCell.setCellStyle(cellStyle);
					newCell.setCellValue(question.getMessage());
					newCell = row.createCell(colNum2++);
					newCell.setCellValue(question.getRaveFieldLabel());
					newCell = row.createCell(colNum2++);
					newCell.setCellStyle(cellStyle);				
					newCell.setCellValue(question.getRaveFieldLabelResult());
					newCell = row.createCell(colNum2++);
					newCell.setCellStyle(cellStyle);				
					newCell.setCellValue(question.getCdePermitQuestionTextChoices());
					newCell = row.createCell(colNum2++);
					newCell.setCellValue(question.getRaveControlType());				
					newCell = row.createCell(colNum2++);
					newCell.setCellValue(question.getControlTypeResult());								
					newCell = row.createCell(colNum2++);
					newCell.setCellValue(question.getCdeValueDomainType());
					List<String> raveCodedData = question.getRaveCodedData();
					List<String> raveUserString = question.getRaveUserString();
					List<String> codedDataResult = question.getCodedDataResult();
					Row rowBeforeCD = row;
					for (int m = 0; m < raveCodedData.size(); m++)	{
						int colNum3 = codedDataColStart;
						newCell = row.createCell(colNum3++);
						newCell.setCellValue(raveCodedData.get(m));					
						newCell = row.createCell(colNum3++);
						if (codedDataResult.isEmpty())
							newCell.setCellValue("Cell empty");
						else	
							newCell.setCellValue(codedDataResult.get(m)); 
						newCell = row.createCell(colNum3);
						newCell.setCellValue(raveUserString.get(m)); // substituting pv values from ALS for now TODO - needs to get actual values from caDSR db			
						if (m != raveCodedData.size()-1)
							row = sheet2.createRow(rowNum++);
					}
					int rowNumAfterCD = rowNum;
					row = rowBeforeCD;
					int newColNum = allowableCdeValueCol;
					newCell = row.createCell(newColNum++);
					newCell.setCellStyle(cellStyle);				
					newCell.setCellValue(question.getAllowableCdeValue());
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getPvResult());				
					newCell = row.createCell(newColNum++);
					newCell.setCellStyle(cellStyle);				
					newCell.setCellValue(question.getAllowableCdeTextChoices());
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getRaveFieldDataType());				
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getDatatypeCheckerResult());								
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getCdeDataType());
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getRaveUOM());
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getUomCheckerResult());																				
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getCdeUOM());																								
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getRaveLength());
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getLengthCheckerResult());
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getCdeMaxLength());				
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getRaveDisplayFormat());								
					newCell = row.createCell(newColNum++);
					newCell.setCellValue(question.getFormatCheckerResult());												
					newCell = row.createCell(newColNum);
					newCell.setCellValue(question.getCdeDisplayFormat());
					if (rowNumAfterCD > rowNum)
						rowNum = rowNumAfterCD;
				}
			}
			buildNrdsTab(workbook, cccReport.getNrdsCdeList());
			buildMissingNrdsCdesTab(workbook, cccReport.getMissingNrdsCdeList());
			buildStdCrfMissingTabs(workbook, cccReport.getMissingStandardCrfCdeList());			
			FileOutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(OUTPUT_XLSX_FILE_PATH);
				logger.debug("..outputStream created for " + OUTPUT_XLSX_FILE_PATH);
				autoSizeColumns(workbook);
				logger.debug("...autoSizeColumns done");
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
		
		public static void autoSizeColumns(Workbook workbook) {
			if (workbook!=null) {
			    int numberOfSheets = workbook.getNumberOfSheets();
			    logger.debug("autoSizeColumns NumberOfSheets: " + numberOfSheets);
			    //FIXME this is slow; consider re-sizing Form sheets only
			    for (int i = 0; (i < numberOfSheets) && (i < 2); i++) {
			    	try {
				        Sheet sheet = workbook.getSheetAt(i);
				        if (sheet != null) {
					        if (sheet.getPhysicalNumberOfRows() > 0) {
					        	for (int j = sheet.getFirstRowNum()+3; j < sheet.getLastRowNum(); j++) {
						            Row row = sheet.getRow(j); 
						            if (row != null) {
							            Iterator<Cell> cellIterator = row.cellIterator();
							            while (cellIterator.hasNext()) {
							                Cell cell = cellIterator.next();
							                if (cell != null) {
								                int columnIndex = cell.getColumnIndex();
								                sheet.autoSizeColumn(columnIndex);
							                }
							            }
						            }
					            }
					        }
				        }
			    	}
			    	catch (Exception e) {
			    		logger.error("autoSizeColumns Exception on i=: " + i + ", " + e);
			    	}
			    }
			}
		}
		
		public static XSSFWorkbook buildNrdsTab (XSSFWorkbook workbook, List<NrdsCde> nrdsCdeList) {
			Row row;
			String[] nrdsRowHeaders = { "Rave Form OID", "RAVE Field Order", "RAVE Field Label", "CDE ID Version", "CDE Name", "Result", "Message"};
			XSSFSheet sheet = workbook.createSheet(matching_nrds_cdes_tab_name);
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
		
		public static XSSFWorkbook buildMissingNrdsCdesTab (XSSFWorkbook workbook, List<NrdsCde> missingNrdsCdeList) {
			Row row;
			String[] nrdsRowHeaders = { "CDE ID Version", "CDE Name"};
			XSSFSheet sheet = workbook.createSheet(nrds_missing_cde_tab_name);
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
		
		
		public static XSSFWorkbook buildStdCrfMissingTabs (XSSFWorkbook workbook, List<StandardCrfCde> stdCrfCdeList) {
			String[] templateTypes = {"Mandatory", "Optional", "Conditional"};
			String[] tabNames = {"Standard CRF Mandatory Missing", "Standard CRF Optional Missing", "Standard CRF Conditional Missing"};
			int crfTabsCount = 3; // 3 categories of standard CRF CDEs		
			for (int i = 0; i < crfTabsCount; i++ )
				buildCrfTab(workbook.createSheet(tabNames[i]), stdCrfCdeList, templateTypes[i]);	
	 		return workbook;		
		}	
		
		
		private static XSSFSheet buildCrfTab (XSSFSheet sheet, List<StandardCrfCde> stdCrfCdeList, String category) {
			Row row;
			String[] crfRowHeaders = { "CDE IDVersion", "CDE Name", "Template Name", "CRF ID Version"};
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
		
}
