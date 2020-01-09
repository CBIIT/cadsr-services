/*
 * Copyright (C) 2019 Frederick National Laboratory for Cancer Research - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer /*extends WebMvcConfigurationSupport*/ {
	private static final Logger logger = LoggerFactory.getLogger(InterceptorConfiguration.class);
	@Autowired
	RestCallsInterceptor restCallsInterceptor;
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(restCallsInterceptor);
	}
}