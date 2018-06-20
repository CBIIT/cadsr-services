package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nci.cadsr.data.CCCForm;

public class CCCReport {
	
String reportOwner;
String reportDate;
String raveProtocolName;
String raveProtocolNumber;

int totalFormsCount;
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

// NCI standard questions requirement
int countNciCongruent;
int countNciMissing;
int countNciwWithWarnings;
int countNciWithErrors;

// NRDS mandatory questions requirement
int countNrdsCongruent;
int countNrdsMissing;
int countNrdsWithWarnings;
int countNrdsWithErrors;

List<CCCForm> cccForms = new ArrayList<CCCForm>();
List<NrdsCde> nrdsCdeList = new ArrayList<NrdsCde>();
List<StandardCrfCde> standardCrfCdeList = new ArrayList<StandardCrfCde>();

CCCError cccError = new CCCError();

public String getReportOwner() {
	return reportOwner;
}

public void setReportOwner(String reportOwner) {
	this.reportOwner = reportOwner;
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

public int getCountNciMissing() {
	return countNciMissing;
}

public void setCountNciMissing(int countNciMissing) {
	this.countNciMissing = countNciMissing;
}

public int getCountNciCongruent() {
	return countNciCongruent;
}

public void setCountNciCongruent(int countNciCongruent) {
	this.countNciCongruent = countNciCongruent;
}

public int getCountNciwWithWarnings() {
	return countNciwWithWarnings;
}

public void setCountNciwWithWarnings(int countNciwWithWarnings) {
	this.countNciwWithWarnings = countNciwWithWarnings;
}

public int getCountNciWithErrors() {
	return countNciWithErrors;
}

public void setCountNciWithErrors(int countNciWithErrors) {
	this.countNciWithErrors = countNciWithErrors;
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

public List<StandardCrfCde> getStandardCrfCdeList() {
	return standardCrfCdeList;
}

public void setStandardCrfCdeList(List<StandardCrfCde> standardCrfCdeList) {
	this.standardCrfCdeList = standardCrfCdeList;
}

public CCCError getCccError() {
	return cccError;
}

public void setCccError(CCCError cccError) {
	this.cccError = cccError;
}

}
