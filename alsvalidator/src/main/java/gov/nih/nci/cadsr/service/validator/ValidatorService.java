/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.service.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import gov.nih.nci.cadsr.dao.model.PermissibleValuesModel;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.ReferenceDocument;

public class ValidatorService {

	private static final Logger logger = Logger.getLogger(ValidatorService.class);
	private static final String errorString = "ERROR";
	private static final String matchString = "MATCH";
	private static final String warningString = "WARNING";
	private static final String retiredString = "RETIRED";
	private static final String msg1 = "CDE not in caDSR database";
	private static final String msg2 = "CDE has been retired";
	private static final String msg3 = "Newer Versions exist";
	private static final String msg4_1 = "caDSR Max Length too short. PVs MaxLength ";
	private static final String msg4_2 = ", caDSR MaxLength ";		
	private static final String msg5 = "This CDE is not enumerated but question in input file has Coded Data (Permissible values)";
	private static final String msg6 = "Question Text in input file does not match available CDE question text(s)";	
	private static String congStatus_errors = "ERRORS";
	private static String congStatus_warn = "WARNINGS";
	private static List<String> characterDataFormats = Arrays.asList("CHAR", "VARCHAR2", "CHARACTER", "ALPHANUMERIC", "java.lang.String", "java.lang.Character", 
			"xsd:string");
	private static List<String> numericDataFormats = Arrays.asList("NUMBER", "number", "numeric", "integer", "Integer", "java.lang.Integer", "xsd:integer");
	private static List<String> dateDataFormats = Arrays.asList("DATE", "xsd:date");
	private static List<String> timeDataFormats = Arrays.asList("TIME", "xsd:time");
	private static final String characters_string = "characters";
	private static final String punct_pattern = "\\p{P}";
	private static final String patternHolderChar = "d";
	private static final String patternHolderNum = "9";	

	
	public static CCCQuestion validate(ALSField field, CCCQuestion question, CdeDetails cdeDetails) {
		try {
		if (cdeDetails.getDataElement()==null && cdeDetails.getValueDomain()==null) {			
			question.setMessage(msg1);
			question.setQuestionCongruencyStatus(congStatus_errors);
			logger.debug("CDE data not available for "+question.getCdePublicId());
		} else {
			
			//Checking for retired CDEs 
			question = checkCdeRetired(cdeDetails,question);

			//Checking for different versions of CDEs			
			question = checkCdeVersions(cdeDetails,question);
			
			// Adding Reference documents' PQTs and AQTs in a list for comparison against the ALS field preText 
			question = setRaveFieldLabelResult(cdeDetails,question);
			
			// Comparing the RAVE control type and the caDSR VD data type for Control Type Checker Result
			question = setRaveControlTypeResult(cdeDetails.getValueDomain().getValueDomainDetails().getValueDomainType(), question);

			// Gathering Permissible Values and Value Meanings in separate lists			
			List<String> pvList = new ArrayList<String>();
			List<String> pvVmList = new ArrayList<String>();
			int pvMaxLen = 0;
			int vdMaxLen = 0;
			StringBuffer allowableCdes = new StringBuffer();
			
			if (cdeDetails.getValueDomain()!=null) {
				vdMaxLen = cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength();
				for (PermissibleValuesModel pv : cdeDetails.getValueDomain().getPermissibleValues()) {
					pvList.add(pv.getValue());
					pvVmList.add(pv.getShortMeaning());
					if (allowableCdes.length() > 0)
						allowableCdes.append("|"+pv.getValue());
					else 
						allowableCdes.append(pv.getValue());
					if (pv.getValue().length() > pvMaxLen)
						pvMaxLen = pv.getValue().length();
				}
			}			
			
			// Checking for the presence of RAVE user data string in the PV Value meaning list - PV Checker result
			question = setPvCheckerResult (pvVmList, question);			
			
			// Setting the Allowable CDEs
			if (allowableCdes.length() > 0) 
				question.setAllowableCdeValue(allowableCdes.toString());
				
			// Checking for the presence of RAVE Coded data in the PV values list - Coded Data Checker Result
			question = setCodedDataCheckerResult(pvList, question);
				
			// Comparing RAVE Data format with caDSR Value Domain Datatype - Datatype Checker Result
			question = checkDataTypeCheckerResult (question, field.getDataFormat(), cdeDetails.getValueDomain().getValueDomainDetails().getDataType());
				
			// Comparing RAVE UOM (FixedUnit) with the caDSR Value Domain Unit of Measure - UOM Checker Result	
			question = setUomCheckerResult (question, cdeDetails.getValueDomain().getValueDomainDetails().getUnitOfMeasure());			
					
			// Comparing RAVE Length (FixedUnit) with the caDSR Value Domain Max length - RAVE Length Checker result
			question = setLengthCheckerResult (question, cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength());
			
			// Comparing RAVE Length (FixedUnit) with the caDSR PVs Max length
			question = checkCdeMaxLength (question, pvMaxLen, vdMaxLen, computeRaveLength(question.getRaveLength()));			
			
			//Comparing the ALS RAVE Data Format with caDSR Value Domain Display Format
			question = checkFormatCheckerResult (question, field.getDataFormat(), cdeDetails.getValueDomain().getValueDomainDetails().getDisplayFormat());

		}

		} catch (NullPointerException npe) {
			npe.printStackTrace();		
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}		
	return question;	
	}
	
	
	protected static CCCQuestion checkCdeRetired(CdeDetails cdeDetails, CCCQuestion question) {
		//Checking for retired CDEs 
		if (cdeDetails.getDataElement()!=null && cdeDetails.getDataElement().getDataElementDetails().getWorkflowStatus().equalsIgnoreCase(retiredString)) {	
			question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg6));
			if (!question.getQuestionCongruencyStatus().equalsIgnoreCase(congStatus_errors))
				question.setQuestionCongruencyStatus(congStatus_warn);
		}		
		return question;
	}


	protected static CCCQuestion checkCdeVersions(CdeDetails cdeDetails, CCCQuestion question) {
		//Checking for different versions of CDEs			
		if (cdeDetails.getDataElement()!=null && (cdeDetails.getDataElement().getDataElementDetails().getVersion() >  Float.valueOf(question.getCdeVersion()))) {
			question.setMessage(assignQuestionErrorMessage(question.getMessage(),msg3));
			if (!question.getQuestionCongruencyStatus().equalsIgnoreCase(congStatus_errors))
				question.setQuestionCongruencyStatus(congStatus_warn);
		}
		return question;		
	}
	
	
	protected static CCCQuestion setRaveFieldLabelResult (CdeDetails cdeDetails, CCCQuestion question) {
		List<String> rdDocTextList = new ArrayList<String>();
		StringBuffer rdDocs = new StringBuffer();
		String rdDocText;
		
		// Adding Reference documents' PQTs and AQTs in a list for comparison against the ALS field preText 
		if (cdeDetails.getDataElement()!=null) {
			for (ReferenceDocument rd : cdeDetails.getDataElement().getQuestionTextReferenceDocuments()) {
				rdDocText =  rd.getDocumentText();
				rdDocTextList.add(rdDocText);
				// Concatenating the entire list of AQTs and PQTs together 
				if (rd.getDocumentType().equalsIgnoreCase("Preferred Question Text") || rd.getDocumentType().equalsIgnoreCase("Alternate Question Text")) {
					if (rdDocs.length() > 0)
						rdDocs.append("|"+rdDocText);
					else 
						rdDocs.append(rdDocText);
				}	
			}
		}			

		if (!rdDocTextList.isEmpty() && rdDocTextList.contains(question.getRaveFieldLabel())) {
			question.setRaveFieldLabelResult(matchString);
		} else {
			question.setRaveFieldLabelResult(errorString);
			question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg6));
			question.setQuestionCongruencyStatus(congStatus_errors);
		}
		// Setting the concatenated string of AQTs and PQTs into CDE permitted question text choices
		question.setCdePermitQuestionTextChoices(rdDocs.toString());
		return question;		
	}

	
	protected static CCCQuestion setRaveControlTypeResult (String vdType, CCCQuestion question) {
		// Comparing the RAVE control type and the caDSR VD data type for Control Type Checker Result
			if (question.getRaveControlType()!=null) {
				if (question.getRaveControlType().equalsIgnoreCase("TEXT") && vdType.equalsIgnoreCase("N")) {
					question.setControlTypeResult(matchString);
					if (!question.getRaveCodedData().isEmpty()) {
						question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg5));
					}
						
				} else if (!question.getRaveControlType().equalsIgnoreCase("TEXT") && vdType.equalsIgnoreCase("E")) {
					question.setControlTypeResult(matchString);
				} else {
					question.setControlTypeResult(errorString);
					question.setQuestionCongruencyStatus(congStatus_errors);
				}
				question.setCdeValueDomainType(vdType);	
			} else {
				question.setControlTypeResult(errorString);
				question.setQuestionCongruencyStatus(congStatus_errors);
			}
		return question;
	}
	
	
	protected static CCCQuestion setPvCheckerResult (List<String> pvVmList, CCCQuestion question) {
		// Checking for the presence of RAVE user data string in the PV Value meaning list - PV Checker result
		if (!pvVmList.isEmpty()) {
			for (String userDataString : question.getRaveUserString()) {
				if (pvVmList.contains(userDataString)) {
					question.setPvResult(matchString);
				} else {
					question.setPvResult(errorString);
					question.setQuestionCongruencyStatus(congStatus_errors);
				}
			}
		}
		return question;
	}
	
	
	protected static CCCQuestion setCodedDataCheckerResult (List<String> pvList, CCCQuestion question) {
		// Checking for the presence of RAVE Coded data in the PV values list - Coded Data Checker Result
		List<String> cdResult = new ArrayList<String>();
		if (!pvList.isEmpty()) {
			for (String codedData : question.getRaveCodedData()) {
				if (pvList.contains(codedData)) {
					cdResult.add(matchString);
				} else {
					cdResult.add(errorString);
					question.setQuestionCongruencyStatus(congStatus_errors);
				}
			}
			if (!cdResult.isEmpty())
				question.setCodedDataResult(cdResult); 
			}
		return question;
	}
	

	protected static CCCQuestion checkDataTypeCheckerResult (CCCQuestion question, String raveDataFormat, String vdDataType) {
		// Comparing RAVE Data format with caDSR Value Domain Datatype - Datatype Checker Result
		Boolean result = false;
		if (raveDataFormat!=null) {
			if (raveDataFormat.startsWith("$")) {
				if (characterDataFormats.contains(vdDataType)) 
					result = true;
			} else if (raveDataFormat.startsWith("dd") || raveDataFormat.startsWith("DD")) {
				if (dateDataFormats.contains(vdDataType)) 
					result = true;
			} else if (raveDataFormat.startsWith("hh") || raveDataFormat.startsWith("HH")) {
				if (timeDataFormats.contains(vdDataType))
					result = true;
			} else {
				if (numericDataFormats.contains(vdDataType))
					result = true;
			}
		}
		if (result)
			question.setDatatypeCheckerResult(matchString);
		else {
			question.setDatatypeCheckerResult(errorString);
			question.setQuestionCongruencyStatus(congStatus_errors);
		}
		return question;
	}
	
	
	protected static CCCQuestion setUomCheckerResult (CCCQuestion question, String unitOfMeasure) {
		// Comparing RAVE UOM (FixedUnit) with the caDSR Value Domain Unit of Measure - UOM Checker Result
		if (question.getRaveUOM()!=null) {
				if (question.getRaveUOM().equals(unitOfMeasure)) {
					question.setUomCheckerResult(matchString);
				} else {
					question.setUomCheckerResult(warningString);
					if (!question.getQuestionCongruencyStatus().equalsIgnoreCase(congStatus_errors))
						question.setQuestionCongruencyStatus(congStatus_warn);
					question.setCdeUOM(unitOfMeasure);
				}
		}
		return question;
	}
	
	
	protected static CCCQuestion setLengthCheckerResult (CCCQuestion question, Integer vdMaxLength) {
		// Comparing RAVE Length (FixedUnit) with the caDSR Value Domain Max length - RAVE Length Checker result
		String raveLength = question.getRaveLength();
		if (raveLength!=null) {
			if (Float.valueOf(computeRaveLength(raveLength)) < Float.valueOf(vdMaxLength)) {
				question.setLengthCheckerResult(matchString);
			} else {
				question.setLengthCheckerResult(warningString);
				if (!question.getQuestionCongruencyStatus().equalsIgnoreCase(congStatus_errors))
					question.setQuestionCongruencyStatus(congStatus_warn);
			}
		}		
		return question;
	}
	
	
	protected static CCCQuestion checkFormatCheckerResult (CCCQuestion question, String raveDataFormat, String vdDisplayFormat) {
		//Comparing the ALS RAVE Data Format with caDSR Value Domain Display Format
		if (raveDataFormat!=null) {
				if (raveDataFormat.equals(vdDisplayFormat)) {
					question.setFormatCheckerResult(matchString);
				} else {
					question.setFormatCheckerResult(warningString);
					if (!question.getQuestionCongruencyStatus().equalsIgnoreCase(congStatus_errors))
						question.setQuestionCongruencyStatus(congStatus_warn);
				} 
		}
		return question;
	}
	

	protected static CCCQuestion checkCdeMaxLength (CCCQuestion question, int pvMaxLen, int vdMaxLen, int cdeMaxLen) {
		if (pvMaxLen > vdMaxLen)
			question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg4_1 + pvMaxLen + msg4_2 + vdMaxLen));
		return question;
	}
	
	protected static int computeRaveLength (String raveLength) {
		if (raveLength!=null && !raveLength.equals("%")) {
			if (raveLength.indexOf(characters_string)>-1) {
				raveLength.replaceAll(punct_pattern,"");
				int index = 0;
				if ((raveLength.indexOf("(") > -1) || (raveLength.indexOf(")") > -1)) {
					index = 1;
				}  else {
					index = 0;
				}
				raveLength = raveLength.substring(index,raveLength.indexOf(characters_string));
			} else if (StringUtils.countOccurrencesOf(raveLength, patternHolderChar) > 1) {
				raveLength = String.valueOf(StringUtils.countOccurrencesOf(raveLength, patternHolderChar));
			} else if (StringUtils.countOccurrencesOf(raveLength, patternHolderNum) > 1) {
				raveLength = String.valueOf(StringUtils.countOccurrencesOf(raveLength, patternHolderNum));
			}	
		} else {
			raveLength = "0";
		}
		return Integer.parseInt(raveLength.trim());
	}
	
	protected static String assignQuestionErrorMessage (String questionMessage, String newMessage) {
		String errorMessage = null;
		if (questionMessage!=null) 
			errorMessage = questionMessage +"\n"+ newMessage;
		else 
			errorMessage = newMessage;
		return errorMessage;
	}	

}
