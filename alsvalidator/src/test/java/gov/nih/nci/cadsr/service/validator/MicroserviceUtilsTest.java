package gov.nih.nci.cadsr.service.validator;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import gov.nih.nci.cadsr.service.validator.MicroserviceUtils;

public class MicroserviceUtilsTest {
	final static String ignored = ".,:;\"'?-()";
	@Test
	public void testTwoDifferentStrings() {
		assertFalse("failed on different strings", MicroserviceUtils.compareWithIgnore("cadsr Str", "als Str"));
	}
	@Test
	public void testTwoDifferentStringsWarn() {
		assertTrue("failed on different strings", MicroserviceUtils.compareWithIgnore("Primary Diagnosis Disease Group", "Primary diagnosis disease group:"));
	}	
	@Test
	public void testSamePunct1() {
		assertTrue("failed of different punc 1", MicroserviceUtils.compareWithIgnore("cadsr Str" + ignored, "cadsr Str"));
	}
	@Test
	public void testSamePunct1Both() {
		assertTrue("failed of different Punc both", MicroserviceUtils.compareWithIgnore("cadsr Str"+ ignored,
				ignored + "cadsr Str"));
	}
	@Test
	public void testSamePunct2() {
		assertTrue("failed of different Punc 2", MicroserviceUtils.compareWithIgnore("cadsr Str", 
				ignored + "cadsr Str"));
	}
	@Test
	public void testSamePunct1Case() {
		assertTrue("failed of different punc 1 Case", MicroserviceUtils.compareWithIgnore("cadsr Str" + ignored, "Cadsr str"));
	}
	@Test
	public void testSamePunct1BothCase() {
		assertTrue("failed of different Punc both Case", MicroserviceUtils.compareWithIgnore("cadsr Str"+ ignored,
				ignored + "Cadsr str"));
	}
	@Test
	public void testSamePunct2Case() {
		assertTrue("failed of different Punc 2 Case", MicroserviceUtils.compareWithIgnore("cadsr Str", 
				ignored + "Cadsr str"));
	}
	@Test
	public void testSamePunct2CaseSpaces() {
		assertFalse("failed of different Punc 2 Case Spaces", MicroserviceUtils.compareWithIgnore(" cadsr  Str ", 
				ignored + "Cadsr str"));
	}
	@Test
	public void testEmptyOne() {
		assertTrue("failed of empty", MicroserviceUtils.compareWithIgnore("  ", 
				null));
	}
	@Test
	public void testEmptyTwo() {
		assertTrue("failed of empty", MicroserviceUtils.compareWithIgnore(null, 
				""));
	}
	@Test
	public void testEmptyNulls() {
		assertTrue("failed of empty", MicroserviceUtils.compareWithIgnore(null, 
				null));
	}
	@Test
	public void testEmptyBoth() {
		assertTrue("failed of empty", MicroserviceUtils.compareWithIgnore(" ", 
				"    "));
	}
	@Test
	public void testOneNull() {
		assertFalse("failed of empty", MicroserviceUtils.compareWithIgnore(null, 
			ignored));
	}
	@Test
	public void testSecondNull() {
		assertFalse("failed of empty", MicroserviceUtils.compareWithIgnore(ignored, 
			null));
	}	
	@Test
	public void testSecondEmpty() {
		assertFalse("failed of empty", MicroserviceUtils.compareWithIgnore(ignored, 
			"   "));
	}

	
	/*
	FORMBUILD-651 exclude the following common sentence punctuation:
	period - .
	comma -  ,
	colon - :
	semi-colon - ;
	quotes - ' and "
	question mark = ?
	dash - - 
	parenthesis - ( and )
	 */
	@Test
	public void testRemovePeriod() {
		assertEquals("failed clean Period", MicroserviceUtils.removeIgnored("cadsr Str"+"."), 
				"cadsr Str");
	}
	@Test
	public void testRemoveComma() {
		assertEquals("failed clean Comma", MicroserviceUtils.removeIgnored("cadsr Str"+","), 
				"cadsr Str");
	}
	@Test
	public void testRemoveColon() {
		assertEquals("failed clean Colon", MicroserviceUtils.removeIgnored("cadsr Str"+":"), 
				"cadsr Str");
	}
	@Test
	public void testRemoveSemiColon() {
		assertEquals("failed clean SemiColon", MicroserviceUtils.removeIgnored("cadsr Str"+";"), 
				"cadsr Str");
	}
	@Test
	public void testRemoveSingleQuotes() {
		assertEquals("failed clean Single Quotes", MicroserviceUtils.removeIgnored("cadsr Str"+"\'"), 
				"cadsr Str");
	}
	@Test
	public void testRemoveQuotes() {
		assertEquals("failed clean Quotes", MicroserviceUtils.removeIgnored("cadsr Str"+"\""), 
				"cadsr Str");
	}
	@Test
	public void testRemoveQuestion() {
		assertEquals("failed clean Question", MicroserviceUtils.removeIgnored("cadsr Str"+"?"), 
				"cadsr Str");
	}
	@Test
	public void testRemoveDash() {
		assertEquals("failed clean Dash", MicroserviceUtils.removeIgnored("cadsr Str"+"-"), 
				"cadsr Str");
	}
	@Test
	public void testRemoveParenthL() {
		assertEquals("failed clean ParenthL", MicroserviceUtils.removeIgnored("cadsr Str"+"("), 
				"cadsr Str");
	}
	@Test
	public void testRemoveParenthR() {
		assertEquals("failed clean ParenthR", MicroserviceUtils.removeIgnored("cadsr Str"+")"), 
				"cadsr Str");
	}
	
	
	@Test
	public void testCompareValNoMatch() {
		assertFalse("Not match - different case", MicroserviceUtils.compareValues("Person", "PERSON"));
	}
	
	@Test
	public void testCompareValMatch() {
		assertTrue("Match - same case", MicroserviceUtils.compareValues("PERSON", "PERSON"));
	}	
	
	@Test
	public void testCompDiffPunctMatch() {
		assertTrue("Match - same punctuation", MicroserviceUtils.compareValues("PERSON"+ignored, "PERSON"+ignored));
	}
	
	@Test
	public void testCompDiffPunctNoMatch() {
		assertFalse("No Match - same punctuation, diff order", MicroserviceUtils.compareValues("PERSON"+ignored, ignored+"PERSON"));
	}
	
	@Test
	public void testCompExcludedPunct() {
		assertFalse("No Match - exluded punctuation", MicroserviceUtils.compareValues("PERSON"+ignored, "PERSON"+"}"));
	}	
	
	
	@Test
	public void testCompWithNullA() {
		assertFalse("Compare with Null A", MicroserviceUtils.compareValues("PERSON", null));
	}
	
	@Test
	public void testCompWithNullB() {
		assertFalse("Compare with Null B", MicroserviceUtils.compareValues(null, "PERSON"));
	}	
	
	@Test
	public void testCompWithNullBoth() {
		assertTrue("Compare with Null", MicroserviceUtils.compareValues(null, null));
	}	
	
	@Test
	public void testCompValLeadSpacesNoMatch() {
		assertFalse("Compare with lead spaces", MicroserviceUtils.compareValues("PERSON", " PERSON"));
	}	
	
	@Test
	public void testCompValTrailSpacesNoMatch() {
		assertFalse("Compare with trail spaces", MicroserviceUtils.compareValues("PERSON", "PERSON "));
	}
	
	@Test
	public void testCompValMidSpacesNoMatch() {
		assertFalse("Compare with spaces in the middle", MicroserviceUtils.compareValues("PERSON", "PER SON"));
	}
	
	@Test
	public void testCompValSpacesMatch() {
		assertTrue("Compare with blank spaces for values", MicroserviceUtils.compareValues(" ", " "));
	}
	
	@Test
	public void testCompValUnequalSpacesMatch() {
		assertTrue("Compare with unequal blank spaces for values", MicroserviceUtils.compareValues("  ", " "));
	}	
	
	@Test
	public void testCompValSpaceNull() {
		assertTrue("Compare null with blank spaces for values", MicroserviceUtils.compareValues("  ", null));
	}		
	
	/**
	 *  Test Get PV VM List - Success - case insensitivity: false
	 */		
	@Test
	public void testGetPVVMList1() {
		String expectedValue = "Lymph Node, Axilla";
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Lymph Node, Axilla");
		Map<String, List<String>> pvVmMap =  new HashMap<String, List<String>>();
		pvVmMap.put("LN, Axilla", pvVmList);
		List<String> obtainedPvVmList = MicroserviceUtils.getPVVMList (pvVmMap, "LN- Axilla", false);
		assertEquals(expectedValue, obtainedPvVmList.get(0));
	}
	
	
	/**
	 *  Test Get PV VM List - Failure(returns null) - case sensitivity: true
	 */		
	@Test
	public void testGetPVVMList2() {
		String expectedValue = null;
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Lymph Node, Axilla");
		Map<String, List<String>> pvVmMap =  new HashMap<String, List<String>>();
		pvVmMap.put("LN, Axilla", pvVmList);
		List<String> obtainedPvVmList = MicroserviceUtils.getPVVMList (pvVmMap, "LN- Axilla", true);
		assertEquals(expectedValue, obtainedPvVmList);
	}	
	
	/**
	 *  Test Get PV VM List - Success - case insensitivity: false
	 */		
	@Test
	public void testGetPVVMList3() {
		String expectedValue = "Lymph Node, Axilla";
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Lymph Node, Axilla");
		Map<String, List<String>> pvVmMap =  new HashMap<String, List<String>>();
		pvVmMap.put("LN, Axilla", pvVmList);
		List<String> obtainedPvVmList = MicroserviceUtils.getPVVMList (pvVmMap, "", false);
		assertNotEquals(expectedValue, obtainedPvVmList);
	}
	
	
	/**
	 *  Test Get PV VM List - Success - case sensitivity: true
	 */		
	@Test
	public void testGetPVVMList4() {
		String expectedValue = "Lymph Node, Axilla";
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Lymph Node, Axilla");
		Map<String, List<String>> pvVmMap =  new HashMap<String, List<String>>();
		pvVmMap.put("LN, Axilla", pvVmList);
		List<String> obtainedPvVmList = MicroserviceUtils.getPVVMList (pvVmMap, "LN, Axilla", true);
		assertEquals(expectedValue, obtainedPvVmList.get(0));
	}		
	
	/**
	 *  Test Get PV VM List - Success - case insensitivity: true
	 */		
	@Test
	public void testGetPVVMList5() {
		String expectedValue = "Mandible";
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Mandible");
		Map<String, List<String>> pvVmMap =  new HashMap<String, List<String>>();
		pvVmMap.put("Mandible", pvVmList);
		List<String> obtainedPvVmList = MicroserviceUtils.getPVVMList (pvVmMap, "Mandible", true);
		assertEquals(expectedValue, obtainedPvVmList.get(0));
	}
	
	
	/**
	 *  Test Get PV VM List - Success - case sensitivity: false
	 */		
	@Test
	public void testGetPVVMList6() {
		String expectedValue = "Ear, Inner";
		List<String> pvVmList = new ArrayList<String>();
		pvVmList.add("Ear, Inner");
		Map<String, List<String>> pvVmMap =  new HashMap<String, List<String>>();
		pvVmMap.put("EAR, Inner", pvVmList);
		List<String> obtainedPvVmList = MicroserviceUtils.getPVVMList (pvVmMap, "Ear, Inner", false);
		assertEquals(expectedValue, obtainedPvVmList.get(0));
	}			
	
	
}
