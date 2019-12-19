/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

public class StandardCrfCde {	

String stdTemplateType; //Mandatory, Conditional or Optional
String cdeIdVersion;
String cdeName;
String templateName;
String idVersion;
String preferredQuestionText;

// FORMBUILD-636
String raveFormOid;
String raveFieldOrder;
String raveFieldLabel;
String result; // ERRORS/WARNINGS
String message;

public String getStdTemplateType() {
	return stdTemplateType;
}
public void setStdTemplateType(String stdTemplateType) {
	this.stdTemplateType = stdTemplateType;
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
public String getTemplateName() {
	return templateName;
}
public void setTemplateName(String templateName) {
	this.templateName = templateName;
}
public String getIdVersion() {
	return idVersion;
}
public void setIdVersion(String idVersion) {
	this.idVersion = idVersion;
}
public String getPreferredQuestionText() {
	return preferredQuestionText;
}
public void setPreferredQuestionText(String preferredQuestionText) {
	this.preferredQuestionText = preferredQuestionText;
}
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

}
