/**
 * Copyright (C) 2016 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;
/**
 * This class is for testing purposes only.
 * 
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@EnableAutoConfiguration
@SuppressWarnings({ "rawtypes", "unchecked" })
public class GatewayUploadFileController {
	private static final Logger logger = LoggerFactory.getLogger(GatewayUploadFileController.class);
	//Save the uploaded file to this folder
	private static String UPLOADED_FOLDER;
	{
		loadProperties();
	}

	
	/**
	 * Upload one file.
	 * 
	 * @param uploadfile
	 * @return ResponseEntity
	 */
	@PostMapping("/uploadfileservice")
	//@ResponseBody
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadfile) {
		logger.debug("Single file upload!");
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "text/plain");
		
		if (uploadfile.isEmpty()) {
			return new ResponseEntity("please select a file!", httpHeaders,  HttpStatus.BAD_REQUEST);
		}

		try {
			saveUploadedFiles(Arrays.asList(uploadfile));
		} 
		catch (IOException e) {
			return new ResponseEntity<>(e.toString(), httpHeaders, HttpStatus.SERVICE_UNAVAILABLE);
		}

		return new ResponseEntity("Successfully uploaded - " + uploadfile.getOriginalFilename() + "\n",
				httpHeaders, HttpStatus.OK);
	}


	//save file
	//TODO basic - needs improvements
	private void saveUploadedFiles(List<MultipartFile> files) throws IOException {

		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue; // next pls
			}

			byte[] bytes = file.getBytes();
			Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
			Files.write(path, bytes);
		}
	}
    protected static void loadProperties() {
    	UPLOADED_FOLDER = GatewayBootWebApplication.UPLOADED_FOLDER;
		logger.debug("GatewayUploadFileController UPLOADED_FOLDER: " + UPLOADED_FOLDER);
    }
}
