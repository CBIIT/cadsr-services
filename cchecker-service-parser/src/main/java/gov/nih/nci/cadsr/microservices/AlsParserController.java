/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.CCCError;
import gov.nih.nci.cadsr.parser.Parser;
import gov.nih.nci.cadsr.parser.impl.AlsParser;

@RestController
@EnableAutoConfiguration
public class AlsParserController {
	private final static Logger logger = LoggerFactory.getLogger(AlsParserController.class);
	private static String UPLOADED_FOLDER = CCheckerParserService.UPLOADED_FOLDER;
	public final String strNoFilePath = "Server problems Parser component no file information received";
	//TODO consider sSpring singleton bean usage
	private static final Parser alsParser = new AlsParser();
	@PostMapping("/rest/alsparserservice")
	//@ResponseBody
	public ResponseEntity<ALSData>parseAls(@RequestParam("filename") String fileName) {
		logger.debug("Parse file: " + fileName);
		if (StringUtils.isBlank(fileName)) {
			//no filepath received
			logger.error(strNoFilePath);;
			return createALSDataError(strNoFilePath, HttpStatus.SERVICE_UNAVAILABLE);
		}
		String filePath = buildFilePath(fileName);
		logger.info("Parse file in path: " + filePath + ", fileName: " + fileName);
		try {
			ALSData alsData = alsParser.parse(filePath, fileName);
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("Content-Type", "application/json");	

			return new ResponseEntity<ALSData>(alsData,
					httpHeaders, HttpStatus.OK);
		}
		catch (Exception e) {
			//FIXME parser shall not throw Exception
			e.printStackTrace();
			return createALSDataError("error in parser: " + e, HttpStatus.valueOf(400));
		}

	}
	
	protected String buildFilePath(String fileName) {
		return UPLOADED_FOLDER + fileName;
	}
	
	private ResponseEntity<ALSData> createALSDataError(String strError, HttpStatus httpStatus) {
		ALSData error = new ALSData();
		CCCError cccError = new CCCError();
		String noFilePathError = "Server problems Parser component no file information received";
		ALSError alsError = new ALSError();
		alsError.setErrorDesc(noFilePathError);
		alsError.setErrorSeverity("FATAL");
		cccError.addAlsError(alsError);
		error.setCccError(cccError);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		return new ResponseEntity<ALSData>(error,
			httpHeaders, HttpStatus.SERVICE_UNAVAILABLE);//HTTP 503 - ?
	}
}
