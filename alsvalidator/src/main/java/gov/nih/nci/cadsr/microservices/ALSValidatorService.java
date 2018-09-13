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

@SpringBootApplication(scanBasePackages = { "gov.nih.nci.cadsr.microservices"})

@RestController
@EnableAutoConfiguration
// (exclude={DataSourceAutoConfiguration.class,
// HibernateJpaAutoConfiguration.class})
public class ALSValidatorService {
	private static final Logger logger = LoggerFactory.getLogger(ALSValidatorService.class);
	protected static String CCHECKER_DB_SERVICE_URL_RETRIEVE;
	protected static String CDEBROWSER_REST_GET_CDE;
	
	public static String getCDEBROWSER_REST_GET_CDE() {
		return CDEBROWSER_REST_GET_CDE;
	}

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
		CDEBROWSER_REST_GET_CDE = properties.getProperty("CDEBROWSER_REST_GET_CDE");
		logger.info("CDEBROWSER_REST_GET_CDE: " + CDEBROWSER_REST_GET_CDE);
		
		SpringApplication.run(ALSValidatorService.class, args);
	}

}
