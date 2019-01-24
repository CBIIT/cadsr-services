/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.FormDisplay;
import gov.nih.nci.cadsr.data.FormsUiData;
import static java.util.stream.Collectors.toList;

@Service
public class FormService {

	private static final Logger logger = LoggerFactory.getLogger(FormService.class);
	private static String errorSeverity_warn = "WARNING";
	
	public FormsUiData collectFormsUiData (ALSData alsData) {
		List<FormDisplay> formsList = new ArrayList<FormDisplay>();
		
		FormsUiData formUiData = new FormsUiData();
		List<ALSForm> alsFormList = alsData.getForms();
		if (alsFormList != null) {
			for (ALSForm form : alsFormList) {
				formsList.add(createFormDisplay(form, alsData.getCccError().getAlsErrors()));
			}
			formUiData.setFormsList(formsList);
		}
		return formUiData;
	}
	protected List<ALSError> collectAlsFormError(List<ALSError> errorsWarningsList, String formOid) {
		List<ALSError> errorsList = errorsWarningsList.stream()
			.filter(error->error.getFormOid()!=null)
			.filter(error->(error.getFormOid().equalsIgnoreCase(formOid) && !error.getErrorSeverity().equals(errorSeverity_warn)))
			.collect(toList());
		return errorsList;
	}
	protected FormDisplay createFormDisplay(ALSForm form, List<ALSError> alsErrorList) {
		FormDisplay fd = new FormDisplay();
		fd.setFormName(form.getDraftFormName());
		fd.setIsValid(true);
		fd.setErrors(collectAlsFormError(alsErrorList, form.getFormOid()));
		if (!fd.getErrors().isEmpty()) {
			fd.setIsValid(false);
		}
		else {
			fd.setIsValid(true);
		}
		int qCount = form.getFields().size();
		fd.setQuestionsCount(qCount);
		return fd;
	}
}
