package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class CCCQuestion {	

String raveFormOId;
String fieldOrder;
// placeholder for missing field on row #35 in DetailedRequirements
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
//missing field for row #47 - String dataDictionaryName;
List<String> raveCodedData = new ArrayList<String>();
String codedDataResult;
String allowableCdeValue;
List<String> raveUserString;
String pvResult;
String allowableCdeTextChoices;

//Below section of properties do not have a source, data type or any other info related to them. Seen below is their placeholder.

String raveFieldDataType;
String datatypeResult;
String cdeDataType;
String raveUOM;
String uomResult;
String cdeUOM;
String raveLength;
String lengthResult;
String cdeMaxLength;
String raveDisplayFormat;
String formatResult;	
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
public String getCodedDataResult() {
	return codedDataResult;
}
public void setCodedDataResult(String codedDataResult) {
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
public String getAllowableCdeTextChoices() {
	return allowableCdeTextChoices;
}
public void setAllowableCdeTextChoices(String allowableCdeTextChoices) {
	this.allowableCdeTextChoices = allowableCdeTextChoices;
}	

}
