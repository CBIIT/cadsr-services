/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.report.impl;
/**
 * This is a test data report
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSError;
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
	public CCCReport getFinalReportData(ALSData alsData) throws NullPointerException {
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
		ALSError alsError;	
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
					try {
						idVersion = draftFieldName.substring(draftFieldName.indexOf("PID"), draftFieldName.length());
						String id = idVersion.substring(3, idVersion.indexOf("_"));
						String version = (idVersion.substring(idVersion.indexOf("_V") + 2, idVersion.length()));
				        question.setCdePublicId(id.trim());
				        String[] versionTokens = version.split("\\_");
				        Integer.parseInt(versionTokens[0]);
				        Integer.parseInt(versionTokens[1]);
				        version = versionTokens[0] + "." + versionTokens[1];
				        question.setCdeVersion(version);				        
				    }
				    catch (NumberFormatException e) {
				    	e.printStackTrace();
						/*alsError = getErrorInstance();
						alsError.setErrorDesc(err_msg_23+ " " + idVersion +" Sheet: "+sheet.getSheetName()+" Row: "+row.getRowNum()+" Cell: "+cell_draftFieldName+".");
						alsError.setErrorSeverity(errorSeverity_error);
						alsData.getCccError().addAlsError(alsError);*/				    		
				    }
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
					question.setDatatypeCheckerResult("Error/Match"); // Will be replaced with the caDSR db validation result
					question.setCdeDataType(""); // Will be set with the caDSR db value domain data type after a comparison with data format
					String raveUOM = null;
					if (alsField.getFixedUnit()!=null)
						raveUOM = alsField.getFixedUnit();
					else 
						if (alsField.getUnitDictionaryName()!=null)
							raveUOM = alsField.getUnitDictionaryName();
					question.setRaveUOM(raveUOM);
					question.setUomCheckerResult("Error/Match"); // Will be replaced with the caDSR db validation result
					question.setCdeUOM(""); // caDSR DB Value domain UOM, if it doesnt match with RAVE UOM					
					question.setRaveLength(alsField.getFixedUnit());
					question.setLengthCheckerResult("");
					question.setCdeMaxLength(0);// caDSR DB Value domain max length
					question.setRaveDisplayFormat(alsField.getDataFormat());
					question.setFormatCheckerResult("");
					question.setCdeDisplayFormat(""); // caDSR DB Value domain display format
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

}
