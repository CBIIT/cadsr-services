/**
 * Copyright (C) 2016 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import org.springframework.http.HttpStatus;

import gov.nih.nci.cadsr.data.ALSData;

public class StringResponseWrapper {
	private String responseData;
	private HttpStatus statusCode;
	
	public String getResponseData() {
		return responseData;
	}
	public void setResponseData(String responseData) {
		this.responseData = responseData;
	}
	public HttpStatus getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(HttpStatus statusCode) {
		this.statusCode = statusCode;
	}

	@Override
	public String toString() {
		return "StringResponseWrapper [responseData=" + responseData + ", statusCode=" + statusCode + "]";
	}
}
