package gov.nih.nci.cadsr.formloader.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace ="gov.nih.nci.cadsr.formloader.domain.FormStatus")
public class ModuleStatus {
	protected String identifier;
	
	List<QuestionStatus> questionStatuses;
	
	public ModuleStatus() {}
	
	public ModuleStatus(String id) {
		this.identifier = id;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	@XmlElement
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public List<QuestionStatus> getQuestionStatuses() {
		return questionStatuses;
	}
	
	 @XmlElementWrapper(name ="questions")
	 @XmlElement(name ="question")
	 public void setQuestionStatuses(List<QuestionStatus> questStatuses) {
		 this.questionStatuses = questStatuses;
	 }
}
