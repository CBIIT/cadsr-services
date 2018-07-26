/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.FormsUiData;
import gov.nih.nci.cadsr.report.CongruencyCheckerReportInvoker;
import gov.nih.nci.cadsr.service.FormService;

	@Controller
	public class GatewayBootController {
		static String CCHECKER_PARSER_URL;
		static String CCHECKER_DB_SERVICE_URL;
		static String UPLOADED_FOLDER;
		static String ACCESS_CONTROL_ALLOW_ORIGIN;
		static final String sessionCookieName = "_cchecker";
		//FIXME shall be defined in ALSError
		static final String FATAL_ERROR_STATUS = "FATAL";
		static final String EXCEL_FILE_EXT = ".xlsx";
		
		{
			loadProperties();
		}
		
		private final static Logger logger = LoggerFactory.getLogger(GatewayBootController.class);
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
	    public ALSData parseFileService(HttpServletRequest request, HttpServletResponse response, @RequestParam(name="filepath", required=true) String filepath) {
	    	ALSDataWrapper alsData;
	    	Cookie cookie = generateCookie();
	    	alsData = submitPostRequestParser(filepath);
	    	response.addCookie(cookie);
	        return alsData.getAlsData();
	    }

		protected Cookie generateCookie() {
	    	Cookie cookie = new Cookie(sessionCookieName, generateIdseq());
	    	//cookie.setMaxAge(24 * 60 * 60);  // (24 hours in seconds)
	    	cookie.setMaxAge(-1);  // negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits
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
	     * @param filePath - file saved previously on the service file system.
	     * @return ALSDataWrapper
	     */
	    protected ALSDataWrapper submitPostRequestParser(String filePath) {
	        RestTemplate restTemplate = new RestTemplate();
	        
	        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(CCHECKER_PARSER_URL);

	        //add some String
	        builder.queryParam("filepath", filePath);

	        //another staff
	        ALSDataWrapper wrapper = new ALSDataWrapper();
	        
	        ResponseEntity<ALSData> responseEntity = restTemplate.postForEntity(
	                builder.build().encode().toUri(),
	                HttpMethod.POST,
	                ALSData.class);

	        HttpStatus statusCode = responseEntity.getStatusCode();
	        wrapper.setStatusCode(statusCode);
	        if (statusCode == HttpStatus.OK) {
	        	wrapper.setAlsData(responseEntity.getBody());
	            logger.debug("parseservice result received" );
	            
	        }
	        else {
	        	logger.error("parsefileservice sent an error: " + statusCode);
	        }
	        return wrapper;
	    }
	    protected StringResponseWrapper submitPostRequestSaveAls(ALSData alsData, String idseq) {
	        RestTemplate restTemplate = new RestTemplate();
	        
	        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(CCHECKER_DB_SERVICE_URL);

	        //add some String
	        builder.queryParam(sessionCookieName, idseq);

	        //another staff
	        StringResponseWrapper wrapper = new StringResponseWrapper();
	        HttpEntity<ALSData> requestData = new HttpEntity<>(alsData);
	        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
	                builder.build().encode().toUri(),
	                requestData,
	                String.class);

	        HttpStatus statusCode = responseEntity.getStatusCode();
	        wrapper.setStatusCode(statusCode);
	        if (statusCode == HttpStatus.OK) {
	        	wrapper.setResponseData(responseEntity.getBody());
	            logger.debug("parseservice result received" );
	            
	        }
	        else {
	        	logger.error("parsefileservice sent an error: " + statusCode);
	        }
	        return wrapper;
	    }
		/**
		 * Upload one file.
		 * 
		 * @param uploadfile
		 * @return ResponseEntity
		 */
		@CrossOrigin
		@PostMapping("/parseservice")
		//@ResponseBody
		public ResponseEntity<?> parseService(HttpServletRequest request, HttpServletResponse response,
				@RequestParam(name="owner", defaultValue="guest") String reportOwner, @RequestParam("file") MultipartFile uploadfile) {
			logger.debug("uploadFile started");
			
			//upload file
			if (uploadfile.isEmpty()) {
				String errorMessage = "Please submit a file!";
				return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST);
			}
			String orgFileName = uploadfile.getOriginalFilename();
			logger.info("ALS file upload request: " + orgFileName);
			
			Path pathSavedFile;
			//create a Cookie
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

			//call parser
			ALSDataWrapper wrapper = submitPostRequestParser(saveAbsPath);
			HttpStatus parserStatusCode = wrapper.getStatusCode();
			if (! HttpStatus.OK.equals(parserStatusCode)) {
				String errorMessage = "Error on parsing file: " + uploadfile.getOriginalFilename();
				return buildErrorResponse(errorMessage, parserStatusCode);
			}
			
			ALSData alsData = wrapper.getAlsData();
			//check for fatal errors
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
			
			//set file name and owner			
			alsData.setFileName(orgFileName);
			alsData.setReportOwner(reportOwner);
	    	logger.debug("........before sending save alsData request. REPORT_OWNER: " + alsData.getReportOwner() + ", FILE_NAME: " + alsData.getFileName() + ", idseq: " + idseq);
	    	
			//implement save ALSData in DB
	    	//FIXME I commented save request code until we decide how to configure DB into in a container
//			StringResponseWrapper saveResponse = submitPostRequestSaveAls(alsData, idseq);
//			HttpStatus responseStatusCode = saveResponse.getStatusCode();
//			if (! HttpStatus.OK.equals(responseStatusCode)) {
//				String errorMessage = "Error on saving ALS file: " + orgFileName + ", idseq: " + idseq;
//				return buildErrorResponse(errorMessage, parserStatusCode);
//			}
//			else {
//				logger.info("Saved ALS file: " + orgFileName + ", idseq: " + idseq);
//			}
//			
			//build result from parser data
			FormsUiData formUiData = FormService.buildFormsUiData(alsData);
			
			//set session cookie
			Cookie sessionCookie = retrieveCookie(request);
			if (sessionCookie != null) {
				sessionCookie.setValue(idseq);
			}
			else {
				sessionCookie = new Cookie(sessionCookieName, idseq);
			}
			response.addCookie(cookie);
			
			//If decided always return json type, put Content-Type to annotations then
			HttpHeaders httpHeaders = createHttpOkHeaders();
			return new ResponseEntity<FormsUiData>(formUiData, httpHeaders, HttpStatus.OK);
		}
		@CrossOrigin
		@PostMapping("/checkservice")
		public ResponseEntity<?> checkService(HttpServletRequest request, HttpServletResponse response,
				@RequestParam(name="checkUOM", required = false, defaultValue="false") boolean checkUOM,
				@RequestParam(name="checkCRF", required = false, defaultValue="false") boolean checkCRF,
				@RequestParam(name="displayExceptions", required = false, defaultValue="false") boolean displayExceptions,
				RequestEntity<List<String>> requestEntity) {
			logger.debug("request received parseService");
			//check for session cookie
			Cookie cookie = retrieveCookie(request);
			if (cookie == null) {
				return buildErrorResponse("Session is not found", HttpStatus.BAD_REQUEST);
			}
			List<String> formNames = requestEntity.getBody();
			logger.debug("Selected forms received: " + formNames);
			
			//FIXME call Validator service instead of the example code below
			//TODO this is test code only getting file data
			String filepath = buildFilePath(cookie.getValue());
	    	ALSDataWrapper alsDataWrapper = submitPostRequestParser(filepath);
	    	response.addCookie(cookie);
	    	CCCReport cccReport = CongruencyCheckerReportInvoker.builTestReport(alsDataWrapper.getAlsData());
	    	cccReport.setReportOwner(alsDataWrapper.getAlsData().getReportOwner());
	    	HttpHeaders httpHeaders = createHttpOkHeaders();
			return new ResponseEntity<CCCReport>(cccReport, httpHeaders, HttpStatus.OK);
			
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
		 * This method is for feasibility only.
		 * We will retrieve ALSData from DB.
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
			//TODO what context type shall be returned on an error - ? Now text/plain 
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("Content-Type", "text/plain");
			//We have configured springframework CrossOrigin so we do not need this header
			//assignAccessControlHeader(httpHeaders);
			logger.error(errorMessage);
			return new ResponseEntity<String>(errorMessage, httpHeaders, HttpStatus.BAD_REQUEST);
		}
		/**
		 * Creates a new file from its parameter.
		 * 
		 * @param file not null
		 * @param fileIdseqName not null 
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
			//We have configured springframework CrossOrigin so we do not need this header
			//httpHeaders.setAccessControlAllowOrigin(ACCESS_CONTROL_ALLOW_ORIGIN);
			return httpHeaders;
		}
		/**
		 * The properties are taken from the boot service.
		 */
	    protected static void loadProperties() {
			CCHECKER_PARSER_URL = GatewayBootWebApplication.CCHECKER_PARSER_URL;
			UPLOADED_FOLDER = GatewayBootWebApplication.UPLOADED_FOLDER;
			CCHECKER_DB_SERVICE_URL = GatewayBootWebApplication.CCHECKER_DB_SERVICE_URL;
			ACCESS_CONTROL_ALLOW_ORIGIN = GatewayBootWebApplication.ACCESS_CONTROL_ALLOW_ORIGIN;
			logger.debug("GatewayBootController CCHECKER_PARSER_URL: " + CCHECKER_PARSER_URL);
			logger.debug("GatewayBootController UPLOADED_FOLDER: " + UPLOADED_FOLDER);
			logger.debug("GatewayBootController CCHECKER_DB_SERVICE_URL: " + CCHECKER_DB_SERVICE_URL);
			logger.debug("GatewayBootController ACCESS_CONTROL_ALLOW_ORIGIN: " + ACCESS_CONTROL_ALLOW_ORIGIN);
	    }
	    //TODO remove testReportService service
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
	    		@RequestParam(name="filepath", required=true) String filepath, @RequestParam(name="owner", defaultValue="guest") String reportOwner) {
	    	ALSDataWrapper alsDataWrapper;
	    	Cookie cookie = generateCookie();
	    	alsDataWrapper = submitPostRequestParser(filepath);
	    	response.addCookie(cookie);
	    	CCCReport cccReport = CongruencyCheckerReportInvoker.builTestReport(alsDataWrapper.getAlsData());
	    	cccReport.setReportOwner(reportOwner);
	        return cccReport;
	    }
	}