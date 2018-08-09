package gov.nih.nci.cadsr.data;
/*
 * Copyright 2018 Leidos Biomedical Research, Inc.
 */
public class CategoryCde {
    private int cdeId;
    private float deVersion;
    private String deName;
    private String moduleType;
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
	@Override
	public String toString() {
		return "CategoryCde [cdeId=" + cdeId + ", deVersion=" + deVersion + ", deName=" + deName + ", moduleType="
				+ moduleType + "]";
	}
    
}
