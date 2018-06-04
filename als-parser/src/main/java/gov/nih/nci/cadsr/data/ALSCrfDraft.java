package gov.nih.nci.cadsr.data;

public class ALSCrfDraft {
	
String draftName;
Boolean deleteExisting;
String projectName;
String projectType;
String primaryFormOid;
String defaultMatrixOid;
String confirmationMessage;
String signPrompt;
String labStandardGroup;
String referenceLabs;
String alertLabs;
String syncOidProject;
String syncOidDraft;
String syncOidProjectType;
Boolean syncOidOriginalVersion;

public String getDraftName() {
	return draftName;
}
public void setDraftName(String draftName) {
	this.draftName = draftName;
}
public Boolean getDeleteExisting() {
	return deleteExisting;
}
public void setDeleteExisting(Boolean deleteExisting) {
	this.deleteExisting = deleteExisting;
}
public String getProjectName() {
	return projectName;
}
public void setProjectName(String projectName) {
	this.projectName = projectName;
}
public String getProjectType() {
	return projectType;
}
public void setProjectType(String projectType) {
	this.projectType = projectType;
}
public String getPrimaryFormOid() {
	return primaryFormOid;
}
public void setPrimaryFormOid(String primaryFormOid) {
	this.primaryFormOid = primaryFormOid;
}
public String getDefaultMatrixOid() {
	return defaultMatrixOid;
}
public void setDefaultMatrixOid(String defaultMatrixOid) {
	this.defaultMatrixOid = defaultMatrixOid;
}
public String getConfirmationMessage() {
	return confirmationMessage;
}
public void setConfirmationMessage(String confirmationMessage) {
	this.confirmationMessage = confirmationMessage;
}
public String getSignPrompt() {
	return signPrompt;
}
public void setSignPrompt(String signPrompt) {
	this.signPrompt = signPrompt;
}
public String getLabStandardGroup() {
	return labStandardGroup;
}
public void setLabStandardGroup(String labStandardGroup) {
	this.labStandardGroup = labStandardGroup;
}
public String getReferenceLabs() {
	return referenceLabs;
}
public void setReferenceLabs(String referenceLabs) {
	this.referenceLabs = referenceLabs;
}
public String getAlertLabs() {
	return alertLabs;
}
public void setAlertLabs(String alertLabs) {
	this.alertLabs = alertLabs;
}
public String getSyncOidProject() {
	return syncOidProject;
}
public void setSyncOidProject(String syncOidProject) {
	this.syncOidProject = syncOidProject;
}
public String getSyncOidDraft() {
	return syncOidDraft;
}
public void setSyncOidDraft(String syncOidDraft) {
	this.syncOidDraft = syncOidDraft;
}
public String getSyncOidProjectType() {
	return syncOidProjectType;
}
public void setSyncOidProjectType(String syncOidProjectType) {
	this.syncOidProjectType = syncOidProjectType;
}
public Boolean getSyncOidOriginalVersion() {
	return syncOidOriginalVersion;
}
public void setSyncOidOriginalVersion(Boolean syncOidOriginalVersion) {
	this.syncOidOriginalVersion = syncOidOriginalVersion;
}

}
