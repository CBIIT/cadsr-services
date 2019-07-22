/*
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
/**
 * RESTful API generate caDSR forms from ALS forms.
 * 
 * @author asafievan
 *
 */
@RestController
@EnableAutoConfiguration
public class GatewayFormController {
	private static final Logger logger = LoggerFactory.getLogger(GatewayFormController.class);

	private static String CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS;
	{
		loadProperties();
	}
	
	@Autowired
	protected RestTemplate restTemplate;
	
	/**
	 * 
	 * @param request
	 * @return List of caDSR context names.
	 */
	@GetMapping("/retrievecontexts")
	public ResponseEntity<?> retrieveContextList(HttpServletRequest request) {
		logger.debug("gateway retrieveContextList called");
		List<String> categoryCdeList = retrieveContextNameList();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		return new ResponseEntity<List<String>>(categoryCdeList, httpHeaders, HttpStatus.OK);
	}

	/**
	 * 
	 * @return List of Context names.
	 */
	protected List<String> retrieveContextNameList() {
		return retrieveData(CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS, List.class);
	}

	/**
	 * 
	 * @param retrieveUrlStr
	 * @param clazz
	 * @return object of specified class.
	 */
	protected <T> T retrieveData(String retrieveUrlStr, Class<T> clazz) {
		T data = null;

		// logger.debug("...retrieveData from URL: " + retrieveUrlStr);

		data = restTemplate.getForObject(retrieveUrlStr, clazz);

		return data;
	}

	protected static void loadProperties() {
		CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS = GatewayBootWebApplication.CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS;
		logger.debug("GatewayFormController CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS: "
				+ CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS);
	}
}
