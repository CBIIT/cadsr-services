/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.report;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jackson.map.ObjectMapper;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.service.FormService;
import gov.nih.nci.cadsr.parser.Parser;
import gov.nih.nci.cadsr.parser.impl.AlsParser;
import gov.nih.nci.cadsr.report.impl.GenerateReport;

public class CongruencyCheckerReportInvoker {
	
	
	private static final Logger logger = Logger.getLogger(CongruencyCheckerReportInvoker.class);
	private static Parser alsParser = null;
	private static ReportOutput generateReport = null;
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
	private static int formStartColumn = 4;	
	private static int allowableCdeValueCol = 19;
	private static int codedDataColStart = 16;
	
	
	
	public static void main(String[] args) {
			CCCReport cccReport = new CCCReport();
			alsParser = new AlsParser();
			generateReport = new GenerateReport();
			Properties prop = new Properties();
			InputStream input = null;
			String filename = "config.properties";		
			try {
			input = CongruencyCheckerReportInvoker.class.getClassLoader().getResourceAsStream(filename);
			prop.load(input);
			String INPUT_XLSX_FILE_PATH = "target/classes/" + prop.getProperty("ALS-INPUT-FILE");
			String OUTPUT_XLSX_FILE_PATH = "target/" + prop.getProperty("VALIDATOR-OUTPUT-FILE");
			ALSData alsData = alsParser.parse (INPUT_XLSX_FILE_PATH);
			FormService.getFormsListJSON(alsData);
			// Set Forms list to be sent to UI for selection, in ALSData
			alsData.setFormsUiData(FormService.buildFormsUiData(alsData));
			for (ALSError alsError1 : alsData.getCccError().getAlsErrors()) {
				logger.debug("Error description: "+alsError1.getErrorDesc()+" Severity: "+alsError1.getErrorSeverity());
			}			
			cccReport  = generateReport.getFinalReportData(alsData);
			for (CCCForm forms : cccReport.getCccForms()) {
				//logger.debug("Form name: " + forms.getRaveFormOId());
				//logger.debug("Questions list: " + forms.getQuestions().size());
				for (CCCQuestion question : forms.getQuestions()) {
					/*if (question.getRaveCodedData() != null && question.getRaveCodedData().size() != 0)
						logger.debug("Question coded data list: " + question.getRaveCodedData().size());
					if (question.getRaveUserString() != null && question.getRaveUserString().size() != 0)
						logger.debug("Questions user string data list: " + question.getRaveUserString().size());*/
				}
			}
			writeExcel(OUTPUT_XLSX_FILE_PATH, cccReport);
			//writeToJSON(cccReport);
			//logger.debug("Output object forms count: " + cccReport.getCccForms().size());			
			} catch (IOException ioe) {
				ioe.printStackTrace();			
			} catch (InvalidFormatException ife) {
				ife.printStackTrace();
			} catch (NullPointerException npe) {
				npe.printStackTrace();			
			}			
		}		
	
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

		try {
			FileOutputStream outputStream = new FileOutputStream(OUTPUT_XLSX_FILE_PATH);
			workbook.write(outputStream);
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}		
	
	
	private static void writeToJSON (CCCReport cccReport) {
		
		ObjectMapper jsonMapper = new ObjectMapper();
		try {
            String jsonStr = jsonMapper.writeValueAsString(cccReport);
            logger.debug(jsonStr);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }		
		
	}
}
