package gov.nih.nci.cadsr.data;
/*
 * Copyright 2018 Leidos Biomedical Research, Inc.
 */
public class CategoryCde implements Comparable {
    private int cdeId;
    private float deVersion;
    private String deName;
    private String moduleType;
    private String formId;
    private String formName;
	public int getCdeId() {
		return cdeId;
	}
	public void setCdeId(int cdeId) {
		this.cdeId = cdeId;
	}
	public float getDeVersion() {
		return deVersion;
	}
	public void setDeVersion(float deVersion) {
		this.deVersion = deVersion;
	}
	public String getDeName() {
		return deName;
	}
	public void setDeName(String deName) {
		this.deName = deName;
	}
	public String getModuleType() {
		return moduleType;
	}
	public void setModuleType(String moduleType) {
		this.moduleType = moduleType;
	}
	public String getFormId() {
		return formId;
	}
	public void setFormId(String formId) {
		this.formId = formId;
	}

	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}	
	
	@Override
	public String toString() {
		return "CategoryCde [cdeId=" + cdeId + ", deVersion=" + deVersion + ", deName=" + deName + ", moduleType="
				+ moduleType + ", formId=" + formId + ", formName=" + formName + "]";
	}
	
	@Override
	public int compareTo(Object o) {
		int res = 0;
		if ((o == null) || (! (o instanceof CategoryCde))) {
			return -1;
		}
		CategoryCde other = (CategoryCde)o;
		if (other.getCdeId() < this.getCdeId()){
			res = 1;
		}
		else if (other.getCdeId() > this.getCdeId()){
			res = -1;
		}
		else if (other.getDeVersion() < this.getDeVersion()) {
			res = -1; 
		}
		else if (other.getDeVersion() > this.getDeVersion()) {
			res = 1; 
		}
		return res;
	}
}
