/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import gov.nih.nci.cadsr.data.ALSData;

	@Controller
	public class GatewayBootController {
		static String CCHECKER_PARSER_URL;
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
	    public ALSData parseFileService(HttpServletRequest request, HttpServletResponse response, @RequestParam(name="filename", required=true) String filename) {
	    	ALSData alsData;
	    	Cookie cookie = generateCookie();
	    	alsData = submitPostRequest(filename);
	    	response.addCookie(cookie);
	        return alsData;
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
	    private ALSData submitPostRequest(String filename) {
	        RestTemplate restTemplate = new RestTemplate();

	        //add file
//	        LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
//	        params.add("filename", new FileSystemResource(file));
	        
	        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(CCHECKER_PARSER_URL);

	        //add some String
	        builder.queryParam("filename", filename);

	        //another staff
	        ALSData result = new ALSData();
	        result.setFileName(filename);
	        
	        ResponseEntity<ALSData> responseEntity = restTemplate.postForEntity(
	                builder.build().encode().toUri(),
	                HttpMethod.POST,
	                ALSData.class);

	        HttpStatus statusCode = responseEntity.getStatusCode();
	        if (statusCode == HttpStatus.OK) {
	            result = responseEntity.getBody();
	            logger.debug("parsefileservice pase result received" );
	        }
	        
	        return result;
	    }

	    protected static void loadProperties() {
			CCHECKER_PARSER_URL = GatewayBootWebApplication.CCHECKER_PARSER_URL;
			logger.debug("GatewayBootController CCHECKER_PARSER_URL: " + CCHECKER_PARSER_URL);
	    }
	}