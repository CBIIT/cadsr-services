/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"gov.nih.nci.cadsr.microservices"})
@RestController
@EnableAutoConfiguration
//(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class CCheckerParserService {
	private final Logger logger = LoggerFactory.getLogger(CCheckerParserService.class);
    static String UPLOADED_FOLDER;
	@RequestMapping("/")
	String home() {
		return "CCheckerParserService is running!\n";
	}
	public static void main(String[] args) throws Exception {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("boot.properties");
		//
		Properties properties = new Properties();
		properties.load(input);
		String propVal;
		if ((propVal = properties.getProperty("UPLOADED_FOLDER")) != null)
			UPLOADED_FOLDER = propVal;
		else UPLOADED_FOLDER = "/local/content/cchecker";

		File uploadeDir = new File(UPLOADED_FOLDER);
		if (! uploadeDir.exists())	{
			uploadeDir.mkdirs();//OK for testing only
		}
		SpringApplication.run(CCheckerParserService.class, args);
	}
}
