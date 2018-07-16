/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class FormDisplay {
	
	Boolean isValid;
	List<ALSError> errors = new ArrayList<ALSError>();
	String formName;
	int questionsCount;
	
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
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}	
	public int getQuestionsCount() {
		return questionsCount;
	}
	public void setQuestionsCount(int questionsCount) {
		this.questionsCount = questionsCount;
	}

}
