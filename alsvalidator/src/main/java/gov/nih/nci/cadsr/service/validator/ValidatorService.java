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

import org.apache.commons.lang3.math.NumberUtils;
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
	private static final String notCheckedString = "NOT CHECKED";
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
	private static String congStatus_congruent = "CONGRUENT";	
	private static List<String> characterDataFormats = Arrays.asList("ALPHANUMERIC", "ALPHA DVG", "CHARACTER", "JAVA.LANG.CHARACTER", "JAVA.LANG.STRING", 
			"NUMERIC ALPHA DVG", "XSD:STRING");
	private static List<String> numericDataFormats = Arrays.asList("NUMBER", "INTEGER", "JAVA.LANG.INTEGER", "XSD:INTEGER");
	private static List<String> dateDataFormats = Arrays.asList("DATE", "XSD:DATE");
	private static List<String> timeDataFormats = Arrays.asList("TIME", "XSD:TIME");
	private static final String characters_string = "characters";
	private static final String patternHolderChar = "d";
	private static final String patternHolderNum = "9";
	private static final String regex_nbsp_space = "[\\p{Z}\\s]";//"'\u00A0', '\u2007', '\u202F'" - (NON BREAKING SPACE)
	protected static final String regex_inverted_qm = "[\\u00bf]";// u00BF, 0xbf, U+00BF, c2BF	- ¿	(INVERTED QUESTION MARK)
	protected static final String apostrophe_str = "'";
	protected static final String regex_super_2 = "[\\u00b2]";// u00B2, 0xb2, U+00B2, c2B2	- ²	(SUPERSCRIPT TWO)
	//this character does not appear to work on Linux
	//protected static final String superscript_2_str = "²";
	protected static final String superscript_2_str = "(0xb2)";
	private static final String alternateNames_key = "AlternateNames";
	private static final String vmPvMeanings_key = "PVMeanings";
	private static final List<String> nonEnumList = Arrays.asList("TEXT", "LONGTEXT");

	
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
			question = setRaveControlTypeResult(cdeDetails.getValueDomain().getValueDomainDetails().getValueDomainType(), 
					cdeDetails.getValueDomain().getValueDomainDetails().getDataType(), field.getDataFormat(), question);

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
						String pvVal = cleanStringforNbsp(pv.getValue());
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
			// Not Checking for UOM, based on Customer's feedback
			//question = setUomCheckerResult (question, cdeDetails.getValueDomain().getValueDomainDetails().getUnitOfMeasure());
			question.setCdeUOM(cdeDetails.getValueDomain().getValueDomainDetails().getUnitOfMeasure());
					
			// Comparing RAVE Length (FixedUnit) with the caDSR Value Domain Max length - RAVE Length Checker result
			question = setLengthCheckerResult (question, cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength());
			
			// Comparing RAVE Length (FixedUnit) with the caDSR PVs Max length
			if (vdMaxLen != 0)
				question = checkCdeMaxLength (question, pvMaxLen, vdMaxLen, computeRaveLength(question.getRaveLength()));
			
			//Comparing the ALS RAVE Data Format with caDSR Value Domain Display Format
			question = checkFormatCheckerResult (question, field.getDataFormat(), cdeDetails.getValueDomain().getValueDomainDetails().getDisplayFormat());

			// Setting Question Congruency Status to Congruent if it's not WARNINGS or ERRORS
			if (question.getQuestionCongruencyStatus() == null)
				question.setQuestionCongruencyStatus(congStatus_congruent);
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
				if (rdDocText==null)
					continue;
				// Concatenating the entire list of AQTs and PQTs together 
				if ("Preferred Question Text".equalsIgnoreCase(rd.getDocumentType()) || "Alternate Question Text".equalsIgnoreCase(rd.getDocumentType())) {
					rdDocTextList.add(rdDocText = cleanStringforNbsp(rdDocText));
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
	protected static CCCQuestion setRaveControlTypeResult (String vdType, String vdDataType, String raveDataFormat, CCCQuestion question) {
			question.setCdeValueDomainType(vdType);
			List<Object> errorVal = new ArrayList<Object>();
			errorVal.add(question.getRaveControlType());
			if (vdType!=null) {
				if ("N".equalsIgnoreCase(vdType)) {
					errorVal.add("Non-enumerated"); 
				} else if ("E".equalsIgnoreCase(vdType)) {
					errorVal.add("Enumerated"); 
				} else {
					errorVal.add("Unknown"); 
				}
			} else {
					errorVal.add("Unknown");
			}
			
			/* Compare Rave ControlType to caDSR Value Domain Type
			If caDSR VD is non-enumerated, and the Rave ControlType is "Text", it is valid.  
			If Value Domain is non-enumerated and ControlType is not "Text", then check the Rave Datatype. 
			If Rave Datatype matches caDSR Value Domain datatype, then result is "Match" otherwise 
			it's an error. (we will provide the team with a list of the mappings between the Rave Datatypes 
			and caDSR datatypes ,the names are not the same). */ 
			
			if (question.getRaveControlType()!=null) {
				if (isNonEnumerated(question.getRaveControlType().toUpperCase()) && "N".equalsIgnoreCase(vdType)) {
					question.setControlTypeResult(matchString);
					if (!question.getRaveCodedData().isEmpty()) {
						question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg5, question.getRaveCodedData())));
					}						
				} else if ((!isNonEnumerated(question.getRaveControlType().toUpperCase())) && "E".equalsIgnoreCase(vdType)) {
								question.setControlTypeResult(matchString);
				} else	if (isNonEnumerated(question.getRaveControlType().toUpperCase()) && "E".equalsIgnoreCase(vdType)) {
					question.setControlTypeResult(errorString);						
					question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg7, errorVal.toArray())));
					question.setQuestionCongruencyStatus(congStatus_errors); 					
				} else {					
					String result = compareDataType (raveDataFormat, vdDataType);
					question.setControlTypeResult(result);
					if (errorString.equals(result)) {
							question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg7, errorVal.toArray())));
							question.setQuestionCongruencyStatus(congStatus_errors); 
						}
					// Introducing Not Checked status for those data types that are not part of the 
					// designated data types that will be verified against the CDE
					if (notCheckedString.equals(result) && question.getQuestionCongruencyStatus()==null)
						question.setQuestionCongruencyStatus(congStatus_warn);
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
		Boolean isMatch = true;
		List<String> userDataStringList = question.getRaveUserString();
		List<String> codedDataList = question.getRaveCodedData();
		List<String> allowCdesList = new ArrayList<String>();
		List<String> pvCheckerResultsList = new ArrayList<String>();
		
		/* Compare to PV.ValueMeaning.longName and the matched PV.Value. 
		 The UserDataString must match the PermissibleValues.ValueMeaning.LongName 
		 for the matched CodedData PV Value OR it can be the same as the matched 
		 PV Value (which means the UserDataString =  CodedData ). 
		 It is also compared to all the ValueMeaning Alternate Names.
		 Exceptions: If it does not match the PV Value MEaning or the PV for the CodedData, 
		 or one of the ValueMeaning Alternate Names, "ERROR" */
		
		
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
		
		/* Compare each CodedData value to all of the Value Domain's PermissibleValue.value
			Exceptions: If it does not match one of the CDEs PV Value, "ERROR"
		 */
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
		List<Object> errorVal = new ArrayList<Object>();
		question.setCdeDataType(vdDataType);
		errorVal.add(raveDataFormat);
		errorVal.add(vdDataType);					
		String result = compareDataType (raveDataFormat, vdDataType);
		question.setDatatypeCheckerResult(result);
		if (errorString.equals(result)) {
			question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg11, errorVal.toArray())));
			question.setQuestionCongruencyStatus(congStatus_errors);
		}
		// Introducing Not Checked status for those data types that are not part of the 
		// designated data types that will be verified against the CDE
		if (notCheckedString.equals(result) && question.getQuestionCongruencyStatus()==null)
			question.setQuestionCongruencyStatus(congStatus_warn);
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
			
			/* If the Value Domain Unit of Measure in not null/blank, then check to see 
			   If there is a matching value in FixedUnit or CodedUnit.
			   If it does not match, then display the Rave UOM and the Value Domain UOM and result "WARNING".
			 */
			
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
		
		/* If caDSR VD maxlengthNumber does not match FixedUnit number of characters, 
		display both Rave value and caDSR value and result  "WARNING" */
		
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
		Boolean result = false;
		//if CDE VD has no data format (null or empty), we consider this validation is OK
		if (org.apache.commons.lang3.StringUtils.isBlank(vdDisplayFormat)) {
			result = true;
		}
		else //we compare VD data format with anything received from ALS including empty
		{
			result = vdDisplayFormat.equals(raveDataFormat);
		}
		
		if (result) {
			question.setFormatCheckerResult(matchString);			
		} else {
			question.setFormatCheckerResult(warningString);
			question.setMessage(assignQuestionErrorMessage(question.getMessage(), String.format(msg13, errorVal.toArray())));
			if (question.getQuestionCongruencyStatus()==null)
				question.setQuestionCongruencyStatus(congStatus_warn);
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
		
		/* Compare to ALS.Fields.FixedUnit, and compare to Value Domain PermissibleValue.Value.  
		If the caDSR VD MaximumLengthNumber is less than the longest PermissibleValue.Value, 
		report "caDSR Max Length too short. "PVs MaxLength X, caDSR MaxLength X" */
		
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
		if (raveLength!=null && raveLength.trim().length() > 0 && !"%".equals(raveLength)) {
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
									altNameList.add(cleanStringforNbsp(altName.getName()));
								}
							} 
							if (vm.getPvMeaning()!=null) {
								pvMeanList.add(cleanStringforNbsp(vm.getPvMeaning()));
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
		if (textToBeCleaned!=null)
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
	 * Data Type comparison
	 * @param raveDataFormat
	 * @param vdDataType
	 * @return Boolean
	 * 	Introducing Not Checked status for those data types that are not part of the 
		designated data types that will be verified against the CDE
	 */
	protected static String compareDataType (String raveDataFormat, String vdDataType) {
		Boolean result = false;
		if (raveDataFormat!=null && (raveDataFormat.trim().length() > 0)) {
			if (vdDataType!=null) {
				if (raveDataFormat.startsWith("$")) {
					if (characterDataFormats.contains(vdDataType.toUpperCase())) 
						result = true;
					else if (dateDataFormats.contains(vdDataType.toUpperCase()) || timeDataFormats.contains(vdDataType.toUpperCase())
							|| numericDataFormats.contains(vdDataType.toUpperCase())) {
						result = false;
					} else 
						return notCheckedString; 
				} else if (raveDataFormat.toUpperCase().startsWith("DD") || raveDataFormat.toUpperCase().startsWith("DY")  
						|| raveDataFormat.toUpperCase().startsWith("MM") || raveDataFormat.toUpperCase().startsWith("MON") 
						|| raveDataFormat.toUpperCase().startsWith("YY") || raveDataFormat.toUpperCase().startsWith("YYYY")) {
					if (dateDataFormats.contains(vdDataType.toUpperCase())) 
						result = true;
					else if (characterDataFormats.contains(vdDataType.toUpperCase()) || timeDataFormats.contains(vdDataType.toUpperCase())
							|| numericDataFormats.contains(vdDataType.toUpperCase())) {
						result = false;
					} else 
						return notCheckedString;					
				} else if (raveDataFormat.toUpperCase().startsWith("HH") || raveDataFormat.toUpperCase().startsWith("TIME")) {
					if (timeDataFormats.contains(vdDataType.toUpperCase()))
						result = true;
					else if (characterDataFormats.contains(vdDataType.toUpperCase()) || dateDataFormats.contains(vdDataType.toUpperCase())
							|| numericDataFormats.contains(vdDataType.toUpperCase())) {
						result = false;
					} else 
						return notCheckedString;
				} else if (NumberUtils.isNumber(raveDataFormat)) {
					if (numericDataFormats.contains(vdDataType.toUpperCase()))
						result = true;
					else if (characterDataFormats.contains(vdDataType.toUpperCase()) || dateDataFormats.contains(vdDataType.toUpperCase())
							|| timeDataFormats.contains(vdDataType.toUpperCase())) {
						result = false;
					} else 
						return notCheckedString;
				} else 
					return notCheckedString; 
			}
		} else {
			// Adding 'Not checked' compare for RAVE Data type (Data format) is empty/null in the ALS file
			if (vdDataType!=null && vdDataType.trim().length() > 0) {
				if (characterDataFormats.contains(vdDataType.toUpperCase()) || dateDataFormats.contains(vdDataType.toUpperCase())
						|| timeDataFormats.contains(vdDataType.toUpperCase()) || numericDataFormats.contains(vdDataType.toUpperCase())) {
						result = false;
					} else {
						return notCheckedString;
					}
			}
		}
		if (result)
			return matchString;
		else 
			return errorString;

	}
	
	/**
	 * Checking if a given data type is non-enumerated
	 * @param dataType
	 * @return Boolean
	 */	
	protected static Boolean isNonEnumerated (String dataType) {
		if (nonEnumList.contains(dataType.toUpperCase()))
			return true;
		else
			return false;
	}	
	
}