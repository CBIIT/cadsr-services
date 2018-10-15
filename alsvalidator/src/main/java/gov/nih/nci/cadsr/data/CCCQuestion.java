/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class CCCQuestion {	

String raveFormOId;
String fieldOrder;
String cdePublicId;
String cdeVersion;
String nciCategory;
String questionCongruencyStatus;
String message;
String raveFieldLabel;
String raveFieldLabelResult;
String cdePermitQuestionTextChoices;
String raveControlType;
String controlTypeResult;
String cdeValueDomainType;
List<String> raveCodedData = new ArrayList<String>();
List<String> codedDataResult = new ArrayList<String>();
String allowableCdeValue;
List<String> raveUserString;
String pvResult;
List<String> allowableCdeTextChoices = new ArrayList<String>();

String raveFieldDataType;
String datatypeCheckerResult;
String cdeDataType;
String raveUOM;
String uomCheckerResult;
String cdeUOM;
String raveLength;
String lengthCheckerResult;
int cdeMaxLength;
String raveDisplayFormat;
String formatCheckerResult;	
String cdeDisplayFormat;

public String getRaveFormOId() {
	return raveFormOId;
}
public void setRaveFormOId(String raveFormOId) {
	this.raveFormOId = raveFormOId;
}
public String getFieldOrder() {
	return fieldOrder;
}
public void setFieldOrder(String fieldOrder) {
	this.fieldOrder = fieldOrder;
}
public String getCdePublicId() {
	return cdePublicId;
}
public void setCdePublicId(String cdePublicId) {
	this.cdePublicId = cdePublicId;
}
public String getCdeVersion() {
	return cdeVersion;
}
public void setCdeVersion(String cdeVersion) {
	this.cdeVersion = cdeVersion;
}
public String getNciCategory() {
	return nciCategory;
}
public void setNciCategory(String nciCategory) {
	this.nciCategory = nciCategory;
}
public String getQuestionCongruencyStatus() {
	return questionCongruencyStatus;
}
public void setQuestionCongruencyStatus(String questionCongruencyStatus) {
	this.questionCongruencyStatus = questionCongruencyStatus;
}
public String getMessage() {
	return message;
}
public void setMessage(String message) {
	this.message = message;
}
public String getRaveFieldLabel() {
	return raveFieldLabel;
}
public void setRaveFieldLabel(String raveFieldLabel) {
	this.raveFieldLabel = raveFieldLabel;
}
public String getRaveFieldLabelResult() {
	return raveFieldLabelResult;
}
public void setRaveFieldLabelResult(String raveFieldLabelResult) {
	this.raveFieldLabelResult = raveFieldLabelResult;
}
public String getCdePermitQuestionTextChoices() {
	return cdePermitQuestionTextChoices;
}
public void setCdePermitQuestionTextChoices(String cdePermitQuestionTextChoices) {
	this.cdePermitQuestionTextChoices = cdePermitQuestionTextChoices;
}
public String getRaveControlType() {
	return raveControlType;
}
public void setRaveControlType(String raveControlType) {
	this.raveControlType = raveControlType;
}
public String getControlTypeResult() {
	return controlTypeResult;
}
public void setControlTypeResult(String controlTypeResult) {
	this.controlTypeResult = controlTypeResult;
}
public String getCdeValueDomainType() {
	return cdeValueDomainType;
}
public void setCdeValueDomainType(String cdeValueDomainType) {
	this.cdeValueDomainType = cdeValueDomainType;
}
public List<String> getRaveCodedData() {
	return raveCodedData;
}
public void setRaveCodedData(List<String> raveCodedData) {
	this.raveCodedData = raveCodedData;
}
public List<String> getCodedDataResult() {
	return codedDataResult;
}
public void setCodedDataResult(List<String> codedDataResult) {
	this.codedDataResult = codedDataResult;
}
public String getAllowableCdeValue() {
	return allowableCdeValue;
}
public void setAllowableCdeValue(String allowableCdeValue) {
	this.allowableCdeValue = allowableCdeValue;
}
public List<String> getRaveUserString() {
	return raveUserString;
}
public void setRaveUserString(List<String> raveUserString) {
	this.raveUserString = raveUserString;
}
public String getPvResult() {
	return pvResult;
}
public void setPvResult(String pvResult) {
	this.pvResult = pvResult;
}
public List<String> getAllowableCdeTextChoices() {
	return allowableCdeTextChoices;
}
public void setAllowableCdeTextChoices(List<String> allowableCdeTextChoices) {
	this.allowableCdeTextChoices = allowableCdeTextChoices;
}
public String getRaveFieldDataType() {
	return raveFieldDataType;
}
public void setRaveFieldDataType(String raveFieldDataType) {
	this.raveFieldDataType = raveFieldDataType;
}
public String getDatatypeCheckerResult() {
	return datatypeCheckerResult;
}
public void setDatatypeCheckerResult(String datatypeCheckerResult) {
	this.datatypeCheckerResult = datatypeCheckerResult;
}
public String getCdeDataType() {
	return cdeDataType;
}
public void setCdeDataType(String cdeDataType) {
	this.cdeDataType = cdeDataType;
}
public String getRaveUOM() {
	return raveUOM;
}
public void setRaveUOM(String raveUOM) {
	this.raveUOM = raveUOM;
}
public String getUomCheckerResult() {
	return uomCheckerResult;
}
public void setUomCheckerResult(String uomCheckerResult) {
	this.uomCheckerResult = uomCheckerResult;
}
public String getCdeUOM() {
	return cdeUOM;
}
public void setCdeUOM(String cdeUOM) {
	this.cdeUOM = cdeUOM;
}
public String getRaveLength() {
	return raveLength;
}
public void setRaveLength(String raveLength) {
	this.raveLength = raveLength;
}
public String getLengthCheckerResult() {
	return lengthCheckerResult;
}
public void setLengthCheckerResult(String lengthCheckerResult) {
	this.lengthCheckerResult = lengthCheckerResult;
}
public int getCdeMaxLength() {
	return cdeMaxLength;
}
public void setCdeMaxLength(int cdeMaxLength) {
	this.cdeMaxLength = cdeMaxLength;
}
public String getRaveDisplayFormat() {
	return raveDisplayFormat;
}
public void setRaveDisplayFormat(String raveDisplayFormat) {
	this.raveDisplayFormat = raveDisplayFormat;
}
public String getFormatCheckerResult() {
	return formatCheckerResult;
}
public void setFormatCheckerResult(String formatCheckerResult) {
	this.formatCheckerResult = formatCheckerResult;
}
public String getCdeDisplayFormat() {
	return cdeDisplayFormat;
}
public void setCdeDisplayFormat(String cdeDisplayFormat) {
	this.cdeDisplayFormat = cdeDisplayFormat;
}	

}
