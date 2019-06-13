/*
 * Copyright (C) 2019 FNLCR. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
/**
 * This filter is mostly to add session information to microservice console log.
 * 
 * @author asafievan
 *
 */
@Component
public class RequestFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RequestFilter.class);
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			//Setup MDC data:
			String sessionCookie = request.getParameter("_cchecker");
			if (StringUtils.isNotBlank(sessionCookie)) {
				String mdcData = String.format("| %s |", sessionCookie);
				//Variable 'sessionId' is referenced in Spring Boot's logging.pattern.console property
				MDC.put("sessionId", mdcData); 
			}
			chain.doFilter(request, response);
		} finally {
			//Tear down MDC data. Cleans up the ThreadLocal.
			MDC.clear();
		}
	}
}
