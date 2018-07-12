/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

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
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(GatewayBootWebApplication.class);
    }

    public static void main(String[] args) throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("cchecker-gateway.properties");
		//
		Properties properties = new Properties();
		properties.load(input);
		CCHECKER_PARSER_URL = properties.getProperty("CCHECKER_PARSER_URL");
		logger.debug("!!! Loaded CChecker Gateway properties: " + properties);
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