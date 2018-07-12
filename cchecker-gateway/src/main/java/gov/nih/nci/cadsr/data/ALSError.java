/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

public class ALSError {
	
String errorDesc;
String sheetName;
String rowNumber;
String colNumber;
String formOid;
String fieldOid;
String dataDictionaryName;
String unitDictionaryName;
String errorSeverity; // FATAL, ERROR, WARNING

public String getErrorDesc() {
	return errorDesc;
}
public void setErrorDesc(String errorDesc) {
	this.errorDesc = errorDesc;
}
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
public String getErrorSeverity() {
	return errorSeverity;
}
public void setErrorSeverity(String errorSeverity) {
	this.errorSeverity = errorSeverity;
}
public String getSheetName() {
	return sheetName;
}
public void setSheetName(String sheetName) {
	this.sheetName = sheetName;
}
public String getRowNumber() {
	return rowNumber;
}
public void setRowNumber(String rowNumber) {
	this.rowNumber = rowNumber;
}
public String getColNumber() {
	return colNumber;
}
public void setColNumber(String colNumber) {
	this.colNumber = colNumber;
}
@Override
public String toString() {
	return "ALSError [errorDesc=" + errorDesc + ", formOid=" + formOid + ", fieldOid=" + fieldOid
			+ ", dataDictionaryName=" + dataDictionaryName + ", unitDictionaryName=" + unitDictionaryName
			+ ", errorSeverity=" + errorSeverity + "]";
}

}
