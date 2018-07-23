/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.FormDisplay;
import gov.nih.nci.cadsr.data.FormsUiData;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;

public class FormService {

	private static final Logger logger = Logger.getLogger(FormService.class);
	private static String errorSeverity_warn = "WARNING";
	
	public static FormsUiData buildFormsUiData (ALSData alsData) {
		List<FormDisplay> formsList = new ArrayList<FormDisplay>();
		FormsUiData formUiData = new FormsUiData();
			for (ALSForm form : alsData.getForms()) {
				FormDisplay fd = new FormDisplay();
				fd.setFormName(form.getFormOId());
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
