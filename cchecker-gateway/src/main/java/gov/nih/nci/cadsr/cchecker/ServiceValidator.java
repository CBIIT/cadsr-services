/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.util.List;

import gov.nih.nci.cadsr.data.CCCReport;

public interface ServiceValidator {
	CCCReport sendPostRequestValidator(List<String> selForms, String idseq, boolean checkUom, boolean checkCrf, boolean displayExceptions);
}
