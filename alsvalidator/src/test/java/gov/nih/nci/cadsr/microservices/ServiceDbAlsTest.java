/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import gov.nih.nci.cadsr.data.CCCReport;

@RunWith(MockitoJUnitRunner.class)
public class ServiceDbAlsTest {
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    @Spy
    private ServiceDbAls serviceDbAls;

	@Test
	public void testSubmitPostRequestSaveReportError() throws RestClientException, URISyntaxException {
		StringResponseWrapper given = new StringResponseWrapper();
		given.setStatusCode(HttpStatus.OK);
		CCCReport cccReport = new CCCReport();
		cccReport.setReportOwner("testreportOwner");
		given.setResponseData("OK");
		URI testUri = new URI("http://localhost:4803/rest/createreporterror?_cchecker=testid");
		Mockito
        .when(restTemplate.postForEntity(Mockito.eq(testUri), 
        		Mockito.any(Object.class), Mockito.eq(String.class)))
        .thenReturn(new ResponseEntity<String>("OK", HttpStatus.OK));
		//MUT
		StringResponseWrapper received = serviceDbAls.submitPostRequestSaveReportError(cccReport, "testid", 
				"http://localhost:4803/rest/createreporterror");
		assertEquals(given, received);
	}

}
