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
@Service
public class ServiceDbAls implements ServiceDb {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDbAls.class.getName());
	@Override
	public StringResponseWrapper submitPostRequestSaveAls(ALSData alsData, String idSeq, String urlStr) {
		return submitPostRequestCreateGeneric(alsData, idSeq, urlStr);
	}
	/**
	 * 
	 * @param CCCReport
	 * @param idseq
	 * @return StringResponseWrapper
	 */
	protected <T>StringResponseWrapper submitPostRequestCreateGeneric(T data, String idseq, String createRequestUrlStr) {
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createRequestUrlStr);

		// add some String
		builder.queryParam(GatewayBootController.sessionCookieName, idseq);

		// another staff
		StringResponseWrapper wrapper = new StringResponseWrapper();
		HttpEntity<T> requestData = new HttpEntity<>(data);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(),
				requestData, String.class);

		HttpStatus statusCode = responseEntity.getStatusCode();
		wrapper.setStatusCode(statusCode);
		if (statusCode == HttpStatus.OK) {
			wrapper.setResponseData(responseEntity.getBody());
			logger.debug(createRequestUrlStr + " OK result received");

		} 
		else {
			logger.error(createRequestUrlStr + " sent an error: " + statusCode);
		}
		return wrapper;
	}
}
