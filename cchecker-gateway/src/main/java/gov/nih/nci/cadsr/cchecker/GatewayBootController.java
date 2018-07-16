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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
import gov.nih.nci.cadsr.data.FormsUiData;
import gov.nih.nci.cadsr.service.FormService;

	@Controller
	public class GatewayBootController {
		static String CCHECKER_PARSER_URL;
		static String UPLOADED_FOLDER;
		//FIXME shall be defined in ALSError
		static final String FATAL_ERROR_STATUS = "FATAL";
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
	    @GetMapping("/parsefileservice")
	    @ResponseBody
	    public ALSData parseFileService(HttpServletRequest request, HttpServletResponse response, @RequestParam(name="filepath", required=true) String filepath) {
	    	ALSDataWrapper alsData;
	    	Cookie cookie = generateCookie();
	    	alsData = submitPostRequest(filepath);
	    	response.addCookie(cookie);
	        return alsData.getAlsData();
	    }

		protected Cookie generateCookie() {
	    	Cookie cookie = new Cookie("_cchecker", generateIdseq());
	    	cookie.setMaxAge(24 * 60 * 60);  // (24 hours in seconds)
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
	    
	    protected ALSDataWrapper submitPostRequest(String filePath) {
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
		/**
		 * Upload one file.
		 * 
		 * @param uploadfile
		 * @return ResponseEntity
		 */
		@PostMapping("/parseservice")
		//@ResponseBody
		public ResponseEntity<?> parseService(HttpServletRequest request, HttpServletResponse response,
				@RequestParam(name="owner", defaultValue="guest") String reportOwner, @RequestParam("file") MultipartFile uploadfile) {
			logger.debug("uploadFile started");
			//TODO what context type shall be returned on an error - ? 
			//FIXME we need user name here
			HttpHeaders httpHeaders = new HttpHeaders();
			
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
				pathSavedFile = saveUploadedFile(uploadfile, idseq + ".xlsx");
			} 
			catch (IOException e) {
				String errorMessage = "Error saving uploaded file: " + uploadfile.getName() + ' ' + e;
				return buildErrorResponse(errorMessage, HttpStatus.SERVICE_UNAVAILABLE);
			}
			
			String saveAbsPath = pathSavedFile.toFile().getAbsolutePath();
			logger.info("Successfully uploaded - " + orgFileName + " saved as " + saveAbsPath);

			//call parser
			ALSDataWrapper wrapper = submitPostRequest(saveAbsPath);
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
			//set session cookie
			FormsUiData formUiData = FormService.buildFormsUiData(alsData);
			response.addCookie(cookie);
			
			//TODO implement save ALSData in DB
			
			//TODO If decided always return json type, put this to annotations then
			httpHeaders.add("Content-Type", "application/json");
			return new ResponseEntity<FormsUiData>(formUiData, httpHeaders, HttpStatus.OK);
		}
		private ResponseEntity<String> buildErrorResponse(String errorMessage, HttpStatus httpStatus) {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("Content-Type", "text/plain");
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
		/**
		 * The properties are taken from the boot service.
		 */
	    protected static void loadProperties() {
			CCHECKER_PARSER_URL = GatewayBootWebApplication.CCHECKER_PARSER_URL;
			UPLOADED_FOLDER = GatewayBootWebApplication.UPLOADED_FOLDER;
			logger.debug("GatewayBootController CCHECKER_PARSER_URL: " + CCHECKER_PARSER_URL);
			logger.debug("GatewayBootController UPLOADED_FOLDER: " + UPLOADED_FOLDER);
	    }
	}