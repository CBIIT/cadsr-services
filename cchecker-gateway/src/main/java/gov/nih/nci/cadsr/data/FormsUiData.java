/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class FormsUiData {

	List<FormDisplay> formsList = new ArrayList<FormDisplay>();
	Boolean checkUom;
	Boolean checkStdCrfCde;
	Boolean mustDisplayException;
	public List<FormDisplay> getFormsList() {
		return formsList;
	}
	public void setFormsList(List<FormDisplay> formsList) {
		this.formsList = formsList;
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
	public Boolean getMustDisplayException() {
		return mustDisplayException;
	}
	public void setMustDisplayException(Boolean mustDisplayException) {
		this.mustDisplayException = mustDisplayException;
	}
	
}
