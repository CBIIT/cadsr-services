/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

public class CdeMissing implements Comparable<CdeMissing>{
	String cdeIdVersion;
	String cdeName;
	String preQuestionText;
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
	public String getPreQuestionText() {
		return preQuestionText;
	}
	public void setPreQuestionText(String preQuestionText) {
		this.preQuestionText = preQuestionText;
	}
	@Override
	public String toString() {
		return "CdeMissing [cdeIdVersion=" + cdeIdVersion + ", cdeName=" + cdeName + "preQuestionText=" + preQuestionText +"]";
	}
	
	@Override
	public int compareTo(CdeMissing other) {
		if (cdeIdVersion == null) 
			return -1;
		else 
			return (cdeIdVersion.compareTo(other.getCdeIdVersion()));
	}
	
}
