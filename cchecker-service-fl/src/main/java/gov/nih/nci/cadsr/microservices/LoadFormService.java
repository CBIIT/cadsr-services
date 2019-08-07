/*
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.repository.impl.LoadServiceRepositoryImpl;
import gov.nih.nci.cadsr.formloader.service.impl.ContentValidationServiceImpl;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObjectExt;
/**
 * This is a class to load forms or generate FL Forms XML.
 * 
 * @author asafievan
 *
 */
@Service
public class LoadFormService {
	private final static Logger logger = LoggerFactory.getLogger(LoadFormService.class);
	
	@Autowired
	private ConverterFormService formConverterService;
	
	@Autowired
	private LoadServiceRepositoryImpl loadServiceRepositoryImpl;
	
	@Autowired
	private ContentValidationServiceImpl contentValidationServiceImpl;
		
	public String loadForm2caDsr (String contextName, String contextIdseq, 
			ALSData alsData, ALSForm alsForm, List<ProtocolTransferObjectExt> protocols) {
		String formLongName = null;
		//Load ALS Form to caDSR DB
		try {
			formLongName = loadAlsForms2caDSR(contextName, contextIdseq, alsData, alsForm, protocols);
		}
		catch(Exception e) {
			logger.error("loadFormTocaDsr error: contextName: " + contextName +
				", Report Owner: " + alsData.getReportOwner() + ", alsForm: " + alsForm.getDraftFormName() + ", exception: " +  e);
		}
		return formLongName;
	}
	/**
	 * 
	 * @param contextName
	 * @param contextIdseq
	 * @param alsData
	 * @param alsForm
	 * @param protocols
	 * @return FormCollection FL class to generate forms
	 * @throws Exception
	 */
	protected FormCollection mapAlsForm(String contextName, String contextIdseq, 
			ALSData alsData, ALSForm alsForm, List<ProtocolTransferObjectExt> protocols) throws Exception {
		//map ALS data to FL form attributes
		FormDescriptor formDescriptor = new FormDescriptor();
		formDescriptor.setContext(contextName);
		formDescriptor.setContextSeqid(contextIdseq);
		formDescriptor.setProtocols(protocols);
		formDescriptor.setSelected(true);
		//add FL Form attributes mapping ALS and FL data
		formDescriptor = formConverterService.convertAlsToCadsr(alsForm, alsData, formDescriptor);
		
		//add other form attributes using FL code
		FormCollection formColl = new FormCollection();//this object is created for FL API only
		List<FormDescriptor> forms = new ArrayList<FormDescriptor>();
		forms.add(formDescriptor);
		formColl.setForms(forms);
		formColl = contentValidationServiceImpl.validateXmlContent(formColl);
		return formColl;
	}
	/**
	 * This method loads a form to caDSR DB.
	 * 
	 * @param contextName
	 * @param contextIdseq
	 * @param alsData
	 * @param alsForm
	 * @param protocols
	 * @return created form long name
	 * @throws Exception
	 */
	protected String loadAlsForms2caDSR(String contextName, String contextIdseq, 
			ALSData alsData, ALSForm alsForm, List<ProtocolTransferObjectExt> protocols) throws Exception {
		//add other form attributes using FL code
		FormCollection formColl = mapAlsForm(contextIdseq, contextIdseq, alsData, alsForm, protocols);//this object is created for FL API only
		String currIdseq = null;
		for (FormDescriptor validFormDescriptor : formColl.getForms()) {//always  expecting 1 object in our case
			//load new form using FL code
			logger.info("........Load new form started: " + alsForm.getDraftFormName());
			currIdseq = loadServiceRepositoryImpl.createForm(validFormDescriptor, null);
			logger.info("Loaded a new form: " + alsForm.getDraftFormName() + ". IDSeq: " + currIdseq);
		}
		return alsForm.getDraftFormName();
	}


}
