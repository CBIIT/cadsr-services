/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.report;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.CCCReport;

public interface ReportOutput {

	/**
	 * @ALSData
	 * @return Populates the CDE Congruency Checker Report object
	 * 
	 */
	public CCCReport getFinalReportData(ALSData alsData) throws NullPointerException;

}
