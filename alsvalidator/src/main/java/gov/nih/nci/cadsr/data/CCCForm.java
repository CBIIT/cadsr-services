/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class CCCForm {	

String raveFormOId;
String caDSRFormId;
int countTotalQuestions;
String version;
//String fieldOrder;

List<CCCQuestion> questions = new ArrayList<CCCQuestion>();

public String getRaveFormOId() {
	return raveFormOId;
}
public void setRaveFormOId(String raveFormOId) {
	this.raveFormOId = raveFormOId;
}
public String getCaDSRFormId() {
	return caDSRFormId;
}
public void setCaDSRFormId(String caDSRFormId) {
	this.caDSRFormId = caDSRFormId;
}
public int getCountTotalQuestions() {
	return countTotalQuestions;
}
public void setCountTotalQuestions(int countTotalQuestions) {
	this.countTotalQuestions = countTotalQuestions;
}
public String getVersion() {
	return version;
}
public void setVersion(String version) {
	this.version = version;
}
public List<CCCQuestion> getQuestions() {
	return questions;
}
public void setQuestions(List<CCCQuestion> questions) {
	this.questions = questions;
}	

}
