/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
import gov.nih.nci.cadsr.data.FormsUiData;
import gov.nih.nci.cadsr.service.FormService;

	@Controller
	public class GatewayBootController {
		static String CCHECKER_PARSER_URL;
		static String UPLOADED_FOLDER;
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
				httpHeaders.add("Content-Type", "text/plain");
				return new ResponseEntity<String>("Please submit a file!", httpHeaders,  HttpStatus.BAD_REQUEST);
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
				httpHeaders.add("Content-Type", "text/plain");
				logger.error(errorMessage) ;
				return new ResponseEntity<String>(errorMessage, httpHeaders, HttpStatus.SERVICE_UNAVAILABLE);
			}
			
			String saveAbsPath = pathSavedFile.toFile().getAbsolutePath();
			logger.info("Successfully uploaded - " + orgFileName + " saved as " + saveAbsPath);

			//call parser
			ALSDataWrapper wrapper = submitPostRequest(saveAbsPath);
			HttpStatus parserStatusCode = wrapper.getStatusCode();
			if (! HttpStatus.OK.equals(parserStatusCode)) {
				httpHeaders.add("Content-Type", "text/plain");
				return new ResponseEntity<>("Error on parsing file: " + uploadfile.getOriginalFilename(), httpHeaders,  parserStatusCode);
			}
			
			ALSData alsData = wrapper.getAlsData();
			alsData.setFileName(orgFileName);
			alsData.setReportOwner(reportOwner);
			
			FormsUiData formUiData = FormService.buildFormsUiData(alsData);
			response.addCookie(cookie);
			
			//TODO save ALSData in DB
			
			//TODO We could always return FormsUiData type, put this to annotations then
			httpHeaders.add("Content-Type", "application/json");
			return new ResponseEntity<>(formUiData, httpHeaders, HttpStatus.OK);
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