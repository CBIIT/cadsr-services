/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.CCCError;
import gov.nih.nci.cadsr.data.CCCForm;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.FormsUiData;
import gov.nih.nci.cadsr.data.ValidateParamWrapper;
import gov.nih.nci.cadsr.report.CongruencyCheckerReportInvoker;
import gov.nih.nci.cadsr.service.FormService;

@Controller
public class GatewayBootController {
	static String CCHECKER_PARSER_URL;
	static String CCHECKER_DB_SERVICE_URL_CREATE;
	static String CCHECKER_DB_SERVICE_URL_RETRIEVE;
	static String CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR;
	static String CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_ERROR;
	static String CCHECKER_DB_SERVICE_URL_CREATE_REPORT_FULL;
	static String CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_FULL;	
	static String CCHECKER_VALIDATE_SERVICE_URL;
	static String CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL;

	private static String URL_RETRIEVE_ALS_FORMAT;
	private static String URL_RETRIEVE_REPORT_ERROR_FORMAT;
	private static String URL_RETRIEVE_REPORT_FULL_FORMAT;
	private static String URL_GEN_EXCEL_REPORT_ERROR_FORMAT;
	
	static String UPLOADED_FOLDER;
	static String ACCESS_CONTROL_ALLOW_ORIGIN;
	static final String sessionCookieName = "_cchecker";
	// FIXME shall be defined in ALSError
	static final String FATAL_ERROR_STATUS = "FATAL";
	static final String EXCEL_FILE_EXT = ".xlsx";
	public static final String fileExcelReportPrefix = "Report-";

	{
		loadProperties();
	}

	private final static Logger logger = LoggerFactory.getLogger(GatewayBootController.class);

	protected Cookie generateCookie() {
		Cookie cookie = new Cookie(sessionCookieName, generateIdseq());
		// cookie.setMaxAge(24 * 60 * 60); // (24 hours in seconds)
		cookie.setMaxAge(-1); // negative value means that the cookie is not
								// stored persistently and will be deleted when
								// the Web browser exits
		cookie.setPath("/");
		return cookie;
	}

	/**
	 * 
	 * @return UUID for IDSEQ
	 */
	protected String generateIdseq() {
		return java.util.UUID.randomUUID().toString().toUpperCase();
	}

	/**
	 * 
	 * @param filePath
	 *            - file saved previously on the service file system.
	 * @return ALSDataWrapper
	 */
	protected ALSDataWrapper submitPostRequestParser(String filePath) {
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(CCHECKER_PARSER_URL);

		// add some String
		builder.queryParam("filepath", filePath);

		// another staff
		ALSDataWrapper wrapper = new ALSDataWrapper();

		ResponseEntity<ALSData> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(),
				HttpMethod.POST, ALSData.class);

		HttpStatus statusCode = responseEntity.getStatusCode();
		wrapper.setStatusCode(statusCode);
		if (statusCode == HttpStatus.OK) {
			wrapper.setAlsData(responseEntity.getBody());
			logger.debug("parseservice result received");

		} 
		else {
			logger.error("parsefileservice sent an error: " + statusCode);
		}
		return wrapper;
	}

	/**
	 * 
	 * @param idseq
	 *            - saved previously in DB not null
	 * @return ALSData
	 */
	protected ALSData retrieveAlsData(String idseq) {
		return retrieveData(idseq, URL_RETRIEVE_ALS_FORMAT, ALSData.class);
	}
	/**
	 * 
	 * @param idseq
	 *            - saved previously in DB not null
	 * @return ALSData
	 */
	protected CCCReport retrieveReportError(String idseq) {
		return retrieveData(idseq, URL_RETRIEVE_REPORT_ERROR_FORMAT, CCCReport.class);
	}
	/**
	 * 
	 * @param idseq
	 *            - saved previously in DB not null
	 * @return ALSData
	 */
	protected Object retrieveReportFull(String idseq) {
		return retrieveData(idseq, URL_RETRIEVE_REPORT_FULL_FORMAT, Object.class);
	}
	/**
	 * 
	 * @param idseq
	 *            - saved previously in DB not null
	 * @return ALSData
	 */
	protected InputStreamResource retrieveGenExcelReportError(String idseq) {
		return retrieveData(idseq, URL_GEN_EXCEL_REPORT_ERROR_FORMAT, InputStreamResource.class);
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
			RestTemplate restTemplate = new RestTemplate();

			String urlStr = String.format(retrieveUrlStr, idseq);
			logger.debug("...retrieveData" + urlStr);

			data = restTemplate.getForObject(urlStr, clazz);
		}
		return data;
	}
	protected StringResponseWrapper submitPostRequestValidateForms(ALSData alsData, String idseq) {
		return submitPostRequestCreateGeneric(alsData, idseq, CCHECKER_DB_SERVICE_URL_CREATE);
	}
	/**
	 * 
	 * @param alsData
	 * @param idseq
	 * @return StringResponseWrapper
	 */
	protected StringResponseWrapper submitPostRequestSaveAls(ALSData alsData, String idseq) {
		return submitPostRequestCreateGeneric(alsData, idseq, CCHECKER_DB_SERVICE_URL_CREATE);
	}
	/**
	 * 
	 * @param CCCReport
	 * @param idseq
	 * @return StringResponseWrapper
	 */
	protected StringResponseWrapper submitPostRequestSaveReportError(CCCReport data, String idseq) {
		return submitPostRequestCreateGeneric(data, idseq, CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR);
	}
	/**
	 * This is a placeholder. Not implemented.
	 * 
	 * @param CCCReport
	 * @param idseq
	 * @return StringResponseWrapper
	 */
	protected StringResponseWrapper submitPostRequestSaveReportFull(Object reportFullData, String idseq) {
		//FIXME use a real Report Full Class
		return submitPostRequestCreateGeneric(reportFullData, idseq, CCHECKER_DB_SERVICE_URL_CREATE_REPORT_FULL);
	}
	/**
	 * 
	 * @param CCCReport
	 * @param idseq
	 * @return StringResponseWrapper
	 */
	protected <T>StringResponseWrapper submitPostRequestCreateGeneric(T data, String idseq, String createRequestUrlStr) {
		RestTemplate restTemplate = new RestTemplate();

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createRequestUrlStr);

		// add some String
		builder.queryParam(sessionCookieName, idseq);

		// another staff
		StringResponseWrapper wrapper = new StringResponseWrapper();
		HttpEntity<T> requestData = new HttpEntity<>(data);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(),
				requestData, String.class);

		HttpStatus statusCode = responseEntity.getStatusCode();
		wrapper.setStatusCode(statusCode);
		if (statusCode == HttpStatus.OK) {
			wrapper.setResponseData(responseEntity.getBody());
			logger.debug(createRequestUrlStr + " OK result received");

		} 
		else {
			logger.error(createRequestUrlStr + " sent an error: " + statusCode);
		}
		return wrapper;
	}
	
	protected CCCReport sendPostRequestValidator(List<String> selForms, String idseq, boolean checkUom, boolean checkCrf, boolean displayExceptions) {
		RestTemplate restTemplate = new RestTemplate();
		ValidateParamWrapper wrapper = new ValidateParamWrapper();
		wrapper.setSelForms(selForms);
		wrapper.setCheckUom(checkUom);
		wrapper.setCheckCrf(checkCrf);
		wrapper.setDisplayExceptions(displayExceptions);
		CCCReport cccReport = null;
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(CCHECKER_VALIDATE_SERVICE_URL);
		builder.queryParam(sessionCookieName, idseq);
		
		HttpEntity<ValidateParamWrapper> requestData = new HttpEntity<>(wrapper);
		ResponseEntity<CCCReport> responseEntity = restTemplate.postForEntity(builder.build().encode().toUri(),
				requestData, CCCReport.class);

		HttpStatus statusCode = responseEntity.getStatusCode();

		if (statusCode == HttpStatus.OK) {
			logger.debug(CCHECKER_VALIDATE_SERVICE_URL + " OK result received on validate: " + idseq);
			cccReport = (CCCReport) responseEntity.getBody();
			String fileNameOrg = cccReport.getFileName();
			logger.debug("Original file name of report: " + fileNameOrg);
			if (StringUtils.isBlank(fileNameOrg)) {
				cccReport.setFileName("Unknown");
			}
			List<CCCForm> forms = cccReport.getCccForms();
			if ((forms == null) || (forms.isEmpty())) {
				logger.error("!!!Red flag!!! forms are empty for report: "+ idseq);
			}
		} 
		else {
			logger.error("submitPostRequestValidator received an error on  an error: " + idseq + ", HTTP response code: " + statusCode);
		}
		
		return cccReport;
	}
	
	/**
	 * Upload one file.
	 * 
	 * @param uploadfile
	 * @return ResponseEntity
	 */
	@CrossOrigin
	@PostMapping("/parseservice")
	// @ResponseBody
	public ResponseEntity<?> parseService(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name = "owner", defaultValue = "guest") String reportOwner,
			@RequestParam("file") MultipartFile uploadfile) {
		logger.debug("uploadFile started");

		// upload file
		if (uploadfile.isEmpty()) {
			String errorMessage = "Please submit a file!";
			return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST);
		}
		String orgFileName = uploadfile.getOriginalFilename();
		logger.info("ALS file upload request: " + orgFileName);

		Path pathSavedFile;
		// create a Cookie
		Cookie cookie = generateCookie();
		String idseq = cookie.getValue();
		try {
			pathSavedFile = saveUploadedFile(uploadfile, idseq + EXCEL_FILE_EXT);
		} 
		catch (IOException e) {
			String errorMessage = "Error saving uploaded file: " + uploadfile.getName() + ' ' + e;
			return buildErrorResponse(errorMessage, HttpStatus.SERVICE_UNAVAILABLE);
		}

		String saveAbsPath = pathSavedFile.toFile().getAbsolutePath();
		logger.info("Successfully uploaded - " + orgFileName + " saved as " + saveAbsPath);

		// call parser
		ALSDataWrapper wrapper = submitPostRequestParser(saveAbsPath);
		HttpStatus parserStatusCode = wrapper.getStatusCode();
		if (!HttpStatus.OK.equals(parserStatusCode)) {
			String errorMessage = "Error on parsing file: " + uploadfile.getOriginalFilename();
			return buildErrorResponse(errorMessage, parserStatusCode);
		}

		ALSData alsData = wrapper.getAlsData();
		// check for fatal errors
		CCCError cccError = alsData.getCccError();
		List<ALSError> parserErrorList;
		if (cccError != null) {
			parserErrorList = cccError.getAlsErrors();
			if ((parserErrorList != null) && (!parserErrorList.isEmpty())) {
				ALSError alsError = parserErrorList.get(0);
				if (FATAL_ERROR_STATUS.equals(alsError.getErrorSeverity())) {
					String errorMessage = "Error in uploaded file: " + alsError.getErrorDesc();
					return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST);
				}
			}
		}

		// set file name and owner
		alsData.setFileName(orgFileName);
		alsData.setReportOwner(reportOwner);
		logger.debug("...after ALS parser before sending save alsData request. REPORT_OWNER: " + alsData.getReportOwner()
				+ ", FILE_NAME: " + alsData.getFileName() + ", idseq: " + idseq);

		// save ALSData DB into in a container
		 StringResponseWrapper saveResponse = submitPostRequestSaveAls(alsData, idseq);
		 HttpStatus responseStatusCode = saveResponse.getStatusCode();
		 if (! HttpStatus.OK.equals(responseStatusCode)) {
			 String errorMessage = "Error on saving ALS file: " + orgFileName + ", owner: " + reportOwner + ", idseq: " + idseq;
			 return buildErrorResponse(errorMessage, parserStatusCode);
		 }
		 else {
			 logger.info("Saved ALS file: " + orgFileName + ", idseq: " + idseq + ", owner: " + reportOwner);
		 }
		//
		// build result from parser data
		FormsUiData formUiData = FormService.buildFormsUiData(alsData);

		// set session cookie
		Cookie sessionCookie = retrieveCookie(request);
		if (sessionCookie != null) {
			sessionCookie.setValue(idseq);
		} else {
			sessionCookie = new Cookie(sessionCookieName, idseq);
		}
		response.addCookie(cookie);

		// If decided always return json type, put Content-Type to annotations
		// then
		HttpHeaders httpHeaders = createHttpOkHeaders();
		return new ResponseEntity<FormsUiData>(formUiData, httpHeaders, HttpStatus.OK);
	}

	@CrossOrigin
	@PostMapping("/checkservice")
	public ResponseEntity<?> checkService(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name = "checkUOM", required = false, defaultValue = "false") boolean checkUOM,
			@RequestParam(name = "checkCRF", required = false, defaultValue = "false") boolean checkCRF,
			@RequestParam(name = "displayExceptions", required = false, defaultValue = "false") boolean displayExceptions,
			RequestEntity<List<String>> requestEntity) {
		logger.debug("request received parseService");
		// check for session cookie
		Cookie cookie = retrieveCookie(request);
		if (cookie == null) {
			return buildErrorResponse("Session is not found", HttpStatus.BAD_REQUEST);
		}
		
		String sessionCookieValue = cookie.getValue();
		if (! ParameterValidator.validateIdSeq(sessionCookieValue)) {
			return buildErrorResponse("Session is not valid: " + sessionCookieValue, HttpStatus.BAD_REQUEST);
		}
		
		Cookie sessionCookie = new Cookie(sessionCookieName, sessionCookieValue);
		response.addCookie(sessionCookie);
		
		logger.debug("checkService session cookie: " + sessionCookieValue);
		
		List<String> formNames = requestEntity.getBody();
		logger.debug("Selected forms received: " + formNames);

		HttpStatus errorCode =  HttpStatus.BAD_REQUEST;

		//call Validator service
		CCCReport cccReport = sendPostRequestValidator(formNames, sessionCookieValue, checkUOM, checkCRF, displayExceptions);
		
		if (cccReport == null) {
			//We never expect validate request failure. It shall always send a report.
			logger.error("sendPostRequestValidator error on " + sessionCookieValue);
			return buildErrorResponse("Session data is not found based on: " + sessionCookieValue, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		StringResponseWrapper saveResponse = submitPostRequestSaveReportError(cccReport, sessionCookieValue);
		HttpStatus statusCode = saveResponse.getStatusCode();
		if (HttpStatus.OK.equals(statusCode)) {
			HttpHeaders httpHeaders = createHttpOkHeaders();
			return new ResponseEntity<CCCReport>(cccReport, httpHeaders, HttpStatus.OK);
		}
		else {
			logger.error("submitPostRequestSaveReportError received statusCode: " + statusCode);
			errorCode = statusCode;//This can be user error or server error
			return buildErrorResponse("Session data is not found based on: " + sessionCookieValue, errorCode);
		}
	}
	
	@CrossOrigin
	@GetMapping("/retrievereporterror")
	public ResponseEntity<?> retrieveErrorReport(HttpServletRequest request,
			@RequestParam(name="_cchecker", required=true) String idseq) {
		logger.debug("retrieveErrorReport called: " + idseq);

		Cookie cookie = retrieveCookie(request);
		if (cookie == null) {
			return buildErrorResponse("Session is not found", HttpStatus.BAD_REQUEST);
		}
		String sessionCookieValue = cookie.getValue();
		//FIXME idseq format check! check session token
		logger.debug("checkService session cookie: " + sessionCookieValue);
		CCCReport data = retrieveReportError(idseq);
		HttpStatus httpStatus;
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		if (data != null) {
			httpHeaders.add("Content-Type", "application/json");
			httpStatus = HttpStatus.OK;
		}
		else {
			httpStatus = HttpStatus.BAD_REQUEST;
		}
		return new ResponseEntity<CCCReport>(data, httpHeaders, httpStatus);
	}
	
	@CrossOrigin
	@GetMapping("/genexcelreporterror")
	public ResponseEntity<?> genExcelReportError(HttpServletRequest request) throws IOException {
		Cookie cookie = retrieveCookie(request);
		if (cookie == null) {
			return buildErrorResponse("Session is not found", HttpStatus.BAD_REQUEST);
		}
		
		String sessionCookieValue = cookie.getValue();
		//FIXME idseq format check! check session token
		logger.debug("genExcelReportError session cookie: " + sessionCookieValue);
		
		InputStream reqInputStream = request.getInputStream();
		InputStreamResource data = new InputStreamResource(reqInputStream);
		HttpStatus httpStatus;
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/vnd.ms-excel");
		httpHeaders.add("Content-Disposition", "attachment; filename=" + fileExcelReportPrefix + sessionCookieValue + EXCEL_FILE_EXT);
		httpStatus = HttpStatus.OK;
		return new ResponseEntity<InputStreamResource>(data, httpHeaders, httpStatus);
	}
	
	private Cookie retrieveCookie(HttpServletRequest request) {
		Cookie[] cookieArr = request.getCookies();
		Cookie sessionCookie = null;
		if (cookieArr != null) {
			for (Cookie currCookie : cookieArr) {
				if (sessionCookieName.equals(currCookie.getName())) {
					logger.debug("found sesion cookie: " + currCookie.getValue());
					sessionCookie = currCookie;
					break;
				}
			}
		}
		return sessionCookie;
	}

	/**
	 * This method is for feasibility only. We will retrieve ALSData from DB.
	 * 
	 * @return String file to ALS Excel file
	 */
	private String buildFilePath(String sessionUID) {
		return UPLOADED_FOLDER + sessionUID + EXCEL_FILE_EXT;
	}

	/**
	 * 
	 * @param errorMessage
	 * @param httpStatus
	 * @return ResponseEntity
	 */
	private ResponseEntity<String> buildErrorResponse(String errorMessage, HttpStatus httpStatus) {
		// TODO what context type shall be returned on an error - ? Now
		// text/plain
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "text/plain");
		// We have configured springframework CrossOrigin so we do not need this
		// header
		// assignAccessControlHeader(httpHeaders);
		logger.error(errorMessage);
		return new ResponseEntity<String>(errorMessage, httpHeaders, HttpStatus.BAD_REQUEST);
	}

	/**
	 * Creates a new file from its parameter.
	 * 
	 * @param file
	 *            not null
	 * @param fileIdseqName
	 *            not null
	 * @throws IOException
	 */
	protected Path saveUploadedFile(MultipartFile uploadfile) throws IOException {
		return saveUploadedFile(uploadfile, uploadfile.getOriginalFilename());
	}

	protected Path saveUploadedFile(MultipartFile uploadfile, String fileName) throws IOException {
		byte[] bytes = uploadfile.getBytes();
		Path path = Paths.get(UPLOADED_FOLDER + fileName);
		Path pathNew = Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
		return pathNew;
	}

	protected void assignAccessControlHeader(HttpHeaders httpHeaders) {
		httpHeaders.setAccessControlAllowOrigin(ACCESS_CONTROL_ALLOW_ORIGIN);
	}

	protected HttpHeaders createHttpOkHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		// We have configured springframework CrossOrigin so we do not need this
		// header
		// httpHeaders.setAccessControlAllowOrigin(ACCESS_CONTROL_ALLOW_ORIGIN);
		return httpHeaders;
	}

	/**
	 * The properties are taken from the boot service.
	 */
	protected static void loadProperties() {
		CCHECKER_PARSER_URL = GatewayBootWebApplication.CCHECKER_PARSER_URL;
		UPLOADED_FOLDER = GatewayBootWebApplication.UPLOADED_FOLDER;
		CCHECKER_DB_SERVICE_URL_CREATE = GatewayBootWebApplication.CCHECKER_DB_SERVICE_URL_CREATE;
		CCHECKER_DB_SERVICE_URL_RETRIEVE = GatewayBootWebApplication.CCHECKER_DB_SERVICE_URL_RETRIEVE;
		CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR = GatewayBootWebApplication.CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR;
		CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_ERROR = GatewayBootWebApplication.CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_ERROR;
		CCHECKER_DB_SERVICE_URL_CREATE_REPORT_FULL = GatewayBootWebApplication.CCHECKER_DB_SERVICE_URL_CREATE_REPORT_FULL;
		CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_FULL = GatewayBootWebApplication.CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_FULL;
		CCHECKER_VALIDATE_SERVICE_URL = GatewayBootWebApplication.CCHECKER_VALIDATE_SERVICE_URL;
		CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL = GatewayBootWebApplication.CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL;
		ACCESS_CONTROL_ALLOW_ORIGIN = GatewayBootWebApplication.ACCESS_CONTROL_ALLOW_ORIGIN;
		logger.debug("GatewayBootController CCHECKER_PARSER_URL: " + CCHECKER_PARSER_URL);
		logger.debug("GatewayBootController UPLOADED_FOLDER: " + UPLOADED_FOLDER);
		logger.debug("GatewayBootController CCHECKER_DB_SERVICE_URL_CREATE: " + CCHECKER_DB_SERVICE_URL_CREATE);
		logger.debug("GatewayBootController CCHECKER_DB_SERVICE_URL_RETRIEVE: " + CCHECKER_DB_SERVICE_URL_RETRIEVE);
		logger.debug("GatewayBootController CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR: " + CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR);
		logger.debug("GatewayBootController CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_ERROR: " + CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_ERROR);
		logger.debug("GatewayBootController CCHECKER_DB_SERVICE_URL_CREATE_REPORT_FULL: " + CCHECKER_DB_SERVICE_URL_CREATE_REPORT_FULL);
		logger.debug("GatewayBootController CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_FULL: " + CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_FULL);
		logger.debug("GatewayBootController CCHECKER_VALIDATE_SERVICE_URL: " + CCHECKER_VALIDATE_SERVICE_URL);
		logger.debug("GatewayBootController ACCESS_CONTROL_ALLOW_ORIGIN: " + ACCESS_CONTROL_ALLOW_ORIGIN);
		URL_RETRIEVE_ALS_FORMAT = CCHECKER_DB_SERVICE_URL_RETRIEVE + "?" + sessionCookieName + "=%s";
		URL_GEN_EXCEL_REPORT_ERROR_FORMAT = CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL + "?" + sessionCookieName + "=%s";
		URL_RETRIEVE_REPORT_ERROR_FORMAT = CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_ERROR + "?" + sessionCookieName + "=%s";
		URL_RETRIEVE_REPORT_FULL_FORMAT = CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_FULL + "?" + sessionCookieName + "=%s";
		logger.debug("GatewayBootController URL_RETRIEVE_ALS_FORMAT: " + URL_RETRIEVE_ALS_FORMAT);
		logger.debug("GatewayBootController URL_RETRIEVE_REPORT_ERROR_FORMAT: " + URL_RETRIEVE_REPORT_ERROR_FORMAT);
		logger.debug("GatewayBootController URL_RETRIEVE_REPORT_FULL_FORMAT: " + URL_RETRIEVE_REPORT_FULL_FORMAT);
		logger.debug("GatewayBootController CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL: " + CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL);
		logger.debug("GatewayBootController URL_GEN_EXCEL_REPORT_ERROR: " + URL_GEN_EXCEL_REPORT_ERROR_FORMAT);
	}

	// TODO remove testReportService service
	/**
	 * 
	 * @param request
	 * @param response
	 * @param filename
	 * @return ALSData
	 */
	@GetMapping("/testreportservice")
	@ResponseBody
	public CCCReport testReportService(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name = "filepath", required = true) String filepath,
			@RequestParam(name = "owner", defaultValue = "guest") String reportOwner) {
		ALSDataWrapper alsDataWrapper;
		Cookie cookie = generateCookie();
		alsDataWrapper = submitPostRequestParser(filepath);
		response.addCookie(cookie);
		CCCReport cccReport = CongruencyCheckerReportInvoker.builTestReport(alsDataWrapper.getAlsData());
		cccReport.setReportOwner(reportOwner);
		return cccReport;
	}
	//TODO Remove this test service
	/**
	 * 
	 * @param request
	 * @param response
	 * @param filename
	 * @return ALSData
	 */
	@CrossOrigin
	@GetMapping("/parsefileservice")
	@ResponseBody
	public ALSData parseFileService(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name = "filepath", required = true) String filepath) {
		ALSDataWrapper alsData;
		Cookie cookie = generateCookie();
		alsData = submitPostRequestParser(filepath);
		response.addCookie(cookie);
		return alsData.getAlsData();
	}
}