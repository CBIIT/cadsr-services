/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.service;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.web.client.RestTemplate;

import gov.nih.nci.cadsr.microservices.ALSValidatorService;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;

public class CdeService {

	private static final Logger logger = Logger.getLogger(CdeService.class);
	private static final String CDEBROWSER_REST_GET_CDE = ALSValidatorService.getCDEBROWSER_REST_GET_CDE();

	/**
	 * retrieve CDE by calling cde details restful service
	 * 
	 * @param uploadfile
	 * @return ResponseEntity
	 */
	public static CdeDetails retrieveDataElement(String publicId,
			String versionNumber) throws Exception {
		CdeDetails cdeDetails = null;
		if (checkLinkParameters(publicId, versionNumber)) {
			String cdeBrowserRestApiUrl = String.format(CDEBROWSER_REST_GET_CDE, publicId, versionNumber);
			RestTemplate restTemplate = new RestTemplate();
			cdeDetails = restTemplate.getForObject(cdeBrowserRestApiUrl, CdeDetails.class);
		}
		else {
			logger.info("Unexpected parameter values are ignored in retrieveDataElementDetailsByLink, publicId: "
					+ publicId + ", versionNumber: " + versionNumber);
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
		else
			return false;
	}

}
