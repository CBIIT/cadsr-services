/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class CCCError {

List<ALSError> alsErrors = new ArrayList<ALSError>();
String raveProtocolName;
String raveProtocolNumber;


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

public List<ALSError> getAlsErrors() {
	return alsErrors;
}

public void setAlsErrors(List<ALSError> alsErrors) {
	this.alsErrors = alsErrors;
}

public void addAlsError(ALSError alsError) {
	alsErrors.add(alsError);
}

}
