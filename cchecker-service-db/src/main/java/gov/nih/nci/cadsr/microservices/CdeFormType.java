/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

public class CdeFormType {
	private String  moduleType;
	private int cdeId;
	private int deVersion;
	private String deName;
	public String getModuleType() {
		return moduleType;
	}
	public void setModuleType(String moduleType) {
		this.moduleType = moduleType;
	}
	public int getCdeId() {
		return cdeId;
	}
	public void setCdeId(int cdeId) {
		this.cdeId = cdeId;
	}
	public int getDeVersion() {
		return deVersion;
	}
	public void setDeVersion(int deVersion) {
		this.deVersion = deVersion;
	}
	public String getDeName() {
		return deName;
	}
	public void setDeName(String deName) {
		this.deName = deName;
	}
	@Override
	public String toString() {
		return "CdeType [moduleType=" + moduleType + ", cdeId=" + cdeId + ", deVersion=" + deVersion + ", deName="
				+ deName + "]";
	}
	
}
