package gov.nih.nci.cadsr.data;

public class NrdsCde {	

String raveFormOid;
int raveFieldOrder;
String raveFieldLabel;
String cdeIdVersion;
String cdeName;
String result; // ERRORS/WARNINGS
String message;

public String getRaveFormOid() {
	return raveFormOid;
}
public void setRaveFormOid(String raveFormOid) {
	this.raveFormOid = raveFormOid;
}
public int getRaveFieldOrder() {
	return raveFieldOrder;
}
public void setRaveFieldOrder(int raveFieldOrder) {
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
