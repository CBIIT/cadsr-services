package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class FormDisplay {
	
	String formName;
	String context;
	String workflowStatus;
	Boolean isValid;
	List<ALSError> errors = new ArrayList<ALSError>();
	
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getWorkflowStatus() {
		return workflowStatus;
	}
	public void setWorkflowStatus(String workflowStatus) {
		this.workflowStatus = workflowStatus;
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
