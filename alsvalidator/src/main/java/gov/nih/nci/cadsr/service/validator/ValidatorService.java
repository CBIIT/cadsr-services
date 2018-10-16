/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.service.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gov.nih.nci.cadsr.dao.model.AlternateNameUiModel;
import gov.nih.nci.cadsr.dao.model.PermissibleValuesModel;
import gov.nih.nci.cadsr.dao.model.ValueMeaningUiModel;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.OtherVersion;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.ReferenceDocument;

/**
 * Service with methods checking the congruency of the ALS input with CDE from caDSR
 *
 */
public class ValidatorService {

	private static final Logger logger = LoggerFactory.getLogger(ValidatorService.class);
	private static final String errorString = "ERROR";
	private static final String matchString = "MATCH";
	private static final String warningString = "WARNING";
	private static final String retiredArchivedStatus = "RETIRED ARCHIVED";
	private static final String retiredPhasedOutStatus = "RETIRED PHASED OUT";
	private static final String retiredWithdrawnStatus = "RETIRED WITHDRAWN";
	private static final String msg1 = "CDE not in caDSR database";
	private static final String msg2 = "CDE has been retired";
	private static final String msg3 = "Newer version of CDE exists: ";
	private static final String msg4_1 = "caDSR Max Length too short. PVs MaxLength ";
	private static final String msg4_2 = ", caDSR MaxLength ";
	private static final String msg5 = "This CDE is not enumerated but question in input file has Coded Data (Permissible values)";
	private static final String msg6 = "Question Text in input file does not match available CDE question text(s)";
	private static final String msg7 = "Unknown Control Type from caDSR DB";
	private static final String msg8 = "Control Type nor present in the ALS input data";
	private static final String msg9 = "Additional PVs in Valid Value list";
	private static final String msg10 = "PVs not in caDSR DB";
	private static final String msg11 = "Data type from ALS input doesn't match with caDSR DB";
	private static final String msg12 = "Unit of Measure from ALS input doesn't match with that of the Value Domain";
	private static final String msg13 = "Format doesn't match with that of the Value Domain";
	private static final String msg14 = "Fixed Unit from ALS input data doesn't match with Value Domain Max length";	
	private static String congStatus_errors = "ERRORS";
	private static String congStatus_warn = "WARNINGS";
	private static List<String> characterDataFormats = Arrays.asList("CHAR", "VARCHAR2", "CHARACTER", "ALPHANUMERIC",
			"java.lang.String", "java.lang.Character", "xsd:string");
	private static List<String> numericDataFormats = Arrays.asList("NUMBER", "number", "numeric", "integer", "Integer",
			"java.lang.Integer", "xsd:integer");
	private static List<String> dateDataFormats = Arrays.asList("DATE", "xsd:date");
	private static List<String> timeDataFormats = Arrays.asList("TIME", "xsd:time");
	private static final String characters_string = "characters";
	private static final String punct_pattern = "\\p{P}";
	private static final String patternHolderChar = "d";
	private static final String patternHolderNum = "9";

	
	/**
	 * Calls the methods to perform congruency checks against caDSR DB 
	 * @param field
	 * @param question
	 * @param cdeDetails
	 * @return CCCQuestion
	 */
	public static CCCQuestion validate(ALSField field, CCCQuestion question, CdeDetails cdeDetails) {
		try {
		if (cdeDetails.getDataElement()==null && cdeDetails.getValueDomain()==null) {
			question.setMessage(msg1);
			question.setQuestionCongruencyStatus(congStatus_errors);
			logger.debug("CDE data not available for publicId:version="+question.getCdePublicId() + ":" + question.getCdeVersion());
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
			Map<String, List<String>> pvVmMap =  new HashMap<String, List<String>>();
			int pvMaxLen = 0;
			int vdMaxLen = 0;
			StringBuffer allowableCdes = new StringBuffer();
			
			if (cdeDetails.getValueDomain()!=null) {
				if (cdeDetails.getValueDomain().getValueDomainDetails()!=null) {
					if (cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength()!=null) {
						vdMaxLen = cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength();
					}
				}

				if (cdeDetails.getValueDomain().getPermissibleValues()!=null) {
					for (PermissibleValuesModel pv : cdeDetails.getValueDomain().getPermissibleValues()) {
						pvList.add(pv.getValue());
						pvVmList = new ArrayList<String>();
						pvVmList = buildPvAltNamesList (cdeDetails, pv.getVmIdseq());
						if (pv.getShortMeaning()!=null)
							pvVmList.add(pv.getShortMeaning());
						pvVmMap.put(pv.getValue(), pvVmList);
						if (allowableCdes.length() > 0)
							allowableCdes.append("|"+pv.getValue());
						else 
							allowableCdes.append(pv.getValue());
						if (pv.getValue().length() > pvMaxLen)
							pvMaxLen = pv.getValue().length();
					}
				}
			}			

			// Setting the Allowable CDEs
			if (allowableCdes.length() > 0) 
				question.setAllowableCdeValue(allowableCdes.toString());
			
			// Checking for the presence of RAVE user data string in the PV Value meaning list - PV Checker result
			question = setPvCheckerResult (pvVmMap, question);
				
			// Checking for the presence of RAVE Coded data in the PV values list - Coded Data Checker Result
			question = setCodedDataCheckerResult(pvList, question);
				
			// Comparing RAVE Data format with caDSR Value Domain Datatype - Datatype Checker Result
			question = checkDataTypeCheckerResult (question, field.getDataFormat(), cdeDetails.getValueDomain().getValueDomainDetails().getDataType());
				
			// Comparing RAVE UOM (FixedUnit) with the caDSR Value Domain Unit of Measure - UOM Checker Result	
			question = setUomCheckerResult (question, cdeDetails.getValueDomain().getValueDomainDetails().getUnitOfMeasure());			
					
			// Comparing RAVE Length (FixedUnit) with the caDSR Value Domain Max length - RAVE Length Checker result
			question = setLengthCheckerResult (question, cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength());
			
			// Comparing RAVE Length (FixedUnit) with the caDSR PVs Max length
			if (vdMaxLen != 0)
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
	
	
	/**
	 * Check if the CDE is retired [Workflow status - RETIRED ARCHIVED, RETIRED PHASED OUT or RETIRED WITHDRAWN]
	 * @param cdeDetails
	 * @param question
	 * @return CCCQuestion
	 */
	protected static CCCQuestion checkCdeRetired(CdeDetails cdeDetails, CCCQuestion question) {
		//Checking for retired CDEs 
		if (cdeDetails.getDataElement()!=null && (cdeDetails.getDataElement().getDataElementDetails().getWorkflowStatus().equalsIgnoreCase(retiredArchivedStatus)
				|| cdeDetails.getDataElement().getDataElementDetails().getWorkflowStatus().equalsIgnoreCase(retiredPhasedOutStatus)
				|| cdeDetails.getDataElement().getDataElementDetails().getWorkflowStatus().equalsIgnoreCase(retiredWithdrawnStatus))) {	
			question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg2));
			if (question.getQuestionCongruencyStatus()==null)
				question.setQuestionCongruencyStatus(congStatus_warn);
		}		
		return question;
	}


	/**
	 * Retrieve & check for newer versions of the CDE
	 * @param cdeDetails
	 * @param question
	 * @return CCCQuestion
	 */
	protected static CCCQuestion checkCdeVersions(CdeDetails cdeDetails, CCCQuestion question) {
		//Checking for different versions of CDEs
		Boolean newerVersionExists = false;
		Float latestVersion = null;
		if (cdeDetails.getDataElement()!=null) {
			for (OtherVersion otherVersion : cdeDetails.getDataElement().getOtherVersions()) {
				if (otherVersion.getVersion() >  Float.valueOf(question.getCdeVersion())) {
					newerVersionExists = true;
					latestVersion = otherVersion.getVersion();
				}					
			}
							
			if (newerVersionExists) {
				question.setMessage(assignQuestionErrorMessage(question.getMessage(),msg3+latestVersion));
				if (question.getQuestionCongruencyStatus()==null)
					question.setQuestionCongruencyStatus(congStatus_warn);
			} 
		}
		return question;		
	}
	
	
	/**
	 * Check & Set the congruency check result for RAVE Field Label
	 * @param cdeDetails
	 * @param question
	 * @return CCCQuestion
	 */
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

	
	/**
	 * Comparing the RAVE control type and the caDSR VD data type for Control Type Checker Result
	 * @param vdType
	 * @param question
	 * @return CCCQuestion
	 */
	protected static CCCQuestion setRaveControlTypeResult (String vdType, CCCQuestion question) {
			question.setCdeValueDomainType(vdType);		
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
					question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg7));
					question.setQuestionCongruencyStatus(congStatus_errors);
				}	
			} else {
				question.setControlTypeResult(errorString);
				question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg8));				
				question.setQuestionCongruencyStatus(congStatus_errors);
			}
		return question;
	}
	
	
	/**
	 * Check the PV values, Long name and VM's alternate names against the User Data String in the ALS input file
	 * @param pvList
	 * @param question
	 * @return CCCQuestion
	 */
	protected static CCCQuestion setPvCheckerResult (Map<String, List<String>> pvVmMap, CCCQuestion question) {
		// Checking for the presence of RAVE user data string in the PV Value meaning list - PV Checker result
		Boolean isMatch = false;
		List<String> userDataStringList = question.getRaveUserString();
		List<String> codedDataList = question.getRaveCodedData();
		List<String> allowCdesList = new ArrayList<String>();
		if (userDataStringList!=null) {
			for (String userDataString : userDataStringList) {
				String pvValue = codedDataList.get(userDataStringList.indexOf(userDataString));
				List<String> pvVmList = pvVmMap.get(pvValue);
				if (pvVmList!=null) {
					if (pvVmList.contains(userDataString) || pvValue.equals(userDataString)) {
						question.setPvResult(matchString);
						isMatch = true;
					}
					StringBuffer allowableVmTextChoices = new StringBuffer();
					// Building a list of Allowable CDE text choices (in case of a 'Not match' for PV checker)
					for (String altName : pvVmList) {
						if (allowableVmTextChoices.length() > 0)
							allowableVmTextChoices.append("|"+altName);
						else
							allowableVmTextChoices.append(altName);
					}
					allowCdesList.add(allowableVmTextChoices.toString());					
				}
			}
			if (!isMatch) {
				question.setPvResult(errorString);
				question.setAllowableCdeTextChoices(allowCdesList);					
				if((question.getMessage() != null) && (question.getMessage().indexOf(msg9) == -1))
					question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg9));
				question.setQuestionCongruencyStatus(congStatus_errors);
			}			
		}
		return question;
	}
	
	
	/**
	 * Checking for the presence of RAVE Coded data in the PV values list - Coded Data Checker Result
	 * @param pvList
	 * @param question
	 * @return CCCQuestion
	 */
	protected static CCCQuestion setCodedDataCheckerResult (List<String> pvList, CCCQuestion question) {
		List<String> cdResult = new ArrayList<String>();
		if (!pvList.isEmpty()) {
			for (String codedData : question.getRaveCodedData()) {
				if (pvList.contains(codedData)) {
					cdResult.add(matchString);
				} else {
					cdResult.add(errorString);
					if ((question.getMessage()!=null) && (question.getMessage().indexOf(msg10) == -1))
						question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg10));
					question.setQuestionCongruencyStatus(congStatus_errors);
				}
			}
			if (!cdResult.isEmpty())
				question.setCodedDataResult(cdResult); 
			}
		return question;
	}
	

	/**
	 * Comparing RAVE Data format with caDSR Value Domain Datatype - Datatype Checker Result
	 * @param question
	 * @param raveDataFormat
	 * @param vdDataType
	 * @return CCCQuestion
	 */
	protected static CCCQuestion checkDataTypeCheckerResult (CCCQuestion question, String raveDataFormat, String vdDataType) {
		Boolean result = false;
		question.setCdeDataType(vdDataType);
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
			question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg11));			
			question.setQuestionCongruencyStatus(congStatus_errors);
		}
		return question;
	}
	
	
	/**
	 * Comparing RAVE UOM (FixedUnit) with the caDSR Value Domain Unit of Measure - UOM Checker Result
	 * @param question
	 * @param unitOfMeasure
	 * @return CCCQuestion
	 */
	protected static CCCQuestion setUomCheckerResult (CCCQuestion question, String unitOfMeasure) {
		question.setCdeUOM(unitOfMeasure);
		if (unitOfMeasure!=null) {
			if (question.getRaveUOM()!=null) {
					if (question.getRaveUOM().equals(unitOfMeasure)) {
						question.setUomCheckerResult(matchString);
					} else {
						question.setUomCheckerResult(warningString);
						question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg12));
						if (question.getQuestionCongruencyStatus()==null)
							question.setQuestionCongruencyStatus(congStatus_warn);
					}
			} else {
				question.setUomCheckerResult(warningString);
				question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg12));
				if (question.getQuestionCongruencyStatus()==null)
					question.setQuestionCongruencyStatus(congStatus_warn);
			} 
		} else {
			question.setUomCheckerResult(matchString);
		}
		return question;
	}
	
	
	/**
	 * Comparing RAVE Length (FixedUnit) with the caDSR Value Domain Max length - RAVE Length Checker result
	 * @param question
	 * @param vdMaxLength
	 * @return CCCQuestion
	 */
	protected static CCCQuestion setLengthCheckerResult (CCCQuestion question, Integer vdMaxLength) {
		String raveLength = question.getRaveLength();
		if (vdMaxLength!=null) {
			if (raveLength!=null) {			
				if (!(Float.valueOf(computeRaveLength(raveLength)) > Float.valueOf(vdMaxLength))) {
					question.setLengthCheckerResult(matchString);
				} else {
					question.setLengthCheckerResult(warningString);
					question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg14));
					if (question.getQuestionCongruencyStatus()==null)
						question.setQuestionCongruencyStatus(congStatus_warn);
				}
			} else {
				question.setLengthCheckerResult(warningString);
				question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg14));
				if (question.getQuestionCongruencyStatus()==null)
					question.setQuestionCongruencyStatus(congStatus_warn);				
			}
		} else {
			question.setLengthCheckerResult(matchString);
		}
		return question;
	}
	
	
	/**
	 * Comparing the ALS RAVE Data Format with caDSR Value Domain Display Format
	 * @param question
	 * @param raveDataFormat
	 * @param vdDisplayFormat
	 * @return CCCQuestion
	 */
	protected static CCCQuestion checkFormatCheckerResult (CCCQuestion question, String raveDataFormat, String vdDisplayFormat) {
		question.setCdeDisplayFormat(vdDisplayFormat);
		if (raveDataFormat!=null) {
				if (raveDataFormat.equals(vdDisplayFormat)) {
					question.setFormatCheckerResult(matchString);
				} else {
					question.setFormatCheckerResult(warningString);
					question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg13));
					if (question.getQuestionCongruencyStatus()==null)
						question.setQuestionCongruencyStatus(congStatus_warn);
				} 
		}
		return question;
	}
	

	/**
	 * Checking for the max length of CDEs against the max length of the Value Domain
	 * @param question
	 * @param pvMaxLen
	 * @param vdMaxLen
	 * @param cdeMaxLen
	 * @return CCCQuestion
	 */
	protected static CCCQuestion checkCdeMaxLength (CCCQuestion question, int pvMaxLen, int vdMaxLen, int cdeMaxLen) {
		question.setCdeMaxLength(vdMaxLen);
		if (pvMaxLen > vdMaxLen)
			question.setMessage(assignQuestionErrorMessage(question.getMessage(), msg4_1 + pvMaxLen + msg4_2 + vdMaxLen));
		return question;
	}
	
	
	/**
	 * Compute the Rave length
	 * @param raveLength
	 * @return int
	 */
	protected static int computeRaveLength (String raveLength) {
		int raveLengthInt = 0;
		if (raveLength!=null && !raveLength.equals("%")) {
			raveLength = raveLength.toLowerCase();
			if (raveLength.indexOf(characters_string) > -1) {
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
		try {
			raveLengthInt = Integer.parseInt(raveLength.trim());
		} catch (NumberFormatException nfe) {
			logger.debug("Error in computing Rave Length from Field Unit: "+raveLength+" :: "+nfe.getMessage());
		}
		return raveLengthInt;
	}
	
	
	/**
	 * Assign a question message to a question
	 * @param questionMessage
	 * @param newMessage
	 * @return String
	 */
	protected static String assignQuestionErrorMessage (String questionMessage, String newMessage) {
		String errorMessage = null;
		if (questionMessage!=null) 
			errorMessage = questionMessage +"\n"+ newMessage;
		else 
			errorMessage = newMessage;
		return errorMessage;
	}
	
	/**
	 * Build a list of Value Meaning Alternate Names for a set of Permissible Values
	 * @param vmIdSeq
	 * @param cdeDetails
	 * @return List<String>
	 */			
	protected static List<String> buildPvAltNamesList(CdeDetails cdeDetails, String vmIdSeq) {
		List<ValueMeaningUiModel> vmUiModelList = new ArrayList<ValueMeaningUiModel>();
		List<String> allowableCdeValueList = new ArrayList<String>();		
		if (cdeDetails.getValueDomain()!=null) {
			if (cdeDetails.getValueDomain().getValueMeaning()!=null) {
				vmUiModelList = cdeDetails.getValueDomain().getValueMeaning();
					for (ValueMeaningUiModel vm : vmUiModelList) {
						if (vm.getVmIdseq().equalsIgnoreCase(vmIdSeq)) {
							if (vm.getAlternateNames()!=null) {
								for (AlternateNameUiModel altName : vm.getAlternateNames()) {
									allowableCdeValueList.add(altName.getName());
								}
							} 
						}
					}								
			}
		}
		return allowableCdeValueList;		
	}	

}
