package gov.nih.nci.cadsr.formloader.domain;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FormCollectionStatus {
	String seqid;
	String name;
	String xmlFileName;
	String createdBy;
	String dateCreated;
	
	String message;

	List<FormStatus> formStatuses = new ArrayList<FormStatus>();

	public String getSeqid() {
		return seqid;
	}

	@XmlElement
	public void setSeqid(String seqid) {
		this.seqid = seqid;
	}

	public String getName() {
		return name;
	}

	@XmlElement
	public void setName(String name) {
		this.name = name;
	}

	public String getXmlFileName() {
		return xmlFileName;
	}

	@XmlElement
	public void setXmlFileName(String xmlFileName) {
		this.xmlFileName = xmlFileName;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	@XmlElement
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	@XmlElement
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public List<FormStatus> getFormStatuses() {
		return formStatuses;
	}

	public String getMessage() {
		return message;
	}

	@XmlElement
	public void setMessage(String message) {
		this.message = message;
	}

	@XmlElementWrapper(name ="forms")
	 @XmlElement(name ="form")
	public void setFormStatuses(List<FormStatus> formStatuses) {
		this.formStatuses = formStatuses;
	}
	
	
	
}
