/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.FormDisplay;
import gov.nih.nci.cadsr.data.FormsUiData;

public class FormService {

	private static final Logger logger = Logger.getLogger(FormService.class);
	private static String errorSeverity_warn = "WARNING";
	
	public static FormsUiData buildFormsUiData (ALSData alsData) {
		List<FormDisplay> formsList = new ArrayList<FormDisplay>();
		FormsUiData formUiData = new FormsUiData();
			for (ALSForm form : alsData.getForms()) {
				FormDisplay fd = new FormDisplay();
				fd.setFormName(form.getDraftFormName());
				fd.setIsValid(true);
				for (ALSError error : alsData.getCccError().getAlsErrors()) {
					if (error.getFormOid()!=null && error.getFormOid().equalsIgnoreCase(form.getFormOId()) && !error.getErrorSeverity().equals(errorSeverity_warn) ) {
						fd.getErrors().add(error);
					}
				}
				if (!fd.getErrors().isEmpty())
					fd.setIsValid(false);					
				int qCount = form.getFields().size();
				fd.setQuestionsCount(qCount);
				formsList.add(fd);
			}	
			formUiData.setFormsList(formsList);
			return formUiData;
	}

}
