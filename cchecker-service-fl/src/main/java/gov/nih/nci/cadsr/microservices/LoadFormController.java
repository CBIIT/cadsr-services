/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
	public final String MS_EXCEL_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ConverterFormService formConverterService;
	
	@Autowired
	private LoadServiceRepositoryImpl loadServiceRepositoryImpl;
	
	@PostMapping(value = "/rest/loadforms")
	public ResponseEntity<?>loadForms(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name="_cchecker", required=true) String idseq,
			RequestEntity<FormLoadParamWrapper> requestEntity) {
		logger.debug("loadForms session: " + idseq);
		if (StringUtils.isBlank(idseq)) {
			//no session received
			return buildErrorResponse(strError, HttpStatus.BAD_REQUEST);
		}
		
		FormLoadParamWrapper formLoadParamWrapper = requestEntity.getBody();
		logger.info("loadForms body: " + formLoadParamWrapper);
		String fileName = null;
		String strMsg = "OK";
		try {
			ALSData alsData = retrieveAlsData(idseq);
			if (alsData != null) {
				logger.debug("Retrieved parsed file for validation, with name: " + alsData.getFileName());
				fileName = alsData.getFileName();//expected file name
				List<String> selForms = formLoadParamWrapper.getSelForms();
				List<ALSForm> alsFormList = alsData.getForms();
				
				if ((selForms != null) && (! selForms.isEmpty())) 
				{
					FormDescriptor formDescriptor;
					for (ALSForm alsForm : alsFormList) {
						alsForm.getDraftFormName();
						formDescriptor = formConverterService.convertAlsToCadsr(alsForm, alsData);
						formDescriptor.setContext(formLoadParamWrapper.getContextName());
						formDescriptor.setType("CRF");
						//FIXME this is for test only, what shall it be?
						formDescriptor.setLoadType(FormDescriptor.LOAD_TYPE_NEW);
						loadServiceRepositoryImpl.createForm(formDescriptor, null);
					}
					logger.info("Loaded forms ");
				}
				else {//source default data to add
					logger.error("No selected forms");
				}
			}
			else {
				strMsg = "FATAL error: no data found in retrieving ALSData parser data by ID: " + idseq;
				logger.error(strMsg);
			}
			
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("Content-Type", "text/plain");
			return new ResponseEntity<String>("load form controller " + fileName + ", strMsg: " + strMsg +" \n", 
				httpHeaders, HttpStatus.OK);
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
}
