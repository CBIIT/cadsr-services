/*
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.io.InputStreamResource;
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
import org.springframework.web.util.UriComponentsBuilder;

import gov.nih.nci.cadsr.data.FormLoadParamWrapper;

/**
 * RESTful API generate caDSR forms from ALS forms and to generate XML for FL from ALS forms.
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
	private static String CCHECKER_FORM_XML_SERVICE_URL;
	static final String FORM_XML_SERVICE_URL_STR = "formxmlservice";
	static final String RETRIEVE_FORM_XML_URL_STR = "retrieveformxml";

	static final String FORM_NAMES_NOT_FOUND = "Form names are not received for session: ";
	
	public static final String fileFormLoaderPrefix = "FormLoader-";
	static final String XML_FILE_EXT = ".xml";
	public static final String XML_MIME_TYPE = "application/xml";
	
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
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
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
	//Commented since we do not release this service
//	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
//	@PostMapping("/loadformservice")
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
	@PostMapping("/formxmlservice")
	public ResponseEntity<?> formXmlService(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name = "sessionid", required = true) String sessionid,
			RequestEntity<FormLoadParamWrapper> requestEntity) {
		//logger.debug("request received loadFormService");
		//check for session cookie
		Cookie cookie = GatewayBootController.retrieveCookie(request);
		String sessionCookieValue = null;

		if ((cookie == null) || (StringUtils.isBlank((sessionCookieValue = cookie.getValue()))) || (!ParameterValidator.validateIdSeq(sessionCookieValue))) {
			return GatewayBootController.buildErrorResponse(GatewayBootController.SESSION_NOT_VALID + sessionCookieValue, HttpStatus.BAD_REQUEST);
		}
		
		logger.debug("formXmlService session cookie: " + sessionCookieValue);
		
		//sessionid parameter to validate
		if (!ParameterValidator.validateIdSeq(sessionid)) {
			logger.error("formXmlService sessionid invalid: " + sessionid);
			return GatewayBootController.buildErrorResponse(GatewayBootController.SESSION_NOT_VALID + sessionid, HttpStatus.BAD_REQUEST);
		}
		
		//sessionid parameter is validated, and we will use it from now on.
		logger.debug("formXmlService session information provided in sessionid: " + sessionid);
		
		FormLoadParamWrapper formLoadParamWrapper = requestEntity.getBody();
		if (formLoadParamWrapper == null) {
			logger.error("formXmlService request invalid with empty form list for session: " + sessionid);
			return GatewayBootController.buildErrorResponse(FORM_NAMES_NOT_FOUND + sessionid, HttpStatus.BAD_REQUEST);
		}
		
		String detailsStr = formLoadParamWrapper.toString();
		logger.debug("formXmlService request body received: " + formLoadParamWrapper);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(CCHECKER_FORM_XML_SERVICE_URL);
		builder.queryParam(GatewayBootController.sessionCookieName, sessionid);
		
		try {
			//call load form service; the response is String array 
			ResponseEntity<?> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(), formLoadParamWrapper, ArrayList.class);
			
			if (responseEntity == null) {
				logger.error("formXmlService error on " + sessionid);
				return GatewayBootController.buildErrorResponse(GatewayBootController.SESSION_DATA_NOT_FOUND + sessionid, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			HttpStatus statusCode = responseEntity.getStatusCode();
			Object responseData = responseEntity.getBody();
			//This is String array 
			logger.debug("responseData XML: " + responseData);

			if (HttpStatus.OK.equals(statusCode)) {
				URI url = requestEntity.getUrl();
				String locationHeaderString = buildLocationUri(url, sessionid);	
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.add("Content-Type", GatewayBootController.TEXT_PLAIN_MIME_TYPE);
				httpHeaders.add("Location", locationHeaderString);
				return new ResponseEntity<String>(sessionid, httpHeaders, HttpStatus.CREATED);
			}
			else {
				logger.error("formXmlService error response: " + statusCode);
				HttpStatus errorCode = responseEntity.getStatusCode();//This can be user error or server error
				return GatewayBootController.buildErrorResponse("Error on load forms for session: " + sessionid + detailsStr + responseData, errorCode);
			}
		}
		catch (RestClientException re) {
			 String errorMessage = "Error on form xml service session: " + sessionid + detailsStr + ". Error details: " + re.getMessage();
			 return GatewayBootController.buildErrorResponse(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/**
	 * This function builds location URL based on request URI.
	 *  
	 * @param url not null
	 * @param sessionid not null
	 * @return String URI to get validation report
	 */
	protected static String buildLocationUri(URI url, String sessionid) {
		String location;
		
		String path = String.format("%s://%s:%d%s", url.getScheme(), url.getHost(), url.getPort(), url.getPath());
		location = path.replace(FORM_XML_SERVICE_URL_STR, RETRIEVE_FORM_XML_URL_STR) + '/'+ sessionid;
		logger.info("Location header value using request URI: " + location);	

		return location;
	}
	
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
	@GetMapping("/retrieveformxml/{idseq}")
	public void retrieveFormXml(HttpServletRequest request, HttpServletResponse response, 
			@PathVariable("idseq") String idseq) throws Exception {
		if  (!ParameterValidator.validateIdSeq(idseq)) {
			response.setHeader("Content-Type", GatewayBootController.TEXT_PLAIN_MIME_TYPE);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			IOUtils.copy(new ByteArrayInputStream(("Report ID is not valid: " + idseq + '\n').getBytes()),
				response.getOutputStream());
		} 
		else {
			String filePath = buildXmllFilePath(idseq);
			logger.debug("...retrieveExcelReportError from: " + filePath);
			response.setHeader("Content-Type", XML_MIME_TYPE);
			response.setHeader("Content-Disposition", "attachment; filename=" + fileFormLoaderPrefix + idseq + XML_FILE_EXT);
			response.setHeader("Access-Control-Expose-Headers","Content-Disposition");
			response.setStatus(HttpServletResponse.SC_OK);
			InputStream istream = GatewayBootController.openFileAsInputStream(filePath);
			if (istream != null) {
				IOUtils.copy(GatewayBootController.openFileAsInputStream(filePath), response.getOutputStream());
			}
			else {
				response.setHeader("Content-Type", GatewayBootController.TEXT_PLAIN_MIME_TYPE);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				IOUtils.copy(new ByteArrayInputStream(("Form XML file with ID is not found: " + idseq + '\n').getBytes()),
					response.getOutputStream());
			}
		}
		response.flushBuffer();
	}
	/**
	 * Return path to previously generated Excel file.
	 * 
	 * @param String idseq not null
	 * @return String file full path to Excel report file.
	 */
	private String buildXmllFilePath(String idseq) {
		return GatewayBootController.UPLOADED_FOLDER + fileFormLoaderPrefix + idseq + XML_FILE_EXT;
	}
	/**
	 * 
	 * @param idseq
	 *            - saved previously in DB not null
	 * @return ALSData
	 */
	protected InputStreamResource retrieveFormXmlDocument(String idseq) {
		return retrieveData(idseq, RETRIEVE_FORM_XML_URL_STR, InputStreamResource.class);
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
			//logger.debug("...retrieveData from URL: " + urlStr);

			data = restTemplate.getForObject(urlStr, clazz);
		}
		return data;
	}
	
	protected static void loadProperties() {
		CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS = GatewayBootWebApplication.CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS;
		logger.debug("GatewayFormController CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS: "
				+ CCHECKER_DB_SERVICE_URL_RETRIEVE_CONTEXTS);
		CCHECKER_LOAD_FORM_SERVICE_URL = GatewayBootWebApplication.CCHECKER_LOAD_FORM_SERVICE_URL;
		logger.debug("GatewayFormController CCHECKER_LOAD_FORM_SERVICE_URL: "
				+ CCHECKER_LOAD_FORM_SERVICE_URL);
		CCHECKER_FORM_XML_SERVICE_URL = GatewayBootWebApplication.CCHECKER_FORM_XML_SERVICE_URL;
		logger.debug("GatewayFormController CCHECKER_FORM_XML_SERVICE_URL: "
				+ CCHECKER_FORM_XML_SERVICE_URL);
	}
}
