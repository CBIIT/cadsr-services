/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.parser;

import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import gov.nih.nci.cadsr.data.ALSData;

public interface Parser { 

	/**
	 * Parsing an ALS input file into data objects for validating against the caDSR database
	 * 
	 */
	public ALSData parse (String INPUT_XLSX_FILE_PATH) throws IOException, InvalidFormatException, NullPointerException;

}
