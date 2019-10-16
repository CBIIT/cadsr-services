/*
 * Copyright (C) 2019 Frederick National Laboratory for Cancer Research - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
/**
 * Generate FL Forms from ALS forms service.
 * 
 * @author asafievan
 *
 */
@SpringBootApplication(scanBasePackages = {"gov.nih.nci.cadsr.microservices, gov.nih.nci.cadsr.formloader.repository.impl"})
@RestController
@EnableAutoConfiguration
public class CCheckerLoadFormService {
	private static final Logger logger = LoggerFactory.getLogger(CCheckerLoadFormService.class);
	protected static String CCHECKER_DB_SERVICE_URL_REPORT_ERROR_RETRIEVE;
	protected static String REPORT_FOLDER;
	protected static String CCHECKER_DB_SERVICE_URL_RETRIEVE;
	@RequestMapping("/")
	String home() {
		return "CCheckerLoadFormService is running!\n";
	}
	
	@Primary
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	@Autowired
	DataSource dataSource;

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
		//we save XML FL files to this folder
		if ((propVal = properties.getProperty("REPORT_FOLDER")) != null) {
			REPORT_FOLDER = propVal;
		}
		else {
			throw new RuntimeException("REPORT_FOLDER parameter is not found");
		}
		
		CCHECKER_DB_SERVICE_URL_RETRIEVE = properties.getProperty("CCHECKER_DB_SERVICE_URL_RETRIEVE");
		if (CCHECKER_DB_SERVICE_URL_RETRIEVE != null) {
			logger.info("CCHECKER_DB_SERVICE_URL_RETRIEVE: " + CCHECKER_DB_SERVICE_URL_RETRIEVE);
		}
		else {
			throw new RuntimeException("CCHECKER_DB_SERVICE_URL_RETRIEVE parameter is not found");
		}
		
		logger.info("boot.properties:" + properties);
		
		SpringApplication.run(CCheckerLoadFormService.class, args);
	}
}
