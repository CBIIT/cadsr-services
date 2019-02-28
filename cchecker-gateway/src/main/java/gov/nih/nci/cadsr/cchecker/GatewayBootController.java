/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
//TODO Tomcat dependency
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSError;
import gov.nih.nci.cadsr.data.CCCError;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.FormsUiData;

@Controller
//@RestController
//@RequestMapping("/")
public class GatewayBootController {
	static String CCHECKER_PARSER_URL;
	static String CCHECKER_DB_SERVICE_URL_CREATE;
	static String CCHECKER_DB_SERVICE_URL_RETRIEVE;
	static String CCHECKER_DB_SERVICE_URL_CREATE_REPORT_ERROR;
	static String CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_ERROR;
	static String CCHECKER_DB_SERVICE_URL_CREATE_REPORT_FULL;
	static String CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_FULL;	
	static String CCHECKER_VALIDATE_SERVICE_URL;
	static String CCHECKER_FEED_VALIDATE_SERVICE_URL;
	static String CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL;

	private static String URL_RETRIEVE_ALS_FORMAT;
	private static String URL_RETRIEVE_REPORT_ERROR_FORMAT;
	private static String URL_RETRIEVE_REPORT_FULL_FORMAT;
	private static String URL_GEN_EXCEL_REPORT_ERROR_FORMAT;
	private static String URL_FEED_VALIDATE_STATUS_FORMAT;
	public static final String MS_EXCEL_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String TEXT_PLAIN_MIME_TYPE = "text/plain";
	
	static String UPLOADED_FOLDER;
	static String ACCESS_CONTROL_ALLOW_ORIGIN;
	static final String sessionCookieName = "_cchecker";
	// FIXME shall be defined in ALSError
	static final String FATAL_ERROR_STATUS = "FATAL";
	static final String EXCEL_FILE_EXT = ".xlsx";
	public static final String fileExcelReportPrefix = "Report-";
	public static final String COOKIE_PATH = "/gateway";
	
	static final int timeBetweenFeeds = 2000;
	static final int maxFeedRequests = 300;
	static final String SESSION_NOT_VALID = "Session is not found or not valid: ";
	static final String SESSION_DATA_NOT_FOUND = "Session data is not found based on: ";
	static final String VALIDATE_SERVICE_URL_STR = "validateservice";
	static final String RETRIEVE_ERROR_REPORT_URL_STR = "retrievereporterror";

	{
		loadProperties();
	}

	private final static Logger logger = LoggerFactory.getLogger(GatewayBootController.class);
	
	@Autowired
    private ServiceParser serviceParser;
	@Autowired
    private ServiceDb serviceDb;
	@Autowired
	private FormService formService;
	
	@Autowired
	private ServiceValidator serviceValidator;
	
	protected static Cookie generateCookie() {
		Cookie cookie = new Cookie(sessionCookieName, generateIdseq());
		// cookie.setMaxAge(24 * 60 * 60); // (24 hours in seconds)
		cookie.setMaxAge(-1); // negative value means that the cookie is not
								// stored persistently and will be deleted when
								// the Web browser exits
		cookie.setPath(COOKIE_PATH);
		return cookie;
	}

	/**
	 * 
	 * @return UUID for IDSEQ
	 */
	protected static String generateIdseq() {
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
	 * 
	 * @param idseq - session cookie for a document under validation; not null. Format shall be checked before this call.
	 * @return ALSData
	 */
	protected String retrieveFeedValidate(String idseq) {
		try {
			return retrieveData(idseq, URL_FEED_VALIDATE_STATUS_FORMAT, String.class);
		}
		catch (Exception e) {
			logger.error("retrieveFeedValidate error: " + e) ;
			return "0";//we do not want to send an exception on feed to UI
		}
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
			//logger.debug("...retrieveData from URL: " + urlStr);

			data = restTemplate.getForObject(urlStr, clazz);
		}
		return data;
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
	 * @param T data generic type not null
	 * @param String idseq not null
	 * @param String URL string not null
	 * @return StringResponseWrapper
	 */
	protected static <T>StringResponseWrapper submitPostRequestCreateGeneric(T data, String idseq, String createRequestUrlStr) {
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
	
	/**
	 * Upload one file. maxAge is in seconds, negative means undefined.
	 * 
	 * @param uploadfile
	 * @return ResponseEntity
	 */
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
	@PostMapping("/parseservice")
	// @ResponseBody
	public ResponseEntity<?> parseService(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name = "owner", defaultValue = "guest") String reportOwner,
			@RequestParam(name = "file", required=true) MultipartFile file) {
		logger.debug("uploadFile started");

		// upload file
		if (file.isEmpty()) {
			String errorMessage = "Please submit a non-empty file!";
			return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST);
		}
		String orgFileName = file.getOriginalFilename();
		logger.info("ALS file upload request: " + orgFileName);

		Path pathSavedFile;
		// create a Cookie
		Cookie cookie = generateCookie();
		String idseq = cookie.getValue();
		try {
			pathSavedFile = saveUploadedFile(file, idseq + EXCEL_FILE_EXT);
		} 
		catch (IOException e) {
			String errorMessage = "Unexpected error in saving uploaded file: " + file.getName() + ". Details:" + e;
			return buildErrorResponse(errorMessage, HttpStatus.SERVICE_UNAVAILABLE);
		}

		String saveAbsPath = pathSavedFile.toFile().getAbsolutePath();
		logger.info("Successfully uploaded - " + orgFileName + " saved as " + saveAbsPath);
		//catch REST client exception
		try {
			// call parser
			ALSDataWrapper wrapper = serviceParser.submitPostRequestParser(saveAbsPath,CCHECKER_PARSER_URL);
			HttpStatus parserStatusCode = wrapper.getStatusCode();
			if (!HttpStatus.OK.equals(parserStatusCode)) {
				String errorMessage = "Error on parsing file: " + file.getOriginalFilename();
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
			 StringResponseWrapper saveResponse = serviceDb.submitPostRequestSaveAls(alsData, idseq, CCHECKER_DB_SERVICE_URL_CREATE);
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
			FormsUiData formUiData = formService.collectFormsUiData(alsData);
			formUiData.setSessionid(idseq);
	
			// set session cookie
			logger.debug("set new Cookie value: " + idseq);
			response.addCookie(cookie);
	
			// If decided always return json type, put Content-Type to annotations
			// then
			HttpHeaders httpHeaders = createHttpOkHeaders();
			return new ResponseEntity<FormsUiData>(formUiData, httpHeaders, HttpStatus.OK);
		}
		catch (RestClientException re) {
			 String errorMessage = "Unexpected error on file: " + orgFileName + ", owner: " + reportOwner + ", session: " + idseq + ". Details: " + re.getMessage();
			 return buildErrorResponse(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	/**
	 * 
	 * @param request
	 * @param response
	 * @param checkUOM
	 * @param checkCRF
	 * @param displayExceptions
	 * @param requestEntity not null and not empty
	 * @return ResponseEntity
	 */
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
	@PostMapping("/validateservice")
	public ResponseEntity<?> validateService(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(name = "checkUOM", required = false, defaultValue = "false") boolean checkUOM,
			@RequestParam(name = "checkCRF", required = false, defaultValue = "false") boolean checkCRF,
			@RequestParam(name = "displayExceptions", required = false, defaultValue = "false") boolean displayExceptions,
			RequestEntity<List<String>> requestEntity) {
		//logger.debug("request received validateService");
		// check for session cookie
		Cookie cookie = retrieveCookie(request);
		String sessionCookieValue = null;

		if ((cookie == null) || (StringUtils.isBlank((sessionCookieValue = cookie.getValue()))) || (!ParameterValidator.validateIdSeq(sessionCookieValue))) {
			return buildErrorResponse(SESSION_NOT_VALID + sessionCookieValue, HttpStatus.BAD_REQUEST);
		}

		logger.debug("validateService session cookie: " + sessionCookieValue);
		
		List<String> formNames = requestEntity.getBody();
		logger.debug("Selected forms received: " + formNames);

		HttpStatus errorCode =  HttpStatus.BAD_REQUEST;

		//call Validator service
		try {
			StringResponseWrapper stringResponseWrapper = serviceValidator.sendPostRequestValidator(formNames, sessionCookieValue, checkUOM, checkCRF, displayExceptions);
			
			if (stringResponseWrapper == null) {
				//We never expect validate request failure. It shall always send a report.
				logger.error("sendPostRequestValidator error on " + sessionCookieValue);
				return buildErrorResponse(SESSION_DATA_NOT_FOUND + sessionCookieValue, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			HttpStatus statusCode = stringResponseWrapper.getStatusCode();
			if (HttpStatus.OK.equals(statusCode)) {
				URI url = requestEntity.getUrl();
				String path = String.format("%s://%s:%d%s",url.getScheme(),  url.getHost(), url.getPort(), url.getPath());
				String location = path.replace(VALIDATE_SERVICE_URL_STR, RETRIEVE_ERROR_REPORT_URL_STR) + '/'+ sessionCookieValue;
				//logger.debug("Report error Location header value: " + location);	
				HttpHeaders httpHeaders = createHttpValidateHeaders(TEXT_PLAIN_MIME_TYPE, location);
				return new ResponseEntity<String>(location, httpHeaders, HttpStatus.CREATED);
			}
			else {
				logger.error("sendPostRequestValidator error response: " + stringResponseWrapper);
				errorCode = stringResponseWrapper.getStatusCode();//This can be user error or server error
				String detailsStr = StringUtils.isNotBlank(stringResponseWrapper.getResponseData()) ? ". Details: " + stringResponseWrapper.getResponseData() : "";
				return buildErrorResponse("Unexpected error on validate for session: " + sessionCookieValue + detailsStr, errorCode);
			}
		}
		catch (RestClientException re) {
			 String errorMessage = "Unexpected error on validate for session: " + sessionCookieValue + ". Details: " + re.getMessage();
			 return buildErrorResponse(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
	@GetMapping("/retrievereporterror/{idseq}")
	public ResponseEntity<?> retrieveErrorReport(HttpServletRequest request,
			@PathVariable("idseq") String idseq) {
		logger.debug("retrieveErrorReport called: " + idseq);

		if (!ParameterValidator.validateIdSeq(idseq)) {
			return buildErrorResponse("Report ID is invalid: " + idseq + '\n', HttpStatus.BAD_REQUEST);
		}
		try {
			CCCReport data = retrieveReportError(idseq);
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("Content-Type", "application/json");
			return new ResponseEntity<CCCReport>(data, httpHeaders, HttpStatus.OK);
		}
		catch (HttpClientErrorException e){
			return buildErrorResponse("Report is not found by ID: " + idseq + '\n', e.getStatusCode());
		}
	
	}
	
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
	@GetMapping("/genexcelreporterror")
	public void genExcelReportError(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Cookie cookie = retrieveCookie(request);
		String sessionCookieValue = null;
		if ((cookie == null) || (StringUtils.isBlank((sessionCookieValue = cookie.getValue()))) || (!ParameterValidator.validateIdSeq(sessionCookieValue))) {
			response.setHeader("Content-Type", TEXT_PLAIN_MIME_TYPE);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			IOUtils.copy(new ByteArrayInputStream(("Session Cookie is not found or not valid: " + sessionCookieValue).getBytes()),
				response.getOutputStream());
		} 
		else {
			try {
				RestTemplate restTemplate = new RestTemplate();
				String urlStr = String.format(URL_GEN_EXCEL_REPORT_ERROR_FORMAT, sessionCookieValue);
				logger.debug("...retrieveData: " + urlStr);
				response.setHeader("Content-Type", MS_EXCEL_MIME_TYPE);
				response.setHeader("Content-Disposition", "attachment; filename=" + fileExcelReportPrefix + sessionCookieValue + EXCEL_FILE_EXT);
				response.setHeader("Access-Control-Expose-Headers","Content-Disposition");
				response.setStatus(HttpServletResponse.SC_OK);
	
				restTemplate.execute(urlStr, HttpMethod.GET, (ClientHttpRequest requestCallback) -> {
				}, responseExtractor -> {
					IOUtils.copy(responseExtractor.getBody(), response.getOutputStream());
					return null;
				});
			}
			catch (RestClientException re) {
				response.setHeader("Content-Type", TEXT_PLAIN_MIME_TYPE);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				//origin ?
				String errorMessage = "Unexpected error on generate Excel: session: " + sessionCookieValue + ". Details: " + re.getMessage();
				logger.error(errorMessage + ", exception: " + re.getClass().getName());
				IOUtils.copy(new ByteArrayInputStream(errorMessage.getBytes()), response.getOutputStream());
			}
		}
		response.flushBuffer();
	}
	
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
	@GetMapping("/retrieveexcelreporterror/{idseq}")
	public void retrieveExcelReportError(HttpServletRequest request, HttpServletResponse response, 
			@PathVariable("idseq") String idseq) throws Exception {
		if  (!ParameterValidator.validateIdSeq(idseq)) {
			response.setHeader("Content-Type", TEXT_PLAIN_MIME_TYPE);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			IOUtils.copy(new ByteArrayInputStream(("Report ID is not valid: " + idseq + '\n').getBytes()),
				response.getOutputStream());
		} 
		else {
			String filePath = buildExcelFilePath(idseq);
			logger.debug("...retrieveExcelReportError from: " + filePath);
			response.setHeader("Content-Type", MS_EXCEL_MIME_TYPE);
			response.setHeader("Content-Disposition", "attachment; filename=" + fileExcelReportPrefix + idseq + EXCEL_FILE_EXT);
			response.setHeader("Access-Control-Expose-Headers","Content-Disposition");
			response.setStatus(HttpServletResponse.SC_OK);
			InputStream istream = openFileAsInputStream(filePath);
			if (istream != null) {
				IOUtils.copy(openFileAsInputStream(filePath), response.getOutputStream());
			}
			else {
				response.setHeader("Content-Type", TEXT_PLAIN_MIME_TYPE);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				IOUtils.copy(new ByteArrayInputStream(("Report with ID is not found: " + idseq + '\n').getBytes()),
					response.getOutputStream());
			}
		}
		response.flushBuffer();
	}
	
	private Cookie retrieveCookie(HttpServletRequest request) {
		Cookie[] cookieArr = request.getCookies();
		Cookie sessionCookie = null;
		if (cookieArr != null) {
			for (Cookie currCookie : cookieArr) {
				if (sessionCookieName.equals(currCookie.getName())) {
					logger.debug("...found session cookie: " + currCookie.getValue());
					sessionCookie = currCookie;
					//if we have many cookie values take the last one
				}
			}
		}

		if (sessionCookie != null) {
			logger.info("found session cookie: " + sessionCookie.getValue());
		}
		else {
			logger.debug("session cookie is not found");
		}
		return sessionCookie;
	}

	/**
	 * Return path to previously generated Excel file.
	 * 
	 * @param String idseq not null
	 * @return String file full path to Excel report file.
	 */
	private String buildExcelFilePath(String idseq) {
		return UPLOADED_FOLDER + fileExcelReportPrefix + idseq + EXCEL_FILE_EXT;
	}

	/**
	 * 
	 * @param errorMessage String not null
	 * @param httpStatus HttpStatus not null
	 * @return ResponseEntity
	 */
	protected static ResponseEntity<String> buildErrorResponse(String errorMessage, HttpStatus httpStatus) {
		// context type on an error text/plain
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", TEXT_PLAIN_MIME_TYPE);
		// We have configured springframework CrossOrigin so we do not need this
		//assignAccessControlHeader(httpHeaders);
		logger.error(errorMessage);
		return new ResponseEntity<String>(errorMessage + " with response status: " + httpStatus, httpHeaders, httpStatus);
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
	/**
	 * 
	 * @param filename full path not null
	 * @return BufferedInputStream
	 * @throws Exception
	 */
	protected BufferedInputStream openFileAsInputStream(String filePathString) throws Exception {
		BufferedInputStream bis = null;
		Path path = Paths.get(filePathString);
		if (Files.exists(path)) {
			FileInputStream fis = new FileInputStream(filePathString);
			bis = new BufferedInputStream(fis);
		}
		else {
			logger.error("Requested file is not found: " + filePathString);
		}
		return bis;
	}
	
	protected static void assignAccessControlHeader(HttpHeaders httpHeaders) {
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
	protected HttpHeaders createHttpValidateHeaders(String contextTypeSting, String locationHeaderString) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", contextTypeSting);
		httpHeaders.add("Location", locationHeaderString);
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
		CCHECKER_FEED_VALIDATE_SERVICE_URL = GatewayBootWebApplication.CCHECKER_FEED_VALIDATE_SERVICE_URL;
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
		logger.debug("GatewayBootController CCHECKER_FEED_VALIDATE_SERVICE_URL: " + CCHECKER_FEED_VALIDATE_SERVICE_URL);
		logger.debug("GatewayBootController ACCESS_CONTROL_ALLOW_ORIGIN: " + ACCESS_CONTROL_ALLOW_ORIGIN);
		URL_RETRIEVE_ALS_FORMAT = CCHECKER_DB_SERVICE_URL_RETRIEVE + "?" + sessionCookieName + "=%s";
		URL_GEN_EXCEL_REPORT_ERROR_FORMAT = CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL + "?" + sessionCookieName + "=%s";
		URL_RETRIEVE_REPORT_ERROR_FORMAT = CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_ERROR + "?" + sessionCookieName + "=%s";
		URL_RETRIEVE_REPORT_FULL_FORMAT = CCHECKER_DB_SERVICE_URL_RETRIEVE_REPORT_FULL + "?" + sessionCookieName + "=%s";
		URL_FEED_VALIDATE_STATUS_FORMAT = CCHECKER_FEED_VALIDATE_SERVICE_URL + "/%s";
		logger.debug("GatewayBootController URL_RETRIEVE_ALS_FORMAT: " + URL_RETRIEVE_ALS_FORMAT);
		logger.debug("GatewayBootController URL_RETRIEVE_REPORT_ERROR_FORMAT: " + URL_RETRIEVE_REPORT_ERROR_FORMAT);
		logger.debug("GatewayBootController URL_RETRIEVE_REPORT_FULL_FORMAT: " + URL_RETRIEVE_REPORT_FULL_FORMAT);
		logger.debug("GatewayBootController CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL: " + CCHECKER_GEN_EXCEL_REPORT_ERROR_SERVICE_URL);
		logger.debug("GatewayBootController URL_GEN_EXCEL_REPORT_ERROR: " + URL_GEN_EXCEL_REPORT_ERROR_FORMAT);
		logger.debug("GatewayBootController URL_FEED_VALIDATE_STATUS_FORMAT: " + URL_FEED_VALIDATE_STATUS_FORMAT);
	}

	//TODO Remove this test service
	/**
	 * 
	 * @param request
	 * @param response
	 * @param filename
	 * @return ALSData
	 */
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
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
	//TODO remove test method testFeed
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
	@GetMapping("/testfeed/{id}")
	public ResponseBodyEmitter testFeed(@PathVariable("id") String amount) {
		int num = Integer.parseInt(amount);
		final SseEmitter emitter = new SseEmitter();
		ExecutorService service = Executors.newSingleThreadExecutor();
		service.execute(() -> {
			for (int i = 0; i < num; i++) {
				try {
					emitter.send(""+i, MediaType.TEXT_PLAIN);
	
					Thread.sleep(200);
				} 
				catch (Exception e) {
					e.printStackTrace();
					emitter.completeWithError(e);
					return;
				}
			}
			emitter.complete();
		});

		return emitter;
	}
	/**
	 * Returns form under validation number.
	 * 
	 * @param idseq not null
	 * @return SseEmitter
	 */
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
	@GetMapping("/testfeedvalidatestatus/{idseq}")
	public ResponseBodyEmitter feedStatus(@PathVariable("idseq") String idseq) {
		logger.debug("feedStatus called with idseq: " + idseq);

		final SseEmitter emitter = new SseEmitter();
		if  (!ParameterValidator.validateIdSeq(idseq)) {
			logger.error("feedstatus wrong parameter format: " + idseq);
			return null;
		}

		ExecutorService service = Executors.newSingleThreadExecutor();
		service.execute(() -> {
			String res = "-1";//we expect to receive a form number
			//TODO make loop run until we got "0"
			for (int i = 0; i < maxFeedRequests; i++) {//let's restrict not to risk endless cycle
				try {
					res = retrieveFeedValidate(idseq);
					if (!("0".equals(res))) {
							emitter.send(res, MediaType.TEXT_PLAIN);
							Thread.sleep(timeBetweenFeeds);
					}
					else {
						logger.info("feedvalidatestatus is over: " + idseq);
						break;
					}
				} 
				catch (Exception e) {
					logger.error("Error in feedvalidatestatus " + e);
					e.printStackTrace();
					emitter.completeWithError(e);
					return;
				}
			}
			emitter.complete();
		});

		return emitter;
	}
	/**
	 * Returns form under validation number.
	 * 
	 * @param idseq not null
	 * @return SseEmitter
	 */
	@CrossOrigin(allowedHeaders = "*",allowCredentials="true",maxAge=9000)
	@GetMapping("/feedvalidatestatus")
	public ResponseBodyEmitter feedStatusByCookie(HttpServletRequest request, HttpServletResponse response) {

		Cookie cookie = retrieveCookie(request);
		final String idseq;
		
		if ((cookie == null) || (StringUtils.isBlank((idseq = cookie.getValue()))) || (!ParameterValidator.validateIdSeq(idseq))) {
			logger.debug("feedvalidatestatus session cookie is not found");
			return null;
		}

		logger.debug("feedvalidatestatus session cookie: " + idseq);
		final SseEmitter emitter = new SseEmitter();

		ExecutorService service = Executors.newSingleThreadExecutor();
		service.execute(() -> {
			String resPre = "-1";//we expect to receive a form number
			String res;
			try {
				Thread.sleep(timeBetweenFeeds*2);//delay to give validator some time to start
			} catch (Exception e1) {
				// Do nothing
			}
			//TODO make loop run until we got "0"
			for (int i = 0; i < maxFeedRequests; i++) {//let's restrict not to risk endless cycle
				try {
					res = retrieveFeedValidate(idseq);
					if (logger.isDebugEnabled()) {
						if (! StringUtils.equals(res, resPre))  {//reduce amount of logs
							logger.debug("feedvalidatestatus current form for " + idseq + ", " + res);
							resPre = res;
						}
					}
					if (!("0".equals(res))) {
							emitter.send(res, MediaType.TEXT_PLAIN);
							Thread.sleep(timeBetweenFeeds);
					}
					else if (("0".equals(res)) && (i < 1)) {//it looks like this request goes to validator sooner than the form validate
						Thread.sleep(timeBetweenFeeds);
					}
					else {
						logger.info("feedvalidatestatus is over: " + idseq);
						break;
					}
				} 
				catch (Exception e) {
					logger.error("Error in feedvalidatestatus " + idseq + ", " + e);
					//e.printStackTrace();
					emitter.completeWithError(e);
					return;
				}
			}
			emitter.complete();
		});

		return emitter;
	}
}