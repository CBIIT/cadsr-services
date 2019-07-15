/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.FormLoadParamWrapper;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.repository.impl.LoadServiceRepositoryImpl;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObjectExt;
/**
 * Generate FL Forms from ALS forms Controller.
 * 
 * @author asafievan
 *
 */
@RestController
@EnableAutoConfiguration
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

	protected String retrieveContextIdseq(String contextName) {
		String res = null;
		if (!StringUtils.isBlank(contextName))
			return loadServiceRepositoryImpl.getContextSeqIdByName(contextName);
		else {
			logger.error("Request context is empty");
		}
		return res;
	}

	protected String retrieveProtocolIdseq(String protocolPreferredName) {
		List<String> protocolIdseqList;
		//we need Protocol IDSEQ to add a protocol to a form
		String protocolIdseq = null;
		//retrieve protocol information for forms
		if (StringUtils.isNotBlank(protocolPreferredName)) {
			//retrieve protocol information for forms
			protocolIdseqList = loadServiceRepositoryImpl.getProtocolV2Dao()
					.getProtocolIdseqByPreferredName(protocolPreferredName);
			if ((protocolIdseqList != null) && (protocolIdseqList.size() == 1)) {
				protocolIdseq = protocolIdseqList.get(0);
			} 
			else if ((protocolIdseqList != null) && (protocolIdseqList.size() > 1)) {
				logger.warn("Protocol Preferred Name is not unique: " + protocolPreferredName, protocolIdseqList);
				protocolIdseq = protocolIdseqList.get(0);
			} 
			else {
				logger.error("Protocol Preferred Name is not found: " + protocolPreferredName);
			}
		}
		return protocolIdseq;
	}
	protected List<ProtocolTransferObjectExt> buildProtocolListForForms(String protocolPreferredName, String protocolIdseq) {
		List<ProtocolTransferObjectExt> protocols = new ArrayList<ProtocolTransferObjectExt>();
		if (protocolIdseq != null) {
			ProtocolTransferObjectExt protocol = new ProtocolTransferObjectExt();
			//we do not need protocolPreferredName for FL legacy code, only IDSEQ
			protocol.setPreferredName(protocolPreferredName);
			protocol.setIdseq(protocolIdseq);
			protocols.add(protocol);
		}
		return protocols;
	}
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
				String protocolPreferredName = alsData.getCrfDraft().getProjectName();
				//we need Protocol IDSEQ to add a protocol to a form
				String protocolIdseq = retrieveProtocolIdseq(protocolPreferredName);

				List<ProtocolTransferObjectExt> protocols = buildProtocolListForForms(protocolPreferredName, protocolIdseq);
				
				List<String> formNamesLoaded = new ArrayList<String>();

				for (ALSForm alsForm : alsFormList) {
					if (selForms.contains(alsForm.getFormOid())) {
						logger.info("Loading form: " + alsForm.getDraftFormName());
						//collect processed form names to return as JSON Array
						formNamesLoaded.add(alsForm.getDraftFormName());
						//map ALS data to FL form attributes
						FormDescriptor formDescriptor = new FormDescriptor();
						formDescriptor.setContext(formLoadParamWrapper.getContextName());
						formDescriptor.setContextSeqid(contextIdseq);
						formDescriptor.setProtocols(protocols);
						formDescriptor = formConverterService.convertAlsToCadsr(alsForm, alsData, formDescriptor);
						//add other form attributes
						String currIdseq = loadServiceRepositoryImpl.createForm(formDescriptor, null);
						logger.info("Loaded form: " + alsForm.getDraftFormName() + ". IDSeq: " + currIdseq);
					}
				}
				logger.info("Total forms loaded: " + formNamesLoaded.size());
				httpHeaders.add("Content-Type", "application/json");
				return new ResponseEntity<List<String>>(formNamesLoaded, httpHeaders, httpStatus);
			}
			else {
				strMsg = "FATAL error: no parsed data found in retrieving ALSData parser data by ID: " + idseq;
				logger.error(strMsg);
				httpStatus = HttpStatus.BAD_REQUEST;
				httpHeaders.add("Content-Type", "text/plain");
				return new ResponseEntity<String>(strMsg +" \n", httpHeaders, httpStatus);
			}
		}
		catch (RestClientException e) {
			e.printStackTrace();
			logger.error("RestClientException on idseq: " + idseq, e);
			return buildErrorResponse("ALS is not found by " + idseq + e, HttpStatus.BAD_REQUEST);
		}
		catch (Exception e) {
			e.printStackTrace();
			return buildErrorResponse("error in form load on id: " + idseq + e, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	protected Path saveUploadedFile(MultipartFile uploadfile, String fileName) throws IOException {
		byte[] bytes = uploadfile.getBytes();
		Path path = Paths.get(REPORT_FOLDER + fileName);
		Path pathNew = Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
		return pathNew;
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
}
