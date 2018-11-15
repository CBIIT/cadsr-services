/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.service.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static final String msg1 = "CDE not found in caDSR database.";
	private static final String msg2 = "CDE has been retired.";
	private static final String msg3 = "Newer version of CDE exists: {%.1f}.";
	private static final String msg4 = "Value domain Max Length too short. PVs MaxLength is {%d} , caDSR MaxLength is {%d}.";
	private static final String msg5 = "This CDE is not enumerated but question in input file has Coded Data (Permissible values) - %s.";
	private static final String msg6 = "Question Text in input file does not match available CDE question text(s) - %s.";
	private static final String msg7 = "Control Type {%s} isn't compatible with the corresponding mapping for Value domain type {%s}.";
	private static final String msg8 = "Missing Control Type in the ALS input data.";
	private static final String msg9 = "Additional PVs in Valid Value list.";
	private static final String msg10 = "The Coded data {%s} for the question do not belong to the corresponding Value domain.";
	private static final String msg11 = "Data type {%s} from ALS input data doesn't match with the corresponding Value Domain's data type {%s}.";
	private static final String msg12 = "Unit of Measure {%s} from ALS input data doesn't match with the corresponding Value Domain's UOM {%s}.";
	private static final String msg13 = "Format {%s} doesn't match with the corresponding Value Domain's format {%s}.";
	private static final String msg14 = "Fixed Unit {%s} from ALS input data doesn't match with the corresponding Value Domain's Max length {%d}.";
	private static String congStatus_errors = "ERRORS";
	private static String congStatus_warn = "WARNINGS";
	private static List<String> characterDataFormats = Arrays.asList("CHAR", "VARCHAR2", "CHARACTER", "ALPHANUMERIC",
			"java.lang.String", "java.lang.Character", "xsd:string");
	private static List<String> numericDataFormats = Arrays.asList("NUMBER", "number", "numeric", "integer", "Integer",
			"java.lang.Integer", "xsd:integer");
	private static List<String> dateDataFormats = Arrays.asList("DATE", "xsd:date");
	private static List<String> timeDataFormats = Arrays.asList("TIME", "xsd:time");
	private static final String characters_string = "characters";
	private static final String patternHolderChar = "d";
	private static final String patternHolderNum = "9";
	private static final String regex_nbsp_space = "[\\p{Z}\\s]";//"'\u00A0', '\u2007', '\u202F'" - (NON BREAKING SPACE)
	private static final String space_str = " ";
	private static final String regex_inverted_qm = "[\\u00bf]";// u00BF, 0xbf, U+00BF, c2BF	- ¿	(INVERTED QUESTION MARK)
	private static final String apostrophe_str = "'";
	private static final String regex_super_2 = "[\\u00b2]";// u00B2, 0xb2, U+00B2, c2B2	- ²	(SUPERSCRIPT TWO)
	private static final String superscript_2_str = "²";
	private static final String alternateNames_key = "AlternateNames";
	private static final String vmPvMeanings_key = "PVMeanings";

	
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
			String allowableCdes = "";
			
			if (cdeDetails.getValueDomain()!=null) {
				if (cdeDetails.getValueDomain().getValueDomainDetails()!=null) {
					if (cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength()!=null) {
						vdMaxLen = cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength();
					}
				}

				if (cdeDetails.getValueDomain().getPermissibleValues()!=null) {
					for (PermissibleValuesModel pv : cdeDetails.getValueDomain().getPermissibleValues()) {
						String pvVal = cleanUtfString(pv.getValue());
						pvList.add(pvVal);
						pvVmList = new ArrayList<String>();
						Map<String, List<String>> vmMap = buildValueMeaningMap (cdeDetails, pv.getVmIdseq()); 
						pvVmList = vmMap.get(alternateNames_key);						
						if (pv.getShortMeaning()!=null)
							pvVmList.add(pv.getShortMeaning());
						pvVmMap.put(pvVal, pvVmList);
						if (allowableCdes.length() > 0)
							allowableCdes = allowableCdes + "|"+pvVal;
						else 
							allowableCdes = allowableCdes + pvVal;
						if (pvVal.length() > pvMaxLen)
							pvMaxLen = pvVal.length();
					}
				}
			}			

			// Setting the Allowable CDEs
			if (allowableCdes.length() > 0) 
				question.setAllowableCdeValue(allowableCdes);
			
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
		if (cdeDetails.getDataElement()!=null && (retiredArchivedStatus.equalsIgnoreCase(cdeDetails.getDataElement().getDataElementDetails().getWorkflowStatus())
				|| retiredPhasedOutStatus.equalsIgnoreCase(cdeDetails.getDataElement().getDataElementDetails().getWorkflowStatus())
				|| retiredWithdrawnStatus.equalsIgnoreCase(cdeDetails.getDataElement().getDataElementDetails().getWorkflowStatus()))) {	
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
				question.setMessage(assignQuestionErrorMessage(question.getMessage(),String.format(msg3, latestVersion)));
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
				rdDocTextList.add(cleanUtfString(rdDocText));
				// Concatenating the entire list of AQTs and PQTs together 
				if ("Preferred Question Text".equalsIgnoreCase(rd.getDocumentType()) || "Alternate Question Text".equalsIgnoreCase(rd.getDocumentType())) {
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
			question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg6, rdDocTextList)));
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
			List<Object> errorVal = new ArrayList<Object>();
			errorVal.add(question.getRaveControlType());
			if (vdType!=null) {
				if ("N".equalsIgnoreCase(vdType))
					errorVal.add("Non-enumerated");
				else if ("E".equalsIgnoreCase(vdType))
					errorVal.add("Enumerated");
				else
					errorVal.add("Unknown");
			}	else
				errorVal.add("Unknown");
			// TODO: This validation is pending a mapping table with the control types and their appropriate data types
			// For now we're just directly checking ALS input against the value domain.
			if (question.getRaveControlType()!=null) {
				if ("TEXT".equalsIgnoreCase(question.getRaveControlType()) && "N".equalsIgnoreCase(vdType)) {
					question.setControlTypeResult(matchString);
					if (!question.getRaveCodedData().isEmpty()) {
						question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg5, question.getRaveCodedData())));
					}
						
				} else if (!"TEXT".equalsIgnoreCase(question.getRaveControlType()) && "E".equalsIgnoreCase(vdType)) {
					question.setControlTypeResult(matchString);
				} else {
					question.setControlTypeResult(errorString);
					question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg7, errorVal.toArray())));
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
		List<String> pvCheckerResultsList = new ArrayList<String>();
		
		if (userDataStringList!=null) {
			for (String userDataString : userDataStringList) {
				String pvValue = codedDataList.get(userDataStringList.indexOf(userDataString));
				List<String> pvVmList = pvVmMap.get(pvValue);				
				if (pvVmList!=null) {
					if (pvVmList.contains(userDataString)) {
						isMatch = true;
						pvCheckerResultsList.add(matchString);
						allowCdesList.add("");
					} else {
						isMatch = false;
						pvCheckerResultsList.add(errorString);
						allowCdesList.add(createAllowableTextChoices(pvVmList));
					}	
				} else {					
					isMatch = false;
					pvCheckerResultsList.add(errorString);
					allowCdesList.add("");
				}
			}
			question.setPvResults(pvCheckerResultsList);
			question.setAllowableCdeTextChoices(allowCdesList);
			if (!isMatch) {
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
		String at_str = "@@";
		String hash_str = "##";		
		String comma_str = ",";
		String semicolon_str = ";";
		if (!pvList.isEmpty()) {
			for (String codedData : question.getRaveCodedData()) {
				if (codedData!=null) {
					if (codedData.indexOf(at_str) > -1)
						codedData = replacePattern(codedData, at_str, comma_str);
					if (codedData.indexOf(hash_str) > -1)
						codedData = replacePattern(codedData, hash_str, semicolon_str);
				}
				if (pvList.contains(codedData)) {
					cdResult.add(matchString);
				} else {
					cdResult.add(errorString);
					if ((question.getMessage()!=null) && (question.getMessage().indexOf(msg10) == -1))
						question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg10, question.getRaveCodedData())));
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
		List<Object> errorVal = new ArrayList<Object>();		
		question.setCdeDataType(vdDataType);
		if (raveDataFormat!=null) {
			errorVal.add(raveDataFormat);
			errorVal.add(vdDataType);			
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
			question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg11, errorVal.toArray())));			
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
		List<Object> errorVal = new ArrayList<Object>();
			errorVal.add(question.getRaveUOM());
			errorVal.add(unitOfMeasure);		
		if (unitOfMeasure!=null) {
			if (question.getRaveUOM()!=null) {
					if (question.getRaveUOM().equals(unitOfMeasure)) {
						question.setUomCheckerResult(matchString);
					} else {
						question.setUomCheckerResult(warningString);
						question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg12, errorVal.toArray())));
						if (question.getQuestionCongruencyStatus()==null)
							question.setQuestionCongruencyStatus(congStatus_warn);
					}
			} else {
				question.setUomCheckerResult(warningString);
				question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg12, errorVal.toArray())));
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
		List<Object> errorVal = new ArrayList<Object>();
		errorVal.add(raveLength);
		errorVal.add(vdMaxLength);				
		if (vdMaxLength!=null) {
			if (raveLength!=null) {			
				if (!(Float.valueOf(computeRaveLength(raveLength)) > Float.valueOf(vdMaxLength))) {
					question.setLengthCheckerResult(matchString);
				} else {
					question.setLengthCheckerResult(warningString);
					question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg14, errorVal.toArray())));
					if (question.getQuestionCongruencyStatus()==null)
						question.setQuestionCongruencyStatus(congStatus_warn);
				}
			} else {
				question.setLengthCheckerResult(warningString);
				question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg14, errorVal.toArray())));
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
		List<Object> errorVal = new ArrayList<Object>();
		errorVal.add(raveDataFormat);
		errorVal.add(vdDisplayFormat);
		if (raveDataFormat!=null) {
				if (raveDataFormat.equals(vdDisplayFormat)) {
					question.setFormatCheckerResult(matchString);
				} else {
					question.setFormatCheckerResult(warningString);
					question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg13, errorVal.toArray())));
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
		List<Object> errorVal = new ArrayList<Object>();
		errorVal.add(pvMaxLen);
		errorVal.add(vdMaxLen);
		if (pvMaxLen > vdMaxLen)
			question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg4, errorVal.toArray())));
		return question;
	}
	
	
	/**
	 * Compute the Rave length
	 * @param raveLength
	 * @return int
	 */
	protected static int computeRaveLength (String raveLength) {
		int raveLengthInt = 0;
		if (raveLength!=null && !"%".equals(raveLength)) {
			raveLength = raveLength.toLowerCase();
			if (raveLength.indexOf(characters_string) > -1) {
				Pattern pattern = Pattern.compile("\\d+");
				Matcher matcher = pattern.matcher(raveLength);
				while (matcher.find()) {					
					raveLength = matcher.group();
				}
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
	 * Build a map with two lists - Value Meaning Alternate Names and PV meanings
	 * @param vmIdSeq
	 * @param cdeDetails
	 * @return List<String>
	 */			
	protected static Map<String, List<String>> buildValueMeaningMap(CdeDetails cdeDetails, String vmIdSeq) {
		List<ValueMeaningUiModel> vmUiModelList = new ArrayList<ValueMeaningUiModel>();
		Map<String, List<String>> vmAltNamesPvMeaningMap =  new HashMap<String, List<String>>();
		List<String> altNameList = new ArrayList<String>();
		List<String> pvMeanList = new ArrayList<String>();
		if (cdeDetails.getValueDomain()!=null) {
			if (cdeDetails.getValueDomain().getValueMeaning()!=null) {
				vmUiModelList = cdeDetails.getValueDomain().getValueMeaning();
					for (ValueMeaningUiModel vm : vmUiModelList) {
						if (vm.getVmIdseq().equalsIgnoreCase(vmIdSeq)) {							
							if (vm.getAlternateNames()!=null) {
								for (AlternateNameUiModel altName : vm.getAlternateNames()) {
									altNameList.add(cleanUtfString(altName.getName()));
								}
							} 
							if (vm.getPvMeaning()!=null) {
								pvMeanList.add(cleanUtfString(vm.getPvMeaning()));
							}
						}
					}								
			}
		}
		vmAltNamesPvMeaningMap.put(alternateNames_key, altNameList);
		vmAltNamesPvMeaningMap.put(vmPvMeanings_key, pvMeanList);
		return vmAltNamesPvMeaningMap;		
	}	
	
	
	/**
	 * Cleaning the string for any potential occurrence of NBSP characters such as 'c2A0'
	 * @param String
	 * @return String
	 */			
	protected static String cleanStringforNbsp (String textToBeCleaned) {
		textToBeCleaned = textToBeCleaned.replaceAll(regex_nbsp_space, " "); 
		return textToBeCleaned;
	}
	
	
	/**
	 * Creating a Single concatenation of allowable CDE text choices
	 * @param pvVmList
	 * @return String
	 */				
	protected static String createAllowableTextChoices (List<String> pvVmList) {
		StringBuffer allowableVmTextChoices = new StringBuffer();
		// Building a list of Allowable CDE text choices (in case of a 'Not match' for PV checker)
		for (String altName : pvVmList) {
			if (allowableVmTextChoices.length() > 0)
				allowableVmTextChoices.append("|"+altName);
			else
				allowableVmTextChoices.append(altName);
		}
		return allowableVmTextChoices.toString();
	}	
	
	/**
	 * Finds a given pattern and replaces it with a replacement string
	 * @param stringWithPattern
	 * @param patternToReplace
	 * @param replacement
	 * @return String
	 */				
	protected static String replacePattern (String stringWithPattern, String patternToReplace, String replacement) {
		Pattern pattern = Pattern.compile(patternToReplace);
		Matcher matcher;		
		matcher = pattern.matcher(stringWithPattern);

		if (matcher.find()) {
			stringWithPattern = matcher.replaceAll(replacement);
		}
		return stringWithPattern;
	}		
	
	/**
	 * Identifies UTF characters to replace them with the appropriate character
	 * @param stringToClean
	 * @return String
	 */					
	protected static String cleanUtfString (String stringToClean) {
		Map<String, String> utfPatternReplacement = new HashMap<String, String>();
		utfPatternReplacement.put(regex_nbsp_space, space_str);
		utfPatternReplacement.put(regex_inverted_qm, apostrophe_str);
		utfPatternReplacement.put(regex_super_2, superscript_2_str);
		for (String patternToReplace : utfPatternReplacement.keySet()) {
			replacePattern(stringToClean, patternToReplace, utfPatternReplacement.get(patternToReplace));
		}
		return stringToClean;
	}			

}