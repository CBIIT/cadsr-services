/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.CCCError;
import gov.nih.nci.cadsr.parser.impl.AlsParser;

@Service
public class AlsPostProcessor {
	public final static String formSheetName = AlsParser.formsSheetName;
	/**
	 * This method searched for duplicates in Form OIDs and Form Names.
	 * 
	 * @param alsData
	 */
	public void postProcess(ALSData alsData) {
		List<ALSForm> alsFormList;
		if ((alsData == null) || ((alsFormList = alsData.getForms()) == null))
			return;
		Set<String> oidDupSet = findOidDuplicates(alsFormList);
		Set<String> formNameDupSet = findFormNameDuplicates(alsFormList);
		if ((oidDupSet.isEmpty()) && (formNameDupSet.isEmpty())) {//for performance
			return;//no duplicates in most cases
		}
		CCCError cccError = alsData.getCccError();
		for (ALSForm form : alsData.getForms()) {
			processForm(form, cccError, oidDupSet, formNameDupSet);
		}
	}
	protected Set<String> findOidDuplicates(final List<ALSForm> formList) {
	    Set<String> uniques = new HashSet<>();
	    return formList.stream()
	    	.map(ALSForm::getFormOid)
	        .filter(str -> !uniques.add(str))
	        .collect(Collectors.toSet());
	}
	protected Set<String> findFormNameDuplicates(final List<ALSForm> formList) {
	    Set<String> uniques = new HashSet<>();
	    return formList.stream()
	    	.map(ALSForm::getDraftFormName)
	        .filter(str -> !uniques.add(str))
	        .collect(Collectors.toSet());
	}
	protected void processForm(final ALSForm form, CCCError cccError, final Set<String> oidDupSet, final Set<String> formNameDupSet) {
		String strOid;
		String strFormName;
		strOid = form.getFormOid();
		strFormName = form.getDraftFormName();
		if (oidDupSet.contains(strOid)) {//duplicate OID
			cccError.addAlsError(createPostProcessorDupError(AlsParser.err_msg_25, strOid, strOid, strFormName));
		}
		if (formNameDupSet.contains(strFormName)) {//duplicate Form Name
			cccError.addAlsError(createPostProcessorDupError(AlsParser.err_msg_26, strFormName, strOid, strFormName));
		}
	}
	protected ALSError createPostProcessorDupError(final String formatMsg, final String dupValue, final String formOid, final String formName) {
		ALSError alsError = new ALSError();
		alsError.setCellValue(formOid + " :: " + formName);
		alsError.setErrorDesc(String.format(formatMsg, dupValue));
		alsError.setErrorSeverity(AlsParser.errorSeverity_error);
		alsError.setFormOid(formOid);
		alsError.setSheetName(formSheetName);
		return alsError; 
	}
}
