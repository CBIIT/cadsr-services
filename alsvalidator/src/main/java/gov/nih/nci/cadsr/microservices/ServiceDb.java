/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import gov.nih.nci.cadsr.data.CCCReport;

public interface ServiceDb {
	StringResponseWrapper submitPostRequestSaveReportError(CCCReport data, String idseq, String urlStr);
}
