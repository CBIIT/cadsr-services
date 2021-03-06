/*
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"gov.nih.nci.cadsr.microservices"})
@RestController
@EnableAutoConfiguration
public class CCheckerDbService {
    private static final Logger logger = LoggerFactory.getLogger(CCheckerDbService.class);

	@RequestMapping("/")
	String home() {
		return "CChecker DB Service is running!\n";
	}
//    @Value("classpath:boot.properties")
//    private Resource res;
    //Save the uploaded file to this folder
    static String UPLOADED_FOLDER;// ="/local/content/cchecker";//default
    static String CDEBROWSER_REST_GET_CDE;
	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream input = classLoader.getResourceAsStream("boot.properties")) {
			properties.load(input);
		}
		String propVal;
		if ((propVal = properties.getProperty("UPLOADED_FOLDER")) != null)
			UPLOADED_FOLDER = propVal;
		else UPLOADED_FOLDER = "/local/content/cchecker";
		if ((propVal = properties.getProperty("CDEBROWSER_REST_GET_CDE")) != null) {
			CDEBROWSER_REST_GET_CDE = propVal;
		}
	    else {
	    	throw new RuntimeException("CDEBROWSER_REST_GET_CDE parameter is not found");
	    }
		File uploadeDir = new File(UPLOADED_FOLDER);
		if (! uploadeDir.exists())	{
			uploadeDir.mkdirs();//OK for testing only
		}
		final ApplicationContext ctx = SpringApplication.run(CCheckerDbService.class, args);
		DataSource ds = ctx.getBean(DataSource.class);
		if (ds != null)
			logger.debug("DataSource bean in use: "+ ds.getClass().getName());
		else {
			logger.error("!!! DataSource bean not defined !!!");
		}
	}
}