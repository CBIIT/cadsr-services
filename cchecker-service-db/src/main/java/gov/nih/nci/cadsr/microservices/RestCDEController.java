/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.Arrays;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;
@RestController
@EnableAutoConfiguration
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RestCDEController {
	private static final Logger logger = LoggerFactory.getLogger(RestCDEController.class);

    @Autowired
    private DataElementRepository dataElemenRepository;
	/**
	 * retrieve CDE by calling cdebrowser restful service
	 * 
	 * @param uploadfile
	 * @return ResponseEntity
	 */
    @RequestMapping( value = "/rest/cdedetails" )
    @ResponseBody
	public CdeDetails retrieveDataElement(@RequestParam("publicId") String publicId,
			@RequestParam("version") String versionNumber) {
		logger.debug("Single file upload!");
		CdeDetails cdeDetails = null;
        if (checkLinkParameters(publicId, versionNumber)) {
	        // Get the data model from the database
	        	String cdeBrowserRestApiUrl = String.format(CCheckerDbService.CDEBROWSER_REST_GET_CDE, publicId, versionNumber);
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
    private boolean checkLinkParameters(String publicId, String versionNumber) {
    	if ((NumberUtils.isNumber(versionNumber)) && (NumberUtils.isDigits(publicId))) {
    		return true;
    	}
    	else return false;
    }
}
