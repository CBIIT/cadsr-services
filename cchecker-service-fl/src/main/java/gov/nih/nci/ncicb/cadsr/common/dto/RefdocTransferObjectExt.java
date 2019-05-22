package gov.nih.nci.ncicb.cadsr.common.dto;

public class RefdocTransferObjectExt extends ReferenceDocumentTransferObject {
	
	private static final long serialVersionUID = 1L;
	
	String contextName;

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}
	
	
}
