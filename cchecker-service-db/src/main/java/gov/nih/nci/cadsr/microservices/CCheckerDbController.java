/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.nih.nci.cadsr.data.ALSData;

@RestController
@EnableAutoConfiguration
public class CCheckerDbController {
	private static final Logger logger = LoggerFactory.getLogger(CCheckerDbController.class);
	static final String sessionCookieName = "_cchecker";
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
	@PostMapping("/rest/createalsdata")
	//@ResponseBody
	public ResponseEntity<String> createAldData(HttpServletRequest request, RequestEntity<ALSData> requestEntity,
			@RequestParam(name="_cchecker", required=true) String idseq) {
		logger.debug("createAldData called");
		//FIXME idseq format check! check session token
		ALSData alsData = requestEntity.getBody();
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		httpHeaders.add("Accept", "text/plain");

		String result = dataElemenRepository.createAlsData(alsData, idseq);
		String res = result.toString();
	
		return new ResponseEntity<String>(res, httpHeaders, HttpStatus.OK);
	}

}
