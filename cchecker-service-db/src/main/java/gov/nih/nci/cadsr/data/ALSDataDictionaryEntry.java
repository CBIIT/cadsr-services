/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class ALSDataDictionaryEntry {
	
String dataDictionaryName;
List<String> codedData = new ArrayList<String>();
List<String> ordinal = new ArrayList<String>();
List<String> userDataString = new ArrayList<String>();

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
public List<String> getOrdinal() {
	return ordinal;
}
public void setOrdinal(List<String> ordinal) {
	this.ordinal = ordinal;
}
public List<String> getUserDataString() {
	return userDataString;
}
public void setUserDataString(List<String> userDataString) {
	this.userDataString = userDataString;
}

}
