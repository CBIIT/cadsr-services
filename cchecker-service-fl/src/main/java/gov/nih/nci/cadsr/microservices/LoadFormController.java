/*
 * Copyright (C) 2019 Frederick National Laboratory for Cancer Research - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.util.ArrayList;
import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.FormLoadParamWrapper;
import gov.nih.nci.cadsr.formloader.repository.impl.LoadServiceRepositoryImpl;
import gov.nih.nci.cadsr.formloader.service.impl.ContentValidationServiceImpl;
import gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.resource.Protocol;
import gov.nih.nci.ncicb.cadsr.common.resource.Context;
/**
 * Generate FL Forms from ALS forms Controller.
 * 
 * @author asafievan
 *
 */
@RestController
@EnableAutoConfiguration
@EnableAsync
public class LoadFormController {
	private final static Logger logger = LoggerFactory.getLogger(LoadFormController.class);
	protected static String CCHECKER_DB_SERVICE_URL_REPORT_ERROR_RETRIEVE = CCheckerLoadFormService.CCHECKER_DB_SERVICE_URL_REPORT_ERROR_RETRIEVE;
	protected static String CCHECKER_DB_SERVICE_URL_RETRIEVE = CCheckerLoadFormService.CCHECKER_DB_SERVICE_URL_RETRIEVE;
	protected static String REPORT_FOLDER = CCheckerLoadFormService.REPORT_FOLDER;
	public final String strError = "No report information received";
	public final String strErrorWrongContext = "Wrong context information received: ";
	public final String strErrorNoFormFound= "No form found";
	public final String MS_EXCEL_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private ConverterFormService formConverterService;
	
	@Autowired
	private LoadServiceRepositoryImpl loadServiceRepositoryImpl;
	
	@Autowired
	private ContentValidationServiceImpl contentValidationServiceImpl;
	
	@Autowired
	private LoadFormService loadFormService;
	
	@Autowired
	private ConverterFormV2Service converterFormV2Service;

	protected String retrieveContextIdseq(String contextName) {
		String res = null;
		if (!StringUtils.isBlank(contextName))
			return loadServiceRepositoryImpl.getContextSeqIdByName(contextName);
		else {
			logger.error("Request context is empty");
		}
		return res;
	}

	protected String retrieveProtocolIdseq(String protocolAlsName) {
		List<String> protocolIdseqList;
		//we need Protocol IDSEQ to add a protocol to a form
		String protocolIdseq = null;
		if (StringUtils.isNotBlank(protocolAlsName)) {
			//retrieve protocol information for forms by Protocol LongName
			protocolIdseqList = loadServiceRepositoryImpl.getProtocolV2Dao()
				.getProtocolIdseqByLongName(protocolAlsName);
			if ((protocolIdseqList != null) && (protocolIdseqList.size() == 1)) {
				protocolIdseq = protocolIdseqList.get(0);
			} 
			else if ((protocolIdseqList != null) && (protocolIdseqList.size() > 1)) {
				logger.warn("Protocol ALS Name is not unique as caDSR Protocol LongName: " + protocolAlsName, protocolIdseqList);
				protocolIdseq = protocolIdseqList.get(0);
			} 
			if (protocolIdseq == null) {//we could not a protocol find by LongName
				//retrieve protocol information for forms by Protocol PreferredName
				protocolIdseqList = loadServiceRepositoryImpl.getProtocolV2Dao()
						.getProtocolIdseqByPreferredName(protocolAlsName);
					if ((protocolIdseqList != null) && (protocolIdseqList.size() == 1)) {
						protocolIdseq = protocolIdseqList.get(0);
					} 
					else if ((protocolIdseqList != null) && (protocolIdseqList.size() > 1)) {
						logger.warn("Protocol ALS Name is not unique as caDSR Protocol PreferredName: " + protocolAlsName, protocolIdseqList);
						protocolIdseq = protocolIdseqList.get(0);
					} 
					else {
						logger.error("Protocol ALS Name is not found: " + protocolAlsName);
					}
			}
		}
		return protocolIdseq;
	}
	protected List<ProtocolTransferObjectExt> buildProtocolListForFormLoad(String protocolAlsdName, String protocolIdseq) {
		List<ProtocolTransferObjectExt> protocols = new ArrayList<ProtocolTransferObjectExt>();
		if (protocolIdseq != null) {//at that point this is an existed caDSR DB Protocol
			ProtocolTransferObjectExt protocol = new ProtocolTransferObjectExt();
			//we need only IDSEQ for FL load legacy code
			protocol.setIdseq(protocolIdseq);
			protocols.add(protocol);
		}
		return protocols;
	}
	protected List<ProtocolTransferObjectExt> buildProtocolListForFormXml(String protocolAlsdName, String protocolIdseq, String formContextname) {
		List<ProtocolTransferObjectExt> protocols = new ArrayList<ProtocolTransferObjectExt>();
		ProtocolTransferObjectExt protocol = new ProtocolTransferObjectExt();
		if (protocolIdseq != null) {//at that point this is an existed caDSR DB Protocol
			Protocol protocolcaDSR = loadServiceRepositoryImpl.getProtocolV2Dao().getProtocolByPK(protocolIdseq);
			protocol.setIdseq(protocolIdseq);
			protocol.setPreferredName(protocolcaDSR.getPreferredName());
			protocol.setLongName(protocolcaDSR.getLongName());
			protocol.setPreferredDefinition(protocolcaDSR.getPreferredDefinition());
			protocol.setProtocolId(protocolcaDSR.getProtocolId());
			protocol.setContext(protocolcaDSR.getContext());
		}
		else {
			Context context = new ContextTransferObject();
			context.setName(formContextname);
			protocol.setPreferredName(protocolAlsdName);
			protocol.setPreferredDefinition(protocolAlsdName);
			protocol.setProtocolId(protocolAlsdName);
			protocol.setContext(context);
		}
		protocols.add(protocol);
		return protocols;
	}
	/**
	 * Load ALS Forms to caDSR asynchronously.
	 * 
	 * @param alsForm
	 * @param selForms
	 * @return List<String> list of loaded form names
	 */
	/*  We have a problem with DB deadlock
	public List<String> formLoadExecAsync(ALSData alsData, List<String> selForms, 
			String contextName, String conteIdseq, List<ProtocolTransferObjectExt> protocols) {
		//long start = System.currentTimeMillis();
		List<ALSForm> alsFormList = alsData.getForms();
		List<String> resultList = new ArrayList<>();
		if ((alsFormList == null) || (selForms == null) || (selForms.isEmpty())) return resultList;
	
		List<CompletableFuture<String>> arrFuture = new ArrayList<>(selForms.size());

			//long stepStart = System.currentTimeMillis();
		for (ALSForm alsForm : alsFormList) {
			if (selForms.contains(alsForm.getFormOid())) {
				logger.info("Loading form: " + alsForm.getDraftFormName());
				//load ALS data as FL form
				CompletableFuture<String> futureFormLongName = loadFormService.loadFormTocaDsr(contextName, conteIdseq, alsData, alsForm, protocols);
				//collect processed form names to return as JSON Array
				arrFuture.add(futureFormLongName);
			}
		}
		String curr;
		CompletableFuture.allOf(arrFuture.toArray(new CompletableFuture[arrFuture.size()])).join();
		for (CompletableFuture<String> future : arrFuture) {
			try {
				curr = future.get();
				if (curr != null) {
					resultList.add(curr);
				}
			} catch (InterruptedException e) {
				logger.error("formLoadExecAsync InterruptedException: " + e);
				e.printStackTrace();
			} catch (ExecutionException e) {
				logger.error("formLoadExecAsync ExecutionException: " + e);
				e.printStackTrace();
			}
		}
		return resultList;
	}
	*/
	/**
	 * Load ALS Forms to caDSR sequentially.
	 * 
	 * @param alsForm
	 * @param selForms
	 * @return List<String> list of loaded form names
	 */
	
	public List<String> formLoad(ALSData alsData, List<String> selForms, 
			String contextName, String conteIdseq, List<ProtocolTransferObjectExt> protocols) {
		//long start = System.currentTimeMillis();
		List<ALSForm> alsFormList = alsData.getForms();
		List<String> resultList = new ArrayList<>();
		if ((alsFormList == null) || (selForms == null) || (selForms.isEmpty())) return resultList;

			//long stepStart = System.currentTimeMillis();
		for (ALSForm alsForm : alsFormList) {
			if (selForms.contains(alsForm.getFormOid())) {
				logger.info("Loading form: " + alsForm.getDraftFormName());
				//load ALS data as FL form
				String formLongName = loadFormService.loadForm2caDsr(contextName, conteIdseq, alsData, alsForm, protocols);
				//collect processed form names to return as JSON Array
				if (formLongName != null)
					resultList.add(formLongName);
			}
		}

		return resultList;
	}
	/**
	 * 
	 * @param request
	 * @param response
	 * @param idseq
	 * @param requestEntity
	 * @return ResponseEntity which is JSON string array. On error, the array has one string error message.
	 */
	
	@PostMapping(value = "/rest/loadforms")
	public ResponseEntity<?>loadForms(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name="_cchecker", required=true) String idseq,
			RequestEntity<FormLoadParamWrapper> requestEntity) {
		logger.debug("loadForms session: " + idseq);
		//prepare data from parameters
		//session ID
		if (StringUtils.isBlank(idseq)) {
			//no session received
			return buildErrorResponse(strError, HttpStatus.BAD_REQUEST);
		}
		
		FormLoadParamWrapper formLoadParamWrapper = requestEntity.getBody();
		logger.info("loadForms body: " + formLoadParamWrapper);

		//retrieve context
		String contextIdseq = retrieveContextIdseq(formLoadParamWrapper.getContextName());
		if (StringUtils.isBlank(contextIdseq)) {
			//context not found
			return buildErrorResponse(strErrorWrongContext + formLoadParamWrapper.getContextName(), HttpStatus.BAD_REQUEST);
		}
		
		String strMsg = "OK";
		HttpStatus httpStatus = HttpStatus.OK;
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		try {
			ALSData alsData = retrieveAlsData(idseq);
			if (alsData != null) {
				String fileName = alsData.getFileName();//expected file name
				logger.info("Retrieved parsed file for form generation, with name: " + fileName);
				
				List<ALSForm> alsFormList = alsData.getForms();
				List<String> selForms = getFormIdList(formLoadParamWrapper.getSelForms(), alsFormList);
				//check that the file contains selected forms
				if (selForms.isEmpty()) {
					return buildErrorResponse(strErrorNoFormFound , HttpStatus.BAD_REQUEST);
				}
				
				//retrieve protocol information for forms
				String protocolAlsName = alsData.getCrfDraft().getProjectName();
				//we need Protocol IDSEQ to add a protocol to a form
				String protocolIdseq = retrieveProtocolIdseq(protocolAlsName);

				List<ProtocolTransferObjectExt> protocols = buildProtocolListForFormLoad(protocolAlsName, protocolIdseq);
				
				//we do not use Executor pool for FL requests for now because of DB deadlock
//				List<String> formNamesLoaded = formLoadExecAsync(alsData, selForms, 
//					formLoadParamWrapper.getContextName(), contextIdseq, protocols);
				
				List<String> formNamesLoaded = formLoad(alsData, selForms, 
						formLoadParamWrapper.getContextName(), contextIdseq, protocols);
				httpHeaders.add("Content-Type", "application/json");
				return new ResponseEntity<List<String>>(formNamesLoaded, httpHeaders, httpStatus);
			}
			else {
				strMsg = "FATAL error: no parsed data found in retrieving ALSData parser data by ID: " + idseq;
				List<String> errorArr = new ArrayList<>();
				errorArr.add(strMsg);
				logger.error(strMsg);
				httpStatus = HttpStatus.BAD_REQUEST;
				return new ResponseEntity<List<String>>(errorArr, httpHeaders, httpStatus);
			}
		}
		catch (RestClientException e) {
			e.printStackTrace();
			logger.error("RestClientException on idseq: " + idseq, e);
			//return buildErrorResponse("ALS is not found by " + idseq + e, HttpStatus.BAD_REQUEST);
			strMsg = "ALS is not found by " + idseq + e;
			List<String> errorArr = new ArrayList<>();
			errorArr.add(strMsg);
			logger.error(strMsg);
			httpStatus = HttpStatus.BAD_REQUEST;
			return new ResponseEntity<List<String>>(errorArr, httpHeaders, httpStatus);
		}
		catch (Exception e) {
			e.printStackTrace();
			//return buildErrorResponse("server error in form load on id: " + idseq + e, HttpStatus.INTERNAL_SERVER_ERROR);
			strMsg = "server error in form load on id: " + idseq + e;
			List<String> errorArr = new ArrayList<>();
			errorArr.add(strMsg);
			logger.error(strMsg);
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			return new ResponseEntity<List<String>>(errorArr, httpHeaders, httpStatus);
		}
	}

	protected ResponseEntity<String> buildErrorResponse(String errorMessage, HttpStatus httpStatus) {
		// TODO what context type shall be returned on an error - ? Now
		// text/plain
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "text/plain");
		logger.error(errorMessage);
		return new ResponseEntity<String>(errorMessage, httpHeaders, HttpStatus.BAD_REQUEST);
	}
	
	protected HttpHeaders createHttpOkHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		return httpHeaders;
	}
	/**
	 * 
	 * @param idseq not null
	 * @param strMessageFormat not null
	 * @param httpStatus not null
	 * @return
	 */
	protected ResponseEntity<String> buildErrorResponseStream(String idseq, String strMessageFormat,
		HttpStatus httpStatus) {
		String errorMessage = String.format(strMessageFormat, idseq);
		HttpHeaders responseHeaders = new HttpHeaders();
		logger.error("Response error message: " + errorMessage);
		responseHeaders.set("Content-Type", "text/plain");
		return new ResponseEntity<String>(errorMessage, responseHeaders, httpStatus);
	}
	/**
	 * 
	 * @param idseq saved previously in DB not null
	 * @return CCCReport
	 */
	protected ALSData retrieveAlsData(String idseq) {
		return retrieveData(idseq, CCHECKER_DB_SERVICE_URL_RETRIEVE, ALSData.class);
	}	
	/**
	 * 
	 * @param idseq
	 *            - saved previously in DB not null
	 * @return Data
	 */
	protected <T> T retrieveData(String idseq, String retrieveUrlStr, Class<T> clazz) {
		T data = null;
		if (idseq != null) {
			String urlStr = String.format(retrieveUrlStr, idseq);
			logger.debug("...retrieveData: " + urlStr);

			data = restTemplate.getForObject(urlStr, clazz);
		}
		return data;
	}
		
	/**
	 * Returns a list of Form OIDs for the respective Form Names list 
	 * 
	 * @param selForms
	 * @param formsList
	 * @return List<String>
	 */		
	protected static List<String> getFormIdList(List<String> selForms, List<ALSForm> formsList) {
		List<String> formIdsList = new ArrayList<String>();
		for (String selectedFormName : selForms) {			
			for (ALSForm alsForm : formsList) {
				if (alsForm.getDraftFormName().equalsIgnoreCase(selectedFormName)) {
						formIdsList.add(alsForm.getFormOid());
					}
				}
		}
		return formIdsList;
	}

	@PostMapping(value = "/rest/formxml")
	public ResponseEntity<?>generateLoadFormXml(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name="_cchecker", required=true) String idseq,
			RequestEntity<FormLoadParamWrapper> requestEntity) {
		logger.debug("generateLoadFormXml session: " + idseq);
		//prepare data from parameters
		//session ID
		if (StringUtils.isBlank(idseq)) {
			//no session received
			return buildErrorResponse(strError, HttpStatus.BAD_REQUEST);
		}
		
		FormLoadParamWrapper formLoadParamWrapper = requestEntity.getBody();
		logger.info("generateLoadFormXml body: " + formLoadParamWrapper);

		//retrieve context
		String contextIdseq = retrieveContextIdseq(formLoadParamWrapper.getContextName());
		if (StringUtils.isBlank(contextIdseq)) {
			//context not found
			return buildErrorResponse(strErrorWrongContext + formLoadParamWrapper.getContextName(), HttpStatus.BAD_REQUEST);
		}
		
		String strMsg = "OK";
		HttpStatus httpStatus = HttpStatus.OK;
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		try {
			ALSData alsData = retrieveAlsData(idseq);
			if (alsData != null) {
				String fileName = alsData.getFileName();//expected file name
				logger.info("Retrieved parsed file for form XML generation, with name: " + fileName);
				
				List<ALSForm> alsFormList = alsData.getForms();
				List<String> selForms = getFormIdList(formLoadParamWrapper.getSelForms(), alsFormList);
				//check that the file contains selected forms
				if (selForms.isEmpty()) {
					return buildErrorResponse(strErrorNoFormFound , HttpStatus.BAD_REQUEST);
				}
				
				//retrieve protocol information for forms
				String protocolAlsName = alsData.getCrfDraft().getProjectName();
				//we need Protocol IDSEQ to add a protocol to a form
				String protocolIdseq = retrieveProtocolIdseq(protocolAlsName);

				List<ProtocolTransferObjectExt> protocols = buildProtocolListForFormXml(protocolAlsName, protocolIdseq,formLoadParamWrapper.getContextName());

				List<String> xmlFormList = converterFormV2Service.prepareXmlFile(idseq, formLoadParamWrapper.getContextName(), 
						contextIdseq, alsData, selForms, protocols);
				httpHeaders.add("Content-Type", "application/json");
				//return names of Form in XML-generated document
				return new ResponseEntity<List<String>>(xmlFormList, httpHeaders, httpStatus);
			}
			else {
				strMsg = "FATAL error: no parsed data found in retrieving ALSData parser data by ID: " + idseq;
				List<String> errorArr = new ArrayList<>();
				errorArr.add(strMsg);
				logger.error(strMsg);
				httpStatus = HttpStatus.BAD_REQUEST;
				return new ResponseEntity<List<String>>(errorArr, httpHeaders, httpStatus);
			}
		}
		catch (RestClientException e) {
			e.printStackTrace();
			logger.error("RestClientException on idseq: " + idseq, e);
			//return buildErrorResponse("ALS is not found by " + idseq + e, HttpStatus.BAD_REQUEST);
			strMsg = "ALS is not found by " + idseq + e;
			List<String> errorArr = new ArrayList<>();
			errorArr.add(strMsg);
			logger.error(strMsg);
			httpStatus = HttpStatus.BAD_REQUEST;
			return new ResponseEntity<List<String>>(errorArr, httpHeaders, httpStatus);
		}
		catch (Exception e) {
			e.printStackTrace();
			//return buildErrorResponse("server error in form load on id: " + idseq + e, HttpStatus.INTERNAL_SERVER_ERROR);
			strMsg = "server error in form load on id: " + idseq + e;
			List<String> errorArr = new ArrayList<>();
			errorArr.add(strMsg);
			logger.error(strMsg);
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			return new ResponseEntity<List<String>>(errorArr, httpHeaders, httpStatus);
		}
	}

	//We do not use Executor pool for now
//	@Bean(name = "formThreadPoolTaskExecutor")
//	public Executor formAsyncExecutor() {
//		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		executor.setCorePoolSize(6);
//		executor.setMaxPoolSize(12);
//		executor.setQueueCapacity(100);
//		executor.setThreadNamePrefix("LoadAlsForm-");
//		executor.initialize();
//		logger.debug("Created bean formThreadPoolTaskExecutor: " + Thread.currentThread().getName());
//		return executor;
//	}
}
