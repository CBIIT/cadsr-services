/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;
import gov.nih.nci.cadsr.data.CCCForm;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CCCReport {
	
String reportOwner;
String reportDate;
String fileName;
String raveProtocolName;
String raveProtocolNumber;

int totalFormsCount;
int totalFormsCong;
int countQuestionsChecked;
int countCongruentQuestions;
int countQuestionsWithWarnings;
int countQuestionsWithErrors;
int countQuestionsWithoutCde;


// Comments from DetailedRequirements about the following fields

/*  The caDSR team can provide a list of the CDEs that are each including the CDE ID Version and the CRF ID Version 
 *  for these form categories "Mandatory Module", "Conditional" "Optional" "NRDS". Might want to make this list read form a table 
 *  because while it is mostly stable, it can change over time.
 */

// NCI standard Mandatory questions requirement
int countManCrfCongruent;
int countManCrfMissing;
int countManCrfwWithWarnings;
int countManCrfWithErrors;

//NCI standard Optional questions requirement
int countOptCrfCongruent;
int countOptCrfMissing;
int countOptCrfwWithWarnings;
int countOptCrfWithErrors;

//NCI standard Conditional questions requirement
int countCondCrfCongruent;
int countCondCrfMissing;
int countCondCrfwWithWarnings;
int countCondCrfWithErrors;

// NRDS mandatory questions requirement
int countNrdsCongruent;
int countNrdsMissing;
int countNrdsWithWarnings;
int countNrdsWithErrors;

// FORMBUILD-636 
int countNciCongruent;
int countNciMissing;
int countNciWithWarnings;
int countNciWithErrors;

//FORMBUILD-621
//CDASH and SDTM classified missing
int countCdashMissing;
int countSdtmMissing;

//FORMBUILD-636
/*
Required CDASH x.x Questions with warnings
Required CDASH x.x Questions with errors.
Required SDTM x.x Questions with warnings
Required SDTM x.x Questions with errors.
 */
int countCdashWithWarnings;
int countCdashWithErrors;
int countSdtmWithWarnings;
int countSdtmWithErrors;

List<CCCForm> cccForms = new ArrayList<CCCForm>();
int selectedFormsCount = 0;
List<NrdsCde> nrdsCdeList = new ArrayList<NrdsCde>();
List<NrdsCde> missingNrdsCdeList = new ArrayList<NrdsCde>();
List<CdeMissing> missingCdashCdeList = new ArrayList<>();
List<CdeMissing> missingSdtmCdeList = new ArrayList<>();
List<StandardCrfCde> missingStandardCrfCdeList = new ArrayList<StandardCrfCde>();

Boolean isCheckStdCrfCdeChecked;

CCCError cccError = new CCCError();

public String getReportOwner() {
	return reportOwner;
}

public void setReportOwner(String reportOwner) {
	this.reportOwner = reportOwner;
}

public String getFileName() {
	return fileName;
}
public void setFileName(String fileName) {
	this.fileName = fileName;
}

public String getReportDate() {
	return reportDate;
}

public void setReportDate(String reportDate) {
	this.reportDate = reportDate;
}

public String getRaveProtocolName() {
	return raveProtocolName;
}

public void setRaveProtocolName(String raveProtocolName) {
	this.raveProtocolName = raveProtocolName;
}

public String getRaveProtocolNumber() {
	return raveProtocolNumber;
}

public void setRaveProtocolNumber(String raveProtocolNumber) {
	this.raveProtocolNumber = raveProtocolNumber;
}

public int getTotalFormsCount() {
	return totalFormsCount;
}

public void setTotalFormsCount(int totalFormsCount) {
	this.totalFormsCount = totalFormsCount;
}

public int getTotalFormsCong() {
	return totalFormsCong;
}

public void setTotalFormsCong(int totalFormsCong) {
	this.totalFormsCong = totalFormsCong;
}

public int getCountQuestionsChecked() {
	return countQuestionsChecked;
}

public void setCountQuestionsChecked(int countQuestionsChecked) {
	this.countQuestionsChecked = countQuestionsChecked;
}

public int getCountCongruentQuestions() {
	return countCongruentQuestions;
}

public void setCountCongruentQuestions(int countCongruentQuestions) {
	this.countCongruentQuestions = countCongruentQuestions;
}

public int getCountQuestionsWithWarnings() {
	return countQuestionsWithWarnings;
}

public void setCountQuestionsWithWarnings(int countQuestionsWithWarnings) {
	this.countQuestionsWithWarnings = countQuestionsWithWarnings;
}

public int getCountQuestionsWithErrors() {
	return countQuestionsWithErrors;
}

public void setCountQuestionsWithErrors(int countQuestionsWithErrors) {
	this.countQuestionsWithErrors = countQuestionsWithErrors;
}

public int getCountQuestionsWithoutCde() {
	return countQuestionsWithoutCde;
}

public void setCountQuestionsWithoutCde(int countQuestionsWithoutCde) {
	this.countQuestionsWithoutCde = countQuestionsWithoutCde;
}

public int getCountManCrfCongruent() {
	return countManCrfCongruent;
}

public void setCountManCrfCongruent(int countManCrfCongruent) {
	this.countManCrfCongruent = countManCrfCongruent;
}

public int getCountManCrfMissing() {
	return countManCrfMissing;
}

public void setCountManCrfMissing(int countManCrfMissing) {
	this.countManCrfMissing = countManCrfMissing;
}

public int getCountManCrfwWithWarnings() {
	return countManCrfwWithWarnings;
}

public void setCountManCrfwWithWarnings(int countManCrfwWithWarnings) {
	this.countManCrfwWithWarnings = countManCrfwWithWarnings;
}

public int getCountManCrfWithErrors() {
	return countManCrfWithErrors;
}

public void setCountManCrfWithErrors(int countManCrfWithErrors) {
	this.countManCrfWithErrors = countManCrfWithErrors;
}

public int getCountOptCrfCongruent() {
	return countOptCrfCongruent;
}

public void setCountOptCrfCongruent(int countOptCrfCongruent) {
	this.countOptCrfCongruent = countOptCrfCongruent;
}

public int getCountOptCrfMissing() {
	return countOptCrfMissing;
}

public void setCountOptCrfMissing(int countOptCrfMissing) {
	this.countOptCrfMissing = countOptCrfMissing;
}

public int getCountOptCrfwWithWarnings() {
	return countOptCrfwWithWarnings;
}

public void setCountOptCrfwWithWarnings(int countoptCrfwWithWarnings) {
	this.countOptCrfwWithWarnings = countoptCrfwWithWarnings;
}

public int getCountOptCrfWithErrors() {
	return countOptCrfWithErrors;
}

public void setCountOptCrfWithErrors(int countOptCrfWithErrors) {
	this.countOptCrfWithErrors = countOptCrfWithErrors;
}

public int getCountCondCrfCongruent() {
	return countCondCrfCongruent;
}

public void setCountCondCrfCongruent(int countCondCrfCongruent) {
	this.countCondCrfCongruent = countCondCrfCongruent;
}

public int getCountCondCrfMissing() {
	return countCondCrfMissing;
}

public void setCountCondCrfMissing(int countCondCrfMissing) {
	this.countCondCrfMissing = countCondCrfMissing;
}

public int getCountCondCrfwWithWarnings() {
	return countCondCrfwWithWarnings;
}

public void setCountCondCrfwWithWarnings(int countCondCrfwWithWarnings) {
	this.countCondCrfwWithWarnings = countCondCrfwWithWarnings;
}

public int getCountCondCrfWithErrors() {
	return countCondCrfWithErrors;
}

public void setCountCondCrfWithErrors(int countCondCrfWithErrors) {
	this.countCondCrfWithErrors = countCondCrfWithErrors;
}

public int getCountNrdsCongruent() {
	return countNrdsCongruent;
}

public void setCountNrdsCongruent(int countNrdsCongruent) {
	this.countNrdsCongruent = countNrdsCongruent;
}

public int getCountNrdsMissing() {
	return countNrdsMissing;
}

public void setCountNrdsMissing(int countNrdsMissing) {
	this.countNrdsMissing = countNrdsMissing;
}

public int getCountNrdsWithWarnings() {
	return countNrdsWithWarnings;
}

public void setCountNrdsWithWarnings(int countNrdsWithWarnings) {
	this.countNrdsWithWarnings = countNrdsWithWarnings;
}

public int getCountNrdsWithErrors() {
	return countNrdsWithErrors;
}

public void setCountNrdsWithErrors(int countNrdsWithErrors) {
	this.countNrdsWithErrors = countNrdsWithErrors;
}

// FORMBUILD-636
public int getCountNciCongruent() {
	return countNciCongruent;
}

public void setCountNciCongruent(int countNciCongruent) {
	this.countNciCongruent = countNciCongruent;
}

public int getCountNciMissing() {
	return countNciMissing;
}

public void setCountNciMissing(int countNciMissing) {
	this.countNciMissing = countNciMissing;
}

public int getCountNciWithWarnings() {
	return countNciWithWarnings;
}

public void setCountNciWithWarnings(int countNciWithWarnings) {
	this.countNciWithWarnings = countNciWithWarnings;
}

public int getCountNciWithErrors() {
	return countNciWithErrors;
}

public void setCountNciWithErrors(int countNciWithErrors) {
	this.countNciWithErrors = countNciWithErrors;
}

//FORMBUILD-621
public int getCountCdashMissing() {
	return countCdashMissing;
}

public void setCountCdashMissing(int countCdashMissing) {
	this.countCdashMissing = countCdashMissing;
}

public int getCountSdtmMissing() {
	return countSdtmMissing;
}

public void setCountSdtmMissing(int countSdtmMissing) {
	this.countSdtmMissing = countSdtmMissing;
}
//FORMBUILD-636
public int getCountCdashWithWarnings() {
	return countCdashWithWarnings;
}

public void setCountCdashWithWarnings(int countCdashWithWarnings) {
	this.countCdashWithWarnings = countCdashWithWarnings;
}

public int getCountCdashWithErrors() {
	return countCdashWithErrors;
}

public void setCountCdashWithErrors(int countCdashWithErrors) {
	this.countCdashWithErrors = countCdashWithErrors;
}

public int getCountSdtmWithWarnings() {
	return countSdtmWithWarnings;
}

public void setCountSdtmWithWarnings(int countSdtmWithWarnings) {
	this.countSdtmWithWarnings = countSdtmWithWarnings;
}

public int getCountSdtmWithErrors() {
	return countSdtmWithErrors;
}

public void setCountSdtmWithErrors(int countSdtmWithErrors) {
	this.countSdtmWithErrors = countSdtmWithErrors;
}

public List<CCCForm> getCccForms() {
	return cccForms;
}

public void setCccForms(List<CCCForm> cccForms) {
	this.cccForms = cccForms;
}

public List<NrdsCde> getNrdsCdeList() {
	return nrdsCdeList;
}

public void setNrdsCdeList(List<NrdsCde> nrdsCdeList) {
	this.nrdsCdeList = nrdsCdeList;
}

public List<NrdsCde> getMissingNrdsCdeList() {
	return missingNrdsCdeList;
}

public void setMissingNrdsCdeList(List<NrdsCde> missingNrdsCdeList) {
	this.missingNrdsCdeList = missingNrdsCdeList;
}

public List<CdeMissing> getMissingCdashCdeList() {
	return missingCdashCdeList;
}

public void setMissingCdashCdeList(List<CdeMissing> missingCdashCdeList) {
	this.missingCdashCdeList = missingCdashCdeList;
}

public List<CdeMissing> getMissingSdtmCdeList() {
	return missingSdtmCdeList;
}

public void setMissingSdtmCdeList(List<CdeMissing> missingSdtmCdeList) {
	this.missingSdtmCdeList = missingSdtmCdeList;
}

public List<StandardCrfCde> getMissingStandardCrfCdeList() {
	return missingStandardCrfCdeList;
}

public void setMissingStandardCrfCdeList(List<StandardCrfCde> missingStandardCrfCdeList) {
	this.missingStandardCrfCdeList = missingStandardCrfCdeList;
}

public Boolean getIsCheckStdCrfCdeChecked() {
	return isCheckStdCrfCdeChecked;
}

public void setIsCheckStdCrfCdeChecked(Boolean isCheckStdCrfCdeChecked) {
	this.isCheckStdCrfCdeChecked = isCheckStdCrfCdeChecked;
}

public int getSelectedFormsCount() {
	return selectedFormsCount;
}

public void setSelectedFormsCount(int selectedFormsCount) {
	this.selectedFormsCount = selectedFormsCount;
}

public CCCError getCccError() {
	return cccError;
}

public void setCccError(CCCError cccError) {
	this.cccError = cccError;
}

}
