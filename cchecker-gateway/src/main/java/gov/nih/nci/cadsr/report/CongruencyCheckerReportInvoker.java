/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.report;

import org.apache.log4j.Logger;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.CCCReport;
import gov.nih.nci.cadsr.report.impl.GenerateReport;

public class CongruencyCheckerReportInvoker {
		
	private static final Logger logger = Logger.getLogger(CongruencyCheckerReportInvoker.class);
	
	public static CCCReport builTestReport(ALSData alsData) {
		CCCReport cccReport = new CCCReport();

		cccReport  = new GenerateReport().getFinalReportData(alsData);
		return cccReport;
	}
	

}
