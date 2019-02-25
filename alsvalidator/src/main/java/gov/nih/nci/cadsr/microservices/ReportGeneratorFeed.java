/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
import gov.nih.nci.cadsr.report.CdeFormInfo;
import gov.nih.nci.cadsr.report.ReportOutput;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.DataElementDetails;
import gov.nih.nci.cadsr.service.validator.ValidatorService;
@Service
public class ReportGeneratorFeed implements ReportOutput {
//former ReportGenerator
	private static final Logger logger = LoggerFactory.getLogger(ReportGeneratorFeed.class);
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
	private static String noCdeMsg = "No CDE provided : {%s}.";
	private static List<NrdsCde> nrdsCdeList;
	private static List<StandardCrfCde> standardCrfCdeList;
	private static int totalNrdsCong;
	private static int totalNrdsWarn;
	private static int totalNrdsError;	
	
	static{
		categoryCdeList = retrieveCdeCrfData();
		categoryNrdsList = retrieveNrdsData();
	}
	
	@Autowired
	private CdeServiceDetails cdeServiceDetails;
	
	public CdeServiceDetails getCdeServiceDetails() {
		return cdeServiceDetails;
	}

	public void setCdeServiceDetails(CdeServiceDetails cdeServiceDetails) {
		this.cdeServiceDetails = cdeServiceDetails;
	}
	//we will keep here a status of requests: sessionID-form processed number
	private ConcurrentMap<String, String> requestStatusMap = new ConcurrentHashMap<>();
	
	public String feedRequestStatus(String sessionId) {
		String feedNumber = requestStatusMap.get(sessionId);
		if (feedNumber == null) feedNumber = "0";
		return feedNumber;
	}
	
	
	/**
	 * @param alsData - Complete data from the RAVE ALS (XLSX) file uploaded
	 * @param selForms - List of Forms selected by the user for performing congruency checking
	 * @return Map<String, List<CdeFormInfo>>
	 */
	public Map<String, List<CdeFormInfo>> buildFormCdeList(ALSData alsData, List<String> selForms) {
		String formOid = null;
		Map<String, List<CdeFormInfo>> cdeFormMap = new HashMap<>();
		List<CdeFormInfo> cdeFormInfoList = new ArrayList<>();
		for (ALSField alsField : alsData.getFields()) {
			if (selForms.contains(alsField.getFormOid())) {
				if (formOid != null) {
					if (!formOid.equals(alsField.getFormOid())) {
						cdeFormMap.put(formOid, cdeFormInfoList);
						cdeFormInfoList = new ArrayList<>();
						formOid = alsField.getFormOid();
					} 
				} else {
					formOid = alsField.getFormOid();
				}
				CCCQuestion question = new CCCQuestion();
				question.setFieldOrder(alsField.getOrdinal());
				question.setRaveFormOId(alsField.getFormOid());
				String draftFieldName = alsField.getDraftFieldName();
				
				//Skipping rows that do not have CDEs/questions to validate i.e., rows that have FORM_OID in FieldOid
				if (!"FORM_OID".equals(alsField.getFieldOid())) { 
					if (draftFieldName!=null) {
						if (draftFieldName.indexOf(publicid_prefix) > -1 && draftFieldName.indexOf(version_prefix) > -1) {
							question = assignCdeIdVersionToQuestion (question, draftFieldName);
							if (!NumberUtils.isNumber(question.getCdePublicId()) || !NumberUtils.isNumber(question.getCdeVersion()))
								continue;//Not CDE data
							//logger.debug("formOid: " + formOid + ", question.getCdePublicId(): " + question.getCdePublicId() + ", question.getCdeVersion(): " + question.getCdeVersion());
							CdeFormInfo cdeFormInfo = new CdeFormInfo(question.getCdePublicId(), question.getCdeVersion());
							cdeFormInfoList.add(cdeFormInfo);
						}
					}
				}
			}
		}
		//add the last form CDE List
		cdeFormMap.put(formOid, cdeFormInfoList);
		
		return cdeFormMap;
	}
	/**
	 * 
	 * @param cdeFormInfoListOrg not null
	 * @return Map<CdeFormInfo, CdeDetails>
	 */
	
	public Map<CdeFormInfo, CdeDetails> execAsync(List<CdeFormInfo> cdeFormInfoListOrg) {
		//long start = System.currentTimeMillis();
		Map<CdeFormInfo, CdeDetails> resMap = new HashMap<>();
		if ((cdeFormInfoListOrg == null) || (cdeFormInfoListOrg.isEmpty())) return resMap;
		
		//We have mentioned that in many ALS forms we have CDEs duplicate, so we do not need to request the same CDE info multiple times
		List<CdeFormInfo> cdeFormInfoList = cdeFormInfoListOrg.stream().distinct().collect(Collectors.toList());
		//System.out.println(".....Removed amount of duplicates: " + (cdeFormInfoListOrg.size() - cdeFormInfoList.size()));
		
		CdeFormInfo curr;
		int step = 7;//we send 7 requests at a time
		int idx;
		List<CompletableFuture<CdeDetails>> arrFuture = new ArrayList<>(cdeFormInfoList.size());
		for (int i = 0; i < cdeFormInfoList.size(); i += step) {// bunch step
			idx = 0;
			//long stepStart = System.currentTimeMillis();
			for (int j = i; ((j < cdeFormInfoList.size()) && (idx < step)); j++, idx++) {
				curr = cdeFormInfoList.get(j);
				if ((StringUtils.isNotBlank(curr.getCdeId())) && (StringUtils.isNotBlank(curr.getVersion()))) {
					arrFuture.add(cdeServiceDetails.retrieveDataElement(curr.getCdeId(), curr.getVersion()));
				}
			}
			//long stepEnd = System.currentTimeMillis();
			//System.out.println("Time to send execAsync in milliseconds # : " + i + " : "+ (stepEnd - stepStart));
			//We wait till all requests are completed
			CompletableFuture.allOf(arrFuture.toArray(new CompletableFuture[arrFuture.size()])).join();
			//System.out.println("Time to joint execAsync in milliseconds step # : " + i + " : "+ (System.currentTimeMillis() - stepEnd));
		}

		List<CdeDetails> foundCdeArr = new ArrayList<>(arrFuture.size());
		CdeDetails cdeDetailsFuture;
		for (CompletableFuture<CdeDetails> future : arrFuture) {
			try {
				cdeDetailsFuture = future.get();
				if (cdeDetailsFuture != null) {
					foundCdeArr.add(cdeDetailsFuture);
				}
			} catch (InterruptedException e) {
				logger.error("execAsync InterruptedException: " + e);
				e.printStackTrace();
			} catch (ExecutionException e) {
				logger.error("execAsync ExecutionException: " + e);
				e.printStackTrace();
			}
		}
		for (CdeFormInfo cdeFormInfo : cdeFormInfoList) {
			boolean notFound = true;
			for (CdeDetails cdeDetails : foundCdeArr) {
				if ((cdeDetails == null) || (cdeDetails.getDataElement() == null)) continue;//we did not find a CDE, and we received an empty one.
				DataElementDetails dataElementDetails = cdeDetails.getDataElement().getDataElementDetails();
				if ((dataElementDetails == null) || (StringUtils.isBlank(dataElementDetails.getId()))
					|| (StringUtils.isBlank(dataElementDetails.getFormattedVersion()))) continue;//DE not found
				CdeFormInfo cdeFormInfoNew = new CdeFormInfo(
					""+dataElementDetails.getPublicId(),
					dataElementDetails.getFormattedVersion());
				if (cdeFormInfoNew.equals(cdeFormInfo)) {
					resMap.put(cdeFormInfo, cdeDetails);
					foundCdeArr.remove(cdeDetails);
					notFound = false;
					break;
				}
			}
			if (notFound) {
				logger.info("CDE not found for: " + cdeFormInfo);
				resMap.put(cdeFormInfo, new CdeDetails());
			}
		}
		//long end = System.currentTimeMillis();
		//System.out.println("Time to complete execAsync in seconds: " + (end - start) / 1000);
		return resMap;
	}
	/**
	 * TODO Consider to add session ID parameter.
	 * 
	 * @param  alsData not null
	 * @param  selForms not null
	 * @return Populates the output object for the report after initial
	 *         validation and parsing of data
	 * @throws Exception 
	 */
	public CCCReport getFinalReportData(String idseq, ALSData alsData, List<String> selForms, Boolean checkUom,
			Boolean checkStdCrfCde, Boolean displayExceptionDetails) {
		logger.info("getFinalReportData selected forms: " + selForms);
		logger.info("getFinalReportData selected alsData: " + alsData.getFileName() + ", session: " + idseq);
		
		String sessionId = idseq;//we keep track of form number processed by session ID
		
		CCCReport cccReport = new CCCReport();
		cccReport.setReportOwner(alsData.getReportOwner());
		cccReport.setFileName(alsData.getFileName());
		cccReport.setReportDate(alsData.getReportDate());
		cccReport.setRaveProtocolName(alsData.getCrfDraft().getProjectName());
		cccReport.setRaveProtocolNumber(alsData.getCrfDraft().getPrimaryFormOid());
		cccReport.setTotalFormsCount(alsData.getForms().size());
		
		// Flag to indicate if user chose to Check Standard CRFs or not; True = Yes
		cccReport.setIsCheckStdCrfCdeChecked(checkStdCrfCde);
		List<CCCForm> formsList = new ArrayList<CCCForm>();
		CCCForm form = new CCCForm();
		String formOid = null;
		int totalQuestCount = 0;
		int totalFormsCongruent = 0;
		int totalQuestWithoutCde = 0;
		int countQuestChecked = 0;
		totalNrdsCong = 0;
		totalNrdsWarn = 0;
		totalNrdsError = 0;		

		nrdsCdeList = new ArrayList<NrdsCde>();
		standardCrfCdeList = new ArrayList<StandardCrfCde>();
		List<CCCQuestion> questionsList = new ArrayList<CCCQuestion>();
		List<CCCQuestion> congQuestionsList = new ArrayList<CCCQuestion>();
		List<NrdsCde> missingNrdsCdesList = new ArrayList<NrdsCde>();
		List<StandardCrfCde> missingStdCrfCdeList = new ArrayList<StandardCrfCde>();
		Map<String, ALSDataDictionaryEntry> ddeMap = alsData.getDataDictionaryEntries();
		Map<String, List<CdeFormInfo>> cdeFormInfoMap = buildFormCdeList(alsData, selForms);
		List<CdeFormInfo> cdeFormInfoList;
		Map<CdeFormInfo, CdeDetails> formCdeDetailsMap = new HashMap<>();
		
		int feedFormNumber = 1;//this is to feed to UI
		
		// Running through the list of CDEs/Questions (Fields sheet) to identify & 
		// assign them into forms that they belong to
		for (ALSField alsField : alsData.getFields()) {
			Boolean cdeServiceCall = true;
			if (selForms.contains(alsField.getFormOid())) {
				if (formOid == null ) {//the first form
					requestStatusMap.put(sessionId, ""+feedFormNumber);
					logger.debug("Current form to feed map, session: " + sessionId + ", form: " + feedFormNumber + ", FormOid:" + alsField.getFormOid());
					formOid = alsField.getFormOid();
					cdeFormInfoList = cdeFormInfoMap.get(formOid);
					formCdeDetailsMap = execAsync(cdeFormInfoList);
				}
				else /* (formOid != null)*/ {
					if (!formOid.equals(alsField.getFormOid())) {//form changed
							if (questionsList.isEmpty()) {
								form.setCongruencyStatus(congStatus_congruent); 
							} else {
								form.setQuestions(questionsList); 
								form = setFormCongruencyStatus(form);
							}
							feedFormNumber++;
							if (StringUtils.isNotBlank(sessionId)) {
								requestStatusMap.put(sessionId, ""+feedFormNumber);
								logger.debug("Current form to feed map, session: " + sessionId + ", form: " + feedFormNumber);
							}
							form.setRaveFormOid(formOid);
							// Total Questions for the form (incl. FORM_OID questions)
							form.setCountTotalQuestions(totalQuestCount);
							// Total number of questions that were checked, after ignoring FORM_OID questions
							form.setTotalQuestionsChecked(countQuestChecked);
							formsList.add(form);
							totalQuestCount = 0;
							countQuestChecked = 0;
							formOid = alsField.getFormOid();
							form = new CCCForm();
							questionsList = new ArrayList<CCCQuestion>();
							cdeFormInfoList = cdeFormInfoMap.get(formOid);//new form
							formCdeDetailsMap = execAsync(cdeFormInfoList);
					} 
				}
				CCCQuestion question = new CCCQuestion();
					totalQuestCount++;
					question.setFieldOrder(alsField.getOrdinal());
					question.setRaveFormOId(alsField.getFormOid());
					String draftFieldName = alsField.getDraftFieldName();
					
					//Skipping rows that do not have CDEs/questions to validate i.e., rows that have FORM_OID in FieldOid
					if (!"FORM_OID".equals(alsField.getFieldOid())) {
						countQuestChecked++;
					if (draftFieldName!=null) {
						// Detecting the presence of CDE public ID in the format PIDxxxxxxx_Vx_x
						if (draftFieldName.indexOf(publicid_prefix) > -1 && draftFieldName.indexOf(version_prefix) > -1) {
							
							// Splitting draftFieldName to extract CDE Public ID and Version
							question = assignCdeIdVersionToQuestion (question, draftFieldName);
							// Avoiding CDE details fetching in case the extracted Public ID and Version are not numbers
							if (!NumberUtils.isNumber(question.getCdePublicId()) || !NumberUtils.isNumber(question.getCdeVersion()))
								cdeServiceCall = false;
							// Building Coded Data (PV list on caDSR) from the ALS Data Dictionary entries for the CDE 
							question = buildCodedData(alsField, question, ddeMap);
							
							// Setting RAVE field values obtained from the RAVE ALS input file
							question = setRaveFields (alsField, question);
							
							// Assign validation errors that occurred during Parsing, to the question message
							Map<String, String> parseValidationError = pickFieldErrors(alsField, alsData.getCccError().getAlsErrors());
							question = setParseErrorToQuestion (question, parseValidationError);
							CdeDetails cdeDetails = null;

							// Flag to indicate CDE service needs to be invoked or not
							if (cdeServiceCall) {
								try {
									//Service Call to retrieve CDE List
									CdeFormInfo cdeToValidate = new CdeFormInfo(question.getCdePublicId(), question.getCdeVersion());
									cdeDetails = formCdeDetailsMap.get(cdeToValidate);
								} catch (Exception e) {
									//FIXME error handling
									e.printStackTrace();
									continue;
								}
								//Service Call to validate the CDEDetails against the ALSField & Question objects
								question = ValidatorService.validate(alsField, question, cdeDetails);
							}
							// from a static table of NCI standard CRFs
							CdeStdCrfData cdeCrfData = fetchCdeStandardCrfData(question.getCdePublicId(), question.getCdeVersion());
							
							// updating the NCI category to the question
							question = updateNciCategory(checkStdCrfCde, cdeCrfData, question, cdeDetails);
							
							if (question.getQuestionCongruencyStatus() != null) {
								if (congStatus_congruent.equals(question.getQuestionCongruencyStatus())) {
									congQuestionsList.add(question);	
								} else {
									questionsList.add(question);
								}										
							}
						} else {
							// increment the counter for Questions that are not associated with a CDE
							// because the draftFieldName value was not of PIDxxxxxx_Vx_x format
							totalQuestWithoutCde++;
							if ("FORM_OID".equalsIgnoreCase(alsField.getFieldOid())) {
								// Extracting form OID from defaultValue, if the Field OID = FORM_OID
								if (alsField.getDefaultValue()!=null) {
									// Form Public ID and Version is expected to be in the same format as CDE public ID [PIDxxxxxx_Vx_x]
									if (alsField.getDefaultValue().indexOf(publicid_prefix) > -1 && alsField.getDefaultValue().indexOf(version_prefix) > -1) {
										form = assignIdVersionToForm(form, alsField.getDefaultValue());
									}
								}
							} else {
								// When CDE public ID & Version are not present
								question.setRaveFieldLabel(alsField.getPreText());
								question.setQuestionCongruencyStatus(congStatus_warn);
								question.setMessage(String.format(noCdeMsg, draftFieldName));
								Map<String, String> parseValidationError = pickFieldErrors(alsField, alsData.getCccError().getAlsErrors());
								question = setParseErrorToQuestion (question, parseValidationError);
								questionsList.add(question);
							}
						}
				}
					} else {
						if (alsField.getDefaultValue()!=null) {
							if (alsField.getDefaultValue().indexOf(publicid_prefix) > -1 && alsField.getDefaultValue().indexOf(version_prefix) > -1) {
								form = assignIdVersionToForm(form, alsField.getDefaultValue());
							}
						}					
				}
			}
		}
		// After all the fields have been processed, the last form needs to be completed and added to the list of forms
		form.setCountTotalQuestions(totalQuestCount);
		form.setTotalQuestionsChecked(countQuestChecked);
		form.setRaveFormOid(formOid);
		if (!questionsList.isEmpty()) {
			form.setQuestions(questionsList);
			form = setFormCongruencyStatus(form);
		} else {
			form.setCongruencyStatus(congStatus_congruent);
		}
		formsList.add(form);
		// Setting the list of forms to the report
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
		if (checkStdCrfCde) {
			for (CategoryCde cde : missingCdeStd) {
				missingStdCrfCdeList.add(buildMissingStdCrfCde(cde));
			}
		}
		int totalCountQuestChecked = 0;
		for (CCCForm tempForm : formsList) {
			totalCountQuestChecked = totalCountQuestChecked + tempForm.getTotalQuestionsChecked();
			if (tempForm.getCongruencyStatus()!=null) {
				if (congStatus_congruent.equals(tempForm.getCongruencyStatus())) {
					totalFormsCongruent++;
				}
			}
		}
		
		cccReport.setTotalFormsCong(totalFormsCongruent);
		cccReport.setCountQuestionsWithoutCde(totalQuestWithoutCde);
		cccReport.setCountNrdsCongruent(totalNrdsCong);
		cccReport.setCountNrdsWithErrors(totalNrdsError);
		cccReport.setCountNrdsWithWarnings(totalNrdsWarn);
		cccReport.setMissingNrdsCdeList(missingNrdsCdesList);
		cccReport.setCountNrdsMissing(missingNrdsCdesList.size());
		cccReport.setNrdsCdeList(nrdsCdeList);
		if (checkStdCrfCde) {		
			cccReport.setMissingStandardCrfCdeList(missingStdCrfCdeList);
		}
		cccReport.setCountCongruentQuestions(congQuestionsList.size());
		cccReport = computeFormsAndQuestionsCount(cccReport);
		cccReport = addFormNamestoForms(cccReport, alsData.getForms());		
		cccReport.setCountQuestionsChecked(totalCountQuestChecked);
		requestStatusMap.remove(sessionId);
		return cccReport;
	}
	
	protected ALSError getErrorInstance() {
		ALSError alsError = new ALSError();
		return alsError;
	}

	
	
	/**
	 * Checks and assigns NCI category [NRDS, Mandatory, Optional, Conditional] to the question, 
	 * with the totals of the questions in each category
	 * @param checkStdCrfCde
	 * @param cdeCrfData
	 * @param question
	 * @param cdeDetails
	 * @return CCCQuestion
	 */
	protected static CCCQuestion updateNciCategory (Boolean checkStdCrfCde, CdeStdCrfData cdeCrfData, CCCQuestion question, CdeDetails cdeDetails) {
		if (cdeCrfData!=null) {
			if (checkStdCrfCde) {
				question.setNciCategory(cdeCrfData.getNciCategory());
			} else {
				if (nrds_cde.equalsIgnoreCase(cdeCrfData.getNciCategory()))
					question.setNciCategory(nrds_cde);
			}
		}
	if (cdeCrfData!=null && cdeDetails.getDataElement()!=null)  {
	if (nrds_cde.equalsIgnoreCase(question.getNciCategory())) {
		nrdsCdeList.add(buildNrdsCde(question,
				cdeDetails.getDataElement().getDataElementDetails().getLongName()));
			if (congStatus_congruent.equals(question.getQuestionCongruencyStatus())) {
				totalNrdsCong++;
			} else if (congStatus_errors.equals(question.getQuestionCongruencyStatus())) {
				totalNrdsError++;
			} else if (congStatus_warn.equals(question.getQuestionCongruencyStatus())) {
				totalNrdsWarn++;
			}
		}
	else if ((mandatory_crf.equalsIgnoreCase(question.getNciCategory()))
			|| (optional_crf.equalsIgnoreCase(question.getNciCategory()))
			|| (conditional_crf.equalsIgnoreCase(question.getNciCategory()))) {
		if (checkStdCrfCde) {
				standardCrfCdeList.add(buildCrfCde(cdeCrfData, cdeDetails.getDataElement().getDataElementDetails().getLongName()));
			} 
		}
	}		
		return question;
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
		if (NumberUtils.isNumber(cdePublicId) && NumberUtils.isNumber(cdeVersion)) {
			String moduleType = null;
			for (CategoryCde cde : categoryCdeList) {
				if (cde.getCdeId() == Float.valueOf(cdePublicId) && cde.getDeVersion() == Float.valueOf(cdeVersion)) {
					cdeCrfData = new CdeStdCrfData();
					cdeCrfData.setCdePublicId(cdePublicId);
					cdeCrfData.setCdeVersion(cdeVersion);
					cdeCrfData.setCrfIdVersion(cde.getFormId()); // Mock data
					cdeCrfData.setCrfName(cde.getFormName()); // Mock data
					if (moduleType == null)
						moduleType = cde.getModuleType();
					else 
						moduleType = moduleType + ", " + cde.getModuleType();
					cdeCrfData.setNciCategory(moduleType);
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
		// TODO : Handle Runtime exceptions with Integer.parseInt
		nrds.setRaveFieldOrder(Integer.parseInt(question.getFieldOrder()));
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
		
		// If the Standard CRF CDEs are not included in congruency checking 
		// based on user's choice then they're excluded from the summary count
		if (report.getIsCheckStdCrfCdeChecked()) {
			for (StandardCrfCde cde : report.getMissingStandardCrfCdeList()) {
				if (mandatory_crf.equals(cde.getStdTemplateType())) 
					stdManMissingCount++;
				else if (conditional_crf.equals(cde.getStdTemplateType()))
					stdCondMissingCount++;
				else if (optional_crf.equals(cde.getStdTemplateType()))
					stdOptMissingCount++;
			}
		}
		
		for (CCCForm tempForm : report.getCccForms()) {
			for (CCCQuestion tempQuestion : tempForm.getQuestions()) {
				if (tempQuestion.getQuestionCongruencyStatus()!=null) {
					if (congStatus_warn.equals(tempQuestion.getQuestionCongruencyStatus())) {
						countQuestWarn++;
						if (report.getIsCheckStdCrfCdeChecked()) {
							if (tempQuestion.getNciCategory()!=null) {
								if (tempQuestion.getNciCategory().indexOf(mandatory_crf) > -1) {
									manCrfWarn++;	
								} 
								if (tempQuestion.getNciCategory().indexOf(conditional_crf) > -1) {
									condCrfWarn++;
								}
								if (tempQuestion.getNciCategory().indexOf(optional_crf) > -1) {
									optCrfWarn++;
								}
							}
						}
					} else if (congStatus_errors.equals(tempQuestion.getQuestionCongruencyStatus())) {
						countQuestError++;
						if (report.getIsCheckStdCrfCdeChecked()) {
							if (tempQuestion.getNciCategory()!=null) {
								if (tempQuestion.getNciCategory().indexOf(mandatory_crf) > -1) {
									manCrfErr++;	
								}
								if (tempQuestion.getNciCategory().indexOf(conditional_crf) > -1) {
									condCrfErr++;
								}
								if (tempQuestion.getNciCategory().indexOf(optional_crf) > -1) {
									optCrfErr++;
								}
							}
						}
					}					
				}
			}
		}

		// setting counters into the report
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
	 * This method makes a rest call to retrieve all the standard CRF CDEs from the caDSR database
	 * It is called only once per instance of this class.
	 * 
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
	 * This method makes a rest call to retrieve all the NRDS CDEs from the caDSR database
	 * It is called only once per instance of this class.
	 * 
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
			if (completedForm == null) {
				logger.error("RaveFormOid is null");
				continue;
			}
			else if (StringUtils.isBlank(completedForm.getRaveFormOid())) {
				logger.error("RaveFormOid is null, form name: " + completedForm.getFormName());
				continue;
			}
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
		question.setRaveFieldDataType(alsField.getDataFormat());
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
	
	
	/**
	 * Split the FORM public ID and Version, and assign them to the Form  
	 * @param question
	 * @param defaultValue
	 * @return CCCForm
	 */
	protected static CCCForm assignIdVersionToForm (CCCForm form, String defaultValue) {
		String idVersion = defaultValue.substring(defaultValue.indexOf(publicid_prefix), defaultValue.length());
		String id = idVersion.substring(3, idVersion.indexOf("_"));
		String version = (idVersion.substring(idVersion.indexOf(version_prefix) + 2, idVersion.length()));
		id = id.trim();
		String[] versionTokens = version.split("\\_");
		if (NumberUtils.isNumber(id) && NumberUtils.isNumber(versionTokens[0]) && NumberUtils.isNumber(versionTokens[1])) {
			version = versionTokens[0] + "." + versionTokens[1];
			form.setFormPublicId(id.trim());
			form.setFormVersion(version);
		}
		return form;
	}	

}
