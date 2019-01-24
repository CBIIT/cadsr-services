/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import gov.nih.nci.cadsr.cchecker.ALSDataWrapper;

public interface ServiceParser {
	ALSDataWrapper submitPostRequestParser(String saveAbsPath, String urlString);
}
