/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.List;

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
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RestDataElementController {
	private final Logger logger = LoggerFactory.getLogger(RestDataElementController.class);

    @Autowired
    private DataElementRepository dataElemenRepository;
	/**
	 * Retrieve CDE using dataElemenRepository - example.
	 * 
	 * @param uploadfile
	 * @return ResponseEntity
	 */
	@GetMapping("/rest/cde")
	//@ResponseBody
	public ResponseEntity<DataElements> retrieveDataElement() {
		logger.debug("retrieveDataElement called");
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");	

        List<DataElements> users = dataElemenRepository.findAll();

		return new ResponseEntity(users.get(0),
				httpHeaders, HttpStatus.OK);
	}

}
