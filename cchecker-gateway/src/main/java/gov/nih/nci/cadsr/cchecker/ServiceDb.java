/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import gov.nih.nci.cadsr.data.ALSData;

public interface ServiceDb {
	StringResponseWrapper submitPostRequestSaveAls(ALSData alsData, String idSeq, String urlStr);
}
