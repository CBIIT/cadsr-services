/*
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.repository.impl.LoadServiceRepositoryImpl;
import gov.nih.nci.cadsr.formloader.service.impl.ContentValidationServiceImpl;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObjectExt;
/**
 * This is a class to load forms using Executor thread pool.
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
	
	/**
	 * Load ALS Form to caDSR DB.
	 * 
	 * @param contextName not null
	 * @param contextIdseq not null
	 * @param alsData not null
	 * @param alsForm not null
	 * @param protocols not null
	 * @return CompletableFuture with a String form Long Name or null if an error.
	 */
	@Async("formThreadPoolTaskExecutor")
	public CompletableFuture<String> loadFormTocaDsr (String contextName, String contextIdseq, 
			ALSData alsData, ALSForm alsForm, List<ProtocolTransferObjectExt> protocols) {
		String formLongName = null;
		//Load ALS Form to caDSR DB
		try {
			formLongName = createAlsForm(contextName, contextIdseq, alsData, alsForm, protocols);
		}
		catch(Exception e) {
			logger.error("loadFormTocaDsr error: contextName: " + contextName +
				", Report Owner: " + alsData.getReportOwner() + ", alsForm: " + alsForm.getDraftFormName() + ", exception: " +  e);
		}
		return CompletableFuture.completedFuture(formLongName);
	}
	/**
	 * 
	 * @param contextName
	 * @param contextIdseq
	 * @param alsData
	 * @param alsForm
	 * @param protocols
	 * @return created form long name
	 * @throws Exception
	 */
	protected String createAlsForm(String contextName, String contextIdseq, 
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
		String currIdseq = null;
		for (FormDescriptor validFormDescriptor : formColl.getForms()) {//always  expecting 1 object in our case
			//load new form using FL code
			logger.info("........Load new form started: " + alsForm.getDraftFormName());
			currIdseq = loadServiceRepositoryImpl.createForm(validFormDescriptor, null);
			logger.info("Loaded a new form: " + alsForm.getDraftFormName() + ". IDSeq: " + currIdseq);
		}
		Thread.sleep(1000L);
		return alsForm.getDraftFormName();
	}
}
