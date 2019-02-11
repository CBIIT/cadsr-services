/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.CCCReport;

public interface ServiceDb {
	StringResponseWrapper submitPostRequestSaveAls(ALSData alsData, String idSeq, String urlStr);
	StringResponseWrapper submitPostRequestSaveReportError(CCCReport data, String idseq, String urlStr);
}
