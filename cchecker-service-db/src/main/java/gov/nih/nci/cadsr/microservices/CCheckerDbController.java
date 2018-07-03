/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class CCheckerDbController {
	private final Logger logger = LoggerFactory.getLogger(CCheckerDbController.class);
	
    @Autowired
    private DataElementRepository dataElemenRepository;
    
	@GetMapping("/rest/cdeformtype")
	//@ResponseBody
	public ResponseEntity<String> retrieveDataElementType(@RequestParam("publicId") String publicId,
			@RequestParam("version") String versionNumber) {
		logger.debug("retrieveDataElementType called " + publicId + "v" + versionNumber);
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		
		Map<String, Object> result = dataElemenRepository.retrieveCdeType(publicId, versionNumber);
		String res = result.toString();

		return new ResponseEntity<String>(res,
				httpHeaders, HttpStatus.OK);
	}
}
