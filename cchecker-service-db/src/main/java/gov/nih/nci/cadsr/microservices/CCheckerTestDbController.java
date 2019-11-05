/**
 * Copyright (C) 2019 Frederick National Laboratory for Cancer research - All rights reserved.
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
/**
 * Test DB for monitoring.
 * 
 * @author asafievan
 *
 */
@RestController
@EnableAutoConfiguration
public class CCheckerTestDbController {
	private static final Logger logger = LoggerFactory.getLogger(CCheckerDbController.class);
    @Autowired
    private DataElementRepository dataElemenRepository;
    
	@GetMapping("/rest/retrievetest")
	public ResponseEntity<String> retrieveTestDb(HttpServletRequest request) {
		logger.debug("retrieveTestDb called");

		List<String> contextCdeList = dataElemenRepository.retrieveTest();
		String res = contextCdeList.size() > 0 ? contextCdeList.get(0) : "ERROR";
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "text/plain");
		return new ResponseEntity<String>(res, httpHeaders, HttpStatus.OK);
	}
}
