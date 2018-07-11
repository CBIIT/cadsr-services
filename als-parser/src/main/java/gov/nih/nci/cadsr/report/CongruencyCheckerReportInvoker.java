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
import gov.nih.nci.cadsr.parser.FormsService;
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
	private static int formStartRow = 4;
	private static int formStartColumn = 4;	
	private static int allowableCdeValueCol = 19;
	private static int codedDataColStart = 16;
	private static int crfDraftStartRow = 1;
	
	
	
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
			FormsService.getFormsListJSON(alsData);
			FormsService.getSelectedForms("[{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"ENROLLMENT\",\"ordinal\":1,\"draftFormName\":\"Enrollment\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"HISTOLOGY_AND_DISEASE\",\"ordinal\":2,\"draftFormName\":\"Histology and Disease\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"ADMINISTRATIVE_ENROLLMENT\",\"ordinal\":3,\"draftFormName\":\"Administrative Enrollment\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"ELIGIBILITY_CHECKLIST\",\"ordinal\":4,\"draftFormName\":\"Eligibility Checklist\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"PATIENT_ELIGIBILITY\",\"ordinal\":5,\"draftFormName\":\"Patient Eligibility\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"MOLECULAR_MARKER\",\"ordinal\":6,\"draftFormName\":\"Molecular Marker\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"BASELINE_MEDICAL_HISTORY\",\"ordinal\":7,\"draftFormName\":\"Baseline Medical History\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"PRIOR_TREATMENT_SUMMARY\",\"ordinal\":8,\"draftFormName\":\"Prior Treatment Summary\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"PRIOR_THERAPY_SUPPLEMENT\",\"ordinal\":9,\"draftFormName\":\"Prior Therapy Supplement\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"PRIOR_SURGERY_SUPPLEMENT\",\"ordinal\":10,\"draftFormName\":\"Prior Surgery Supplement\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"PRIOR_RADIATION_SUPPLEMENT\",\"ordinal\":11,\"draftFormName\":\"Prior Radiation Supplement\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"BASELINE_SYMPTOMS_PRESENCE\",\"ordinal\":12,\"draftFormName\":\"Baseline Symptoms Presence\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"BSL_SYMP\",\"ordinal\":13,\"draftFormName\":\"Adverse Baseline Symptoms\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"COURSE_INITIATION\",\"ordinal\":14,\"draftFormName\":\"Course Initiation\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"DRUG_ADMINISTRATION\",\"ordinal\":15,\"draftFormName\":\"Drug Administration\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"FOLLOWUP\",\"ordinal\":16,\"draftFormName\":\"FollowUp\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"PHYSICAL_EXAM\",\"ordinal\":17,\"draftFormName\":\"PE\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"ADVERSE_EVENT_PRESENCE\",\"ordinal\":18,\"draftFormName\":\"Adverse Event Presence\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"TOXICITY_OTH\",\"ordinal\":19,\"draftFormName\":\"Adverse Events\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"PHARMACOKINETICS_SAMPLES\",\"ordinal\":20,\"draftFormName\":\"Pharmacokinetics Samples\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"PHARMACOKINETICS_RESULTS\",\"ordinal\":21,\"draftFormName\":\"Pharmacokinetics Results\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"PHARMACODYNAMICS_SAMPLES\",\"ordinal\":22,\"draftFormName\":\"PharmacoDynamics Samples\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"PHARMACOGENETIC_SAMPLES\",\"ordinal\":23,\"draftFormName\":\"Pharmacogenetic Samples\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"URINARY_EXCRETIONS\",\"ordinal\":24,\"draftFormName\":\"Urinary Excretions\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"COURSE_ASSESSMENT\",\"ordinal\":25,\"draftFormName\":\"Course Assessment\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"VITAL_SIGNS\",\"ordinal\":26,\"draftFormName\":\"Vital Signs\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"SEROLOGY_PREGNANCY_SKIN_AND_STOOL_TESTS\",\"ordinal\":27,\"draftFormName\":\"Serology - Pregnancy - Skin and Stool Tests\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"CONCOMITANT_MEASURES_MEDICATIONS\",\"ordinal\":28,\"draftFormName\":\"Concomitant Measures/Medications\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"TRANSFUSION\",\"ordinal\":29,\"draftFormName\":\"Transfusion\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"HEMATOLOGY_LAB\",\"ordinal\":32,\"draftFormName\":\"HM\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"CHEMISTRY___PANCREATIC_THYROID___CARDIAC_LAB\",\"ordinal\":33,\"draftFormName\":\"PTC\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"BLOOD_CHEMISTRY___HEPATIC_LAB\",\"ordinal\":34,\"draftFormName\":\"BCH\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"BLOOD_CHEMISTRY___RENAL_LAB\",\"ordinal\":35,\"draftFormName\":\"BCR\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"BONE_MARROW_LAB\",\"ordinal\":36,\"draftFormName\":\"BM\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"BLOOD_GASES___LAB\",\"ordinal\":37,\"draftFormName\":\"RFB\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"LITERAL_LABORATORY\",\"ordinal\":38,\"draftFormName\":\"Literal Laboratory\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"UNANTICIPATED_LAB\",\"ordinal\":39,\"draftFormName\":\"Unanticipated Lab\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"IMMUNE_PARAMETERS_LAB\",\"ordinal\":40,\"draftFormName\":\"IP\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"OTHER_URINALYSIS_LAB\",\"ordinal\":41,\"draftFormName\":\"OU\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"RED_CELL_INDICES_LAB\",\"ordinal\":42,\"draftFormName\":\"RC\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"RESPIRATORY_FUNCTION_LAB\",\"ordinal\":43,\"draftFormName\":\"RF\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"OTHER_SERUM_CHEMISTRY_LAB\",\"ordinal\":44,\"draftFormName\":\"SC\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"SERUM_ELECTROPHORESIS_LAB\",\"ordinal\":45,\"draftFormName\":\"SE\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"URINALYSIS_LAB\",\"ordinal\":46,\"draftFormName\":\"US\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"URINE_IMMUNE_ELECTROPHORESIS_LAB\",\"ordinal\":47,\"draftFormName\":\"UE\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"LESION_PRESENT_AT_BASELINE\",\"ordinal\":48,\"draftFormName\":\"LS\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"NEW_LESION_PRESENCE\",\"ordinal\":49,\"draftFormName\":\"New Lesion Presence\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"NEW_LESION_DATA\",\"ordinal\":50,\"draftFormName\":\"NLS\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"OFF_TREATMENT_OFF_STUDY\",\"ordinal\":52,\"draftFormName\":\"Off Treatment / Off Study\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"FOLLOW_UP\",\"ordinal\":53,\"draftFormName\":\"Follow Up\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"DEATH_SUMMARY\",\"ordinal\":54,\"draftFormName\":\"Death Summary\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"STUDY_CONCLUSION\",\"ordinal\":83,\"draftFormName\":\"Study Conclusion\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"CORRELATIVE_STUDIES\",\"ordinal\":84,\"draftFormName\":\"CS\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"LAB_EXTRA\",\"ordinal\":85,\"draftFormName\":\"EX\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"SUBJECT_ENROLLMENT\",\"ordinal\":86,\"draftFormName\":\"Subject Enrollment\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"DEMOGRAPHY\",\"ordinal\":87,\"draftFormName\":\"Demography\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"STEP_INFORMATION\",\"ordinal\":88,\"draftFormName\":\"Step Information\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"TREATMENT_ASSIGNMENT\",\"ordinal\":89,\"draftFormName\":\"Treatment Assignment\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"LITERAL_EXTRA\",\"ordinal\":90,\"draftFormName\":\"Literal Extra\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"TUMOR_MARKERS\",\"ordinal\":91,\"draftFormName\":\"Genetic Markers\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"CLARKP_CDECART_1\",\"ordinal\":92,\"draftFormName\":\"CLARKP_cdeCart_1\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"BIOMARKERS\",\"ordinal\":93,\"draftFormName\":\"Biomarkers\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"AGELIST\",\"ordinal\":94,\"draftFormName\":\"Age List\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"CLARKP_CDECART\",\"ordinal\":95,\"draftFormName\":\"CLARKP_cdeCart\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"COMMENTS\",\"ordinal\":96,\"draftFormName\":\"Comments\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"PATIENT_INFORMATION_FOR_NCI_REPORTING\",\"ordinal\":97,\"draftFormName\":\"Patient Information for NCI Reporting\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"LATE_ADVERSE_EVENT_PRESENCE\",\"ordinal\":192,\"draftFormName\":\"Late Adverse Event Presence\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"AELATE\",\"ordinal\":193,\"draftFormName\":\"Late Adverse Events\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"CREATININE_CLEARANCE\",\"ordinal\":194,\"draftFormName\":\"Creatinine Clearance\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"SARAKHAN_CDECART\",\"ordinal\":195,\"draftFormName\":\"sarakhan_cdeCart\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"HIDDEN_LABELS\",\"ordinal\":780,\"draftFormName\":\"Hidden Labels\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"SPECIMEN_TRACKING_ENROLLMENT\",\"ordinal\":974,\"draftFormName\":\"Specimen Tracking Enrollment\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"TRACKING_CONTACTS\",\"ordinal\":975,\"draftFormName\":\"Tracking Contacts\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"SPECIMEN_TRANSMITTAL\",\"ordinal\":1068,\"draftFormName\":\"Specimen Transmittal\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"COPY_SHIPPING\",\"ordinal\":1069,\"draftFormName\":\"Copy Shipping\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"SHIPPING_STATUS\",\"ordinal\":1070,\"draftFormName\":\"Shipping Status\",\"fields\":[]},\"questionsCount\":0},{\"isValid\":true,\"errors\":[],\"form\":{\"formOId\":\"MODIFIED_SEVERITY_WEIGHTED_ASSESSMENT__MSWAT_\",\"ordinal\":1071,\"draftFormName\":\"Modified Severity Weighted Assessment (mSWAT)\",\"fields\":[]},\"questionsCount\":0}]");
			for (ALSError alsError1 : alsData.getCccError().getAlsErrors()) {
				logger.debug("Error: "+alsError1.getErrorDesc()+" Severity: "+alsError1.getErrorSeverity());
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
