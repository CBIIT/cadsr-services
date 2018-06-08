package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class ALSForm {
	
String formOId;
int ordinal;
String draftFormName;
String draftFormActive;
String helpText;
// placeholder for missing field on row #35 in DetailedRequirements
Boolean isTemplate;
Boolean isSignRequired;
Boolean isEproForm;
String viewRestrictions;
String entryRestrictions;
String logDirection;
String ddeOption;
String confirmationStyle;
String linkFolderOid;
String linkFormOid;

List<ALSField> fields = new ArrayList<ALSField>();

public String getFormOId() {
	return formOId;
}
public void setFormOId(String formOId) {
	this.formOId = formOId;
}
public int getOrdinal() {
	return ordinal;
}
public void setOrdinal(int ordinal) {
	this.ordinal = ordinal;
}
public String getDraftFormName() {
	return draftFormName;
}
public void setDraftFormName(String draftFormName) {
	this.draftFormName = draftFormName;
}
public String getDraftFormActive() {
	return draftFormActive;
}
public void setDraftFormActive(String draftFormActive) {
	this.draftFormActive = draftFormActive;
}
public String getHelpText() {
	return helpText;
}
public void setHelpText(String helpText) {
	this.helpText = helpText;
}
public Boolean getIsTemplate() {
	return isTemplate;
}
public void setIsTemplate(Boolean isTemplate) {
	this.isTemplate = isTemplate;
}
public Boolean getIsSignRequired() {
	return isSignRequired;
}
public void setIsSignRequired(Boolean isSignRequired) {
	this.isSignRequired = isSignRequired;
}
public Boolean getIsEproForm() {
	return isEproForm;
}
public void setIsEproForm(Boolean isEproForm) {
	this.isEproForm = isEproForm;
}
public String getViewRestrictions() {
	return viewRestrictions;
}
public void setViewRestrictions(String viewRestrictions) {
	this.viewRestrictions = viewRestrictions;
}
public String getEntryRestrictions() {
	return entryRestrictions;
}
public void setEntryRestrictions(String entryRestrictions) {
	this.entryRestrictions = entryRestrictions;
}
public String getLogDirection() {
	return logDirection;
}
public void setLogDirection(String logDirection) {
	this.logDirection = logDirection;
}
public String getDdeOption() {
	return ddeOption;
}
public void setDdeOption(String ddeOption) {
	this.ddeOption = ddeOption;
}
public String getConfirmationStyle() {
	return confirmationStyle;
}
public void setConfirmationStyle(String confirmationStyle) {
	this.confirmationStyle = confirmationStyle;
}
public String getLinkFolderOid() {
	return linkFolderOid;
}
public void setLinkFolderOid(String linkFolderOid) {
	this.linkFolderOid = linkFolderOid;
}
public String getLinkFormOid() {
	return linkFormOid;
}
public void setLinkFormOid(String linkFormOid) {
	this.linkFormOid = linkFormOid;
}
public List<ALSField> getFields() {
	return fields;
}
public void setFields(List<ALSField> fields) {
	this.fields = fields;
}

}
