/**
 * Copyright (C) 2016 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.testspringboot;

import org.springframework.web.multipart.MultipartFile;

public class ModelUploadFiles {

    private String extraField;

    private MultipartFile[] files;

	public String getExtraField() {
		return extraField;
	}

	public void setExtraField(String extraField) {
		this.extraField = extraField;
	}

	public MultipartFile[] getFiles() {
		return files;
	}

	public void setFiles(MultipartFile[] files) {
		this.files = files;
	}


}