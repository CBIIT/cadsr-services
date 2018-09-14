/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;

@Service
public class CdeServiceDetails {

	private final static Logger logger = LoggerFactory.getLogger(ValidateController.class);
	private static final String CDEBROWSER_REST_GET_CDE = ValidateService.getCDEBROWSER_REST_GET_CDE();

	/**
	 * retrieve CDE by calling cde details restful service
	 * 
	 * @param String
	 *            publicId not null
	 * @param String
	 *            versionNumber not null
	 * @return CompletableFuture<CdeDetails>
	 */
	@Async("threadPoolTaskExecutor")
	public CompletableFuture<CdeDetails> retrieveDataElement(String publicId, String versionNumber) {
		CdeDetails cdeDetails = null;
		// Get the data model from CDE Browser RESTful API
//		System.out.println("Execute method asynchronously publicId:versionNumber " 
//				+ publicId + ":" + versionNumber + 
//			" -  " + Thread.currentThread().getName());
		String cdeBrowserRestApiUrl = String.format(CDEBROWSER_REST_GET_CDE, publicId, versionNumber);
		RestTemplate restTemplate = new RestTemplate();
		cdeDetails = restTemplate.getForObject(cdeBrowserRestApiUrl, CdeDetails.class);
		return CompletableFuture.completedFuture(cdeDetails);
	}

}
