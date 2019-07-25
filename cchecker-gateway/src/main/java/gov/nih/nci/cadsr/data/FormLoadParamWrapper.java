/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.List;

public class FormLoadParamWrapper {
	
	private String contextName;
	private List<String> selForms;

	public List<String> getSelForms() {
		return selForms;
	}

	public void setSelForms(List<String> selForms) {
		this.selForms = selForms;
	}

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}
	
	@Override
	public String toString() {
		return "[contextName=" + contextName + ", selForms=" + selForms + "]";
	}

}
