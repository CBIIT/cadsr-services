/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import gov.nih.nci.cadsr.data.ValidateParamWrapper;

@Service
public class ServiceValidatorAls implements ServiceValidator {
	private static final Logger logger = LoggerFactory.getLogger(ServiceValidatorAls.class.getName());

	@Override
	public StringResponseWrapper sendPostRequestValidator(List<String> selForms, String idseq, boolean checkUom, boolean checkCrf,
			boolean displayExceptions) {
		RestTemplate restTemplate = new RestTemplate();
		ValidateParamWrapper wrapper = new ValidateParamWrapper();
		wrapper.setSelForms(selForms);
		wrapper.setCheckUom(checkUom);
		wrapper.setCheckCrf(checkCrf);
		wrapper.setDisplayExceptions(displayExceptions);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GatewayBootController.CCHECKER_VALIDATE_SERVICE_URL);
		builder.queryParam(GatewayBootController.sessionCookieName, idseq);
		
		HttpEntity<ValidateParamWrapper> requestData = new HttpEntity<>(wrapper);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(), requestData, String.class);
		StringResponseWrapper stringResponseWrapper = new StringResponseWrapper();
		HttpStatus statusCode = responseEntity.getStatusCode();
		String responseData = responseEntity.getBody();
		stringResponseWrapper.setResponseData(responseData);
		stringResponseWrapper.setStatusCode(statusCode);
		if (statusCode == HttpStatus.OK) {
			logger.debug(GatewayBootController.CCHECKER_VALIDATE_SERVICE_URL + " OK result received on validate: " + idseq);
		} 
		else {
			logger.error("submitPostRequestValidator received an error on  an error: " + idseq + ", response: " + stringResponseWrapper);
		}
		
		return stringResponseWrapper;
	}

}
