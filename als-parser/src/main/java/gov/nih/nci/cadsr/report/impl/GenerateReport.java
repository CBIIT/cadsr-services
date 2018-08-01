/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.report.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import gov.nih.nci.cadsr.dao.model.PermissibleValuesModel;
import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.CCCError;
import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.report.ReportOutput;
import gov.nih.nci.cadsr.service.CdeService;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;


public class GenerateReport implements ReportOutput {

	
	private static final Logger logger = Logger.getLogger(GenerateReport.class);
	private static String congStatus_errors= "ERRORS";
	private static String congStatus_warn = "WARNINGS";
	private static String congStatus_congruent = "CONGRUENT";

	/**
	 * @param
	 * @return Populates the output object for the report after initial
	 *         validation and parsing of data
	 * 
	 */
	public CCCReport getFinalReportData(ALSData alsData, List<String> selForms, Boolean checkUom, Boolean checkStdCrfCde, Boolean displayExceptionDetails) throws NullPointerException {
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
		int totalQuestCount = 0;
		List<CCCQuestion> questionsList = new ArrayList<CCCQuestion>();
		Map<String, ALSDataDictionaryEntry> ddeMap = alsData.getDataDictionaryEntries();
		for (ALSField alsField : alsData.getFields()) {
			if (selForms.contains(alsField.getFormOid())) {
				if (formName.equals(""))
					formName = alsField.getFormOid();
				if (!formName.equals("OID")) {
					if (!formName.equals(alsField.getFormOid())) {
						if (!questionsList.isEmpty())
							form.setQuestions(questionsList);
						else
							form.setCongruencyStatus(congStatus_congruent);
						form.setRaveFormOId(formName);
						form.setCountTotalQuestions(totalQuestCount);
						formsList.add(form);
						totalQuestCount = 0;
						formName = alsField.getFormOid();
						form = new CCCForm();
						questionsList = new ArrayList<CCCQuestion>();
					}
					CCCQuestion question = new CCCQuestion();
					totalQuestCount++;
					question.setFieldOrder(alsField.getOrdinal()); 																																
					String draftFieldName = alsField.getDraftFieldName();
					if (draftFieldName.indexOf("PID") > -1 && draftFieldName.indexOf("_V") > -1) {
						String idVersion = draftFieldName.substring(draftFieldName.indexOf("PID"), draftFieldName.length());
							idVersion = draftFieldName.substring(draftFieldName.indexOf("PID"), draftFieldName.length());
							String id = idVersion.substring(3, idVersion.indexOf("_"));
							String version = (idVersion.substring(idVersion.indexOf("_V") + 2, idVersion.length()));
							id = id.trim();
					        String[] versionTokens = version.split("\\_");
					        version = versionTokens[0] + "." + versionTokens[1];
							if (NumberUtils.isNumber(id) && NumberUtils.isNumber(version)) {
						        question.setCdePublicId(id.trim());
						        question.setCdeVersion(version);
							} else {
								logger.debug("CDE public ID and version should be numeric");
							}
						question.setNciCategory("NRDS"); // "NRDS" "Mandatory Module: {CRF ID/V}", "Optional Module {CRF ID/V}", "Conditional Module: {CRF ID/V}"
						question.setRaveFieldLabel(alsField.getPreText());
						question.setCdePermitQuestionTextChoices(""); // From the caDSR DB - docText
						question.setRaveControlType(alsField.getControlType());
		
						for (String key : ddeMap.keySet()) {
							if (key.equals(alsField.getDataDictionaryName())) {
								question.setRaveCodedData(ddeMap.get(key).getCodedData()); // Data dictionary name and its corresponding entries - All the Permissible values
								question.setRaveUserString(ddeMap.get(key).getUserDataString());
							}
						}

						String raveUOM = null;
						if (alsField.getFixedUnit()!=null)
							raveUOM = alsField.getFixedUnit();
						else 
							if (alsField.getUnitDictionaryName()!=null)
								raveUOM = alsField.getUnitDictionaryName();
						question.setRaveUOM(raveUOM);
						question.setRaveLength(alsField.getFixedUnit());
						question.setRaveDisplayFormat(alsField.getDataFormat());
						//question.setQuestionCongruencyStatus(congStatus_congruent);
						question.setMessage(pickFieldErrors(alsField, alsData.getCccError().getAlsErrors()));
						// TODO This should call the service [Should be HTTPResponse] - VS
						CdeDetails cdeDetails = null;
						//if (alsField.getFormOid().equalsIgnoreCase("ENROLLLMENT") || alsField.getFormOid().equalsIgnoreCase("HISTOLOGY_AND_DISEASE") || alsField.getFormOid().equalsIgnoreCase("ELIGIBILITY_CHECKLIST")) {
						try {
							cdeDetails = CdeService.retrieveDataElement(question.getCdePublicId(), question.getCdeVersion());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						// TODO Call to the validation of the CDEDetails against the ALSField & Question objects  
						//question = validate(alsField, question, cdeDetails); //[ValidatorService]
						//}
						if ((question.getMessage() == null || question.getMessage().equals(""))) { 
							form.setCongruencyStatus(congStatus_congruent);
						} else {
									questionsList.add(question); 
									if (form.getCongruencyStatus()!=null) {
										if (!form.getCongruencyStatus().equals(congStatus_errors)) {
											if (question.getQuestionCongruencyStatus()!=null && question.getQuestionCongruencyStatus().equalsIgnoreCase(congStatus_errors))
												form.setCongruencyStatus(congStatus_errors);
											else if (question.getQuestionCongruencyStatus()!=null && question.getQuestionCongruencyStatus().equals(congStatus_warn))
												form.setCongruencyStatus(congStatus_warn);
										} else 
											form.setCongruencyStatus(question.getQuestionCongruencyStatus());
									} else 
										form.setCongruencyStatus(question.getQuestionCongruencyStatus());
								}						
					} else {
							question.setRaveFieldLabel(alsField.getPreText());
							//if (!question.getQuestionCongruencyStatus().equalsIgnoreCase(congStatus_errors))
								question.setQuestionCongruencyStatus(congStatus_warn);
							//question.setMessage(msg_6);
							question.setMessage(pickFieldErrors(alsField, alsData.getCccError().getAlsErrors()));
							questionsList.add(question);
						}
					}				
				}			
			}

		if (questionsList.isEmpty())
			form.setCongruencyStatus(congStatus_congruent);
		else
			form.setQuestions(questionsList);			
		form.setRaveFormOId(formName);
		if (!form.getQuestions().isEmpty())
			formsList.add(form);
		cccReport.setCccForms(formsList);
		return cccReport;
	}	
	
	private static CCCError getErrorObject() {
		CCCError cccError = new CCCError();
		return cccError;
	}	
	
	protected ALSError getErrorInstance() {
		ALSError alsError = new ALSError();
		return alsError;
	}		
	
	
	protected static String pickFieldErrors(ALSField field, List<ALSError> errors) {
		String errorMsg = null;
		List<ALSError> fieldErrors = new ArrayList<ALSError>();
		for (ALSError alsError : errors) {
			if (alsError.getFieldOid()!=null) {
				if (field.getFieldOid().equalsIgnoreCase(alsError.getFieldOid()) && field.getFormOid().equalsIgnoreCase(alsError.getFormOid())) {
					fieldErrors.add(alsError);
				}
			}
		}
		for (ALSError alsError : fieldErrors) {
			//if (alsError.getFieldOid()!=null && field.getFieldOid().equalsIgnoreCase(alsError.getFieldOid())) {
				if (errorMsg!=null)
					errorMsg = errorMsg + alsError.getErrorDesc();
				else
					errorMsg = alsError.getErrorDesc();
			//}
		}					
		return errorMsg;
	} 
	
}
