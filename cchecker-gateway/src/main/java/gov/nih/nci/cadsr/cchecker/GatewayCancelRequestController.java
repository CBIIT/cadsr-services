/*
 * Copyright (C) 2019 FNLCR. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;
/**
 * This is a controller to cancel a running Validation request. JIRA FORMBUILD-641.
 * @author asafievan
 *
 */
@Controller
public class GatewayCancelRequestController {
	private static final Logger logger = LoggerFactory.getLogger(GatewayCancelRequestController.class);
	static String CCHECKER_CANCEL_VALIDATE_SERVICE_URL;
	static String URL_CANCEL_VALIDATE_FORMAT;
	{
		loadProperties();
	}
	@Autowired
	private RestTemplate restTemplate;
	/**
	 * Returns form under validation number.
	 * 
	 * @param idseq not null
	 * @return SseEmitter
	 */
	@CrossOrigin(origins = {"http://localhost:4200", "https://cdevalidator-dev.nci.nih.gov"}, allowCredentials="true",maxAge=9000)
	@GetMapping("/cancelvalidation/{idseq}")
	public ResponseEntity<?> feedCheckStatus(HttpServletRequest request, @PathVariable("idseq") String idseq) {
		logger.debug("cancelvalidation called with session: " + idseq);

		Cookie cookie = GatewayBootController.retrieveCookie(request);

		if ((cookie == null) || (!ParameterValidator.validateIdSeq(cookie.getValue()))) {
			logger.error("cancelvalidation session cookie is not found or not valid");
			return null;
		}
		
		if (!ParameterValidator.validateIdSeq(idseq)) {
			logger.error("cancelvalidation session ID is not valid: " + idseq);
			return null;
		}
		String sessioncancelled = retrieveData(idseq, URL_CANCEL_VALIDATE_FORMAT, String.class);
		logger.info("cancelvalidation validate request is cancelled: " + sessioncancelled);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "text/plain");
		return new ResponseEntity(idseq, httpHeaders, HttpStatus.OK);
	}
	
	protected static void loadProperties() {
		CCHECKER_CANCEL_VALIDATE_SERVICE_URL = GatewayBootWebApplication.CCHECKER_CANCEL_VALIDATE_SERVICE_URL;
		URL_CANCEL_VALIDATE_FORMAT = CCHECKER_CANCEL_VALIDATE_SERVICE_URL + "/%s";
		logger.debug("GatewayCancelRequestController CCHECKER_CANCEL_VALIDATE_SERVICE_URL: " + CCHECKER_CANCEL_VALIDATE_SERVICE_URL);
		logger.debug("GatewayCancelRequestController URL_CANCEL_VALIDATE_FORMAT: " + URL_CANCEL_VALIDATE_FORMAT);
	}
	/**
	 * 
	 * @param idseq - saved previously in DB and not null
	 * @param retrieveUrlStr - String format URL
	 * @return Data
	 */
	protected <T> T retrieveData(String idseq, String retrieveUrlStr, Class<T> clazz) {
		T data = null;
		if (idseq != null) {
			String urlStr = String.format(retrieveUrlStr, idseq);
			logger.debug("...retrieveData from URL: " + urlStr);

			data = restTemplate.getForObject(urlStr, clazz);
		}
		return data;
	}
}
