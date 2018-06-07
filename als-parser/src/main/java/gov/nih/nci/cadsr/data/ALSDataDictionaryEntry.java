package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class ALSDataDictionaryEntry {
	
String dataDictionaryName;
List<String> codedData = new ArrayList<String>();
List<Integer> ordinal = new ArrayList<Integer>();
List<String> userDataString = new ArrayList<String>();
List<Boolean> specify = new ArrayList<Boolean>();

public String getDataDictionaryName() {
	return dataDictionaryName;
}
public void setDataDictionaryName(String dataDictionaryName) {
	this.dataDictionaryName = dataDictionaryName;
}
public List<String> getCodedData() {
	return codedData;
}
public void setCodedData(List<String> codedData) {
	this.codedData = codedData;
}
public List<Integer> getOrdinal() {
	return ordinal;
}
public void setOrdinal(List<Integer> ordinal) {
	this.ordinal = ordinal;
}
public List<String> getUserDataString() {
	return userDataString;
}
public void setUserDataString(List<String> userDataString) {
	this.userDataString = userDataString;
}
public List<Boolean> getSpecify() {
	return specify;
}
public void setSpecify(List<Boolean> specify) {
	this.specify = specify;
}

}
