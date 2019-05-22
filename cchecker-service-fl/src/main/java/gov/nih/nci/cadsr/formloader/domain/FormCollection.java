package gov.nih.nci.cadsr.formloader.domain;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

//@Entity
//@Table(name = "FORM_COLLECTIONS")
@XmlRootElement
public class FormCollection implements java.io.Serializable {
	
	private static final long serialVersionUID = 6315530260286190440L;
	
	//@Id
	//@GeneratedValue
	//@Column(name = "ID", unique = true, nullable = false)
	private String id;
	
	//@Column(name = "NAME", nullable = false)
	private String name;
	
	//@Column(name = "DESCRIPTION", nullable = false)
	private String description;
	
	//@Column(name = "XML_FILE_NAME", nullable = false)
	private String xmlFileName;
	
	//@Column(name = "XML_FILE_PATH")
	private String xmlPathOnServer;
	
	//@Column(name = "DATE_CREATED", nullable = false)
	private Date dateCreated;
	
	//@Column(name = "CREATED_BY", nullable = false)
	private String createdBy;
	
	private int nameRepeatNum;
	
	private List<String> messages = new ArrayList<String>();
	
	//@Transient
	private boolean selected;
	
	
	/**
	 * This overwrite "selected" flag in all the forms in the collection
	 */
	private boolean selectAllForms;
	
	//@OneToMany
	//@JoinTable
	//(
	//	name="FORM_COLLECTION_",
	//	joinColumns={ @JoinColumn(name="EMP_ID", referencedColumnName="EMP_ID") },
	//	inverseJoinColumns={ @JoinColumn(name="PHONE_ID", referencedColumnName="ID", unique=true) }
	//)
	//@Transient
	private List<FormDescriptor> forms;

	public FormCollection() {}
	
	public FormCollection(List<FormDescriptor> forms) {
		this.forms = forms;
	}
	
	public FormCollection(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	public FormCollection(String id, String name, String description, 
			String xmlFileName, String xmlPathOnServer, Date dateCreated, String createdBy) {
		this.id = id;
		this.name = name;
		this.description = description;
		
		this.xmlFileName = xmlFileName;
		this.xmlPathOnServer = xmlPathOnServer;
		this.dateCreated = dateCreated;
		this.createdBy = createdBy;
	}
	
	
	public String getId() {
		return id;
	}
	
	@XmlElement
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	@XmlElement
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	@XmlElement
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getXmlFileName() {
		return xmlFileName;
	}
	@XmlElement
	public void setXmlFileName(String xmlFileName) {
		this.xmlFileName = xmlFileName;
	}
	
	public String getXmlPathOnServer() {
		return xmlPathOnServer;
	}
	@XmlElement
	public void setXmlPathOnServer(String xmlPathOnServer) {
		this.xmlPathOnServer = xmlPathOnServer;
	}
	
	
	public Date getDateCreated() {
		return dateCreated;
	}
	@XmlElement
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	@XmlElement
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	//@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "FORM_COLLECTIONS")
	
	public int getNameRepeatNum() {
		return nameRepeatNum;
	}
	@XmlElement
	public void setNameRepeatNum(int nameRepeatNum) {
		this.nameRepeatNum = nameRepeatNum;
	}

	public List<String> getMessages() {
		return messages;
	}
	
	public String getMessagesInString() {		
		return formatMessages();
	}

	@SuppressWarnings("unused")
	private void setMessages(List<String> messages) {
		this.messages.addAll(messages);
	}
	
	public void addMessage(String message) {
		this.messages.add(message);
	}

	public List<FormDescriptor> getForms() {
		return forms;
	}
	 @XmlElementWrapper(name ="forms")
	 @XmlElement(name ="form")
	public void setForms(List<FormDescriptor> forms) {
		this.forms = forms;
	}
	
	public boolean isSelectAllForms() {
		return selectAllForms;
	}

	public void setSelectAllForms(boolean selectAllForms) {
		this.selectAllForms = selectAllForms;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public FormCollectionStatus getStructuredStatus() {
		FormCollectionStatus formCollStatus = new FormCollectionStatus();
		formCollStatus.setName(this.name);
		formCollStatus.setXmlFileName(this.xmlFileName);
		formCollStatus.setCreatedBy(this.createdBy);
		formCollStatus.setMessage(this.formatMessages());
		
		String dateString = "";
		if (this.dateCreated != null) {
			SimpleDateFormat fomatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
			dateString = fomatter.format(this.dateCreated);
		}
		
		formCollStatus.setDateCreated(dateString);  
		
		if (this.forms != null && this.forms.size() > 0) {
			for (FormDescriptor form : forms) {
				FormStatus formStatus = form.getStructuredStatus();
				formCollStatus.getFormStatuses().add(formStatus);
			}
		}
		
		return formCollStatus;
	}
	
	public String formatMessages() {
		if (messages == null)
			return "";
		
		StringBuilder sb = new StringBuilder();
		for (String message : messages) {
			sb.append(message).append(";");
		}
		
		return sb.toString();
	}
	
	public void resetAllSelectFlag(boolean flag) {
		if (forms == null) return;
		
		for (FormDescriptor form : forms) {
			form.setSelected(flag);
		}
	}
	
	public String getNameWithRepeatIndicator() {
		return (this.nameRepeatNum > 0) ? this.name + " (" + nameRepeatNum + ")" : this.name;
	}
}
