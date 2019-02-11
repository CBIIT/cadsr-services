/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.CCCReport;
@Service
public class ServiceDbAls implements ServiceDb {
	private static final Logger logger = LoggerFactory.getLogger(ServiceDbAls.class.getName());
	@Override
	public StringResponseWrapper submitPostRequestSaveAls(ALSData alsData, String idSeq, String urlStr) {
		return GatewayBootController.submitPostRequestCreateGeneric(alsData, idSeq, urlStr);
	}

	@Override
	public StringResponseWrapper submitPostRequestSaveReportError(CCCReport data, String idseq, String urlStr) {
		return GatewayBootController.submitPostRequestCreateGeneric(data, idseq, urlStr);
	}
}
