/**
 * Copyright (C) 2016 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.testspringboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@EnableAutoConfiguration
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RestUploadController {
	private final Logger logger = LoggerFactory.getLogger(RestUploadController.class);

	//Save the uploaded file to this folder
	private static String UPLOADED_FOLDER = Example.UPLOADED_FOLDER;
	
	//FIXME not tested
	/**
	 * Upload one file.
	 * 
	 * @param uploadfile
	 * @return ResponseEntity
	 */
	@PostMapping("/rest/upload")
	//@ResponseBody
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadfile) {
		logger.debug("Single file upload!");
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "text/plain");
		
		if (uploadfile.isEmpty()) {
			return new ResponseEntity("please select a file!", httpHeaders,  HttpStatus.OK);
		}

		try {
			saveUploadedFiles(Arrays.asList(uploadfile));
		} 
		catch (IOException e) {
			return new ResponseEntity<>(e.toString(), httpHeaders, HttpStatus.BAD_REQUEST);
		}
		

		return new ResponseEntity("Successfully uploaded - " + uploadfile.getOriginalFilename() + "\n",
				httpHeaders, HttpStatus.OK);
	}

	//FIXME not tested
	/**
	 * Upload Multiple files.
	 * 
	 * @param extraField
	 * @param uploadfiles
	 * @return ResponseEntity
	 */
	@PostMapping("/rest/upload/multi")
	public ResponseEntity<?> uploadFileMulti(@RequestParam("extraField") String extraField,
			@RequestParam("files") MultipartFile[] uploadfiles) {

		logger.debug("Multiple file upload!");

		// Get file name
		String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename())
				.filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));

		if (StringUtils.isEmpty(uploadedFileName)) {
			return new ResponseEntity("please select a file!\n", HttpStatus.OK);
		}

		try {
			saveUploadedFiles(Arrays.asList(uploadfiles));

		} 
		catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity("Successfully uploaded - " + uploadedFileName + "\n", HttpStatus.OK);
	}

	//FIXME not tested
	/**
	 * Maps html form to a Model
	 * 
	 * @param model
	 * @return ResponseEntity
	 */
	@PostMapping("/rest/upload/multi/model")
	public ResponseEntity<?> multiUploadFileModel(@ModelAttribute ModelUploadFiles model) {

		logger.debug("Upload using ModelUploadFiles");

		try {
			saveUploadedFiles(Arrays.asList(model.getFiles()));
		} 
		catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity("Successfully uploaded!\n", HttpStatus.OK);
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
}
