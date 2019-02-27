/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.report.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(GenerateReport.class);
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
	public CCCReport getFinalReportData(String idseq, ALSData alsData, List<String> selForms, Boolean checkUom,
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
		List<CCCForm> formsList = new ArrayList<CCCForm>();
		CCCForm form = new CCCForm();
		String formOid = null;
		int totalQuestCount = 0;
		int totalFormsCongruent = 0;


		List<CCCQuestion> questionsList = new ArrayList<CCCQuestion>();
		List<NrdsCde> nrdsCdeList = new ArrayList<NrdsCde>();
		List<StandardCrfCde> standardCrfCdeList = new ArrayList<StandardCrfCde>();
		List<NrdsCde> missingNrdsCdesList = new ArrayList<NrdsCde>();
		List<StandardCrfCde> missingStdCrfCdeList = new ArrayList<StandardCrfCde>();
		Map<String, ALSDataDictionaryEntry> ddeMap = alsData.getDataDictionaryEntries();
		for (ALSField alsField : alsData.getFields()) {
			Boolean cdeServiceCall = true;
			if (selForms.contains(alsField.getFormOid())) {
				if (formOid != null) {
					if (!formOid.equals(alsField.getFormOid())) {
							if (questionsList.isEmpty()) {
								form.setCongruencyStatus(congStatus_congruent); 
							} else {
								form.setQuestions(questionsList); 
								form = setFormCongruencyStatus(form);
							}
							form.setRaveFormOid(formOid);
							form.setCountTotalQuestions(totalQuestCount);
							formsList.add(form);
							totalQuestCount = 0;
							formOid = alsField.getFormOid();
							form = new CCCForm();
							questionsList = new ArrayList<CCCQuestion>();
					} 
				} else {
					formOid = alsField.getFormOid();
				}
					CCCQuestion question = new CCCQuestion();
					totalQuestCount++;
					question.setFieldOrder(alsField.getOrdinal());
					question.setRaveFormOId(alsField.getFormOid());
					String draftFieldName = alsField.getDraftFieldName();
					if (draftFieldName!=null) {
						if (draftFieldName.indexOf(publicid_prefix) > -1 && draftFieldName.indexOf(version_prefix) > -1) {
							question = assignCdeIdVersionToQuestion (question, draftFieldName);
							if (!NumberUtils.isCreatable(question.getCdePublicId()) || !NumberUtils.isCreatable(question.getCdeVersion()))
								cdeServiceCall = false;
							question = buildCodedData(alsField, question, ddeMap);
							question = setRaveFields (alsField, question);
							Map<String, String> parseValidationError = pickFieldErrors(alsField, alsData.getCccError().getAlsErrors());
							question = setParseErrorToQuestion (question, parseValidationError);
							CdeDetails cdeDetails = null;
							logger.debug("cdeServiceCall: " + cdeServiceCall);
							if (cdeServiceCall) {
								try {
								// Service Call to retrieve CDEDetails
								cdeDetails = CdeService.retrieveDataElement(question.getCdePublicId(), question.getCdeVersion());
								} catch (Exception e) {
									//FIXME error handling
									e.printStackTrace();
									continue;
									
								}
								// Service Call to validate the CDEDetails against the ALSField & Question objects
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

							if (question.getQuestionCongruencyStatus() != null) {
								questionsList.add(question);
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
		}
		form.setCountTotalQuestions(totalQuestCount);
		form.setRaveFormOid(formOid);
		if (!questionsList.isEmpty()) {
			form.setQuestions(questionsList);
			form = setFormCongruencyStatus(form);
		} else {
			form.setCongruencyStatus(congStatus_congruent);
		}
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
		
		for (CCCForm tempForm : formsList) {
			if (tempForm.getCongruencyStatus()!=null) {
				if (congStatus_congruent.equals(tempForm.getCongruencyStatus())) {
					totalFormsCongruent++;
				}
			}
		}
		
		cccReport.setTotalFormsCong(totalFormsCongruent);
		cccReport.setMissingNrdsCdeList(missingNrdsCdesList);
		cccReport.setCountNrdsMissing(missingNrdsCdesList.size());
		cccReport.setNrdsCdeList(nrdsCdeList);
		cccReport.setMissingStandardCrfCdeList(missingStdCrfCdeList);		
		cccReport = computeFormsAndQuestionsCount(cccReport);		
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
	

	/**
	 * Transfer the parser errors to the question for the report
	 * @param question
	 * @param parseValidationError
	 * @return CCCQuestion
	 */
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
	

	
	
	/**
	 * Split the CDE public ID and Version, and assign them to the question  
	 * @param question
	 * @param draftFieldName
	 * @return CCCQuestion
	 */
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
	 * List of Standard CRF CDEs & NRDS CDEs
	 * @param String cdePublicId
	 * @param String cdeVersion
	 * @return CRF data for the given CDE
	 * 
	 */
	protected static CdeStdCrfData fetchCdeStandardCrfData(String cdePublicId, String cdeVersion) {
		CdeStdCrfData cdeCrfData = null;
		if (NumberUtils.isCreatable(cdePublicId) && NumberUtils.isCreatable(cdeVersion)) {
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
	 * Populate an NRDS CDE
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
		// This class GenerateReport is not used anymore and is being kept for educational purposes
		// but the below change to field order was done to avoid any compilation errors.
		nrds.setRaveFieldOrder(question.getFieldOrder());
		nrds.setResult(question.getQuestionCongruencyStatus());
		nrds.setMessage(question.getMessage());
		return nrds;
	}
	
	
	/**
	 * Populate a missing NRDS CDE from a NRDS 
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
	 * Populate a Standard CRF CDE
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
	 * Populate a missing Standard CRF CDE
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
	 * Calculate and set counts for different parameters on the summary page
	 * @param report
	 * @return CCCReport
	 */
	protected static CCCReport computeFormsAndQuestionsCount (CCCReport report) {
		int stdManMissingCount = 0;
		int stdOptMissingCount = 0;
		int stdCondMissingCount = 0;
		int countQuestChecked = 0;
		int countQuestCongruent = 0;
		int countQuestWarn = 0;
		int countQuestError = 0;
		int manCrfCong = 0;
		int manCrfWarn = 0;
		int manCrfErr = 0;
		int optCrfCong = 0;
		int optCrfWarn = 0;
		int optCrfErr = 0;
		int condCrfCong = 0;
		int condCrfWarn = 0;
		int condCrfErr = 0;
		
		for (StandardCrfCde cde : report.getMissingStandardCrfCdeList()) {
			if (mandatory_crf.equals(cde.getStdTemplateType())) 
				stdManMissingCount++;
			else if (conditional_crf.equals(cde.getStdTemplateType()))
				stdCondMissingCount++;
			else if (optional_crf.equals(cde.getStdTemplateType()))
				stdOptMissingCount++;
		}
		for (CCCForm tempForm : report.getCccForms()) {
			countQuestChecked = countQuestChecked + tempForm.getQuestions().size();
			for (CCCQuestion tempQuestion : tempForm.getQuestions()) {
				if (tempQuestion.getQuestionCongruencyStatus()!=null) {
					if (congStatus_congruent.equals(tempQuestion.getQuestionCongruencyStatus())) {
						countQuestCongruent++;
						if (tempQuestion.getNciCategory()!=null) {
							if (mandatory_crf.equals(tempQuestion.getNciCategory())) {
								manCrfCong++;	
							} else if (conditional_crf.equals(tempQuestion.getNciCategory())) {
								condCrfCong++;
							} else if (optional_crf.equals(tempQuestion.getNciCategory())) {
								optCrfCong++;
							}
						}
					} else if (congStatus_warn.equals(tempQuestion.getQuestionCongruencyStatus())) {
						countQuestWarn++;
						if (tempQuestion.getNciCategory()!=null) {
							if (mandatory_crf.equals(tempQuestion.getNciCategory())) {
								manCrfWarn++;	
							} else if (conditional_crf.equals(tempQuestion.getNciCategory())) {
								condCrfWarn++;
							} else if (optional_crf.equals(tempQuestion.getNciCategory())) {
								optCrfWarn++;
							}
						}						
					} else if (congStatus_errors.equals(tempQuestion.getQuestionCongruencyStatus())) {
						countQuestError++;
						if (tempQuestion.getNciCategory()!=null) {
							if (mandatory_crf.equals(tempQuestion.getNciCategory())) {
								manCrfErr++;	
							} else if (conditional_crf.equals(tempQuestion.getNciCategory())) {
								condCrfErr++;
							} else if (optional_crf.equals(tempQuestion.getNciCategory())) {
								optCrfErr++;
							}
						}						
					}					
				}
			}
		}
		report.setCountQuestionsChecked(countQuestChecked);
		report.setCountCongruentQuestions(countQuestCongruent);
		report.setCountQuestionsWithWarnings(countQuestWarn);
		report.setCountQuestionsWithErrors(countQuestError);
		report.setCountManCrfMissing(stdManMissingCount);
		report.setCountOptCrfMissing(stdOptMissingCount);
		report.setCountCondCrfMissing(stdCondMissingCount);
		report.setCountManCrfCongruent(manCrfCong);
		report.setCountManCrfwWithWarnings(manCrfWarn);
		report.setCountManCrfWithErrors(manCrfErr);
		report.setCountCondCrfCongruent(condCrfCong);
		report.setCountCondCrfwWithWarnings(condCrfWarn);
		report.setCountCondCrfWithErrors(condCrfErr);
		report.setCountOptCrfCongruent(optCrfCong);
		report.setCountOptCrfwWithWarnings(optCrfWarn);
		report.setCountOptCrfWithErrors(optCrfErr);
		return report;
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

	
	/**
	 *  Iterate through the Questions in the form to check for their Congruency statuses.
	 *  The form takes the highest status occurring in any of the questions in this order
	 *  Congruent being the lowest, Errors being the highest and
	 *   Warnings if only warnings are present. 
	 * @param form
	 * @return CCCForm
	 */
	protected static CCCForm setFormCongruencyStatus (CCCForm form) {
		for (CCCQuestion question : form.getQuestions()) {
			if (question.getQuestionCongruencyStatus()!=null) {
				if (congStatus_warn.equals(question.getQuestionCongruencyStatus())) {
					if (form.getCongruencyStatus() == null) {
							form.setCongruencyStatus(congStatus_warn);
						}
				} else if (congStatus_errors.equals(question.getQuestionCongruencyStatus())) {
					form.setCongruencyStatus(congStatus_errors);
					break;
				}
			}
		}
		// If none of the questions have errors/warnings, set to 'Congruent'
		if (form.getCongruencyStatus() == null)
			form.setCongruencyStatus(congStatus_congruent);
		return form;		
	}
	
	
	/**
	 * Retrieving the RAVE fields and data formats from ALS file to be set in the question object
	 * @param alsField
	 * @param question
	 * @return CCCQuestion
	 */
	protected static CCCQuestion setRaveFields (ALSField alsField, CCCQuestion question) {
		String raveUOM = null;
		if (alsField.getFixedUnit() != null)
			raveUOM = alsField.getFixedUnit();
		else if (alsField.getUnitDictionaryName() != null)
			raveUOM = alsField.getUnitDictionaryName();
		question.setRaveUOM(raveUOM);
		question.setRaveLength(alsField.getFixedUnit());
		question.setRaveDisplayFormat(alsField.getDataFormat());
		// TODO This will need to be checked against a mapping between caDSR datatype to Rave 
		// datatypes that gets applied during the ObjectCart Import.
		// Customer will provide a table for the Checker to use to 
		//compare Rave type with caDSR type to check validity.
		question.setRaveFieldDataType(alsField.getControlType());
		question.setRaveFieldLabel(alsField.getPreText());
		question.setCdePermitQuestionTextChoices("");
		question.setRaveControlType(alsField.getControlType());		
		return question;
	}
	
	
	/**
	 * Iterate through the Coded Data and User data string for the PVs and set them into the question object 
	 * @param alsField
	 * @param question
	 * @param ddeMap
	 * @return CCCQuestion
	 */
	protected static CCCQuestion buildCodedData(ALSField alsField, CCCQuestion question, Map<String, ALSDataDictionaryEntry> ddeMap) {
		for (String key : ddeMap.keySet()) {
			if (key.equals(alsField.getDataDictionaryName())) {
				question.setRaveCodedData(ddeMap.get(key).getCodedData());
				question.setRaveUserString(ddeMap.get(key).getUserDataString());
			}
		}
		
		return question;
	}
	

}
