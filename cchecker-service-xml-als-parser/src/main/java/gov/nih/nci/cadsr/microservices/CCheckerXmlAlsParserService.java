/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

//import java.io.InputStream;
//import java.util.Properties;

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
//(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class CCheckerXmlAlsParserService {
	private static final Logger logger = LoggerFactory.getLogger(CCheckerXmlAlsParserService.class);
	@RequestMapping("/")
	String home() {
		return "CCheckerXmlAlsParserService is running!\n";
	}
	public static void main(String[] args) throws Exception {
		//We do not have microservice specific properties
//		Properties properties = new Properties();
//		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//		try (InputStream input = classLoader.getResourceAsStream("boot.properties")) {
//			properties.load(input);
//		}
		SpringApplication.run(CCheckerXmlAlsParserService.class, args);
	}
}
