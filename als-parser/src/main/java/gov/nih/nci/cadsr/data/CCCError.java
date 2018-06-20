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

public void setErrorDescription(List<String> errors) {
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



}
