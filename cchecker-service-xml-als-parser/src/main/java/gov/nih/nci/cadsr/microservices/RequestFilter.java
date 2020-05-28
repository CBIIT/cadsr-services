/*
 * Copyright (C) 2019 FNLCR. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
			String paramFilePath = request.getParameter("filepath");
			String sessionId = parseSessionId(paramFilePath);
			if (StringUtils.isNotBlank(sessionId)) {
				String mdcData = String.format("| %s |", sessionId);
				//Variable 'sessionId' is referenced in Spring Boot's logging.pattern.console property
				MDC.put("sessionId", mdcData);
			}
			chain.doFilter(request, response);
		} finally {
			//Tear down MDC data. Cleans up the ThreadLocal.
			MDC.clear();
		}
	}
	/**
	 * File name is based on a session ID.
	 * Example: /local/content/cchecker/35F24768-8364-4197-BD5C-C7CCCDD39597.xlsx
	 * We parse session ID from the received file name. In the example, 35F24768-8364-4197-BD5C-C7CCCDD39597.
	 * 
	 * @param paramFilePath
	 * @return String sessionId
	 */
	protected static String parseSessionId(String paramFilePath ) {
		String sessionId = null;
		if (StringUtils.isNotBlank(paramFilePath)) {
			int posStart = paramFilePath.lastIndexOf(File.separator) + 1;//next after the last '/', the first character in session
			int posEnd = paramFilePath.lastIndexOf('.');
			if ((posStart > 0) && (posEnd >= 0) && (posStart < (posEnd - 1))) {
				sessionId = paramFilePath.substring(posStart, posEnd);
			}
		}
		return sessionId;
	}
}
