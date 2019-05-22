package gov.nih.nci.cadsr.formloader.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace ="gov.nih.nci.cadsr.formloader.domain.ModuleStatus")
public class QuestionStatus {
	protected String identifier;
	protected String loadStatus; //skipped or loaded
	protected List<String> messages = new ArrayList<String>();
	
	public QuestionStatus() {}
	
	public QuestionStatus(String id) {
		this.identifier = id;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	@XmlElement
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getLoadStatus() {
		return loadStatus;
	}
	
	@XmlElement
	public void setLoadStatus(String loadStatus) {
		this.loadStatus = loadStatus;
	}
	public List<String> getMessages() {
		return messages;
	}
	
	@XmlElementWrapper(name ="messages")
	 @XmlElement(name ="message")
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	
	
}
