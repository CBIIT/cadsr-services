/*
 * Copyright (C) 2019 Frederick National Laboratory for Cancer Research. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.FormDisplay;
import gov.nih.nci.cadsr.data.FormsUiData;
/**
 * 
 * @author asafievan
 *
 */
public class FormServiceTest {

	@Test
	public void testCollectFormsUiDataCaseIncensitive() {
		String[] formNameArr = new String[3];
		formNameArr[0] = "test1";
		formNameArr[1] = "test"+ FormService.formNameExcludeLower.toUpperCase() + "2";
		formNameArr[2] = "test3";
		
		FormService formServiceTest = new FormService();
		FormsUiData formsUiDataReceived = formServiceTest.collectFormsUiData(buildTestALSData(formNameArr));
		List<FormDisplay> formListReceived = formsUiDataReceived.getFormsList();
		
		assertEquals(formListReceived.size(), 2);
		assertEquals(formListReceived.get(0).getFormName(), formNameArr[0]);
		assertEquals(formListReceived.get(1).getFormName(), formNameArr[2]);
	}
	@Test
	public void testCollectFormsUiDataCaseCensitive() {
		String[] formNameArr = new String[3];
		formNameArr[0] = "test1";
		formNameArr[1] = "test"+ FormService.formNameExcludeLower + "2";
		formNameArr[2] = "test3";
		
		FormService formServiceTest = new FormService();
		FormsUiData formsUiDataReceived = formServiceTest.collectFormsUiData(buildTestALSData(formNameArr));
		List<FormDisplay> formListReceived = formsUiDataReceived.getFormsList();
		
		assertEquals(formListReceived.size(), 2);
		assertEquals(formListReceived.get(0).getFormName(), formNameArr[0]);
		assertEquals(formListReceived.get(1).getFormName(), formNameArr[2]);
	}
	
	protected static ALSData buildTestALSData(String[] formNameArr) {
		ALSData alsData = new ALSData();
		List<ALSForm> forms = new ArrayList<>();
		alsData.setForms(forms);
		for (String draftFormName: formNameArr) {
			ALSForm form = new ALSForm();
			form.setDraftFormName(draftFormName);
			forms.add(form);
		}
		
		return alsData;
	}
}
