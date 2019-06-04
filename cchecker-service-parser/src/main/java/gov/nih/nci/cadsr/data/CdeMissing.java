/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

public class CdeMissing implements Comparable<CdeMissing>{
	String cdeIdVersion;
	String cdeName;
	public String getCdeIdVersion() {
		return cdeIdVersion;
	}
	public void setCdeIdVersion(String cdeIdVersion) {
		this.cdeIdVersion = cdeIdVersion;
	}
	public String getCdeName() {
		return cdeName;
	}
	public void setCdeName(String cdeName) {
		this.cdeName = cdeName;
	}
	@Override
	public String toString() {
		return "CdeMissing [cdeIdVersion=" + cdeIdVersion + ", cdeName=" + cdeName + "]";
	}
	
	@Override
	public int compareTo(CdeMissing other) {
		if (cdeIdVersion == null) 
			return -1;
		else 
			return (cdeIdVersion.compareTo(other.getCdeIdVersion()));
	}
	
}
