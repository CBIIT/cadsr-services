/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.HashMap;
import java.util.Map;

public class ALSField {
	
String formOid;
String fieldOid;
String ordinal;
String draftFieldName;
String dataFormat;
String dataDictionaryName;
String unitDictionaryName;
String controlType;
String preText;
String fixedUnit;

Map<String, ALSDataDictionaryEntry> ddeMap = new HashMap<String, ALSDataDictionaryEntry>();


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
public String getDraftFieldName() {
	return draftFieldName;
}
public void setDraftFieldName(String draftFieldName) {
	this.draftFieldName = draftFieldName;
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
public String getDataFormat() {
	return dataFormat;
}
public void setDataFormat(String dataFormat) {
	this.dataFormat = dataFormat;
}
public String getControlType() {
	return controlType;
}
public void setControlType(String controlType) {
	this.controlType = controlType;
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
public Map<String, ALSDataDictionaryEntry> getDdeMap() {
	return ddeMap;
}
public void setDdeMap(Map<String, ALSDataDictionaryEntry> ddeMap) {
	this.ddeMap = ddeMap;
}

}
