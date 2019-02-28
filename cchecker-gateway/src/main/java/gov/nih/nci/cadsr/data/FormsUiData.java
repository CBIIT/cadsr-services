/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class FormsUiData {

	private List<FormDisplay> formsList = new ArrayList<FormDisplay>();
	private String sessionid;
	private Boolean checkUom;
	private Boolean checkStdCrfCde;
	private Boolean mustDisplayException;
	public List<FormDisplay> getFormsList() {
		return formsList;
	}
	public void setFormsList(List<FormDisplay> formsList) {
		this.formsList = formsList;
	}
	public String getSessionid() {
		return sessionid;
	}
	public void setSessionid(String sessionid) {
		this.sessionid = sessionid;
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
	@Override
	public String toString() {
		return "FormsUiData [formsList=" + formsList + ", sessionid=" + sessionid + ", checkUom=" + checkUom
				+ ", checkStdCrfCde=" + checkStdCrfCde + ", mustDisplayException=" + mustDisplayException + "]";
	}
	
}
