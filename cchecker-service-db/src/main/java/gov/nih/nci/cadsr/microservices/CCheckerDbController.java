/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.List;
import java.util.Map;

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
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.CategoryCde;
import gov.nih.nci.cadsr.data.CategoryNrds;

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
	public ResponseEntity<String> createAlsData(HttpServletRequest request, RequestEntity<ALSData> requestEntity,
			@RequestParam(name="_cchecker", required=true) String idseq) {
		logger.debug("createAlsData called: " + idseq);
		//FIXME idseq format check! check session token
		ALSData alsData = requestEntity.getBody();
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "text/plain");

		String result = dataElemenRepository.createAlsData(alsData, idseq);
		String res = result.toString();
	
		return new ResponseEntity<String>(res, httpHeaders, HttpStatus.OK);
	}
	@GetMapping("/rest/retrievealsdata")
	//@ResponseBody
	public ResponseEntity<ALSData> retrieveAlsData(HttpServletRequest request,
			@RequestParam(name="_cchecker", required=true) String idseq) {
		logger.debug("retrieveAlsData called: " + idseq);
		//FIXME idseq format check! check session token

		ALSData alsData = dataElemenRepository.retrieveAlsData(idseq);
		HttpStatus httpStatus;
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		if (alsData != null) {
			httpHeaders.add("Content-Type", "application/json");
			httpStatus = HttpStatus.OK;
		}
		else {
			httpStatus = HttpStatus.BAD_REQUEST;
		}
		return new ResponseEntity<ALSData>(alsData, httpHeaders, httpStatus);
	}
	
	@GetMapping("/rest/retrievecategorycde")
	public ResponseEntity<List<CategoryCde>> retrieveCategoryCdeList(HttpServletRequest request) {
		logger.debug("retrieveCategoryCdeList called");

		List<CategoryCde> categoryCdeList = dataElemenRepository.retrieveCdeModuleTypeList();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		return new ResponseEntity<List<CategoryCde>>(categoryCdeList, httpHeaders, HttpStatus.OK);
	}
	
	@GetMapping("/rest/retrievecategorynrds")
	public ResponseEntity<List<CategoryNrds>> retrieveCategoryNrdsCdeList(HttpServletRequest request) {
		logger.debug("retrieveCategoryCdeList called");

		List<CategoryNrds> categoryCdeList = dataElemenRepository.retrieveNrdsCdeList();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		return new ResponseEntity<List<CategoryNrds>>(categoryCdeList, httpHeaders, HttpStatus.OK);
	}
	
	@GetMapping("/rest/retrievereporterror")
	//@ResponseBody
	public ResponseEntity<CCCReport> retrieveReportError(HttpServletRequest request,
			@RequestParam(name="_cchecker", required=true) String idseq) {
		logger.debug("retrieveErrorReport called: " + idseq);
		//FIXME idseq format check! check session token

		CCCReport alsData = dataElemenRepository.retrieveReportError(idseq);
		HttpStatus httpStatus;
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		if (alsData != null) {
			httpHeaders.add("Content-Type", "application/json");
			httpStatus = HttpStatus.OK;
		}
		else {
			httpStatus = HttpStatus.BAD_REQUEST;
		}
		return new ResponseEntity<CCCReport>(alsData, httpHeaders, httpStatus);
	}
	@PostMapping("/rest/createreporterror")
	//@ResponseBody
	public ResponseEntity<String> createReportError(HttpServletRequest request, RequestEntity<CCCReport> requestEntity,
			@RequestParam(name="_cchecker", required=true) String idseq) {
		logger.debug("createReportError called: " + idseq);
		//FIXME idseq format check! check session token
		CCCReport reportData = requestEntity.getBody();
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "text/plain");

		String result = dataElemenRepository.createReportError(reportData, idseq);
		String res = result.toString();
	
		return new ResponseEntity<String>(res, httpHeaders, HttpStatus.OK);
	}
}
