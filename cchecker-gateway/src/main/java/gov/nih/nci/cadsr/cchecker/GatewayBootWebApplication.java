/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;


@SpringBootApplication(exclude = {EmbeddedServletContainerAutoConfiguration.class/*, WebMvcAutoConfiguration.class*/})
public class GatewayBootWebApplication extends SpringBootServletInitializer {
	private final static Logger logger = LoggerFactory.getLogger(GatewayBootWebApplication.class);
	static String CCHECKER_PARSER_URL;
	static String CCHECKER_DB_SERVICE_URL_CREATE;
	static String CCHECKER_DB_SERVICE_URL_RETRIEVE;
	//Save the uploaded file to this folder
	static String UPLOADED_FOLDER;
	static String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
	static String ACCESS_CONTROL_ALLOW_ORIGIN;
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("cchecker-gateway.properties");
		//
		Properties properties = new Properties();
		try {
			properties.load(input);
		} catch (IOException e) {
			logger.error("!!! Loaded CChecker Gateway properties load failure" + e);
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
		ACCESS_CONTROL_ALLOW_ORIGIN = properties.getProperty(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER);
		logger.debug("!!! Loaded CChecker Gateway properties: " + properties);

        return application.sources(GatewayBootWebApplication.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(GatewayBootWebApplication.class, args);
    }

}
/*
@SpringBootApplication
public class SpringBootWebApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SpringBootWebApplication.class, args);
	}

}*/