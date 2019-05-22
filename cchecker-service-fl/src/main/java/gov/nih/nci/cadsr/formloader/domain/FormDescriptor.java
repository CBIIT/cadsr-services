package gov.nih.nci.cadsr.formloader.domain;

import gov.nih.nci.cadsr.formloader.service.common.FormLoaderHelper;
import gov.nih.nci.cadsr.formloader.service.common.StatusFormatter;
import gov.nih.nci.cadsr.formloader.service.common.XmlValidationError;
import gov.nih.nci.ncicb.cadsr.common.dto.ClassificationTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ContactCommunicationV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.DefinitionTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.DesignationTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.RefdocTransferObjectExt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FormDescriptor implements java.io.Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_WORKFLOW_STATUS = "DRAFT NEW";
	public static final String DEFAULT_WORKFLOW_STATUS_MOD = "DRAFT MOD";
	
	public static final String LOAD_TYPE_NEW = "New Form";
	public static final String LOAD_TYPE_NEW_VERSION = "New Version";
	public static final String LOAD_TYPE_UPDATE_FORM = "Update Form";
	public static final String LOAD_TYPE_DUPLICATE_FORM = "Duplicate Form"; 
	public static final String LOAD_TYPE_UNKNOWN = "Unknown";
	
	public static final int STATUS_ERROR = -1;
	public static final int STATUS_XML_VALIDATION_FAILED = -2;
	public static final int STATUS_CONTENT_VALIDATION_FAILED = -3;
	public static final int STATUS_LOAD_FAILED = -4;
	public static final int STATUS_UNLOAD_FAILED = -5;
	public static final int STATUS_NO_LONGER_EXISTS = -6;
	
	public static final int STATUS_INITIALIZED = 0;
	public static final int STATUS_XML_VALIDATED = 1; 
	public static final int STATUS_SKIPPED_CONTENT_VALIDATION = 3; 
	public static final int STATUS_CONTENT_VALIDATED = 4;
	public static final int STATUS_LOADED = 5;
	public static final int STATUS_UNLOADED = 6;
	public static final int STATUS_SKIPPED_LOADING = 7;
	public static final int STATUS_SKIPPED_UNLOADING = 8;
	
	//public static final int MAX_LENGTH =255;
	
	String formSeqId = "";
	String publicId = "";
	String version = "";
	
	String longName = "";
	String context = "";
	String type = "";
	String protocolName;
	String workflowStatusName = "";
	String modifiedBy = "";
	
	//pass 2
	String createdBy = "";
	String changeNote;
	String preferredDefinition;
	String registrationStatus;
	String headerInstruction;
	String footerInstruction;
	String categoryName;
	//pass2
	
	List<DesignationTransferObjectExt> designations;
	List<DefinitionTransferObjectExt> definitions;
	List<ProtocolTransferObjectExt> protocols;
	List<RefdocTransferObjectExt> refdocs;
	List<ContactCommunicationV2TransferObject> contactCommnunications;
	List<ClassificationTransferObject> classifications;
	
	Date createdDate;  //cadsr db
	Date modifiedDate;	//cadsr db
	String workflowStatusCadsr; //currently in cadsr
	String versionCadsr;		//currently in cadsr
	
	Date loadUnloadDate; //form loader table
	
	String collectionName;
	String collectionSeqid;
	
	List<FormCollection> belongToCollections = new ArrayList<FormCollection>();
	
	List<ModuleDescriptor> modules = new ArrayList<ModuleDescriptor>();
	
	//Any error (xml, content validation, etc) will be here
	List<XmlValidationError> xmlValidationErrors = new ArrayList<XmlValidationError>();
	List<String> messages = new ArrayList<String>();
	
	//Load services only: new form, new version or update
	String loadType;
	int loadStatus;
	
	int index;
	
	//If loading as a new version form, need to remember the previous latest version 
	//for possible restoration purpose at unload
	float previousLatestVersion;
	
	protected transient boolean selected;
	protected transient int xml_line_begin;
	protected transient int xml_line_end;
	
	//This is from db by name
	protected String contextSeqid; 
	
	public FormDescriptor() {
		loadStatus = STATUS_INITIALIZED;
		loadType = LOAD_TYPE_UNKNOWN;
	}
	
	/**
	 * This intends to be called by front end or web service to gather all errors/info with the form and its
	 * questions.
	 * 
	 * @return
	 */
	public FormStatus getStructuredStatus() {
		FormStatus formStatus = new FormStatus(this.getFormIdString(), this.formSeqId, false);
		formStatus.setLoadType(this.loadType);
		formStatus.setLoadStatus(getLoadStatusString(this.loadStatus));
		
		List<String> msgs = formStatus.getMessages();
		if (this.xmlValidationErrors != null && this.xmlValidationErrors.size() > 0) {
			for (XmlValidationError error : this.xmlValidationErrors)
				msgs.add(error.toString());
		}
		
		if (this.messages.size() > 0) {
			msgs.addAll(this.messages);
		}
		
		if (this.modules.size() > 0) {
			for (int i = 0; i < this.modules.size(); i++) {
				ModuleStatus moduleStatus = new ModuleStatus("" + (i+1));
				moduleStatus.setQuestionStatuses(modules.get(i).getQuestionStatuses(formStatus.getLoadStatus(), false));
				formStatus.getModuleStatuses().add(moduleStatus);
			}
		}
		
		return formStatus;
	}
	
	/**
	 * If debug is true, the form and question's id string will contain their seqid in database.
	 * 
	 * @param debug
	 * @return
	 */
	public FormStatus getStructuredStatus(boolean debug) {
		FormStatus formStatus = new FormStatus(this.getFormIdString(), this.formSeqId, debug);
		formStatus.setLoadType(this.loadType);
		formStatus.setLoadStatus(getLoadStatusString(this.loadStatus));
		
		List<String> msgs = formStatus.getMessages();
		if (this.xmlValidationErrors != null && this.xmlValidationErrors.size() > 0) {
			for (XmlValidationError error : this.xmlValidationErrors)
				msgs.add(error.toString());
		}
		
		if (this.messages.size() > 0) {
			msgs.addAll(this.messages);
		}
		
		if (this.modules.size() > 0) {
			for (int i = 0; i < this.modules.size(); i++) {
				ModuleStatus moduleStatus = new ModuleStatus("" + (i+1));
				moduleStatus.setQuestionStatuses(modules.get(i).getQuestionStatuses(formStatus.getLoadStatus(), debug));
				formStatus.getModuleStatuses().add(moduleStatus);
			}
		}
		
		return formStatus;
	}
	
	public List<String> getFormLevelMessages(FormStatus status) {
		return status.getMessages();
	}
	
	public List<String> getQuestionMessages(FormStatus status) {
		
		List<String> messages = new ArrayList<String>();
		List<ModuleStatus> modStatuses = status.getModuleStatuses();
		
		int mIdx = 0;
		for (ModuleStatus modStatus : modStatuses) {
						
			List<QuestionStatus> questStatuese = modStatus.getQuestionStatuses();
			int qIdx = 0;
			for (QuestionStatus questStatus : questStatuese) {
				List<String> questMessages = questStatus.getMessages();
				for (String qMeg : questMessages) {
					if (qMeg.length() > 0) {
						messages.add("[Module " + mIdx + " Question " + qIdx + "]: " + qMeg);	//JR446
					}
				}
				
				qIdx++;
			}
			
			mIdx++;
		}
		
		return messages;
	}
	
	public FormDescriptor(String id, String publicId, String version) {
		this.formSeqId = id;
		this.publicId = publicId;
		this.version = version;
		loadStatus = STATUS_INITIALIZED;
		loadType = LOAD_TYPE_UNKNOWN;
	}
	
	public String getFormSeqId() {
		return formSeqId;
	}
	public void setFormSeqId(String formSeqId) {
		this.formSeqId = formSeqId;
	}
	
	public String getPublicId() {
		return publicId;
	}
	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getLongName() {
		return longName;
	}
	public void setLongName(String longName) {
		this.longName = longName;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getProtocolName() {
		return protocolName;
	}
	public void setProtocolName(String protocolName) {
		if (this.protocolName != null && protocolName.length() > 0)
			this.protocolName += ";" + protocolName;
		else
			this.protocolName = protocolName;
	}
	public String getWorkflowStatusName() {
		return workflowStatusName;
	}
	public void setWorkflowStatusName(String workflowStatus) {
		this.workflowStatusName = workflowStatus;
	}
	public List<ModuleDescriptor> getModules() {
		return modules;
	}
	public void setModules(List<ModuleDescriptor> modules) {
		this.modules = modules;
	}
	public String getLoadType() {
		return loadType;
	}
	public void setLoadType(String loadType) {
		this.loadType = loadType;
	}
	
	public int getLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(int loadStatus) {
		this.loadStatus = loadStatus;
	}

	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public int getXml_line_begin() {
		return xml_line_begin;
	}
	public void setXml_line_begin(int xml_line_begin) {
		this.xml_line_begin = xml_line_begin;
	}
	public int getXml_line_end() {
		return xml_line_end;
	}
	public void setXml_line_end(int xml_line_end) {
		this.xml_line_end = xml_line_end;
	}
	
	public List<XmlValidationError> getErrors() {
		return xmlValidationErrors;
	}
	public void setErrors(List<XmlValidationError> errors) {
		this.xmlValidationErrors = errors;
	}
	public int getNumberOfModules() {
		return this.modules.size();
		
	}
	
	public void addMessage(String msg) {
		this.messages.add(msg);
	}

	public List<String> getMessages() {
		return messages;
	}
	
	public String getMessagesInString() {
		if (messages == null)
			return "";
		
		StringBuilder sb = new StringBuilder();
		for (String msg : messages) {
			sb.append(msg).append(";");
		}
					
		return sb.toString();
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getPreferredDefinition() {
		return preferredDefinition;
	}

	public void setPreferredDefinition(String preferredDefinition) {
		this.preferredDefinition = preferredDefinition;
	}

	public String getRegistrationStatus() {
		return registrationStatus;
	}

	public void setRegistrationStatus(String registrationStatus) {
		this.registrationStatus = registrationStatus;
	}

	public String getHeaderInstruction() {
		return headerInstruction;
	}

	public void setHeaderInstruction(String headerInstruction) {
		this.headerInstruction = headerInstruction;
	}

	public String getFooterInstruction() {
		return footerInstruction;
	}

	public void setFooterInstruction(String footerInstruction) {
		this.footerInstruction = footerInstruction;
	}

	public String getChangeNote() {
		return changeNote;
	}

	public void setChangeNote(String changeNote) {
		if (this.changeNote == null || this.changeNote.length() == 0)
			this.changeNote = changeNote;
		else
			this.changeNote += ";" + changeNote;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	
	public String getContextSeqid() {
		return contextSeqid;
	}

	public void setContextSeqid(String contextSeqid) {
		this.contextSeqid = contextSeqid;
	}
	
	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Date getLoadUnloadDate() {
		return loadUnloadDate;
	}

	public void setLoadUnloadDate(Date loadUnloadDate) {
		this.loadUnloadDate = loadUnloadDate;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public float getPreviousLatestVersion() {
		return previousLatestVersion;
	}

	public void setPreviousLatestVersion(float previousLatestVersion) {
		this.previousLatestVersion = previousLatestVersion;
	}

	public String getCollectionSeqid() {
		return collectionSeqid;
	}

	public void setCollectionSeqid(String collectionSeqid) {
		this.collectionSeqid = collectionSeqid;
	}

	public List<FormCollection> getBelongToCollections() {
		return belongToCollections;
	}

	public void setBelongToCollections(List<FormCollection> belongToCollections) {
		this.belongToCollections = belongToCollections;
	}
	

	public String getWorkflowStatusCadsr() {
		return workflowStatusCadsr;
	}

	public void setWorkflowStatusCadsr(String workflowStatusCadsr) {
		this.workflowStatusCadsr = workflowStatusCadsr;
	}

	public String getVersionCadsr() {
		return versionCadsr;
	}

	public void setVersionCadsr(String versionCadsr) {
		this.versionCadsr = versionCadsr;
	}
	
	public List<DesignationTransferObjectExt> getDesignations() {
		return designations;
	}

	public void setDesignations(List<DesignationTransferObjectExt> designations) {
		this.designations = designations;
	}

	public List<DefinitionTransferObjectExt> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<DefinitionTransferObjectExt> definitions) {
		this.definitions = definitions;
	}

	/**
	 * Return the current load status in string
	 * @return
	 */
	public String getLoadStatusString() {
		return getLoadStatusString(this.loadStatus);
	}
	
	public String getLoadStatusStringWithMessages() {
		
		//temp disabling loading of update form
		if (FormDescriptor.LOAD_TYPE_UPDATE_FORM.equals(this.loadType)) {
			return "Update Form - (load feature unavailable until version 4.2)";
		}
		//temp disabling loading of update form

		String status = getLoadStatusString(this.loadStatus);
		
		int msgSize = this.messages.size();
		if (msgSize > 0) {
			status += " - ";
			
			if (msgSize == 1)
				status += messages.get(0);
			else {
				StringBuilder sb = new StringBuilder();
				sb.append(status);
				for (int i = 0; i < msgSize; i++) {
					sb.append(" ").append(i+1).append(") ").append(messages.get(i));
				}
				status = sb.toString();
			}
		}
		return status;
	}
	
	protected String getLoadStatusString(int statusCode) {
		
		switch (statusCode) {
		case STATUS_INITIALIZED:
			return "Initialized";
		case STATUS_XML_VALIDATED:
			return "XML Validated";
		case STATUS_CONTENT_VALIDATED:
			return "Content Validated";
		case STATUS_LOADED:
			return "Loaded";
		case STATUS_UNLOADED:
			return "Unloaded";
		case STATUS_SKIPPED_LOADING:
			return "Not selected for Load";
		case STATUS_SKIPPED_UNLOADING:
			return "Skipped Unloading";
		case STATUS_XML_VALIDATION_FAILED:
			return "XML Validation failed (not eligible for DB Validation)";
		case STATUS_LOAD_FAILED:
			return "Load failed";
		case STATUS_UNLOAD_FAILED:
			return "Unload failed";
		case STATUS_ERROR:
			return "Error";
		case STATUS_CONTENT_VALIDATION_FAILED:
			return "DB Validation failed (not eligible for Load)";
		case STATUS_SKIPPED_CONTENT_VALIDATION:
			return "DB Validation not performed (not eligible for Load)";
		case STATUS_NO_LONGER_EXISTS:
			return "Form no longer exists (not eligible to unload)";
		default: 
			return "Status Unknown";
			
		}
	}
	
	public String getLoadTypeLoadStatusString() {
		
		String statusString = getLoadStatusString(loadStatus);
		
		return (loadStatus == FormDescriptor.STATUS_LOADED ||
				loadStatus == FormDescriptor.STATUS_UNLOADED) ? 
						statusString + " - " + loadType : statusString;							
	}
	
	/**
	 * Returns form's id in this format: <publicid>|<version> 
	 * * @return
	 */
	public String getFormIdString() {
		return this.publicId + "|" + this.version;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getXmlValidationErrorString() {
		if (this.xmlValidationErrors == null || this.xmlValidationErrors.size() == 0)
			return "";
		
		StringBuilder sb = new StringBuilder();
		for (XmlValidationError error : xmlValidationErrors) {
			sb.append(error.toString()).append(";");
		}
				return sb.toString();
	}
	
	public String getCollectionsInFullHtml() {
		if (this.belongToCollections == null || this.belongToCollections.size() == 0)
			return "No collection info for this form";
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html><head><title>Collections</title></head><body> " +
"<table><tr><th>Collection Name</th><th>Description</th><th># of Forms</th><th>Loaded By</th><th>Loaded Date</th><tr>");
		sb.append("<td>").append(this.belongToCollections.get(0).getName()).append("</td>");
		sb.append("<td>").append(this.belongToCollections.get(0).getDescription()).append("</td>");
		sb.append("<td>").append(this.belongToCollections.get(0).getForms().size()).append("</td>");
		sb.append("<td>").append(this.belongToCollections.get(0).getCreatedBy()).append("</td>");
		sb.append("<td>").append(this.belongToCollections.get(0).getDateCreated()).append("</td>");
		sb.append("</tr></table></body></html>");
		
		return sb.toString();
	}
	
	public String getCollectionsInHtmlRows() {
		if (this.belongToCollections == null || this.belongToCollections.size() == 0)
			return "No collection info for this form";
		
		StringBuilder sb = new StringBuilder();
		int idx = 1;
		for (FormCollection coll : this.belongToCollections) {
			String odd = (idx % 2 != 0) ? "odd" : "even";
			sb.append("<tr class=\"" + odd + "\"><td>").append(coll.getNameWithRepeatIndicator()).append("</td>");
			sb.append("<td>").append(coll.getDescription()).append("</td>");
			sb.append("<td>").append(coll.getForms().size()).append("</td>");
			sb.append("<td>").append(coll.getCreatedBy()).append("</td>");
			sb.append("<td>").append(coll.getDateCreated()).append("</td>");
			sb.append("<td>").append(this.getModifiedDate()).append("</td>");
			if (idx == 1)
				sb.append("<td align=\"center\">").append("<img src=\"/FormLoader/i/checked.jpg\" />").append("</td>");
			else
				sb.append("<td/>");
			sb.append("</tr>");
			
			idx++;
		}

		return sb.toString();
	}
	
	public boolean isUnloadable() {
		
		if (this.loadStatus != FormDescriptor.STATUS_LOADED)
			return false;
		
		Date loadDate = this.loadUnloadDate;
		Date modDate = this.modifiedDate;
		
		if (modDate == null && FormDescriptor.LOAD_TYPE_NEW.equals(this.loadType))
			return true;
		
		return (loadDate != null && modDate != null) ? loadDate.getTime() == modDate.getTime() : false;
		
	}
	//<a href="https://formbuilder-dev.nci.nih.gov/FormBuilder/formDetailsAction.do?method=getFormDetails&formIdSeq=BC2E08B3-C5B7-4C7A-E040-BB89AD43061F" target="_blank"> 3421713 </a>
	public String getLinkToFormBuilder() {
		
		String urlBase = FormLoaderHelper.getProperty("formbuilder.detailsAction.url");
		return urlBase + this.formSeqId;
		
	}
	
	public void setDefaultWorkflowName() {
		if (FormDescriptor.LOAD_TYPE_UPDATE_FORM.equals(this.loadType))
			this.workflowStatusName = FormDescriptor.DEFAULT_WORKFLOW_STATUS_MOD;
		else
			this.workflowStatusName = FormDescriptor.DEFAULT_WORKFLOW_STATUS;
	}

	public List<ProtocolTransferObjectExt> getProtocols() {
		return protocols;
	}

	public void setProtocols(List<ProtocolTransferObjectExt> protocols) {
		this.protocols = protocols;
	}

	public List<RefdocTransferObjectExt> getRefdocs() {
		return refdocs;
	}

	public void setRefdocs(List<RefdocTransferObjectExt> refdocs) {
		this.refdocs = refdocs;
	}

	public List<ContactCommunicationV2TransferObject> getContactCommnunications() {
		return contactCommnunications;
	}

	public void setContactCommnunications(
			List<ContactCommunicationV2TransferObject> contactCommnunications) {
		this.contactCommnunications = contactCommnunications;
	}

	public List<ClassificationTransferObject> getClassifications() {
		return classifications;
	}

	public void setClassifications(List<ClassificationTransferObject> classifications) {
		this.classifications = classifications;
	}

	@Override
	public String toString() {
		return "FormDescriptor [formSeqId=" + formSeqId + ", publicId="
				+ publicId + ", version=" + version + ", longName=" + longName
				+ ", context=" + context + ", type=" + type + ", protocolName="
				+ protocolName + ", workflowStatusName=" + workflowStatusName
				+ ", modifiedBy=" + modifiedBy + ", createdBy=" + createdBy
				+ ", changeNote=" + changeNote + ", preferredDefinition="
				+ preferredDefinition + ", registrationStatus="
				+ registrationStatus + ", headerInstruction="
				+ headerInstruction + ", footerInstruction="
				+ footerInstruction + ", categoryName=" + categoryName
				+ ", designations=" + designations + ", definitions="
				+ definitions + ", protocols=" + protocols + ", refdocs="
				+ refdocs + ", contactCommnunications="
				+ contactCommnunications + ", createdDate=" + createdDate
				+ ", modifiedDate=" + modifiedDate + ", workflowStatusCadsr="
				+ workflowStatusCadsr + ", versionCadsr=" + versionCadsr
				+ ", loadUnloadDate=" + loadUnloadDate + ", collectionName="
				+ collectionName + ", collectionSeqid=" + collectionSeqid
				+ ", belongToCollections=" + belongToCollections + ", modules="
				+ modules + ", xmlValidationErrors=" + xmlValidationErrors
				+ ", messages=" + messages + ", loadType=" + loadType
				+ ", loadStatus=" + loadStatus + ", index=" + index
				+ ", previousLatestVersion=" + previousLatestVersion
				+ ", selected=" + selected + ", xml_line_begin="
				+ xml_line_begin + ", xml_line_end=" + xml_line_end
				+ ", contextSeqid=" + contextSeqid + "]";
	}	

}
