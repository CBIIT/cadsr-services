/**
 * Copyright (C) 2020 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.Cookie;

import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

//TODO add more tests
@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class GatewayFormControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayFormControllerTest.class.getName());
    @Autowired
	private MockMvc mockMvc;
	
	@Autowired
	WebApplicationContext wContext;
	
	@Autowired
	RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
	
	@BeforeClass
	public static void setUpClass() throws IOException {

	}

	private static Cookie cookie = GatewayBootController.generateCookie();
	
	@Before
	public void setup() {
		
	}
	@After
	public void cleanup() {
		//remove Excel files created by the controller in the test directory
		//location to save the files is not set up by boot - ?
		GenericExtFilter filter = new GenericExtFilter(".xlsx");
		File dir = new File(".");
		//find all Excel files in the current directory
		String[] list = dir.list(filter);
		if ((list == null) || (list.length == 0))
			return;
		//doing that since do not know how to spy generateCookie()
		for (String name : list) {
			Path path = Paths.get(name);
			try {
				boolean deleted = Files.deleteIfExists(path);
				//System.out.println("test cleanup deleted file: " + name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
		
	@Test
	public void checkServiceResponseNoCookie() throws Exception {
		this.mockMvc.perform(post("/formxmlservice")
				.param("sessionid", "40A7A07A-CE45-4DEF-A1E3-3C78F67B2E37")
				.contentType("application/json")
				.content(createRequestBody())
				)
			.andExpect(content().string(new StringContains(false, GatewayBootController.SESSION_NOT_VALID)))
			.andExpect(status().is4xxClientError());
	}
	
	protected String createRequestBody() {
		String requestBodyStrJson= "{\"contextName\":\"TEST\",\n"
				+ "\"selForms\": [\"BCH\",\"BCR\"]\n"
			+ "}";
		System.out.println(requestBodyStrJson);
		return requestBodyStrJson;
	}
	/*
	{
	"contextName":"TEST",
	"selForms": ["Literal Laboratory"]
	}
	 */

	// inner class, generic extension filter
	public class GenericExtFilter implements FilenameFilter {

		private String ext;

		public GenericExtFilter(String ext) {
			this.ext = ext;
		}

		public boolean accept(File dir, String name) {
			return (name.endsWith(ext));
		}
	}
}
