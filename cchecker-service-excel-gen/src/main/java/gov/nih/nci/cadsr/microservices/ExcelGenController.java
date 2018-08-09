/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import gov.nih.nci.cadsr.data.CCCReport;

@RestController
@EnableAutoConfiguration
public class ExcelGenController {
	private final static Logger logger = LoggerFactory.getLogger(ExcelGenController.class);
	protected static String CCHECKER_DB_SERVICE_URL_REPORT_ERROR_RETRIEVE = CCheckerExcelGenService.CCHECKER_DB_SERVICE_URL_REPORT_ERROR_RETRIEVE;
	protected static String REPORT_FOLDER = CCheckerExcelGenService.REPORT_FOLDER;
	public static final String filePrefix = "Report-";
	public static final String fileExtension = ".xlsx";
	public final String strError = "No report information received";

	@GetMapping(value = "/rest/generatereporterror")
	public ResponseEntity<?>generateReportErrorExcel(@RequestParam(name="_cchecker", required=true) String idseq) {
		logger.debug("generateReportExcel: " + idseq);
		if (StringUtils.isBlank(idseq)) {
			//no idseq received
			return buildErrorResponse(strError, HttpStatus.SERVICE_UNAVAILABLE);
		}

		logger.info("generateReportExcel idseq: " + idseq);
		try {
			CCCReport cccReport = retrieveReportErrorData(idseq);
			String fileName = cccReport.getFileName();//expected Excel file name
			String fileNameReport = REPORT_FOLDER + filePrefix + idseq + fileExtension;
			ExcelReportGenerator.writeExcel(fileNameReport, cccReport);
			InputStream inputStream = getExcelFileAsInputStream(fileNameReport);
			InputStreamResource isr = new InputStreamResource(inputStream);
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.add("Content-Type", "application/vnd.ms-excel");
			httpHeaders.add("Content-Disposition", "attachment; filename=" + filePrefix + fileName);

			return new ResponseEntity<InputStreamResource>(isr, httpHeaders, HttpStatus.OK);
		}
		catch (RestClientException e) {
			logger.error("RestClientException on idseq: " + idseq, e);
			return buildErrorResponse("Report is not found by " + idseq + e, HttpStatus.BAD_REQUEST);
		}
		catch (Exception e) {
			e.printStackTrace();
			return buildErrorResponse("error in excel generator on id: " + idseq + e, HttpStatus.INTERNAL_SERVER_ERROR);
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
	protected ResponseEntity<InputStreamResource> buildErrorResponseStream(String idseq, String strMessageFormat,
		HttpStatus httpStatus) {
		String errorMessage = String.format(strMessageFormat, idseq);
		HttpHeaders responseHeaders = new HttpHeaders();
		logger.error("Response error message: " + errorMessage);
		responseHeaders.set("Content-Type", "text/plain");
		InputStreamResource isr = new InputStreamResource(new ByteArrayInputStream(errorMessage.getBytes()));
		return new ResponseEntity<InputStreamResource>(isr, responseHeaders, httpStatus);
	}
	/**
	 * 
	 * @param idseq saved previously in DB not null
	 * @return CCCReport
	 */
	protected CCCReport retrieveReportErrorData(String idseq) {
		return retrieveData(idseq, CCHECKER_DB_SERVICE_URL_REPORT_ERROR_RETRIEVE, CCCReport.class);
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
			logger.debug("...retrieveData: " + urlStr);

			data = restTemplate.getForObject(urlStr, clazz);
		}
		return data;
	}
	protected InputStream getExcelFileAsInputStream(String excelFilename) throws Exception {
		BufferedInputStream bis = null;
		FileInputStream fis = new FileInputStream(excelFilename);
		bis = new BufferedInputStream(fis);

		return bis;
	}
}
