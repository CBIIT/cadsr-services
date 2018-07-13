/**
 * Copyright (C) 2016 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import org.springframework.http.HttpStatus;

import gov.nih.nci.cadsr.data.ALSData;

public class ALSDataWrapper {
	private ALSData alsData;
	public ALSData getAlsData() {
		return alsData;
	}
	public void setAlsData(ALSData alsData) {
		this.alsData = alsData;
	}
	public HttpStatus getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(HttpStatus statusCode) {
		this.statusCode = statusCode;
	}
	private HttpStatus statusCode;
	
}
