/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class FormDisplay {
	
	String formName;
	Boolean isValid;
	List<ALSError> errors = new ArrayList<ALSError>();
	
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public Boolean getIsValid() {
		return isValid;
	}
	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}
	public List<ALSError> getErrors() {
		return errors;
	}
	public void setErrors(List<ALSError> errors) {
		this.errors = errors;
	}

}
