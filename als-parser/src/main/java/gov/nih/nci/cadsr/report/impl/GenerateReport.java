package gov.nih.nci.cadsr.report.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.CCCError;
import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.report.ReportOutput;

public class GenerateReport implements ReportOutput {

	/**
	 * @param
	 * @return Populates the output object for the report after initial
	 *         validation and parsing of data
	 * 
	 */
	public CCCReport getFinalReportOutput(ALSData alsData) throws NullPointerException {
		CCCReport cccReport = new CCCReport();
		cccReport.setReportOwner(alsData.getReportOwner());
		cccReport.setReportDate(alsData.getReportDate());
		cccReport.setRaveProtocolName(alsData.getCrfDraft().getProjectName());
		cccReport.setRaveProtocolNumber(alsData.getCrfDraft().getPrimaryFormOid());
		cccReport.setTotalFormsCount(alsData.getForms().size());
		cccReport.setCountQuestionsChecked(alsData.getFields().size());
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
				question.setFieldOrder(alsField.getOrdinal()); 																																
				String draftFieldName = alsField.getDraftFieldName();
				if (draftFieldName.indexOf("PID") > -1 && draftFieldName.indexOf("_V") > -1) {
					String idVersion = draftFieldName.substring(draftFieldName.indexOf("PID"), draftFieldName.length());
					question.setCdePublicId(idVersion.substring(3, idVersion.indexOf("_")));
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
						}
					}
					question.setAllowableCdeValue("");
					question.setPvResult("Error/match"); // Will be replaced with the caDSR db validation result
					question.setAllowableCdeTextChoices("A|B|C|D"); // Test values - will be replaced with the PV value meanings from caDSR db
					question.setRaveFieldDataType(alsField.getDataFormat());
					question.setRaveLength(alsField.getFixedUnit());
					question.setRaveDisplayFormat(alsField.getDataFormat());
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
		return cccReport;
	}	
	
	private static CCCError getErrorObject() {
		CCCError cccError = new CCCError();
		return cccError;
	}	

}
