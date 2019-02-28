/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import gov.nih.nci.cadsr.data.CCCReport;
@Service
public class ServiceDbAls implements ServiceDb {
	private static final Logger logger = LoggerFactory.getLogger(ServiceDbAls.class.getName());
	static final String sessionCookieName = "_cchecker";
	@Override
	public StringResponseWrapper submitPostRequestSaveReportError(CCCReport data, String idseq, String urlStr) {
		return submitPostRequestCreateGeneric(data, idseq, urlStr);
	}
	/**
	 * We might move this method to controller or to a util class if used in other places.
	 * 
	 * @param T data generic type not null
	 * @param String idseq not null
	 * @param String URL string not null
	 * @return StringResponseWrapper
	 */
	protected static <T>StringResponseWrapper submitPostRequestCreateGeneric(T data, String idseq, String createRequestUrlStr) {
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createRequestUrlStr);

		builder.queryParam(sessionCookieName, idseq);

		StringResponseWrapper wrapper = new StringResponseWrapper();
		HttpEntity<T> requestData = new HttpEntity<>(data);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(), requestData, String.class);

		HttpStatus statusCode = responseEntity.getStatusCode();
		wrapper.setStatusCode(statusCode);
		if (statusCode == HttpStatus.OK) {
			wrapper.setResponseData(responseEntity.getBody());
			logger.debug(createRequestUrlStr + " OK result: " + wrapper);
		} 
		else {
			String obj;
			if ((obj = responseEntity.getBody()) != null)
				wrapper.setResponseData(obj);
			else {
				wrapper.setResponseData("Unexpected error on store report for session: " + idseq);
			}
			logger.error(createRequestUrlStr + " sent an error: " + wrapper);
		}
		return wrapper;
	}
}
