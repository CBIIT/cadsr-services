/**
 * Copyright (C) 2019 FNLCR. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import static org.junit.Assert.*;

import org.junit.Test;

public class RestCallsInterceptorTest {

	@Test
	public void testFormatRequestLog() {
		String actual = RestCallsInterceptor.formatRequestLog("2019-06-17T17:06:35.631", "GET",
			"/gateway//feedcheckstatus/7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC", null,	"7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC, GA1.2.693220844.1552403580", "ownertest");
		String expected = "[CCHECKER_REST_REQUEST][2019-06-17T17:06:35.631][GET][/gateway//feedcheckstatus/7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC][null][7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC, GA1.2.693220844.1552403580][ownertest]";
		assertEquals(expected, actual);;
	}
	@Test
	public void testFormatRequestLogNoOwner() {
		String actual = RestCallsInterceptor.formatRequestLog("2019-06-17T17:06:35.631", "GET",
			"/gateway//feedcheckstatus/7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC", null,	"7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC, GA1.2.693220844.1552403580", null);
		String expected = "[CCHECKER_REST_REQUEST][2019-06-17T17:06:35.631][GET][/gateway//feedcheckstatus/7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC][null][7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC, GA1.2.693220844.1552403580]";
		assertEquals(expected, actual);;
	}
	@Test
	public void testFormatRequestLogSomeNulls() {
		String actual = RestCallsInterceptor.formatRequestLog(null, "GET",
			"/gateway//feedcheckstatus/7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC", null,	"[7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC, GA1.2.693220844.1552403580]", null);
		String expected = "[CCHECKER_REST_REQUEST][null][GET][/gateway//feedcheckstatus/7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC][null][[7FF4D3A2-5AD9-4622-847A-F9A17BF5CACC, GA1.2.693220844.1552403580]]";
		assertEquals(expected, actual);;
	}
}
