package gov.nih.nci.cadsr.cchecker;
/*
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component("restCallsInterceptor")
public class RestCallsInterceptor implements HandlerInterceptor {

	private final static Logger logger = LoggerFactory.getLogger(RestCallsInterceptor.class);
	public static final String REST_CONTROLLER_USAGE_PREFIX_REQUEST = "CCHECKER_REST_REQUEST";
	public static final String REST_CONTROLLER_USAGE_PREFIX_RESPONSE = "CCHECKER_REST_RESPONSE";
	public static final String REST_CONTROLLER_ERROR = "CCHECKER_REST_ERROR";
	public static final String LOG_DATE_TIME_FORMAT = "YYYY-MM-dd'T'HH:mm:ss.SSS";
	public static final String REST_USAGE_LOG_REQUEST_FORMAT = "[%s][%s][%s][%s][%s][%s][%s]";//prefix time method URI query cookie [owner]
	public static final String REST_USAGE_LOG_REQUEST_FORMAT_1 = "[%s][%s][%s][%s][%s][%s]";//prefix time method URI query cookie [owner]
	public static final String REST_USAGE_LOG_RESPONSE_FORMAT = "[%s][%s][response code %d][%s][%s][%s]";//prefix time code method URI query
	public static final String REST_USAGE_LOG_ERROR_FORMAT = "[%s][%s][%s]";//prefix time code method URI query

	@Override
	public void afterCompletion(HttpServletRequest request,
		HttpServletResponse response, Object object, Exception exception) {
		if (exception != null) {
			LocalDateTime now = LocalDateTime.now();
			String formattedCurrentDate = now.format(DateTimeFormatter.ofPattern( LOG_DATE_TIME_FORMAT));
			String toLog = String.format(REST_USAGE_LOG_ERROR_FORMAT, REST_CONTROLLER_ERROR, formattedCurrentDate, exception.getMessage());
			logger.info(toLog);
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object object, ModelAndView model) throws Exception {
		int responseCode = response.getStatus();
		LocalDateTime now = LocalDateTime.now();
		String formattedCurrentDate = now.format( DateTimeFormatter.ofPattern( LOG_DATE_TIME_FORMAT));
		String toLog = String.format(REST_USAGE_LOG_RESPONSE_FORMAT, REST_CONTROLLER_USAGE_PREFIX_RESPONSE, formattedCurrentDate, responseCode, request.getMethod(), request.getRequestURI(),request.getQueryString());
		logger.info(toLog);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		LocalDateTime now = LocalDateTime.now();
		String formattedCurrentDate = now.format( DateTimeFormatter.ofPattern( LOG_DATE_TIME_FORMAT));
		Cookie[] cookieReceived = request.getCookies();
		List<String> cookieList = new ArrayList<>();
				
		if (cookieReceived != null) {
			for (Cookie cookie : cookieReceived) {
				String tmp = cookie.getValue();
				if (tmp != null) {
					cookieList.add(tmp);
				}
			}
		}
		String paramOwner = request.getParameter("owner");
		paramOwner = (StringUtils.isNotBlank(paramOwner)) ? paramOwner : "";
		//prefix time method URI query cookie [owner]
		String toLog = formatRequestLog(
			formattedCurrentDate, request.getMethod(), request.getRequestURI(),
			request.getQueryString(), cookieList.toString(), paramOwner);
		logger.info(toLog);
		
		return true;
	}
	protected static String formatRequestLog(String formattedCurrentDate, String reqMethod, String reqURI, String reqQuery, String reqCookieList, String paramOwner) {
		String toLog;
		if (StringUtils.isNotBlank(paramOwner)) {
		toLog = String.format(REST_USAGE_LOG_REQUEST_FORMAT, 
			REST_CONTROLLER_USAGE_PREFIX_REQUEST, 
			formattedCurrentDate, reqMethod, reqURI,
			reqQuery, reqCookieList, paramOwner);
		}
		else {
			toLog = String.format(REST_USAGE_LOG_REQUEST_FORMAT_1, 
					REST_CONTROLLER_USAGE_PREFIX_REQUEST, 
					formattedCurrentDate, reqMethod, reqURI,
					reqQuery, reqCookieList);
		}
		return toLog;
	}
}