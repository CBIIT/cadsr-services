package gov.nih.nci.cadsr.microservices;

import static org.junit.Assert.*;

import org.junit.Test;

public class RequestFilterTest {

	@Test
	public void testparseSessionIdHappy() {
		String given = "/local/content/cchecker/35F24768-8364-4197-BD5C-C7CCCDD39597.xlsx";
		String received = RequestFilter.parseSessionId(given);
		assertEquals("35F24768-8364-4197-BD5C-C7CCCDD39597", received);
	}
	@Test
	public void testparseSessionIdHappy1() {
		String given = "/35F24768-8364-4197-BD5C-C7CCCDD39597.xlsx";
		String received = RequestFilter.parseSessionId(given);
		assertEquals("35F24768-8364-4197-BD5C-C7CCCDD39597", received);
	}
	@Test
	public void testparseSessionIdHappy2() {
		String given = "/local/35F24768-8364/4197-BD5C-C7CCCDD39597.xlsx";
		String received = RequestFilter.parseSessionId(given);
		assertEquals("4197-BD5C-C7CCCDD39597", received);
	}
	@Test
	public void testparseSessionIdHappy3() {
		String given = "/local/cchecker/35F24768-8364-4197-BD5C-C7CCCDD39597.xlsx";
		String received = RequestFilter.parseSessionId(given);
		assertEquals("35F24768-8364-4197-BD5C-C7CCCDD39597", received);
	}
	@Test
	public void testparseSessionIdNull1() {
		String given = "35F24768-8364-4197-BD5C-C7CCCDD39597.xlsx";
		String received = RequestFilter.parseSessionId(given);
		assertNull(received);
	}
	@Test
	public void testparseSessionIdNull2() {
		String given = "35F24768-8364-4197-BD5C-C7CCCDD39597";
		String received = RequestFilter.parseSessionId(given);
		assertNull(received);
	}
	@Test
	public void testparseSessionIdNull3() {
		String given = "/35F24768-8364-4197-BD5C-C7CCCDD39597";
		String received = RequestFilter.parseSessionId(given);
		assertNull(received);
	}
	@Test
	public void testparseSessionIdNull4() {
		String given = null;
		String received = RequestFilter.parseSessionId(given);
		assertNull(received);
	}
	@Test
	public void testparseSessionIdNull5() {
		String given = "  ";
		String received = RequestFilter.parseSessionId(given);
		assertNull(received);
	}
	@Test
	public void testparseSessionIdNull6() {
		String given = "\n";
		String received = RequestFilter.parseSessionId(given);
		assertNull(received);
	}
}
