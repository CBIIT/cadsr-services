package gov.nih.nci.ncicb.cadsr.common.dto;

import java.util.ArrayList;
import java.util.List;

public class DesignationTransferObjectExt extends DesignationTransferObject {
	String contextName;
	
	List<String> classficationPublicIdVersionPairs = new ArrayList<String>();

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	public List<String> getClassficationPublicIdVersionPairs() {
		return classficationPublicIdVersionPairs;
	}

	public void setClassficationPublicIdVersionPairs(
			List<String> classficationPublicIdVersionPairs) {
		this.classficationPublicIdVersionPairs = classficationPublicIdVersionPairs;
	}
}
