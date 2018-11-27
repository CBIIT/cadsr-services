/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.parser.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class AlsParserTest {

	public void testHtmlStripJsoupMethod(String htmlText, String expectedResult) {
		String htmlStrippedText = AlsParser.stripHtmlV1(htmlText);
		assertEquals(expectedResult, htmlStrippedText);
	}

	public void testHtmlStripReplaceMethod(String htmlText, String expectedResult) {
		String htmlStrippedText = AlsParser.stripHtmlV2(htmlText);
		assertEquals(expectedResult, htmlStrippedText);
	}

	@Test
	public void testHtmlStripPatterns() {
		String htmlText1 = "<font color=\"red\">Start Date</font>";
		String strippedText1 = "Start Date";
		String htmlText2 = "<H10 style=\"color:red;\">***Any solid or nodular lesion greater than or equal to 1 cm in diameter with evidence of deep infiltration in the skin "
				+ "and/or vertical growth.</H10>";
		String strippedText2 = "***Any solid or nodular lesion greater than or equal to 1 cm in diameter with evidence of deep infiltration in the skin and/or vertical growth.";

		testHtmlStripJsoupMethod(htmlText1, strippedText1);
		testHtmlStripReplaceMethod(htmlText1, strippedText1);

		testHtmlStripJsoupMethod(htmlText2, strippedText2);
		testHtmlStripReplaceMethod(htmlText2, strippedText2);

	}
	@Test
	public void testStripHtmlV1Math() {
		String expectedResult = "Start Date x < 2 and x > 1";
		String htmlText1 = "<font color=\"red\"><b>" + expectedResult + "</b></font>";

		String actual = AlsParser.stripHtmlV1(htmlText1);
		assertEquals(expectedResult, actual);
	}
	/**
	 * stripHtmlV2 cannot deal with '<'
	 */
	@Test
	public void testStripHtmlV2MathWrongResult() {
		String expectedResult = "Start Date x < 2 and x > 1";
		String expectedWrongResult = "Start Date x  1";
		String htmlText1 = "<font color=\"red\"><b>" + expectedResult + "</b></font>";

		String actual = AlsParser.stripHtmlV2(htmlText1);
		assertEquals(expectedWrongResult, actual);
	}
	@Test
	public void testStripHtmlV1Math2() {
		String expectedResult = "Start Date x < 2";
		String htmlText1 = "<font color=\"red\"><b>" + expectedResult + "</b></font>";

		String actual = AlsParser.stripHtmlV1(htmlText1);
		assertEquals(expectedResult, actual);
	}
	/**
	 * stripHtmlV2 cannot deal with '<'
	 */
	@Test
	public void testStripHtmlV2Math2WrongResult() {
		String expectedResult = "Start Date x < 2";
		String expectedWrongResult = "Start Date x ";
		String htmlText1 = "<font color=\"red\"><b>" + expectedResult + "</b></font>";

		String actual = AlsParser.stripHtmlV2(htmlText1);
		assertEquals(expectedWrongResult, actual);
	}
}
