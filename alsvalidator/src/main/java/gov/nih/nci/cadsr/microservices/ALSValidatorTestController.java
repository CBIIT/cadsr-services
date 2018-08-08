/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.data.ReportInputWrapper;
import gov.nih.nci.cadsr.data.ValidateDataWrapper;
import gov.nih.nci.cadsr.report.ReportOutput;
import gov.nih.nci.cadsr.report.impl.GenerateReport;
import gov.nih.nci.cadsr.service.validator.ValidatorService;

@RestController
@EnableAutoConfiguration
// (exclude={DataSourceAutoConfiguration.class,
// HibernateJpaAutoConfiguration.class})
public class ALSValidatorTestController {
	private static final Logger logger = LoggerFactory.getLogger(ALSValidatorTestController.class);

	@PostMapping("/rest/validatequestionservice")
	public ResponseEntity<?> validate(HttpServletRequest request, HttpServletResponse response,
			@RequestBody ValidateDataWrapper validateWrapper) {
		CCCQuestion resultQuestion = ValidatorService.validate(validateWrapper.getField(),
				validateWrapper.getQuestion(), validateWrapper.getCdeDetails());
		HttpHeaders httpHeaders = createHttpOkHeaders();
		return new ResponseEntity<CCCQuestion>(resultQuestion, httpHeaders, HttpStatus.OK);
	}
	
	@PostMapping("/rest/builderrorreportservice")
	public ResponseEntity<?> buildErrorReport(HttpServletRequest request, HttpServletResponse response,
			@RequestBody ReportInputWrapper reportInput) throws NullPointerException {
		ReportOutput report = new GenerateReport();
		CCCReport errorsReport = report.getFinalReportData(reportInput.getAlsData(), reportInput.getSelForms(), reportInput.getCheckUom(), reportInput.getCheckStdCrfCde(), reportInput.getDisplayExceptionDetails());
		HttpHeaders httpHeaders = createHttpOkHeaders();
		return new ResponseEntity<CCCReport>(errorsReport, httpHeaders, HttpStatus.OK);
	}	

	protected HttpHeaders createHttpOkHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Content-Type", "application/json");
		return httpHeaders;
	}
}
