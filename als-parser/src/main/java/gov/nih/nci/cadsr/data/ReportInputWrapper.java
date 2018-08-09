/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.List;

public class ReportInputWrapper {
	
ALSData alsData;
List<String> selForms;
Boolean checkUom;
Boolean checkStdCrfCde;
Boolean displayExceptionDetails;

public ALSData getAlsData() {
	return alsData;
}
public void setAlsData(ALSData alsData) {
	this.alsData = alsData;
}
public List<String> getSelForms() {
	return selForms;
}
public void setSelForms(List<String> selForms) {
	this.selForms = selForms;
}
public Boolean getCheckUom() {
	return checkUom;
}
public void setCheckUom(Boolean checkUom) {
	this.checkUom = checkUom;
}
public Boolean getCheckStdCrfCde() {
	return checkStdCrfCde;
}
public void setCheckStdCrfCde(Boolean checkStdCrfCde) {
	this.checkStdCrfCde = checkStdCrfCde;
}
public Boolean getDisplayExceptionDetails() {
	return displayExceptionDetails;
}
public void setDisplayExceptionDetails(Boolean displayExceptionDetails) {
	this.displayExceptionDetails = displayExceptionDetails;
}


}
