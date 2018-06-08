package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class ALSField {
	
String formOid;
String fieldOid;
String ordinal;
String draftFieldNumber;
String draftFieldName;
Boolean draftFieldActive;
String variableOid;
String dataFormat;
String dataDictionaryName;
String unitDictionaryName;
String codingDictionary;
String controlType;
String acceptableFileExtensions;
int indentlevel;
String preText;
String fixedUnit;
String headerText;
String helpText;
Boolean sourceDocument;
Boolean isLog;
String defaultValue;
String sasLabel;
String sasFormat;
String eproFormat;
Boolean isRequired;
Boolean queryFutureDate;
Boolean isVisible;
Boolean isTranslationRequired;
String analyteName;
Boolean isClinicalSignificance;
Boolean queryNonConformance;
Boolean otherVisits;
Boolean canSetRecordDate;
Boolean canSetDataPageDate;
Boolean canSetInstanceDate;
Boolean canSetSubjectDate;
Boolean doesNotBreakSignature;
String lowerRange;
String upperRange;
String ncLowerRange;
String ncUpperRange;
String viewRestrictions;
String entryRestrictions;
String reviewGroups;
Boolean isVisualVerify;

List<ALSDataDictionaryEntry> ddeList = new ArrayList<ALSDataDictionaryEntry>();


public String getFormOid() {
	return formOid;
}
public void setFormOid(String formOid) {
	this.formOid = formOid;
}
public String getFieldOid() {
	return fieldOid;
}
public void setFieldOid(String fieldOid) {
	this.fieldOid = fieldOid;
}
public String getOrdinal() {
	return ordinal;
}
public void setOrdinal(String ordinal) {
	this.ordinal = ordinal;
}
public String getDraftFieldNumber() {
	return draftFieldNumber;
}
public void setDraftFieldNumber(String draftFieldNumber) {
	this.draftFieldNumber = draftFieldNumber;
}
public String getDraftFieldName() {
	return draftFieldName;
}
public void setDraftFieldName(String draftFieldName) {
	this.draftFieldName = draftFieldName;
}
public Boolean getDraftFieldActive() {
	return draftFieldActive;
}
public void setDraftFieldActive(Boolean draftFieldActive) {
	this.draftFieldActive = draftFieldActive;
}
public String getVariableOid() {
	return variableOid;
}
public void setVariableOid(String variableOid) {
	this.variableOid = variableOid;
}
public String getDataFormat() {
	return dataFormat;
}
public void setDataFormat(String dataFormat) {
	this.dataFormat = dataFormat;
}
public String getDataDictionaryName() {
	return dataDictionaryName;
}
public void setDataDictionaryName(String dataDictionaryName) {
	this.dataDictionaryName = dataDictionaryName;
}
public String getUnitDictionaryName() {
	return unitDictionaryName;
}
public void setUnitDictionaryName(String unitDictionaryName) {
	this.unitDictionaryName = unitDictionaryName;
}
public String getCodingDictionary() {
	return codingDictionary;
}
public void setCodingDictionary(String codingDictionary) {
	this.codingDictionary = codingDictionary;
}
public String getControlType() {
	return controlType;
}
public void setControlType(String controlType) {
	this.controlType = controlType;
}
public String getAcceptableFileExtensions() {
	return acceptableFileExtensions;
}
public void setAcceptableFileExtensions(String acceptableFileExtensions) {
	this.acceptableFileExtensions = acceptableFileExtensions;
}
public int getIndentlevel() {
	return indentlevel;
}
public void setIndentlevel(int indentlevel) {
	this.indentlevel = indentlevel;
}
public String getPreText() {
	return preText;
}
public void setPreText(String preText) {
	this.preText = preText;
}
public String getFixedUnit() {
	return fixedUnit;
}
public void setFixedUnit(String fixedUnit) {
	this.fixedUnit = fixedUnit;
}
public String getHeaderText() {
	return headerText;
}
public void setHeaderText(String headerText) {
	this.headerText = headerText;
}
public String getHelpText() {
	return helpText;
}
public void setHelpText(String helpText) {
	this.helpText = helpText;
}
public Boolean getSourceDocument() {
	return sourceDocument;
}
public void setSourceDocument(Boolean sourceDocument) {
	this.sourceDocument = sourceDocument;
}
public Boolean getIsLog() {
	return isLog;
}
public void setIsLog(Boolean isLog) {
	this.isLog = isLog;
}
public String getDefaultValue() {
	return defaultValue;
}
public void setDefaultValue(String defaultValue) {
	this.defaultValue = defaultValue;
}
public String getSasLabel() {
	return sasLabel;
}
public void setSasLabel(String sasLabel) {
	this.sasLabel = sasLabel;
}
public String getSasFormat() {
	return sasFormat;
}
public void setSasFormat(String sasFormat) {
	this.sasFormat = sasFormat;
}
public String getEproFormat() {
	return eproFormat;
}
public void setEproFormat(String eproFormat) {
	this.eproFormat = eproFormat;
}
public Boolean getIsRequired() {
	return isRequired;
}
public void setIsRequired(Boolean isRequired) {
	this.isRequired = isRequired;
}
public Boolean getQueryFutureDate() {
	return queryFutureDate;
}
public void setQueryFutureDate(Boolean queryFutureDate) {
	this.queryFutureDate = queryFutureDate;
}
public Boolean getIsVisible() {
	return isVisible;
}
public void setIsVisible(Boolean isVisible) {
	this.isVisible = isVisible;
}
public Boolean getIsTranslationRequired() {
	return isTranslationRequired;
}
public void setIsTranslationRequired(Boolean isTranslationRequired) {
	this.isTranslationRequired = isTranslationRequired;
}
public String getAnalyteName() {
	return analyteName;
}
public void setAnalyteName(String analyteName) {
	this.analyteName = analyteName;
}
public Boolean getIsClinicalSignificance() {
	return isClinicalSignificance;
}
public void setIsClinicalSignificance(Boolean isClinicalSignificance) {
	this.isClinicalSignificance = isClinicalSignificance;
}
public Boolean getQueryNonConformance() {
	return queryNonConformance;
}
public void setQueryNonConformance(Boolean queryNonConformance) {
	this.queryNonConformance = queryNonConformance;
}
public Boolean getOtherVisits() {
	return otherVisits;
}
public void setOtherVisits(Boolean otherVisits) {
	this.otherVisits = otherVisits;
}
public Boolean getCanSetRecordDate() {
	return canSetRecordDate;
}
public void setCanSetRecordDate(Boolean canSetRecordDate) {
	this.canSetRecordDate = canSetRecordDate;
}
public Boolean getCanSetDataPageDate() {
	return canSetDataPageDate;
}
public void setCanSetDataPageDate(Boolean canSetDataPageDate) {
	this.canSetDataPageDate = canSetDataPageDate;
}
public Boolean getCanSetInstanceDate() {
	return canSetInstanceDate;
}
public void setCanSetInstanceDate(Boolean canSetInstanceDate) {
	this.canSetInstanceDate = canSetInstanceDate;
}
public Boolean getCanSetSubjectDate() {
	return canSetSubjectDate;
}
public void setCanSetSubjectDate(Boolean canSetSubjectDate) {
	this.canSetSubjectDate = canSetSubjectDate;
}
public Boolean getDoesNotBreakSignature() {
	return doesNotBreakSignature;
}
public void setDoesNotBreakSignature(Boolean doesNotBreakSignature) {
	this.doesNotBreakSignature = doesNotBreakSignature;
}
public void setLowerRange(String lowerRange) {
	this.lowerRange = lowerRange;
}
public void setUpperRange(String upperRange) {
	this.upperRange = upperRange;
}
public void setNcLowerRange(String ncLowerRange) {
	this.ncLowerRange = ncLowerRange;
}
public void setNcUpperRange(String ncUpperRange) {
	this.ncUpperRange = ncUpperRange;
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
public String getReviewGroups() {
	return reviewGroups;
}
public void setReviewGroups(String reviewGroups) {
	this.reviewGroups = reviewGroups;
}
public Boolean getIsVisualVerify() {
	return isVisualVerify;
}
public void setIsVisualVerify(Boolean isVisualVerify) {
	this.isVisualVerify = isVisualVerify;
}
public List<ALSDataDictionaryEntry> getDdeList() {
	return ddeList;
}
public void setDdeList(List<ALSDataDictionaryEntry> ddeList) {
	this.ddeList = ddeList;
}




}
