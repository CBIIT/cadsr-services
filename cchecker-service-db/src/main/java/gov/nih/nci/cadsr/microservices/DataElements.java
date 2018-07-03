/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

public class DataElements {
	private static final long serialVersionUID = 1L;
	
	private String deIdseq;
	private int version;
	private String conteIdseq;
	private String preferredName; 
	private String vdIdseq;
	private String decIdseq;
	private String preferredDefinition;
	private String aslName;
	private String longName; 
	@Override
	public String toString() {
		return "DataElements [deIdseq=" + deIdseq + ", version=" + version + ", conteIdseq=" + conteIdseq
				+ ", preferredName=" + preferredName + ", vdIdseq=" + vdIdseq + ", decIdseq=" + decIdseq
				+ ", preferredDefinition=" + preferredDefinition + ", aslName=" + aslName + ", longName=" + longName
				+ ", origin=" + origin + ", cdeId=" + cdeId + ", question=" + question + "]";
	}
	private String origin; 
	private long cdeId;
	private String question;
	public String getDeIdseq() {
		return deIdseq;
	}
	public void setDeIdseq(String deIdseq) {
		this.deIdseq = deIdseq;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public String getConteIdseq() {
		return conteIdseq;
	}
	public void setConteIdseq(String conteIdseq) {
		this.conteIdseq = conteIdseq;
	}
	public String getPreferredName() {
		return preferredName;
	}
	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}
	public String getVdIdseq() {
		return vdIdseq;
	}
	public void setVdIdseq(String vdIdseq) {
		this.vdIdseq = vdIdseq;
	}
	public String getDecIdseq() {
		return decIdseq;
	}
	public void setDecIdseq(String decIdseq) {
		this.decIdseq = decIdseq;
	}
	public String getPreferredDefinition() {
		return preferredDefinition;
	}
	public void setPreferredDefinition(String preferredDefinition) {
		this.preferredDefinition = preferredDefinition;
	}
	public String getAslName() {
		return aslName;
	}
	public void setAslName(String aslName) {
		this.aslName = aslName;
	}
	public String getLongName() {
		return longName;
	}
	public void setLongName(String longName) {
		this.longName = longName;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public long getCdeId() {
		return cdeId;
	}
	public void setCdeId(long cdeId) {
		this.cdeId = cdeId;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	
}
