package gov.nih.nci.cadsr.formloader.domain;

import java.util.ArrayList;
import java.util.List;

public class ModuleDescriptor {
	String moduleSeqId;
	String publicId;
	String version;
	
	String longName;
	String preferredDefinition;
	String createdBy;
	String modifiedBy;
	
	//TODO: we probably don't need to load this. Check with Denise
	String maximumModuleRepeat;
	
	List<QuestionDescriptor> questions = new ArrayList<QuestionDescriptor>();
	
	public List<QuestionStatus> getQuestionStatuses(String formLoadStatus, boolean debug) {
		List<QuestionStatus> statuses = new ArrayList<QuestionStatus>();
		for (QuestionDescriptor question : questions) {
			String id = question.publicId + "|" + question.version;
			if (debug)
				id += "|" + question.getQuestionSeqId();
			
			String seqid = question.getQuestionSeqId();
			QuestionStatus status = new QuestionStatus(id);
			if (question.skip)
				status.setLoadStatus("Skipped");
			else {
				if (seqid != null && seqid.length() > 0)
					status.setLoadStatus("Loaded");	
				else
					status.setLoadStatus(formLoadStatus);
			}
			
			List<String> questMsgs = new ArrayList<String>();
			if (question.messages.size() > 0) 
				questMsgs.addAll(question.messages);
			status.setMessages(questMsgs);
			
			statuses.add(status);
		}
		
		return statuses;
	}

	public String getModuleSeqId() {
		return moduleSeqId;
	}

	public void setModuleSeqId(String moduleSeqId) {
		this.moduleSeqId = moduleSeqId;
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

	public List<QuestionDescriptor> getQuestions() {
		return questions;
	}

	public void setQuestions(List<QuestionDescriptor> questions) {
		this.questions = questions;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getPreferredDefinition() {
		return preferredDefinition;
	}

	public void setPreferredDefinition(String preferredDefinition) {
		this.preferredDefinition = preferredDefinition;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getMaximumModuleRepeat() {
		return maximumModuleRepeat;
	}

	public void setMaximumModuleRepeat(String maximumModuleRepeat) {
		this.maximumModuleRepeat = maximumModuleRepeat;
	}

	@Override
	public String toString() {
		return "ModuleDescriptor [moduleSeqId=" + moduleSeqId + ", publicId="
				+ publicId + ", version=" + version + ", longName=" + longName
				+ ", preferredDefinition=" + preferredDefinition
				+ ", createdBy=" + createdBy + ", modifiedBy=" + modifiedBy
				+ ", maximumModuleRepeat=" + maximumModuleRepeat
				+ ", questions=" + questions + "]";
	}
	
}
