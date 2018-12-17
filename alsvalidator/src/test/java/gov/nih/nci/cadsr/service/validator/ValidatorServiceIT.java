package gov.nih.nci.cadsr.service.validator;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gov.nih.nci.cadsr.data.CCCQuestion;
import gov.nih.nci.cadsr.service.model.cdeData.CdeDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.DataElement;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.DataElementDetails;
import gov.nih.nci.cadsr.service.model.cdeData.dataElement.OtherVersion;

/**
 * Integration Tests for ValidateService
 *
 */
public class ValidatorServiceIT {
	
	private static final String retiredArchivedStatus = "RETIRED ARCHIVED";
	private static final String retiredPhasedOutStatus = "RETIRED PHASED OUT";
	private static final String retiredWithdrawnStatus = "RETIRED WITHDRAWN";
	private static final String errMsg1 = "CDE has been retired.";
	private static final String errMsg2 = "Newer version of CDE exists: {%.1f}.";	
	private static final String errMsg3 = "Value domain Max Length too short. PVs MaxLength is {%d} , caDSR MaxLength is {%d}.";
	CCCQuestion question;
	CdeDetails cdeDetails;
	DataElement de;
	DataElementDetails deDetails; 	

	public void init() {
		question = new CCCQuestion();
		cdeDetails = new CdeDetails();
		de = new DataElement();
		deDetails = new DataElementDetails();
	}

	
	/**
	 * Checking if the given CDE is one of the three Retired Statuses
	 * @param errMsg
	 * @param status
	 */
	public void invokeCheckCdeRetiredStatus(String errMsg, String status) {
		deDetails.setWorkflowStatus(status);
		de.setDataElementDetails(deDetails);
		cdeDetails.setDataElement(de);
		CCCQuestion actualResult = ValidatorService.checkCdeRetired(cdeDetails, question);
		assertEquals(errMsg, actualResult.getMessage());
	}	
		
	/**
	 * Testing for CDE RETIRED ARCHIVED status
	 */	
	@Test
	public void testCheckCdeRetiredArchived () {
		init();
		invokeCheckCdeRetiredStatus(errMsg1, retiredArchivedStatus);
	}	
	
	/**
	 * Testing for CDE RETIRED PHASED OUT status
	 */	
	@Test
	public void testCheckCdeRetiredPhasedOut () {
		init();
		invokeCheckCdeRetiredStatus(errMsg1, retiredPhasedOutStatus);
	}		
	
	/**
	 * Testing for CDE RETIRED WITHDRAWN status
	 */		
	@Test
	public void testCheckCdeRetiredWithdrawn () {
		init();
		invokeCheckCdeRetiredStatus(errMsg1, retiredWithdrawnStatus);
	}
	
	/**
	 * Testing comparison of CDE versions - where the ALS doesn't have the latest version of the CDE
	 */				
	@Test
	public void testCheckCdeVersions1() {
		init();
		Float latestVersion = new Float("5.0");
		question.setCdeVersion("4.0");
		OtherVersion otherVersion = new OtherVersion();
		otherVersion.setVersion(latestVersion);
		List<OtherVersion> otherVersions = new ArrayList<OtherVersion>();
		otherVersions.add(otherVersion);
		de.setOtherVersions(otherVersions);
		cdeDetails.setDataElement(de);
		CCCQuestion actualResult = ValidatorService.checkCdeVersions(cdeDetails, question);
		assertEquals(String.format(errMsg2, latestVersion), actualResult.getMessage());
	}
	
	/**
	 * Testing comparison of CDE versions - where the ALS has the latest version of the CDE
	 */	
	@Test
	public void testCheckCdeVersions2() {
		init();
		Float latestVersion = new Float("1.0");
		question.setCdeVersion("1.0");
		OtherVersion otherVersion = new OtherVersion();
		otherVersion.setVersion(latestVersion);
		List<OtherVersion> otherVersions = new ArrayList<OtherVersion>();
		otherVersions.add(otherVersion);
		de.setOtherVersions(otherVersions);
		cdeDetails.setDataElement(de);
		CCCQuestion actualResult = ValidatorService.checkCdeVersions(cdeDetails, question);
		assertEquals(null, actualResult.getMessage());
	}
	
	/**
 	 *	Compare to ALS.Fields.FixedUnit, and compare to Value Domain PermissibleValue.Value.  
 	 *	If the caDSR VD MaximumLengthNumber is less than the longest PermissibleValue.Value, 
 	 *	report "caDSR Max Length too short. "PVs MaxLength X, caDSR MaxLength X" 
	 * 
	 * Testing checking of CDE max length - PV length is more than VD max length
	 */		
	@Test
	public void testCheckCdeMaxLength1() {
		init();
		List<Object> errorVal = new ArrayList<Object>();
		errorVal.add(10);
		errorVal.add(5);
		String expectedMessage = String.format(errMsg3, errorVal.toArray());
		CCCQuestion actualResult = ValidatorService.checkCdeMaxLength(question, 10, 5, 10);
		assertEquals (expectedMessage, actualResult.getMessage());
	}		
	
	/**
 	 *	Compare to ALS.Fields.FixedUnit, and compare to Value Domain PermissibleValue.Value.  
 	 *	If the caDSR VD MaximumLengthNumber is less than the longest PermissibleValue.Value, 
 	 *	report "caDSR Max Length too short. "PVs MaxLength X, caDSR MaxLength X" 
	 * 
	 * Testing checking of CDE max length - PV length is within range of VD max length
	 */		
	@Test
	public void testCheckCdeMaxLength2() {
		init();
		String expectedMessage = null;
		CCCQuestion actualResult = ValidatorService.checkCdeMaxLength(question, 5, 10, 10);
		assertEquals (expectedMessage, actualResult.getMessage());
	}


}
