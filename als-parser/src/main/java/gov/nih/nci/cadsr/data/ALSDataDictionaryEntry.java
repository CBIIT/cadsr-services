package gov.nih.nci.cadsr.data;

public class ALSDataDictionaryEntry {
	
String dataDictionaryName;
String codedData;
int ordinal;
String userDataString;
Boolean specify;

public String getDataDictionaryName() {
	return dataDictionaryName;
}
public void setDataDictionaryName(String dataDictionaryName) {
	this.dataDictionaryName = dataDictionaryName;
}
public String getCodedData() {
	return codedData;
}
public void setCodedData(String codedData) {
	this.codedData = codedData;
}
public int getOrdinal() {
	return ordinal;
}
public void setOrdinal(int ordinal) {
	this.ordinal = ordinal;
}
public String getUserDataString() {
	return userDataString;
}
public void setUserDataString(String userDataString) {
	this.userDataString = userDataString;
}
public Boolean getSpecify() {
	return specify;
}
public void setSpecify(Boolean specify) {
	this.specify = specify;
}

}
