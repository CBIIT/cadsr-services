/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.report.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.CategoryCde;
import gov.nih.nci.cadsr.data.CategoryNrds;
import gov.nih.nci.cadsr.data.CdeStdCrfData;
import gov.nih.nci.cadsr.data.NrdsCde;
import gov.nih.nci.cadsr.data.StandardCrfCde;
import gov.nih.nci.cadsr.report.ReportOutput;
import gov.nih.nci.cadsr.service.CdeService;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;
import gov.nih.nci.cadsr.service.validator.ValidatorService;

public class GenerateReport implements ReportOutput {

	private static final Logger logger = Logger.getLogger(GenerateReport.class);
	private static String congStatus_errors = "ERRORS";
	private static String congStatus_warn = "WARNINGS";
	private static String parse_errors_error = "ERROR";
	private static String parse_errors_warn = "WARNING";	
	private static String congStatus_congruent = "CONGRUENT";
	private static String nrds_cde = "NRDS";
	private static String mandatory_crf = "Mandatory";
	private static String optional_crf = "Optional";
	private static String conditional_crf = "Conditional";
	private static String publicid_prefix = "PID";
	private static String version_prefix = "_V";
	private static List<CategoryCde> categoryCdeList;
	private static List<CategoryNrds> categoryNrdsList;	
	static{
		categoryCdeList = retrieveCdeCrfData();
		categoryNrdsList = retrieveNrdsData();
	}

	/**
	 * @param  alsData not null
	 * @param  selForms not null
	 * @return Populates the output object for the report after initial
	 *         validation and parsing of data
	 */
	public CCCReport getFinalReportData(ALSData alsData, List<String> selForms, Boolean checkUom,
			Boolean checkStdCrfCde, Boolean displayExceptionDetails) throws NullPointerException {
		logger.info("getFinalReportData selected forms: " + selForms);
		logger.info("getFinalReportData selected alsData: " + alsData.getFileName());
		
		CCCReport cccReport = new CCCReport();
		cccReport.setReportOwner(alsData.getReportOwner());
		cccReport.setFileName(alsData.getFileName());
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
		List<NrdsCde> nrdsCdeList = new ArrayList<NrdsCde>();
		List<StandardCrfCde> standardCrfCdeList = new ArrayList<StandardCrfCde>();
		List<NrdsCde> missingNrdsCdesList = new ArrayList<NrdsCde>();
		List<StandardCrfCde> missingStdCrfCdeList = new ArrayList<StandardCrfCde>();
		Map<String, ALSDataDictionaryEntry> ddeMap = alsData.getDataDictionaryEntries();
		for (ALSField alsField : alsData.getFields()) {
			Boolean cdeServiceCall = true;
			if (selForms.contains(alsField.getFormOid())) {
				if (formName.equals(""))
					formName = alsField.getFormOid();
					if (!formName.equals(alsField.getFormOid())) {
						if (!questionsList.isEmpty())
							form.setQuestions(questionsList);
						else
							form.setCongruencyStatus(congStatus_congruent);
						form.setRaveFormOid(formName);
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
					question.setRaveFormOId(alsField.getFormOid());
					String draftFieldName = alsField.getDraftFieldName();
					if (draftFieldName.indexOf(publicid_prefix) > -1 && draftFieldName.indexOf(version_prefix) > -1) {
						question = assignCdeIdVersionToQuestion (question, draftFieldName);
						if (!NumberUtils.isNumber(question.getCdePublicId()) || !NumberUtils.isNumber(question.getCdeVersion()))
							cdeServiceCall = false;

						question.setRaveFieldLabel(alsField.getPreText());
						question.setCdePermitQuestionTextChoices("");
						question.setRaveControlType(alsField.getControlType());

						for (String key : ddeMap.keySet()) {
							if (key.equals(alsField.getDataDictionaryName())) {
								question.setRaveCodedData(ddeMap.get(key).getCodedData());
								question.setRaveUserString(ddeMap.get(key).getUserDataString());
							}
						}

						String raveUOM = null;
						if (alsField.getFixedUnit() != null)
							raveUOM = alsField.getFixedUnit();
						else if (alsField.getUnitDictionaryName() != null)
							raveUOM = alsField.getUnitDictionaryName();
						question.setRaveUOM(raveUOM);
						question.setRaveLength(alsField.getFixedUnit());
						question.setRaveDisplayFormat(alsField.getDataFormat());
						question.setRaveFieldDataType(alsField.getDataFormat());
						Map<String, String> parseValidationError = pickFieldErrors(alsField, alsData.getCccError().getAlsErrors());
						question = setParseErrorToQuestion (question, parseValidationError);
						CdeDetails cdeDetails = null;
						logger.debug("cdeServiceCall: " + cdeServiceCall);
						if (cdeServiceCall) {
								try {
									// Service Call to retrieve CDEDetails
									cdeDetails = CdeService.retrieveDataElement(question.getCdePublicId(),
											question.getCdeVersion());
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									//FIXME error handling
									continue;
								}
								// Service Call to validate the CDEDetails
								// against the ALSField & Question objects
								question = ValidatorService.validate(alsField, question, cdeDetails);							
						}
						// from a static table of NCI standard CRFs
						CdeStdCrfData cdeCrfData = fetchCdeStandardCrfData(question.getCdePublicId(), question.getCdeVersion());
						if (cdeCrfData!=null)
							question.setNciCategory(cdeCrfData.getNciCategory());						
						if (cdeCrfData!=null && cdeDetails.getDataElement()!=null)  {
						if (nrds_cde.equalsIgnoreCase(question.getNciCategory())) {
							nrdsCdeList.add(buildNrdsCde(question,
									cdeDetails.getDataElement().getDataElementDetails().getLongName()));
							}
						else if ((mandatory_crf.equalsIgnoreCase(question.getNciCategory()))
								|| (optional_crf.equalsIgnoreCase(question.getNciCategory()))
								|| (conditional_crf.equalsIgnoreCase(question.getNciCategory()))) {
								standardCrfCdeList.add(buildCrfCde(cdeCrfData, cdeDetails.getDataElement().getDataElementDetails().getLongName())); 
							}
						}
						if (question.getQuestionCongruencyStatus() == null
								|| question.getQuestionCongruencyStatus().equalsIgnoreCase("")) {
							if (form.getCongruencyStatus() != null
									&& (!form.getCongruencyStatus().equals(congStatus_errors))) {
								form.setCongruencyStatus(congStatus_congruent);
							}
							// questionsList.add(question);
						} else {
							questionsList.add(question);
							if (form.getCongruencyStatus() != null) {
								if (!form.getCongruencyStatus().equals(congStatus_errors)) {
									if (question.getQuestionCongruencyStatus() != null && question
											.getQuestionCongruencyStatus().equalsIgnoreCase(congStatus_errors))
										form.setCongruencyStatus(congStatus_errors);
									else if (question.getQuestionCongruencyStatus() != null
											&& question.getQuestionCongruencyStatus().equals(congStatus_warn))
										form.setCongruencyStatus(congStatus_warn);
								} else
									form.setCongruencyStatus(question.getQuestionCongruencyStatus());
							} else
								form.setCongruencyStatus(question.getQuestionCongruencyStatus());
						}
					} else {
						question.setRaveFieldLabel(alsField.getPreText());
						question.setQuestionCongruencyStatus(congStatus_warn);
						Map<String, String> parseValidationError = pickFieldErrors(alsField, alsData.getCccError().getAlsErrors());
						question = setParseErrorToQuestion (question, parseValidationError);
						questionsList.add(question);
					}
			}
		}

		if (questionsList.isEmpty())
			form.setCongruencyStatus(congStatus_congruent);
		else
			form.setQuestions(questionsList);
		form.setCountTotalQuestions(totalQuestCount);		
		form.setRaveFormOid(formName);
		if (!form.getQuestions().isEmpty())
			formsList.add(form);
		cccReport.setCccForms(formsList);
		logger.info("getFinalReportData created formsList size: " + formsList.size());
		if (formsList.size() == 0) {
			throw new RuntimeException("!!!!!!!!! GenerateReport.getFinalReportData created report with no FORMS!!!");
		}
		// categoryNrdsList and categoryCdeList will be reduced to those CDEs that are missing
		List<CategoryCde> missingCdeStd = createMissingCategoryCdeList(standardCrfCdeList);
		List<CategoryNrds> missingCategoryNrdsList = createMissingNrdsCategoryNrdsList(nrdsCdeList);
		
		for (CategoryNrds cde : missingCategoryNrdsList) {
			missingNrdsCdesList.add(buildMissingNrdsCde(cde));
		}
		for (CategoryCde cde : missingCdeStd) {
			missingStdCrfCdeList.add(buildMissingStdCrfCde(cde));
		}		
		
		cccReport.setMissingNrdsCdeList(missingNrdsCdesList);
		cccReport.setNrdsCdeList(nrdsCdeList);
		cccReport.setMissingStandardCrfCdeList(missingStdCrfCdeList);
		cccReport = addFormNamestoForms(cccReport, alsData.getForms());		
		return cccReport;
	}
	
	protected ALSError getErrorInstance() {
		ALSError alsError = new ALSError();
		return alsError;
	}

	/**
	 * @param ALSField
	 * @param List<ALSError>
	 * @return All error messages from parser validation returned as a single
	 *         concatenated string
	 * 
	 */
	protected static Map<String, String> pickFieldErrors(ALSField field, List<ALSError> errors) {
		String errorMsg = null;
		String severity = null;
		Map<String, String> parseErrorMap = new HashMap<String, String>(); 
		List<ALSError> fieldErrors = new ArrayList<ALSError>();
		for (ALSError alsError : errors) {
			if (alsError.getFieldOid() != null) {
				if (field.getFieldOid().equalsIgnoreCase(alsError.getFieldOid())
						&& field.getFormOid().equalsIgnoreCase(alsError.getFormOid())) {
					fieldErrors.add(alsError);
				}
			}
		}
		for (ALSError alsError : fieldErrors) {
			if (errorMsg != null)
				errorMsg = errorMsg + alsError.getErrorDesc();
			else
				errorMsg = alsError.getErrorDesc();
			if (severity!=null) {
				if (parse_errors_warn.equals(severity) && parse_errors_error.equals(alsError.getErrorSeverity())) { 
					severity = congStatus_errors; 
				} 
			} else {
				if (parse_errors_warn.equals(alsError.getErrorSeverity()))
						severity = congStatus_warn;
			}
				
		}
		if (errorMsg!=null && severity!=null)
			parseErrorMap.put(severity, errorMsg);
		return parseErrorMap;
	}
	
	
	protected static CCCQuestion setParseErrorToQuestion (CCCQuestion question, Map<String, String> parseValidationError) {
		if (parseValidationError.get(parse_errors_error) != null) {
			question.setMessage(parseValidationError.get(parse_errors_error));
			question.setQuestionCongruencyStatus(congStatus_errors);
		} else if (parseValidationError.get(parse_errors_warn) != null) {
			question.setMessage(parseValidationError.get(parse_errors_warn));
			question.setQuestionCongruencyStatus(congStatus_warn);							
		}		
		return question;
	}
	

	
	
	protected static CCCQuestion assignCdeIdVersionToQuestion (CCCQuestion question, String draftFieldName) {
		String idVersion = draftFieldName.substring(draftFieldName.indexOf(publicid_prefix),
				draftFieldName.length());
		idVersion = draftFieldName.substring(draftFieldName.indexOf(publicid_prefix), draftFieldName.length());
		String id = idVersion.substring(3, idVersion.indexOf("_"));
		String version = (idVersion.substring(idVersion.indexOf(version_prefix) + 2, idVersion.length()));
		id = id.trim();
		String[] versionTokens = version.split("\\_");
		version = versionTokens[0] + "." + versionTokens[1];
		question.setCdePublicId(id.trim());
		question.setCdeVersion(version);		
		return question;
	}
	
	
	
	/**
	 * @param String cdePublicId
	 * @param String cdeVersion
	 * @return CRF data for the given CDE
	 * 
	 */
	protected static CdeStdCrfData fetchCdeStandardCrfData(String cdePublicId, String cdeVersion) {
		CdeStdCrfData cdeCrfData = null;
		if (NumberUtils.isNumber(cdePublicId) && NumberUtils.isNumber(cdeVersion)) {
			for (CategoryCde cde : categoryCdeList) {
				if (cde.getCdeId() == Float.valueOf(cdePublicId) && cde.getDeVersion() == Float.valueOf(cdeVersion)) {
					cdeCrfData = new CdeStdCrfData();
					cdeCrfData.setCdePublicId(cdePublicId);
					cdeCrfData.setCdeVersion(cdeVersion);
					cdeCrfData.setCrfIdVersion(cde.getFormId()); // Mock data
					cdeCrfData.setCrfName(cde.getFormName()); // Mock data
					cdeCrfData.setNciCategory(cde.getModuleType());
				}
			}
			if (cdeCrfData == null) {
				for (CategoryNrds cde : categoryNrdsList) {
					if (cde.getCdeId() == Float.valueOf(cdePublicId) && cde.getDeVersion() == Float.valueOf(cdeVersion)) {
						cdeCrfData = new CdeStdCrfData();
						cdeCrfData.setCdePublicId(cdePublicId);
						cdeCrfData.setCdeVersion(cdeVersion);
						cdeCrfData.setNciCategory(nrds_cde);
					}
				}
			}
		}
		return cdeCrfData;
	}

	/**
	 * @param CCCQuestion
	 * @param string
	 * @return Return NrdsCde for a question
	 * 
	 */
	protected static NrdsCde buildNrdsCde(CCCQuestion question, String cdeName) {
		NrdsCde nrds = new NrdsCde();
		nrds.setRaveFormOid(question.getRaveFormOId());
		nrds.setCdeIdVersion(question.getCdePublicId() + "v" + question.getCdeVersion());
		nrds.setCdeName(cdeName);
		nrds.setRaveFieldLabel(question.getRaveFieldLabel());
		nrds.setRaveFieldOrder(Integer.parseInt(question.getFieldOrder()));
		nrds.setResult(question.getQuestionCongruencyStatus());
		nrds.setMessage(question.getMessage());
		return nrds;
	}
	
	
	/**
	 * @param nrdsDb
	 * @return Return NrdsCde for a CDE returned from the static list of NRDS CDEs
	 * 
	 */
	protected static NrdsCde buildMissingNrdsCde(CategoryNrds nrdsDb) {
		NrdsCde nrds = new NrdsCde();
		nrds.setCdeIdVersion(nrdsDb.getCdeId()+"v"+nrdsDb.getDeVersion());
		nrds.setCdeName(nrdsDb.getDeName());
		return nrds;
	}	

	/**
	 * @param cdeCrfData
	 * @param cdeName
	 * @return Return NrdsCde for a question
	 * 
	 */
	protected static StandardCrfCde buildCrfCde(CdeStdCrfData cdeCrfData, String cdeName) {
		StandardCrfCde stdCrdCde = new StandardCrfCde();
		stdCrdCde.setCdeIdVersion(cdeCrfData.getCdePublicId() + "v" + cdeCrfData.getCdeVersion());
		stdCrdCde.setCdeName(cdeName);
		stdCrdCde.setIdVersion(cdeCrfData.getCrfIdVersion());
		stdCrdCde.setTemplateName(cdeCrfData.getCrfName());
		stdCrdCde.setStdTemplateType(cdeCrfData.getNciCategory());
		return stdCrdCde;
	}
	
	
	/**
	 * @param stdCrfCdeDb
	 * @return Return StandardCrfCde for a CDE returned from the static list of Standard CRF CDEs
	 * 
	 */
	protected static StandardCrfCde buildMissingStdCrfCde(CategoryCde stdCrfCdeDb) {
		StandardCrfCde stdCrdCde = new StandardCrfCde();
		stdCrdCde.setCdeIdVersion(stdCrfCdeDb.getCdeId()+"v"+stdCrfCdeDb.getDeVersion());
		stdCrdCde.setCdeName(stdCrfCdeDb.getDeName());
		stdCrdCde.setIdVersion(stdCrfCdeDb.getFormId());
		stdCrdCde.setTemplateName(stdCrfCdeDb.getFormName());
		stdCrdCde.setStdTemplateType(stdCrfCdeDb.getModuleType());
		return stdCrdCde;
	}	
	

	/**
	 * FIXME remove refineMissingCdesList method.
	 * It creates IllegalStateException because of changing an iterator which is in use.
	 * It is not removed for educational purposes.
	 * 
	 * @param nrdsCdeList
	 * @param standardCrfCdeList
	 * @return Reduces the NRDS and Standard CRF CDE lists to the CDEs that are missing in the RAVE ALS input file
	 * 
	 */	
	protected static void refineMissingCdesList (List<NrdsCde> nrdsCdeList, List<StandardCrfCde> standardCrfCdeList) {
		for (Iterator<CategoryNrds> iterator = categoryNrdsList.iterator(); iterator.hasNext();) {
			CategoryNrds nrds = iterator.next();
			for (NrdsCde nrdsCdeStatic : nrdsCdeList) {
			    if (nrdsCdeStatic.getCdeIdVersion().equals(nrds.getCdeId()+"v"+nrds.getDeVersion())) {
			        // Remove the current element from the iterator and the list.
			    	if (iterator!=null)
			    		iterator.remove();
			    }
		    }
		}
		for (Iterator<CategoryCde> iterator = categoryCdeList.iterator(); iterator.hasNext();) {
			CategoryCde cde = iterator.next();
			for (StandardCrfCde stdCrfCdeStatic : standardCrfCdeList) {
				if (stdCrfCdeStatic.getCdeIdVersion().equals(cde.getCdeId()+"v"+cde.getDeVersion())) {
			        // Remove the current element from the iterator and the list.
					if (iterator!=null)					
						iterator.remove();
				}
			}
		}
	}
	
	protected static List<CategoryNrds> createMissingNrdsCategoryNrdsList(List<NrdsCde> nrdsCdeList) {
		List<CategoryNrds> missing = new ArrayList<>();
		for (CategoryNrds nrds : categoryNrdsList ) {
			missing.add(nrds);
			for (NrdsCde nrdsCdeStatic : nrdsCdeList) {
			    if (nrdsCdeStatic.getCdeIdVersion().equals(nrds.getCdeId()+"v"+nrds.getDeVersion())) {
			        // Remove the current element from the list.
			    	missing.remove(nrds);
			    	break;
			    }
		    }
		}		
		return missing;
	}
	
	protected static List<CategoryCde> createMissingCategoryCdeList(List<StandardCrfCde> standardCrfCdeList) {
		List<CategoryCde> missing = new ArrayList<>();
		for (CategoryCde categoryCde : categoryCdeList ) {
			missing.add(categoryCde);
			for (StandardCrfCde curr : standardCrfCdeList) {
			    if (curr.getCdeIdVersion().equals(categoryCde.getCdeId()+"v"+categoryCde.getDeVersion())) {
			        // Remove the current element from the list.
			    	missing.remove(categoryCde);
			    	break;
			    }
		    }
		}		
		return missing;
	}	
		
	/**
	 * @return List of Standard CRF CDEs
	 * 
	 */	
	protected static List<CategoryCde> retrieveCdeCrfData () {
		RestTemplate restTemplate = new RestTemplate();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("boot.properties");
		Properties properties = new Properties();
		try {
			properties.load(input);
		} catch (IOException e) {
			logger.error("failed to load boot properties" + e);
		    /**
		     * If properties not found throws runtime exception
		     */
			e.printStackTrace();
			throw new RuntimeException (e);
		}
		ResponseEntity<List<CategoryCde>> categoryCdeResponse =
		        restTemplate.exchange(properties.getProperty("RETRIEVE_STD_CDECRF_URL"),
		                    HttpMethod.GET, null, new ParameterizedTypeReference<List<CategoryCde>>() {
		            });
		List<CategoryCde> categoryCdeList = categoryCdeResponse.getBody();		
		return categoryCdeList;
	}
	
	/**
	 * @return List of NRDS CDEs
	 * 
	 */		
	protected static List<CategoryNrds> retrieveNrdsData () {
		RestTemplate restTemplate = new RestTemplate();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("boot.properties");
		Properties properties = new Properties();
		try {
			properties.load(input);
		} catch (IOException e) {
			logger.error("failed to load boot properties" + e);
		    /**
		     * If properties not found throws runtime exception
		     */
			e.printStackTrace();
			throw new RuntimeException (e);
		}
		ResponseEntity<List<CategoryNrds>> categoryNrdsResponse =
		        restTemplate.exchange(properties.getProperty("RETRIEVE_NRDS_URL"),
		                    HttpMethod.GET, null, new ParameterizedTypeReference<List<CategoryNrds>>() {
		            });
		List<CategoryNrds> categoryNrdsList = categoryNrdsResponse.getBody();
		return categoryNrdsList;
	}	
	
	/**
	 * Finds the form names by Form OID & adds them, for the forms in the report 
	 * @param cccReport
	 * @param alsForms
	 * @return CCCReport
	 * 
	 */	
	protected static CCCReport addFormNamestoForms (CCCReport cccReport, List<ALSForm> alsForms) {
		for (CCCForm completedForm : cccReport.getCccForms()) {
			for (ALSForm alsForm: alsForms) {
				if (completedForm.getRaveFormOid().equalsIgnoreCase(alsForm.getFormOid())) {
					completedForm.setFormName(alsForm.getDraftFormName());
				}
			}
		}		
		return cccReport;
	}	
	

}
