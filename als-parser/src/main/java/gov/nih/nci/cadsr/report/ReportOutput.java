package gov.nih.nci.cadsr.report;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.CCCReport;

public interface ReportOutput {

	/**
	 * @ALSData
	 * @return Populates the CDE Congruency Checker Report object
	 * 
	 */
	public CCCReport getFinalReportOutput(ALSData alsData) throws NullPointerException;

}
