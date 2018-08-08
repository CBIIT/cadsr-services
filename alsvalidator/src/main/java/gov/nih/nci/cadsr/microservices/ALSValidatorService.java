/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.ReportInputWrapper;
import gov.nih.nci.cadsr.data.ValidateDataWrapper;
import gov.nih.nci.cadsr.report.ReportOutput;
import gov.nih.nci.cadsr.report.impl.GenerateReport;
import gov.nih.nci.cadsr.service.validator.ValidatorService;

@SpringBootApplication(scanBasePackages = { "gov.nih.nci.cadsr.microservices" })
@RestController
@EnableAutoConfiguration
// (exclude={DataSourceAutoConfiguration.class,
// HibernateJpaAutoConfiguration.class})
public class ALSValidatorService {
	private static final Logger logger = LoggerFactory.getLogger(ALSValidatorService.class);
	protected static String CCHECKER_DB_SERVICE_URL_RETRIEVE;
	@RequestMapping("/")
	String home() {
		return "ALS Congruency Checker Validator Service is running!\n";
	}

	public static void main(String[] args) throws Exception {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("boot.properties");
		Properties properties = new Properties();
		properties.load(input);
		CCHECKER_DB_SERVICE_URL_RETRIEVE = properties.getProperty("CCHECKER_DB_SERVICE_URL_RETRIEVE");
		logger.info("CCHECKER_DB_SERVICE_URL_RETRIEVE: " + CCHECKER_DB_SERVICE_URL_RETRIEVE);
		
		SpringApplication.run(ALSValidatorService.class, args);
	}

}
