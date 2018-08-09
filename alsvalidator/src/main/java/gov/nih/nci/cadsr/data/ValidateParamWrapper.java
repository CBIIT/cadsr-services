/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.List;

public class ValidateParamWrapper {
	
	Boolean checkUom = false;
	Boolean checkCrf = false;
	Boolean displayExceptions = false;
	List<String> selForms;

	public Boolean getCheckUom() {
		return checkUom;
	}

	public void setCheckUom(Boolean checkUom) {
		this.checkUom = checkUom;
	}

	public Boolean getCheckCrf() {
		return checkCrf;
	}

	public void setCheckCrf(Boolean checkCrf) {
		this.checkCrf = checkCrf;
	}

	public Boolean getDisplayExceptions() {
		return displayExceptions;
	}

	public void setDisplayExceptions(Boolean displayExceptions) {
		this.displayExceptions = displayExceptions;
	}

	public List<String> getSelForms() {
		return selForms;
	}


	public void setSelForms(List<String> selForms) {
		this.selForms = selForms;
	}

	@Override
	public String toString() {
		return "ValidateParamWrapper [checkUom=" + checkUom + ", checkCrf=" + checkCrf + ", displayExceptions="
				+ displayExceptions + ", selForms=" + selForms + "]";
	}

}
