/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class CCCError {
	
List<String> errors = new ArrayList<String>();
String raveProtocolName;
String raveProtocolNumber;

public List<String> getErrors() {
	return errors;
}

public void setErrors(List<String> errors) {
	if (errors != null)
		this.errors = errors;
}

public String getRaveProtocolName() {
	return raveProtocolName;
}

public void setRaveProtocolName(String raveProtocolName) {
	this.raveProtocolName = raveProtocolName;
}

public String getRaveProtocolNumber() {
	return raveProtocolNumber;
}

public void setRaveProtocolNumber(String raveProtocolNumber) {
	this.raveProtocolNumber = raveProtocolNumber;
}

public void addError(String error) {
	errors.add(error);
}

}
