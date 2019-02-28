/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.util.List;

public interface ServiceValidator {
	StringResponseWrapper sendPostRequestValidator(List<String> selForms, String idseq, boolean checkUom, boolean checkCrf, boolean displayExceptions);
}
