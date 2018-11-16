package gov.nih.nci.cadsr.service.validator;

import static org.junit.Assert.*;

import org.junit.Test;

public class ValidatorServiceTest {

	@Test
	public void testCleanUpNonUtf8Same() {
		String expected = "stringToClean";
		String received = ValidatorService.cleanUpNonUtf8(expected);
		assertEquals(expected, received);
	}
	@Test
	public void testCleanUpNonUtf8Diamond() {
		String expected = "-" + "�";
		String received = ValidatorService.cleanUpNonUtf8(expected);
//		System.out.println("...testCleanUpNonUtf8Diamond\nreceived: " + received + "\nexpected: " + expected + 
//			"\nlength received: " + received.length() + "\nlength expected: " + expected.length());
	}
	@Test
	public void testCleanUpNonUtf8B2() {
		String expected = "stringToClean" + ValidatorService.apostrophe_str;
		String received = ValidatorService.cleanUpNonUtf8(expected);
//		System.out.println("...testCleanUpNonUtf8B2\nreceived: " + received + "\nexpected: " + expected + 
//			"\nlength received: " + received.length() + "\nlength expected: " + expected.length());

	}
	@Test
	public void testCleanUpNonUtf8BF() {
		String expected = "stringToClean" + ValidatorService.superscript_2_str;
		String received = ValidatorService.cleanUpNonUtf8(expected);
//		System.out.println("...testCleanUpNonUtf8BF\nreceived: " + received + "\nexpected: " + expected+ 
//			"\nlength received: " + received.length() + "\nlength expected: " + expected.length());
	}
}
