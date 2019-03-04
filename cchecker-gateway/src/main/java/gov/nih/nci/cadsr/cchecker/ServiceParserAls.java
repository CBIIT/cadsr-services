/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import gov.nih.nci.cadsr.data.ALSData;
@Service
public class ServiceParserAls implements ServiceParser {
	private static final Logger logger = LoggerFactory.getLogger(ServiceParserAls.class.getName());
	@Autowired
	private RestTemplate restTemplate;
	@Override
	public ALSDataWrapper submitPostRequestParser(String filePath, String urlString) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(urlString);

		// add some String
		builder.queryParam("filepath", filePath);

		// another staff
		ALSDataWrapper wrapper = new ALSDataWrapper();

		ResponseEntity<ALSData> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(),
				HttpMethod.POST, ALSData.class);

		HttpStatus statusCode = responseEntity.getStatusCode();
		wrapper.setStatusCode(statusCode);
		if (statusCode == HttpStatus.OK) {
			wrapper.setAlsData(responseEntity.getBody());
			logger.debug("parseservice result received");

		} 
		else {
			logger.error("parsefileservice sent an error: " + statusCode);
		}
		return wrapper;
	}

}
