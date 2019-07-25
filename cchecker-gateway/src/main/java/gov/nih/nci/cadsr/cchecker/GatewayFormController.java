/*
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import gov.nih.nci.cadsr.data.FormLoadParamWrapper;
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
	private static String CCHECKER_LOAD_FORM_SERVICE_URL;

	static final String FORM_NAMES_NOT_FOUND = "Form names are not received for session: ";
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
	/**
	 * Load selected ALS Forms as caDSR Forms.
	 * 
	 * @param request
	 * @param response
	 * @param sessionid
	 * @param requestEntity
	 * @return List of Strings
	 */
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
	@PostMapping("/loadformservice")
	public ResponseEntity<?> loadFormService(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name = "sessionid", required = true) String sessionid,
			RequestEntity<FormLoadParamWrapper> requestEntity) {
		//logger.debug("request received loadFormService");
		//check for session cookie
		Cookie cookie = GatewayBootController.retrieveCookie(request);
		String sessionCookieValue = null;

		if ((cookie == null) || (StringUtils.isBlank((sessionCookieValue = cookie.getValue()))) || (!ParameterValidator.validateIdSeq(sessionCookieValue))) {
			return GatewayBootController.buildErrorResponse(GatewayBootController.SESSION_NOT_VALID + sessionCookieValue, HttpStatus.BAD_REQUEST);
		}
		
		logger.debug("loadformservice session cookie: " + sessionCookieValue);
		
		//sessionid parameter to validate
		if (!ParameterValidator.validateIdSeq(sessionid)) {
			logger.error("loadformservice sessionid invalid: " + sessionid);
			return GatewayBootController.buildErrorResponse(GatewayBootController.SESSION_NOT_VALID + sessionid, HttpStatus.BAD_REQUEST);
		}
		
		//sessionid parameter is validated, and we will use it from now on.
		logger.debug("loadformservice session information provided in sessionid: " + sessionid);
		
		FormLoadParamWrapper formLoadParamWrapper = requestEntity.getBody();
		if (formLoadParamWrapper == null) {
			logger.error("loadformservice request invalid with empty form list for session: " + sessionid);
			return GatewayBootController.buildErrorResponse(FORM_NAMES_NOT_FOUND + sessionid, HttpStatus.BAD_REQUEST);
		}
		
		String detailsStr = formLoadParamWrapper.toString();
		logger.debug("loadformservice request body received: " + formLoadParamWrapper);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(CCHECKER_LOAD_FORM_SERVICE_URL);
		builder.queryParam(GatewayBootController.sessionCookieName, sessionid);
		
		try {
			//call load form service
			ResponseEntity<ArrayList> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(), formLoadParamWrapper, ArrayList.class);
			
			if (responseEntity == null) {
				logger.error("loadFormService error on " + sessionid);
				return GatewayBootController.buildErrorResponse(GatewayBootController.SESSION_DATA_NOT_FOUND + sessionid, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			HttpStatus statusCode = responseEntity.getStatusCode();
			List<String> responseData = responseEntity.getBody();

			if (HttpStatus.OK.equals(statusCode)) {
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.add("Content-Type", "application/json");
				return new ResponseEntity<List<String>>(responseData, httpHeaders, HttpStatus.OK);
			}
			else {
				logger.error("loadFormService error response: " + statusCode);
				HttpStatus errorCode = responseEntity.getStatusCode();//This can be user error or server error
				return GatewayBootController.buildErrorResponse("Error on load forms for session: " + sessionid + detailsStr + responseData, errorCode);
			}
		}
		catch (RestClientException re) {
			 String errorMessage = "Error on load form service session: " + sessionid + detailsStr + ". Error details: " + re.getMessage();
			 return GatewayBootController.buildErrorResponse(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	protected static void loadProperties() {
		CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS = GatewayBootWebApplication.CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS;
		logger.debug("GatewayFormController CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS: "
				+ CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS);
		CCHECKER_LOAD_FORM_SERVICE_URL = GatewayBootWebApplication.CCHECKER_LOAD_FORM_SERVICE_URL;
		logger.debug("GatewayFormController CCHECKER_LOAD_FORM_SERVICE_URL: "
				+ CCHECKER_LOAD_FORM_SERVICE_URL);
	}
}
