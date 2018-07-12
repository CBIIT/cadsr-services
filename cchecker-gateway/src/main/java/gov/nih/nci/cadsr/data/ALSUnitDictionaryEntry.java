/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

public class ALSUnitDictionaryEntry {
	
String unitDictionaryName;
String codedUnit;
int ordinal;
int constantA;
int constantB;
int constantC;
int constantK;
String unitString;

public String getUnitDictionaryName() {
	return unitDictionaryName;
}
public void setUnitDictionaryName(String unitDictionaryName) {
	this.unitDictionaryName = unitDictionaryName;
}
public String getCodedUnit() {
	return codedUnit;
}
public void setCodedUnit(String codedUnit) {
	this.codedUnit = codedUnit;
}
public int getOrdinal() {
	return ordinal;
}
public void setOrdinal(int ordinal) {
	this.ordinal = ordinal;
}
public int getConstantA() {
	return constantA;
}
public void setConstantA(int constantA) {
	this.constantA = constantA;
}
public int getConstantB() {
	return constantB;
}
public void setConstantB(int constantB) {
	this.constantB = constantB;
}
public int getConstantC() {
	return constantC;
}
public void setConstantC(int constantC) {
	this.constantC = constantC;
}
public int getConstantK() {
	return constantK;
}
public void setConstantK(int constantK) {
	this.constantK = constantK;
}
public String getUnitString() {
	return unitString;
}
public void setUnitString(String unitString) {
	this.unitString = unitString;
}



}
