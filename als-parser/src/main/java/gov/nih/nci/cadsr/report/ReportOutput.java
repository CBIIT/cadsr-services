/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.report;

import java.util.List;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.CCCReport;

public interface ReportOutput {

	/**
	 * @ALSData
	 * @return Populates the CDE Congruency Checker Report object
	 * 
	 */
	public CCCReport getFinalReportData(ALSData alsData, List<String> selForms, Boolean checkUom, Boolean checkStdCrfCde, Boolean displayExceptionDetails) throws NullPointerException;

}
