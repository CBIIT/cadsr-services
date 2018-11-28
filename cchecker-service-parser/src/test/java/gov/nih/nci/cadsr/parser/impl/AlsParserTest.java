/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.parser.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class AlsParserTest {

	public void testHtmlStripJsoupMethod(String htmlText, String expectedResult) {
		String htmlStrippedText = AlsParser.stripHtml(htmlText);
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
		testHtmlStripJsoupMethod(htmlText2, strippedText2);

	}
	@Test
	public void teststripHtmlMath() {
		String expectedResult = "Start Date x < 2 and x > 1";
		String htmlText1 = "<font color=\"red\"><b>" + expectedResult + "</b></font>";

		String actual = AlsParser.stripHtml(htmlText1);
		assertEquals(expectedResult, actual);
	}

	@Test
	public void teststripHtmlMath2() {
		String expectedResult = "Start Date x < 2";
		String htmlText1 = "<font color=\"red\"><b>" + expectedResult + "</b></font>";

		String actual = AlsParser.stripHtml(htmlText1);
		assertEquals(expectedResult, actual);
	}

}
