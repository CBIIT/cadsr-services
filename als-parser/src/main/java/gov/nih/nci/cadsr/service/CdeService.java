/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.FormDisplay;
import gov.nih.nci.cadsr.data.FormsUiData;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;

public class CdeService {

	private static final Logger logger = Logger.getLogger(CdeService.class);
		
	/**
	 * retrieve CDE by calling cde details restful service
	 * 
	 * @param uploadfile
	 * @return ResponseEntity
	 */
    @RequestMapping( value = "/rest/cdedetails" )
    @ResponseBody
	public static CdeDetails retrieveDataElement(@RequestParam("publicId") String publicId,
			@RequestParam("version") String versionNumber) throws Exception  {
		CdeDetails cdeDetails = null;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("config.properties");		
        if (checkLinkParameters(publicId, versionNumber)) {
	        // Get the data model from the database
        		String CDEBROWSER_REST_GET_CDE = null;
        		Properties properties = new Properties();
        		properties.load(input);
        		String propVal;
        		if ((propVal = properties.getProperty("CDEBROWSER_REST_GET_CDE")) != null) {
        			CDEBROWSER_REST_GET_CDE = propVal;
        		}
	        	String cdeBrowserRestApiUrl = String.format(CDEBROWSER_REST_GET_CDE, publicId, versionNumber);
	            RestTemplate restTemplate = new RestTemplate();
	    		HttpHeaders httpHeaders = new HttpHeaders();//we have to set up Accept header for a chance a server does not set up Content-Type header on response
	            cdeDetails = restTemplate.getForObject(cdeBrowserRestApiUrl, CdeDetails.class);
	            httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	            HttpEntity<String> entity = new HttpEntity<>("parameters", httpHeaders);
	            ResponseEntity<CdeDetails> responseEntity = restTemplate.exchange(cdeBrowserRestApiUrl, HttpMethod.GET, entity, CdeDetails.class);
	            cdeDetails = responseEntity.getBody();
        }
        else {
        	logger.info("Unexpected parameter values are ignored in retrieveDataElementDetailsByLink, publicId: " + publicId + ", versionNumber: " + versionNumber);
        }
        if (cdeDetails == null) {
        	cdeDetails = new CdeDetails();
        }

		return cdeDetails;
	}	
 
    
    private static boolean checkLinkParameters(String publicId, String versionNumber) {
    	if ((NumberUtils.isNumber(versionNumber)) && (NumberUtils.isDigits(publicId))) {
    		return true;
    	}
    	else return false;
    }	
	

}
