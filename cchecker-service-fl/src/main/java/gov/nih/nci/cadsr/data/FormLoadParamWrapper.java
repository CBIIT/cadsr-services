/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.List;

public class FormLoadParamWrapper {
	
	private List<String> selForms;
	private String contextName;

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
		return "FormLoadParamWrapper [selForms=" + selForms + ", contextName=" + contextName + "]";
	}

}
