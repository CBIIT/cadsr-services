/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.service.validator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import gov.nih.nci.cadsr.dao.model.PermissibleValuesModel;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.data.DBValidationError;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.ReferenceDocument;

public class ValidatorService {

	private static final Logger logger = Logger.getLogger(ValidatorService.class);	
	private static final String errorString = "ERROR";
	private static final String matchString = "MATCH";
	private static final String retiredString = "RETIRED";
	private static final String msg1 = "CDE not in caDSR database";
	private static final String msg2 = "CDE has been retired";
	private static final String msg3 = "Newer Versions exist";
	private static List<String> characterDataFormats = Arrays.asList("CHAR", "VARCHAR2");
	private static List<String> numericDataFormats = Arrays.asList("number", "numeric", "integer");
	private static String congStatus_errors= "ERRORS";
	private static String congStatus_warn = "WARNINGS";
	private static String congStatus_congruent = "CONGRUENT";	
	
	
	public static CCCQuestion validate(ALSField field, CCCQuestion question, CdeDetails cdeDetails) {
		StringBuffer message = new StringBuffer();
		try {
		if (cdeDetails == null) {
			message.append(msg1);
			question.setQuestionCongruencyStatus(congStatus_errors);
		} else {
			if (cdeDetails.getDataElement()!=null && cdeDetails.getDataElement().getDataElementDetails().getWorkflowStatus().equalsIgnoreCase(retiredString)) {
				message.append(msg2);	
				question.setQuestionCongruencyStatus(congStatus_warn);
			}
			if (cdeDetails.getDataElement()!=null && (cdeDetails.getDataElement().getDataElementDetails().getVersion() >  Float.valueOf(question.getCdeVersion()))) {
				message.append(msg3);
				question.setQuestionCongruencyStatus(congStatus_warn);
			}
			List<String> rdDocTextList = new ArrayList<String>();
			StringBuffer rdDocs = new StringBuffer();
			String rdDocText;
			for (ReferenceDocument rd : cdeDetails.getDataElement().getReferenceDocuments()) {
				rdDocText =  rd.getDocumentText();
				rdDocTextList.add(rdDocText);
				if (rd.getDocumentType().equalsIgnoreCase("Preferred Question Text") || rd.getDocumentType().equalsIgnoreCase("Alternate Question Text")) {
					if (!rdDocs.equals(""))
						rdDocs.append("|"+rdDocText);
					else 
						rdDocs.append(rdDocText);
				}

			}
			if (rdDocTextList.contains(question.getRaveFieldLabel())) {
				question.setRaveFieldLabelResult(matchString);
			} else {
				question.setRaveFieldLabelResult(errorString);
				question.setQuestionCongruencyStatus(congStatus_errors);
			}		
			question.setCdePermitQuestionTextChoices(rdDocs.toString());
			//List<String> vdTypeList = new ArrayList<String>();
			if (question.getRaveControlType().equals(cdeDetails.getValueDomain().getValueDomainDetails().getDataType())) {
				question.setControlTypeResult(matchString);
			} else {
				question.setControlTypeResult(errorString);
				question.setQuestionCongruencyStatus(congStatus_errors);
			}
			List<String> pvList = new ArrayList<String>();
			for (PermissibleValuesModel pv : cdeDetails.getValueDomain().getPermissibleValues()) {
				pvList.add(pv.getValue());
			}
			List<String> cdResult = new ArrayList<String>();
			for (String codedData : question.getRaveCodedData()) {
				if (pvList.contains(codedData)) {
					cdResult.add(matchString);
				} else {
					cdResult.add(errorString);
					question.setQuestionCongruencyStatus(congStatus_errors);
				}
			}
			if (cdResult.size()>0)
				question.setCodedDataResult(cdResult);
			if (field.getDataFormat().indexOf("$") > -1 
			&& characterDataFormats.contains(cdeDetails.getValueDomain().getValueDomainDetails().getDataType()))  {
				question.setDatatypeCheckerResult(matchString);
			} else {
				question.setDatatypeCheckerResult(errorString);
				question.setQuestionCongruencyStatus(congStatus_errors);
			}
			if (field.getDataFormat().indexOf("$") == -1 
					&& numericDataFormats.contains(cdeDetails.getValueDomain().getValueDomainDetails().getDataType()))  {
				question.setDatatypeCheckerResult(matchString);
			} else {
				question.setDatatypeCheckerResult(errorString);
				question.setQuestionCongruencyStatus(congStatus_errors);
			}
			
			if (question.getRaveUOM()!=null && question.getRaveUOM().equals(cdeDetails.getValueDomain().getValueDomainDetails().getUnitOfMeasure())) {
				question.setUomCheckerResult(matchString);
			} else {
				question.setUomCheckerResult(errorString);
				question.setQuestionCongruencyStatus(congStatus_errors);
			}
			
			String raveLength = question.getRaveLength();
			if (raveLength!=null && raveLength.indexOf("characters")>-1) {
				raveLength.replaceAll("\\p{P}","");		
				int index = 0;
				if ((raveLength.indexOf("(") > -1) || (raveLength.indexOf(")") > -1)) {
					index = 1;
				}  else {
					index = 0;
				}
				raveLength = raveLength.substring(index,raveLength.indexOf("characters"));
				logger.debug("Max length: "+cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength()+ "  Rave length: " +raveLength);
				if (Float.valueOf(raveLength) < Float.valueOf(cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength())) {
					question.setLengthCheckerResult(matchString);
				} else {
					question.setLengthCheckerResult(errorString);
					question.setQuestionCongruencyStatus(congStatus_errors);	
				}
			} else {
				question.setLengthCheckerResult(errorString);
				question.setQuestionCongruencyStatus(congStatus_errors);
			} 

			if (field.getDataFormat().equals(cdeDetails.getValueDomain().getValueDomainDetails().getDisplayFormat())) {
				question.setFormatCheckerResult(matchString);
			} else {
				question.setFormatCheckerResult(errorString);
				question.setQuestionCongruencyStatus(congStatus_errors);
			}
			
			// TODO 
			//PV checker result

		}
		} catch (NullPointerException npe) {
			npe.printStackTrace();		
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}		
		if (message==null || message.equals("") )
			question.setQuestionCongruencyStatus(congStatus_congruent);
		else
			question.setMessage(message.toString());			
	return question;	
	}
	
	/**
	 * retrieve CDE by calling cde details restful service
	 * 
	 * @param uploadfile
	 * @return ResponseEntity
	 */
    @RequestMapping( value = "/rest/cdedetails" )
    @ResponseBody
	public CdeDetails retrieveDataElement(@RequestParam("publicId") String publicId,
			@RequestParam("version") String versionNumber) throws Exception  {
		logger.debug("Single file upload!");
		CdeDetails cdeDetails = null;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream input = classLoader.getResourceAsStream("boot.properties");		
        if (checkLinkParameters(publicId, versionNumber)) {
	        // Get the data model from the database
        		String CDEBROWSER_REST_GET_CDE = null;
        		Properties properties = new Properties();
        		properties.load(input);
        		String propVal;
        		if ((propVal = properties.getProperty("CDEBROWSER_REST_GET_CDE")) != null) {
        			CDEBROWSER_REST_GET_CDE = propVal;
        		}	
	        	String cdeBrowserRestApiUrl = String.format(CDEBROWSER_REST_GET_CDE, publicId, versionNumber);
	            RestTemplate restTemplate = new RestTemplate();
	    		HttpHeaders httpHeaders = new HttpHeaders();//we have to set up Accept header for a chance a server does not set up Content-Type header on response
	            cdeDetails = restTemplate.getForObject(cdeBrowserRestApiUrl, CdeDetails.class);
	            httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	            HttpEntity<String> entity = new HttpEntity<>("parameters", httpHeaders);
	            ResponseEntity<CdeDetails> responseEntity = restTemplate.exchange(cdeBrowserRestApiUrl, HttpMethod.GET, entity, CdeDetails.class);
	            cdeDetails = responseEntity.getBody();
        }
        else {
        	logger.info("Unexpected parameter values are ignored in retrieveDataElementDetailsByLink, publicId: " + publicId + ", versionNumber: " + versionNumber);
        }
        if (cdeDetails == null) {
        	cdeDetails = new CdeDetails();
        }

		return cdeDetails;
	}	
 
    
    private boolean checkLinkParameters(String publicId, String versionNumber) {
    	if ((NumberUtils.isNumber(versionNumber)) && (NumberUtils.isDigits(publicId))) {
    		return true;
    	}
    	else return false;
    }
    
    
	
	public static DBValidationError getDbErrorInstance() {
		DBValidationError dbError = new DBValidationError();
		return dbError;
	}

}
