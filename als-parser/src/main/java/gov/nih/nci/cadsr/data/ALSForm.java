/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

import java.util.ArrayList;
import java.util.List;

public class ALSForm {
	
String formOId;
int ordinal;
String draftFormName;


List<ALSField> fields = new ArrayList<ALSField>();

public String getFormOId() {
	return formOId;
}
public void setFormOId(String formOId) {
	this.formOId = formOId;
}
public int getOrdinal() {
	return ordinal;
}
public void setOrdinal(int ordinal) {
	this.ordinal = ordinal;
}
public String getDraftFormName() {
	return draftFormName;
}
public void setDraftFormName(String draftFormName) {
	this.draftFormName = draftFormName;
}
public List<ALSField> getFields() {
	return fields;
}
public void setFields(List<ALSField> fields) {
	this.fields = fields;
}

}
