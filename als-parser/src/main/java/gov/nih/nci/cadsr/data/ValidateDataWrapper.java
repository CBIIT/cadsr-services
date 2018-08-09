/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;

public class ValidateDataWrapper {
	
ALSField field;
CCCQuestion question;
CdeDetails cdeDetails;

public ALSField getField() {
	return field;
}
public void setField(ALSField field) {
	this.field = field;
}
public CCCQuestion getQuestion() {
	return question;
}
public void setQuestion(CCCQuestion question) {
	this.question = question;
}
public CdeDetails getCdeDetails() {
	return cdeDetails;
}
public void setCdeDetails(CdeDetails cdeDetails) {
	this.cdeDetails = cdeDetails;
}


}
