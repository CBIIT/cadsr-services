/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

public class NrdsCde {	

String raveFormOid;
String raveFieldOrder;
String raveFieldLabel;
String cdeIdVersion;
String cdeName;
String preferredQuestionText;
String result; // ERRORS/WARNINGS
String message;
String type;

public String getRaveFormOid() {
	return raveFormOid;
}
public void setRaveFormOid(String raveFormOid) {
	this.raveFormOid = raveFormOid;
}
public String getRaveFieldOrder() {
	return raveFieldOrder;
}
public void setRaveFieldOrder(String raveFieldOrder) {
	this.raveFieldOrder = raveFieldOrder;
}
public String getRaveFieldLabel() {
	return raveFieldLabel;
}
public void setRaveFieldLabel(String raveFieldLabel) {
	this.raveFieldLabel = raveFieldLabel;
}
public String getCdeIdVersion() {
	return cdeIdVersion;
}
public void setCdeIdVersion(String cdeIdVersion) {
	this.cdeIdVersion = cdeIdVersion;
}
public String getCdeName() {
	return cdeName;
}
public void setCdeName(String cdeName) {
	this.cdeName = cdeName;
}
public String getPreferredQuestionText() {
	return preferredQuestionText;
}
public void setPreferredQuestionText(String preferredQuestionText) {
	this.preferredQuestionText = preferredQuestionText;
}
public String getResult() {
	return result;
}
public void setResult(String result) {
	this.result = result;
}
public String getMessage() {
	return message;
}
public void setMessage(String message) {
	this.message = message;
}
public String getType() {
	return type;
}
public void setType(String type) {
	this.type = type;
}

}
