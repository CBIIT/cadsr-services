/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ALSData {
	
String reportOwner; // From the user input through the browser
String reportDate;
String fileName;
String filePath;

ALSCrfDraft crfDraft = new ALSCrfDraft();
List<ALSForm> forms = new ArrayList<ALSForm>();
List<ALSField> fields = new ArrayList<ALSField>();
Map<String, ALSDataDictionaryEntry> dataDictionaryEntries = new HashMap<String, ALSDataDictionaryEntry>();
List<ALSUnitDictionaryEntry> unitDictionaryEntries = new ArrayList<ALSUnitDictionaryEntry>();
CCCError cccError = new CCCError(); 


public String getReportOwner() {
	return reportOwner;
}
public void setReportOwner(String reportOwner) {
	this.reportOwner = reportOwner;
}

public List<ALSForm> getForms() {
	return forms;
}
public void setForms(List<ALSForm> forms) {
	this.forms = forms;
}
public ALSCrfDraft getCrfDraft() {
	return crfDraft;
}
public void setCrfDraft(ALSCrfDraft crfDraft) {
	this.crfDraft = crfDraft;
}
public List<ALSField> getFields() {
	return fields;
}
public void setFields(List<ALSField> fields) {
	this.fields = fields;
}
public String getReportDate() {
	return reportDate;
}
public void setReportDate(String reportDate) {
	this.reportDate = reportDate;
}
public String getFileName() {
	return fileName;
}
public void setFileName(String fileName) {
	this.fileName = fileName;
}
public String getFilePath() {
	return filePath;
}
public void setFilePath(String filePath) {
	this.filePath = filePath;
}
public Map<String, ALSDataDictionaryEntry> getDataDictionaryEntries() {
	return dataDictionaryEntries;
}
public void setDataDictionaryEntries(Map<String, ALSDataDictionaryEntry> dataDictionaryEntries) {
	this.dataDictionaryEntries = dataDictionaryEntries;
}
public List<ALSUnitDictionaryEntry> getUnitDictionaryEntries() {
	return unitDictionaryEntries;
}
public void setUnitDictionaryEntries(List<ALSUnitDictionaryEntry> unitDictionaryEntries) {
	this.unitDictionaryEntries = unitDictionaryEntries;
}
public CCCError getCccError() {
	return cccError;
}
public void setCccError(CCCError cccError) {
	this.cccError = cccError;
}


}
