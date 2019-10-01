package gov.nih.nci.cadsr.microservices;
/*
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.CCCError;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.FeedFormStatus;
import gov.nih.nci.cadsr.data.ValidateParamWrapper;

@RestController
@EnableAutoConfiguration
public class ValidateController {
	private final static Logger logger = LoggerFactory.getLogger(ValidateController.class);
	private static String CCHECKER_DB_SERVICE_URL_RETRIEVE = ValidateService.CCHECKER_DB_SERVICE_URL_RETRIEVE;
	private static String CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR = ValidateService.CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR;
	
	@Autowired
	private CdeServiceDetails cdeServiceDetails;//
	@Autowired
	private ReportGeneratorFeed reportGeneratorFeed;//
	@Autowired
    private ServiceDb serviceDb;
	@Autowired
	private RestTemplate restTemplate;
	
	@CrossOrigin
	@GetMapping("/rest/feedvalidateformnumber/{idseq}")
	public ResponseEntity<?> feedValidateStatus(HttpServletRequest request,
			@PathVariable("idseq") String idseq) {
		String formUndervalidation = reportGeneratorFeed.feedRequestStatus(idseq);
		//logger.debug("feedValidateStatus called: " + idseq + ", current form: " + formUndervalidation);
		return new ResponseEntity<String>(formUndervalidation, HttpStatus.OK);
	}
	//FORMBUILD-633
	@CrossOrigin
	@GetMapping("/rest/feedvalidateform/{idseq}")
	public ResponseEntity<?> feedValidateFormStatus(HttpServletRequest request,
			@PathVariable("idseq") String idseq) {
		FeedFormStatus formUndervalidation = reportGeneratorFeed.currentRequestStatus(idseq);
		//logger.debug("feedValidateStatus called: " + idseq + ", current form: " + formUndervalidation);
		return new ResponseEntity<FeedFormStatus>(formUndervalidation, HttpStatus.OK);
	}
	@CrossOrigin
	@GetMapping("/rest/cancelvalidate/{idseq}")
	public ResponseEntity<?> cancelValidate(HttpServletRequest request,
			@PathVariable("idseq") String idseq) {
		logger.info("cancelvalidate request received: " + idseq);
		reportGeneratorFeed.cancelValidate(idseq);
		return new ResponseEntity<String>(idseq, HttpStatus.OK);
	}
	@PostMapping("/rest/validateservice")
	public ResponseEntity<String> validateService(HttpServletRequest request, HttpServletResponse response,
		@RequestParam(name="_cchecker", required=true) String idseq, 
		RequestEntity<ValidateParamWrapper> requestEntity) {

		ValidateParamWrapper validateParamEntity = requestEntity.getBody();
		logger.info("Call validateService: " + validateParamEntity);
		
		String strMsg;
		boolean checkUOM = validateParamEntity.getCheckUom();
		boolean checkCRF = validateParamEntity.getCheckCrf();
		boolean displayExceptions = validateParamEntity.getDisplayExceptions();
		
		CCCReport errorsReport = new CCCReport();
		try {
			ALSData alsData = retrieveAlsData(idseq);
			if (alsData != null) {
				logger.debug("Retrieved parsed file for validation, with name: " + alsData.getFileName());
				List<String> selForms = validateParamEntity.getSelForms();
				
				if ((selForms != null) && (! selForms.isEmpty())) 
				{
					errorsReport = reportGeneratorFeed.getFinalReportData(idseq, alsData, getFormIdList(selForms, alsData.getForms()), checkUOM, checkCRF, displayExceptions);
					logger.info("Created CCCReport with amount of forms: " + errorsReport.getCccForms().size());
				}
				else {//source default data to add
					logger.info("No selected forms, sending CCCReport with no forms");
					errorsReport.setReportOwner(alsData.getReportOwner());
					errorsReport.setFileName(alsData.getFileName());
					errorsReport.setRaveProtocolName(alsData.getCrfDraft().getProjectName());
					errorsReport.setRaveProtocolNumber(alsData.getCrfDraft().getPrimaryFormOid());
					errorsReport.setTotalFormsCount(alsData.getForms().size());
					errorsReport.setCountQuestionsChecked(alsData.getFields().size());
				}
			}
			else {
				strMsg = "FATAL error: no data found in retrieving ALSData parser data by ID: " + idseq;
				CCCError cccError = new CCCError();
				ALSError alsError = new ALSError();
				alsError.setErrorSeverity("FATAL");
				alsError.setErrorDesc(strMsg);
				errorsReport.setCccError(cccError);
				logger.error(strMsg);
			}
		}
		catch(RestClientException e) {
			strMsg = "Error on retrieving ALSData by session ID: " + idseq + e;
			CCCError cccError = new CCCError();
			ALSError alsError = new ALSError();
			alsError.setErrorSeverity("FATAL");
			alsError.setErrorDesc(strMsg);
			errorsReport.setCccError(cccError);
			logger.error(strMsg);
		}
		StringResponseWrapper storeResponse = storeReportError(errorsReport, idseq, CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "text/plain");
		return new ResponseEntity<String>(storeResponse.getResponseData(), httpHeaders, storeResponse.getStatusCode());
	}
	/**
	 * 
	 * @param cccReport not null
	 * @param sessionCookieValue not null
	 * @param url not null
	 * @return StringResponseWrapper
	 */
	protected StringResponseWrapper storeReportError(CCCReport cccReport, String sessionCookieValue, String url) {
		StringResponseWrapper saveResponse;
		try {			
			saveResponse = serviceDb.submitPostRequestSaveReportError(cccReport, sessionCookieValue, CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR);
		}
		catch (RestClientException re) {
			String responseErrorStr = "Unexpected error on store report for session: " + sessionCookieValue + ". Details: " + re;
			logger.error(responseErrorStr);
			saveResponse = new StringResponseWrapper();
			saveResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			saveResponse.setResponseData(responseErrorStr);
		}
		return saveResponse;
	}
	/**
	 * 
	 * @param idseq saved previously in DB not null
	 * @return ALSData
	 */
	protected ALSData retrieveAlsData(String idseq) {
		return retrieveData(idseq, CCHECKER_DB_SERVICE_URL_RETRIEVE, ALSData.class);
	}	
	/**
	 * 
	 * @param idseq
	 *            - saved previously in DB not null
	 * @return Data
	 */
	protected <T> T retrieveData(String idseq, String retrieveUrlStr, Class<T> clazz) {
		T data = null;
		if (idseq != null) {
			String urlStr = String.format(retrieveUrlStr, idseq);
			logger.debug("...retrieveData: " + urlStr);

			data = restTemplate.getForObject(urlStr, clazz);
		}
		return data;
	}
	/**
	 * This method is no used.
	 * 
	 * @param errorMessage
	 * @param httpStatus
	 * @return ResponseEntity
	 */
	protected ResponseEntity<String> buildErrorResponse(String errorMessage, HttpStatus httpStatus) {
		// TODO what context type shall be returned on an error - ? Now
		// text/plain
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "text/plain");
		logger.error(errorMessage);
		return new ResponseEntity<String>(errorMessage, httpHeaders, HttpStatus.BAD_REQUEST);
	}
	
	protected HttpHeaders createHttpOkHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		return httpHeaders;
	}
	
	/**
	 * Returns a list of Form OIDs for the respective Form Names list 
	 * 
	 * @param selForms
	 * @param formsList
	 * @return List<String>
	 */		
	protected static List<String> getFormIdList(List<String> selForms, List<ALSForm> formsList) {
		List<String> formIdsList = new ArrayList<String>();
		for (String selectedFormName : selForms) {			
			for (ALSForm alsForm : formsList) {
				if (alsForm.getDraftFormName().equalsIgnoreCase(selectedFormName)) {
						formIdsList.add(alsForm.getFormOid());
					}
				}
		}
		return formIdsList;
	}
}
