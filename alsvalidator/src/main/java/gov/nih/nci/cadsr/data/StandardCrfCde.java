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

}
