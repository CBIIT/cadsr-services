package gov.nih.nci.cadsr.data;

public class CCCForm {	

String raveFormOId;
String caDSRFormId;
int countTotalQuestions;
String version;
String fieldOrder;
// placeholder for missing field on row #35 in DetailedRequirements
String cdePublicId;
String cdeVersion;
String nciCategory;
String questionCongruenceStatus;
String message;
String raveFieldLabel;
String raveFieldLabelResult;
String cdePermitQuestionTextChoices;
String raveControlType;
String controlTypeResult;
String cdeValueDomainType;
//missing field for row #47 - String dataDictionaryName;
String 	raveCodedData;
String codedDataResult;
String allowableCdeValue;
String raveUserString;
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
public String getCaDSRFormId() {
	return caDSRFormId;
}
public void setCaDSRFormId(String caDSRFormId) {
	this.caDSRFormId = caDSRFormId;
}
public int getCountTotalQuestions() {
	return countTotalQuestions;
}
public void setCountTotalQuestions(int countTotalQuestions) {
	this.countTotalQuestions = countTotalQuestions;
}
public String getVersion() {
	return version;
}
public void setVersion(String version) {
	this.version = version;
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
public String getQuestionCongruenceStatus() {
	return questionCongruenceStatus;
}
public void setQuestionCongruenceStatus(String questionCongruenceStatus) {
	this.questionCongruenceStatus = questionCongruenceStatus;
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
public String getRaveCodedData() {
	return raveCodedData;
}
public void setRaveCodedData(String raveCodedData) {
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
public String getRaveUserString() {
	return raveUserString;
}
public void setRaveUserString(String raveUserString) {
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
