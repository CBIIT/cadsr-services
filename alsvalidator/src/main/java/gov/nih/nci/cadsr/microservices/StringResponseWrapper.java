/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((responseData == null) ? 0 : responseData.hashCode());
		result = prime * result + ((statusCode == null) ? 0 : statusCode.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StringResponseWrapper other = (StringResponseWrapper) obj;
		if (responseData == null) {
			if (other.responseData != null)
				return false;
		} else if (!responseData.equals(other.responseData))
			return false;
		if (statusCode != other.statusCode)
			return false;
		return true;
	}
	
}
