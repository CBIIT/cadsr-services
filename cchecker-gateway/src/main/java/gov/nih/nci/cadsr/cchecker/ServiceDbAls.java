/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import gov.nih.nci.cadsr.data.ALSData;

@Service
public class ServiceDbAls implements ServiceDb {
	private static final Logger logger = LoggerFactory.getLogger(ServiceDbAls.class.getName());
	@Autowired
	private RestTemplate restTemplate;
	@Override
	public StringResponseWrapper submitPostRequestSaveAls(ALSData alsData, String idSeq, String urlStr) {
		return GatewayBootController.submitPostRequestCreateGeneric(alsData, idSeq, urlStr, restTemplate);
	}

}
