/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.FormDisplay;
import gov.nih.nci.cadsr.data.FormsUiData;

public class FormService {

	private static final Logger logger = Logger.getLogger(FormService.class);
	
	public static void getFormsListJSON (ALSData alsData) {
		
		ObjectMapper jsonMapper = new ObjectMapper();
		List<FormDisplay> formsList = new ArrayList<FormDisplay>();
		logger.debug("Filepath: "+alsData.getFilePath());
		try {
			for (ALSForm form : alsData.getForms()) {
				FormDisplay fd = new FormDisplay();
				fd.setFormName(form.getDraftFormName());
				fd.setIsValid(true);
				int qCount = form.getFields().size();
				logger.debug("JSON Forms List: "+fd.getFormName()+ " Questions count: "+qCount);
				fd.setQuestionsCount(qCount);
				formsList.add(fd);
			}
            String jsonStr = jsonMapper.writeValueAsString(formsList);
            jsonMapper.writeValue(new File("/local/content/cchecker/forms.json"), jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}	
	
	public static FormsUiData buildFormsUiData (ALSData alsData) {
		List<FormDisplay> formsList = new ArrayList<FormDisplay>();
		FormsUiData formUiData = new FormsUiData();
			for (ALSForm form : alsData.getForms()) {
				FormDisplay fd = new FormDisplay();
				fd.setFormName(form.getDraftFormName());
				fd.setIsValid(true);
				int qCount = form.getFields().size();
				fd.setQuestionsCount(qCount);
				formsList.add(fd);
			}	
			formUiData.setFormsList(formsList);
			return formUiData;
	}
	
	public static List<FormDisplay> getSelectedForms (String selFormsJson) {
		ObjectMapper jsonMapper = new ObjectMapper();		
		List<FormDisplay> selectedFormsList = new ArrayList<FormDisplay>();
		logger.debug("First JSON: " + selFormsJson);
		try {
			//selectedFormsList = jsonMapper.readValue(new File("/local/content/cchecker/forms.json"), new TypeReference<List<FormDisplay>>(){});
			//selFormsJson = new String ( Files.readAllBytes( Paths.get("/local/content/cchecker/forms.json") ) );
			//selFormsJson = selFormsJson.substring(1, selFormsJson.length()-1);
			//selFormsJson.replaceAll("\\", "");
			logger.debug("JSON: " + selFormsJson);
			selectedFormsList = jsonMapper.readValue(selFormsJson, new TypeReference<List<FormDisplay>>(){});
			for (FormDisplay fd : selectedFormsList) {
				logger.debug("Selected Forms: "+fd.getFormName());
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return selectedFormsList;
	}

}
