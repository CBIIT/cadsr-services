/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.report;

public class CdeFormInfo {
	private String cdeId;
	private String version;
	
	public CdeFormInfo() {
		super();
	}
	
	public CdeFormInfo(String cdeId, String version) {
		super();
		this.cdeId = cdeId;
		this.version = version;
	}

	public String getCdeId() {
		return cdeId;
	}
	public void setCdeId(String cdeId) {
		this.cdeId = cdeId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "CdeFormInfo [cdeId=" + cdeId + ", version=" + version + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cdeId == null) ? 0 : cdeId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CdeFormInfo other = (CdeFormInfo) obj;
		if (cdeId == null) {
			if (other.cdeId != null)
				return false;
		} else if (!cdeId.equals(other.cdeId))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}
