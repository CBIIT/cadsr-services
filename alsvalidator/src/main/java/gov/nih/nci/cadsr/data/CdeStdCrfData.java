/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

public class CdeStdCrfData {

	String nciCategory;
	String crfIdVersion;
	String crfName;
	String cdePublicId;
	String cdeVersion;

	public String getNciCategory() {
		return nciCategory;
	}

	public void setNciCategory(String nciCategory) {
		this.nciCategory = nciCategory;
	}

	public String getCrfIdVersion() {
		return crfIdVersion;
	}

	public void setCrfIdVersion(String crfIdVersion) {
		this.crfIdVersion = crfIdVersion;
	}

	public String getCrfName() {
		return crfName;
	}

	public void setCrfName(String crfName) {
		this.crfName = crfName;
	}

	public String getCdePublicId() {
		return cdePublicId;
	}

	public void setCdePublicId(String cdePublicId) {
		this.cdePublicId = cdePublicId;
	}

	public String getCdeVersion() {
		return cdeVersion;
	}

	public void setCdeVersion(String cdeVersion) {
		this.cdeVersion = cdeVersion;
	}

	@Override
	public String toString() {
		return "CdeStdCrfData [nciCategory=" + nciCategory + ", crfIdVersion=" + crfIdVersion + ", crfName=" + crfName
				+ ", cdePublicId=" + cdePublicId + ", cdeVersion=" + cdeVersion + "]";
	}

}
