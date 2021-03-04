/*
 * Copyright (C) 2019 Frederick National Laboratory for Cancer research - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
/**
 * * This controller is for testing DB Connection.
 * 
 * @author asafievan
 *
 */
@Controller
@CrossOrigin(origins = {"http://localhost:4200", "https://cdevalidator-dev.nci.nih.gov"
		, "https://cdevalidator-qa.nci.nih.gov", "https://cdevalidator-stage.nci.nih.gov", 
		"https://cdevalidator.nci.nih.gov"}, allowCredentials="true",maxAge=9000)
public class GatewayMonitorController {
	static String CCHECKER_DB_SERVICE_TEST_URL;
	private static final Logger logger = LoggerFactory.getLogger(GatewayMonitorController.class);
	{
		loadProperties();
	}
	@Autowired
	private RestTemplate restTemplate;
	
	@GetMapping("/monitorcdevalidator")
	public ResponseEntity<?> monitorCdeValidator(HttpServletRequest request) {
		logger.debug("monitorCdeValidator called");

		try {
			String data = retrieveData(CCHECKER_DB_SERVICE_TEST_URL, String.class);
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("Content-Type", GatewayBootController.TEXT_PLAIN_MIME_TYPE);
			return new ResponseEntity<String>(data, httpHeaders, HttpStatus.OK);
		}
		catch (HttpClientErrorException e){
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("Content-Type", GatewayBootController.TEXT_PLAIN_MIME_TYPE);
			logger.error("ERROR");
			return new ResponseEntity<String>("ERROR", httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	protected static void loadProperties() {
		CCHECKER_DB_SERVICE_TEST_URL = GatewayBootWebApplication.CCHECKER_DB_SERVICE_TEST_URL;
		logger.debug("GatewayMonitorController.loadProperties: " + CCHECKER_DB_SERVICE_TEST_URL);
	}
	/**
	 * 
	 * @param idseq - saved previously in DB and not null
	 * @param retrieveUrlStr - String format URL
	 * @return Data
	 */
	protected <T> T retrieveData(String retrieveUrlStr, Class<T> clazz) {
		T data = null;

		logger.debug("...retrieveData from URL: " + retrieveUrlStr);

		data = restTemplate.getForObject(retrieveUrlStr, clazz);
		
		return data;
	}
}
