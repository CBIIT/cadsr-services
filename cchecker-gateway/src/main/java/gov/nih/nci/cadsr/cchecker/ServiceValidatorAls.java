/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.ValidateParamWrapper;

@Service
public class ServiceValidatorAls implements ServiceValidator {
	private static final Logger logger = LoggerFactory.getLogger(ServiceValidatorAls.class.getName());

	@Override
	public CCCReport sendPostRequestValidator(List<String> selForms, String idseq, boolean checkUom, boolean checkCrf,
			boolean displayExceptions) {
		RestTemplate restTemplate = new RestTemplate();
		ValidateParamWrapper wrapper = new ValidateParamWrapper();
		wrapper.setSelForms(selForms);
		wrapper.setCheckUom(checkUom);
		wrapper.setCheckCrf(checkCrf);
		wrapper.setDisplayExceptions(displayExceptions);
		CCCReport cccReport = null;
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GatewayBootController.CCHECKER_VALIDATE_SERVICE_URL);
		builder.queryParam(GatewayBootController.sessionCookieName, idseq);
		
		HttpEntity<ValidateParamWrapper> requestData = new HttpEntity<>(wrapper);
		ResponseEntity<CCCReport> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(),
				requestData, CCCReport.class);

		HttpStatus statusCode = responseEntity.getStatusCode();

		if (statusCode == HttpStatus.OK) {
			logger.debug(GatewayBootController.CCHECKER_VALIDATE_SERVICE_URL + " OK result received on validate: " + idseq);
			cccReport = (CCCReport) responseEntity.getBody();
			String fileNameOrg = cccReport.getFileName();
			logger.debug("Original file name of report: " + fileNameOrg);
			if (StringUtils.isBlank(fileNameOrg)) {
				cccReport.setFileName("Unknown");
			}
			List<CCCForm> forms = cccReport.getCccForms();
			if ((forms == null) || (forms.isEmpty())) {
				logger.error("!!!Red flag!!! forms are empty for report: "+ idseq);
			}
		} 
		else {
			logger.error("submitPostRequestValidator received an error on  an error: " + idseq + ", HTTP response code: " + statusCode);
		}
		
		return cccReport;
	}

}
