package gov.nih.nci.cadsr.microservices;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
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
import gov.nih.nci.cadsr.data.ValidateParamWrapper;
import gov.nih.nci.cadsr.report.ReportOutput;
import gov.nih.nci.cadsr.report.impl.GenerateReport;

@RestController
@EnableAutoConfiguration
public class AlsValidatorController {
	private final static Logger logger = LoggerFactory.getLogger(AlsValidatorController.class);
	private static String CCHECKER_DB_SERVICE_URL_RETRIEVE = ALSValidatorService.CCHECKER_DB_SERVICE_URL_RETRIEVE;
	/**
	 * This service shall always return CCCReport Entity.
	 * 
	 * @param request
	 * @param response
	 * @param idseq
	 * @param checkUOM
	 * @param checkCRF
	 * @param displayExceptions
	 * @param requestEntity
	 * @return ResponseEntity<CCCReport>
	 */
	@PostMapping("/rest/validateservice")
	public ResponseEntity<CCCReport> validateService(HttpServletRequest request, HttpServletResponse response,
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
				
				if ((selForms != null) && (! selForms.isEmpty())) {
					ReportOutput report = new GenerateReport();
					errorsReport = report.getFinalReportData(alsData, getFormIdList(selForms, alsData.getForms()), checkUOM, checkCRF, displayExceptions);
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
		HttpHeaders httpHeaders = createHttpOkHeaders();
		return new ResponseEntity<CCCReport>(errorsReport, httpHeaders, HttpStatus.OK);
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
			RestTemplate restTemplate = new RestTemplate();

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
