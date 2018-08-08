package gov.nih.nci.cadsr.microservices;
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
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.report.ReportOutput;
import gov.nih.nci.cadsr.report.impl.GenerateReport;

@RestController
@EnableAutoConfiguration
public class AlsValidatorController {
	private final static Logger logger = LoggerFactory.getLogger(AlsValidatorController.class);
	private static String CCHECKER_DB_SERVICE_URL_RETRIEVE = ALSValidatorService.CCHECKER_DB_SERVICE_URL_RETRIEVE;

	@PostMapping("/rest/validateservice")
	public ResponseEntity<?> validateService(HttpServletRequest request, HttpServletResponse response,
		@RequestParam(name="_cchecker", required=true) String idseq, 
		@RequestParam(name = "checkUOM", required = false, defaultValue = "false") boolean checkUOM,
		@RequestParam(name = "checkCRF", required = false, defaultValue = "false") boolean checkCRF,
		@RequestParam(name = "displayExceptions", required = false, defaultValue = "false") boolean displayExceptions,
		RequestEntity<List<String>> requestEntity) {
		String strMsg = "validateService received a request with an error";
		try {
			ALSData alsData = retrieveAlsData(idseq);
			if (alsData != null) {
				List<String> selForms = requestEntity.getBody();
				if ((selForms != null) && (! selForms.isEmpty())) {
					ReportOutput report = new GenerateReport();
					CCCReport errorsReport = report.getFinalReportData(alsData, selForms, checkUOM, checkCRF, displayExceptions);
					errorsReport.setReportOwner(alsData.getReportOwner());
					errorsReport.setFileName(alsData.getFileName());
					HttpHeaders httpHeaders = createHttpOkHeaders();
					return new ResponseEntity<CCCReport>(errorsReport, httpHeaders, HttpStatus.OK);
				}
			}
			else {
				strMsg = "No data found in retrieving ALSData by ID: " + idseq;
			}
		}
		catch(RestClientException e) {
			strMsg = "Error on retrieving ALSData by ID: " + idseq + e;
		}
		logger.error(strMsg);
		return buildErrorResponse(strMsg, HttpStatus.BAD_REQUEST);
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
	 * 
	 * @param errorMessage
	 * @param httpStatus
	 * @return ResponseEntity
	 */
	private ResponseEntity<String> buildErrorResponse(String errorMessage, HttpStatus httpStatus) {
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
}
