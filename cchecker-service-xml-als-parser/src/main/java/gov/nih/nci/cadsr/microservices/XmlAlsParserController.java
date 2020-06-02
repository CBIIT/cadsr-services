/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import gov.nih.nci.cadsr.parser.impl.OoxmlAlsParser;

@RestController
@EnableAutoConfiguration
public class XmlAlsParserController {
	private final static Logger logger = LoggerFactory.getLogger(XmlAlsParserController.class);

	public final String strNoFilePath = "Server problems Parser component no file information received";
	//TODO consider sSpring singleton bean usage
	private static final OoxmlAlsParser ooxmlAlsParser = new OoxmlAlsParser();
	
	@Autowired
	AlsPostProcessor alsPostProcessor;
	
	@PostMapping("/rest/xmlalsparserservice")
	//@ResponseBody
	public ResponseEntity<ALSData>parseXmlAls(@RequestParam("filepath") String filePath) {
		logger.debug("Parse file: " + filePath);
		if (StringUtils.isBlank(filePath)) {
			//no filepath received
			logger.error(strNoFilePath);;
			return createALSDataError(strNoFilePath, HttpStatus.SERVICE_UNAVAILABLE);
		}

		logger.info("Parse file in path: " + filePath);
		try {
			//TODO we do not need filename here consider to remove the second parameter, but keep the class member and a setter method.
			ALSData alsData = new ALSData();//alsParser.parse(filePath);
			alsPostProcessor.postProcess(alsData);
			Long startTime = System.currentTimeMillis();
			//File file = new File("/local/content/cchecker/RAVE-ALS-10057_14-JUN-2017_NS-TEXT-AS-XML.xml");
			//File file = new File("/local/content/cchecker/From-PeterZipFile-RAVE-ALS-10057_14-JUN-2017_NS.xls");
			//File file = new File("/local/content/cchecker/RAVE-ALS-10057-VS-TestFileA.xlsx");
			File file = new File(filePath);
			String fileContent = IOUtils.toString(new FileInputStream(file));
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			SAXParser parser = parserFactory.newSAXParser();
			SAXHandler handler = new SAXHandler();

			ByteArrayInputStream bis = new ByteArrayInputStream(fileContent.getBytes());
			//logger.info("XML file in path: /local/content/cchecker/RAVE-ALS-10057_14-JUN-2017_NS-TEXT-AS-XML.xml");
			//logger.info("XML file in path: /local/content/cchecker/From-PeterZipFile-RAVE-ALS-10057_14-JUN-2017_NS.xls");
			logger.info("XML file in path: "+filePath);
			parser.parse(bis, handler);			
			ooxmlAlsParser.parseXml(handler);
			Long endTime = System.currentTimeMillis();
			logger.info("Time taken: " + (endTime - startTime)+" ms.");
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
