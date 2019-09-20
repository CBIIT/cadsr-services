package gov.nih.nci.cadsr.microservices;

import static org.junit.Assert.*;

import org.junit.Test;

public class MicroserviceUtilsTest {
	final static String ignored = ".,:;\"'?-()";
	@Test
	public void testTwoDifferentStrings() {
		assertFalse("failed on different strings", MicroserviceUtils.compareWithIgnore("cadsr Str", "als Str"));
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
}
