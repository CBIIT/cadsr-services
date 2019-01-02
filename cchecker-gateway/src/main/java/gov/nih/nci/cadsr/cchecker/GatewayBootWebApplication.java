/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.swagger2.annotations.EnableSwagger2;


@SpringBootApplication
@EnableAutoConfiguration
@EnableSwagger2
@ComponentScan(basePackages = {"gov.nih.nci.cadsr.cchecker"})
@EntityScan("gov.nih.nci.cadsr.cchecker.data")
public class GatewayBootWebApplication extends SpringBootServletInitializer {
	private final static Logger logger = LoggerFactory.getLogger(GatewayBootWebApplication.class);
	static String CCHECKER_PARSER_URL;
	static String CCHECKER_DB_SERVICE_URL_CREATE;
	static String CCHECKER_DB_SERVICE_URL_RETRIEVE;
	static String CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR;
	static String CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_ERROR;
	static String CCHECKER_DB_SERVICE_URL_CREATE_REPORT_FULL;
	static String CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_FULL;
	static String CCHECKER_VALIDATE_SERVICE_URL;
	static String CCHECKER_FEED_VALIDATE_SERVICE_URL;
	static String CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL;
	//Save the uploaded file to this folder
	static String UPLOADED_FOLDER;
	static String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
	static String ACCESS_CONTROL_ALLOW_ORIGIN;
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		//
		Properties properties = new Properties();
		try (InputStream input = classLoader.getResourceAsStream("cchecker-gateway.properties")) {
			properties.load(input);
		} 
		catch (IOException e) {
			logger.error("!!! CChecker Gateway properties load failure: " + e);
		    /**
		     * If properties not found throws runtime exception
		     */
			e.printStackTrace();
			throw new RuntimeException (e);
		}
		CCHECKER_PARSER_URL = properties.getProperty("CCHECKER_PARSER_URL");
		UPLOADED_FOLDER =  properties.getProperty("UPLOADED_FOLDER");
		CCHECKER_DB_SERVICE_URL_CREATE = properties.getProperty("CCHECKER_DB_SERVICE_URL_CREATE");
		CCHECKER_DB_SERVICE_URL_RETRIEVE = properties.getProperty("CCHECKER_DB_SERVICE_URL_RETRIEVE");
		CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR = properties.getProperty("CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR");
		CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_ERROR = properties.getProperty("CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_ERROR");
		CCHECKER_DB_SERVICE_URL_CREATE_REPORT_FULL = properties.getProperty("CCHECKER_DB_SERVICE_URL_CREATE_REPORT_FULL");
		CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_FULL = properties.getProperty("CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_FULL");
		CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL = properties.getProperty("CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL");
		ACCESS_CONTROL_ALLOW_ORIGIN = properties.getProperty(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER);
		CCHECKER_VALIDATE_SERVICE_URL = properties.getProperty("CCHECKER_VALIDATE_SERVICE_URL");
		CCHECKER_FEED_VALIDATE_SERVICE_URL = properties.getProperty("CCHECKER_FEED_VALIDATE_SERVICE_URL");
		logger.info("!!! Loaded CChecker Gateway properties: " + properties);

        return application.sources(GatewayBootWebApplication.class);
    }

    public static void main(String[] args) throws Exception {
    	final SpringApplication application = new SpringApplication(GatewayBootWebApplication.class);
        //application.setBannerMode(Banner.Mode.OFF);
        //application.setWebApplicationType(WebApplicationType.SERVLET);//defined in properties
        application.run(args);
        //we can use the call below if other parameters are not needed
        //SpringApplication.run(GatewayBootWebApplication.class, args);
    }

}
/*
@SpringBootApplication
public class SpringBootWebApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SpringBootWebApplication.class, args);
	}

}*/