/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.service.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

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
			if (cdeDetails.getDataElement()!=null) {
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
			}
			if (!rdDocTextList.isEmpty() && rdDocTextList.contains(question.getRaveFieldLabel())) {
				question.setRaveFieldLabelResult(matchString);
			} else {
				question.setRaveFieldLabelResult(errorString);
				question.setQuestionCongruencyStatus(congStatus_errors);
			}		
			question.setCdePermitQuestionTextChoices(rdDocs.toString());
			if (question.getRaveControlType()!=null && cdeDetails.getValueDomain()!=null) {
				if (question.getRaveControlType().equals(cdeDetails.getValueDomain().getValueDomainDetails().getDataType())) {
					question.setControlTypeResult(matchString);
				} else {
					question.setControlTypeResult(errorString);
					question.setQuestionCongruencyStatus(congStatus_errors);
				}
			} else {
				question.setControlTypeResult(errorString);
				question.setQuestionCongruencyStatus(congStatus_errors);				
			}
			List<String> pvList = new ArrayList<String>();
			int pvMaxLen = 0;
			int vdMaxLen = 0;
			if (cdeDetails.getValueDomain()!=null) {
				vdMaxLen = cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength();
				for (PermissibleValuesModel pv : cdeDetails.getValueDomain().getPermissibleValues()) {
					pvList.add(pv.getValue());
					if (pv.getValue().length() > pvMaxLen)
						pvMaxLen = pv.getValue().length();
				}
			}
			List<String> cdResult = new ArrayList<String>();
			if (!pvList.isEmpty()) {
				for (String codedData : question.getRaveCodedData()) {
					if (pvList.contains(codedData)) {
						cdResult.add(matchString);
						logger.debug("CodedData: "+codedData+" PV list size: "+pvList.size());
					} else {
						cdResult.add(errorString);
						question.setQuestionCongruencyStatus(congStatus_errors);
					}
				}
				if (!cdResult.isEmpty())
					question.setCodedDataResult(cdResult);
				if (field.getDataFormat().indexOf("$") > -1 
				&& characterDataFormats.contains(cdeDetails.getValueDomain().getValueDomainDetails().getDataType()))  {
					question.setDatatypeCheckerResult(matchString);
				} else {
					question.setDatatypeCheckerResult(errorString);
					question.setQuestionCongruencyStatus(congStatus_errors);
				}
			} else {
				question.setDatatypeCheckerResult(errorString);
				question.setQuestionCongruencyStatus(congStatus_errors);
			}
			if (cdeDetails.getValueDomain()!=null) {
				if (field.getDataFormat().indexOf("$") == -1 
						&& numericDataFormats.contains(cdeDetails.getValueDomain().getValueDomainDetails().getDataType()))  {
					question.setDatatypeCheckerResult(matchString);
				} else {
					question.setDatatypeCheckerResult(errorString);
					question.setQuestionCongruencyStatus(congStatus_errors);
				} 
			} else {
				if (field.getDataFormat()!=null) {
					question.setDatatypeCheckerResult(errorString);
					question.setQuestionCongruencyStatus(congStatus_errors);
				}
			}
			if (question.getRaveUOM()!=null) {
				if (cdeDetails.getValueDomain()!=null) {
					if (question.getRaveUOM().equals(cdeDetails.getValueDomain().getValueDomainDetails().getUnitOfMeasure())) {
						question.setUomCheckerResult(matchString);
					} else {
						question.setUomCheckerResult(warningString);
						question.setQuestionCongruencyStatus(congStatus_warn);
						question.setCdeUOM(cdeDetails.getValueDomain().getValueDomainDetails().getUnitOfMeasure());
					}
				} else {
					question.setUomCheckerResult(warningString);
					question.setQuestionCongruencyStatus(congStatus_warn);
					question.setCdeUOM("");
				}
			}
			
			String raveLength = question.getRaveLength();
			logger.debug("PV list:  "+pvList.size()+" PV Max Length: "+pvMaxLen+" VD Max Length: "+vdMaxLen+" RAVE Length:  "+raveLength+" RAVE UOM: "+question.getRaveUOM());
			if (raveLength!=null && raveLength.indexOf("characters")>-1) {
				raveLength.replaceAll("\\p{P}","");		
				int index = 0;
				if ((raveLength.indexOf("(") > -1) || (raveLength.indexOf(")") > -1)) {
					index = 1;
				}  else {
					index = 0;
				}
				raveLength = raveLength.substring(index,raveLength.indexOf("characters"));
				if (cdeDetails.getValueDomain()!=null) {
					for (PermissibleValuesModel pv : cdeDetails.getValueDomain().getPermissibleValues())
						logger.debug("pv value: "+pv.getValue());
					if (Float.valueOf(raveLength) < Float.valueOf(cdeDetails.getValueDomain().getValueDomainDetails().getMaximumLength())) {
						question.setLengthCheckerResult(matchString);
					} else {
						question.setLengthCheckerResult(warningString);
						question.setQuestionCongruencyStatus(congStatus_warn);	
					}
				} else {
					question.setLengthCheckerResult(warningString);
					question.setQuestionCongruencyStatus(congStatus_warn);	
				}
			} 
			if (field.getDataFormat()!=null) {
				if (cdeDetails.getValueDomain()!=null) {
					if (field.getDataFormat().equals(cdeDetails.getValueDomain().getValueDomainDetails().getDisplayFormat())) {
						question.setFormatCheckerResult(matchString);
					} else {
						question.setFormatCheckerResult(warningString);
						question.setQuestionCongruencyStatus(congStatus_warn);
					} 
				} else {
					question.setFormatCheckerResult(warningString);
					question.setQuestionCongruencyStatus(congStatus_warn);
				}
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
	
}
