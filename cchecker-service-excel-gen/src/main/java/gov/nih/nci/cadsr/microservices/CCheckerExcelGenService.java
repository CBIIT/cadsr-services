/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"gov.nih.nci.cadsr.microservices"})
@RestController
@EnableAutoConfiguration
public class CCheckerExcelGenService {
	private static final Logger logger = LoggerFactory.getLogger(CCheckerExcelGenService.class);
	protected static String CCHECKER_DB_SERVICE_URL_REPORT_ERROR_RETRIEVE;
	protected static String REPORT_FOLDER;
	@RequestMapping("/")
	String home() {
		return "CCheckerExcelGenService is running!\n";
	}
	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream input = classLoader.getResourceAsStream("boot.properties")) {
			properties.load(input);
		}
		String propVal;
		if ((propVal = properties.getProperty("CCHECKER_DB_SERVICE_URL_REPORT_ERROR_RETRIEVE")) != null) {
			CCHECKER_DB_SERVICE_URL_REPORT_ERROR_RETRIEVE = propVal;
		}
	    else {
	    	throw new RuntimeException("CCHECKER_DB_SERVICE_URL_REPORT_ERROR_RETRIEVE parameter is not found");
	    }
		if ((propVal = properties.getProperty("REPORT_FOLDER")) != null) {
			REPORT_FOLDER = propVal;
		}
	    else {
	    	throw new RuntimeException("REPORT_FOLDER parameter is not found");
	    }
		logger.info("boot.properties:" + properties);
		
		SpringApplication.run(CCheckerExcelGenService.class, args);
	}
}
