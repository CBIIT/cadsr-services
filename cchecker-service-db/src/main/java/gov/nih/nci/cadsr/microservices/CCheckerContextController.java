/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class CCheckerContextController {
	private static final Logger logger = LoggerFactory.getLogger(CCheckerDbController.class);
    @Autowired
    private DataElementRepository dataElemenRepository;
    
	@GetMapping("/rest/retrievecontexts")
	public ResponseEntity<List<String>> retrieveContextList(HttpServletRequest request) {
		logger.debug("retrieveContextList called");

		List<String> categoryCdeList = dataElemenRepository.retrieveContextList();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		return new ResponseEntity<List<String>>(categoryCdeList, httpHeaders, HttpStatus.OK);
	}
}
