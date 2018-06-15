package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ALSData {
	
String reportOwner; // From the user input through the browser
String raveProtocolName;
String raveProtocolNumber;
String reportDate;

List<ALSCrfDraft> crfDrafts = new ArrayList<ALSCrfDraft>();
List<ALSForm> forms = new ArrayList<ALSForm>();
List<ALSField> fields = new ArrayList<ALSField>();
Map<String, ALSDataDictionaryEntry> dataDictionaryEntries = new HashMap<String, ALSDataDictionaryEntry>();


public String getReportOwner() {
	return reportOwner;
}
public void setReportOwner(String reportOwner) {
	this.reportOwner = reportOwner;
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

public List<ALSForm> getForms() {
	return forms;
}
public void setForms(List<ALSForm> forms) {
	this.forms = forms;
}
public List<ALSCrfDraft> getCrfDrafts() {
	return crfDrafts;
}
public void setCrfDrafts(List<ALSCrfDraft> crfDrafts) {
	this.crfDrafts = crfDrafts;
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
public Map<String, ALSDataDictionaryEntry> getDataDictionaryEntries() {
	return dataDictionaryEntries;
}
public void setDataDictionaryEntries(Map<String, ALSDataDictionaryEntry> dataDictionaryEntries) {
	this.dataDictionaryEntries = dataDictionaryEntries;
}


}
