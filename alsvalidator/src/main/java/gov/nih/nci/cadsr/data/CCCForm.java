/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class CCCForm {	

String raveFormOid;
String formName;
int countTotalQuestions;
int totalQuestionsChecked;
String version;
String congruencyStatus; // Congruent, Errors, Warnings
String formPublicId;
String formVersion;

List<CCCQuestion> questions = new ArrayList<CCCQuestion>();

public String getRaveFormOid() {
	return raveFormOid;
}
public String getFormName() {
	return formName;
}
public void setFormName(String formName) {
	this.formName = formName;
}
public void setRaveFormOid(String raveFormOid) {
	this.raveFormOid = raveFormOid;
}
public int getCountTotalQuestions() {
	return countTotalQuestions;
}
public void setCountTotalQuestions(int countTotalQuestions) {
	this.countTotalQuestions = countTotalQuestions;
}
public int getTotalQuestionsChecked() {
	return totalQuestionsChecked;
}
public void setTotalQuestionsChecked(int totalQuestionsChecked) {
	this.totalQuestionsChecked = totalQuestionsChecked;
}
public String getVersion() {
	return version;
}
public void setVersion(String version) {
	this.version = version;
}
public String getCongruencyStatus() {
	return congruencyStatus;
}
public void setCongruencyStatus(String congruencyStatus) {
	this.congruencyStatus = congruencyStatus;
}
public String getFormPublicId() {
	return formPublicId;
}
public void setFormPublicId(String formPublicId) {
	this.formPublicId = formPublicId;
}
public String getFormVersion() {
	return formVersion;
}
public void setFormVersion(String formVersion) {
	this.formVersion = formVersion;
}
public List<CCCQuestion> getQuestions() {
	return questions;
}
public void setQuestions(List<CCCQuestion> questions) {
	this.questions = questions;
}	

}
