/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.FormsUiData;

//FIXME add more tests
@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class GatewayBootControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayBootControllerTest.class.getName());
    @Autowired
	private MockMvc mockMvc;
	@MockBean
	ServiceParser serviceParser;
	@MockBean
	ServiceDb serviceDb;

	@Autowired
	WebApplicationContext wContext;
	
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
	public void shouldSaveUploadedFile() throws Exception {
		//read the data
		byte[] allBytes = loadFile("RAVE-ALS-10057-VS.xlsx");
		
		// Mock Request
		MockMultipartFile excelFile = new MockMultipartFile("file", "RAVE-ALS-10057-VS.xlsx", 
			GatewayBootController.MS_EXCEL_MIME_TYPE,
			allBytes);
		//FormsUiData formsUiData = loadJson("allForms.json");
		ALSDataWrapper alsDataWrapper = new ALSDataWrapper();
		alsDataWrapper.setStatusCode(HttpStatus.OK);
		ALSData alsData = new ALSData();
		ALSForm alsForm = createTestALSForm();
		alsData.getForms().add(alsForm);
		
		alsDataWrapper.setAlsData(alsData);
		StringResponseWrapper stringResponseWrapper = new StringResponseWrapper();
		//TODO create response data
		stringResponseWrapper.setStatusCode(HttpStatus.OK);
		
		given(this.serviceParser.submitPostRequestParser(Mockito.any(), Mockito.any())).willReturn(alsDataWrapper);
		given(this.serviceDb.submitPostRequestSaveAls(Mockito.any(ALSData.class), Mockito.any(), Mockito.any())).willReturn(stringResponseWrapper);

		this.mockMvc.perform(multipart("/parseservice").file(excelFile)).andExpect(status().isOk());
		
		//TODO test results better
		//MockHttpServletResponse response = this.mockMvc.perform(multipart("/parseservice").file(excelFile)).andReturn().getResponse();
		//assertEquals(response.getStatus(), HttpStatus.OK.value());

	}
	@Test
	public void parseServiceResponse() throws Exception {
		//read the data
		byte[] allBytes = loadFile("RAVE-ALS-10057-VS.xlsx");
		
		// Mock Request
		MockMultipartFile excelFile = new MockMultipartFile("file", "RAVE-ALS-10057-VS.xlsx", 
			GatewayBootController.MS_EXCEL_MIME_TYPE,
			allBytes);
		//FormsUiData formsUiData = loadJson("allForms.json");
		ALSDataWrapper alsDataWrapper = new ALSDataWrapper();
		alsDataWrapper.setStatusCode(HttpStatus.OK);
		ALSData alsData = new ALSData();
		ALSForm alsForm = createTestALSForm();
		alsData.getForms().add(alsForm);
		String formUiDataJsonExpected = createTestFormUiDataJson(alsData);
		
		alsDataWrapper.setAlsData(alsData);
		StringResponseWrapper stringResponseWrapper = new StringResponseWrapper();
		//TODO create response data
		stringResponseWrapper.setStatusCode(HttpStatus.OK);
		
		given(this.serviceParser.submitPostRequestParser(Mockito.any(), Mockito.any())).willReturn(alsDataWrapper);
		given(this.serviceDb.submitPostRequestSaveAls(Mockito.any(ALSData.class), Mockito.any(), Mockito.any())).willReturn(stringResponseWrapper);

		this.mockMvc.perform(multipart("/parseservice").file(excelFile))
			.andExpect(content().json(formUiDataJsonExpected)).andExpect(status().isOk());
	}	
	public static ALSForm createTestALSForm() {
		ALSForm alsForm = new ALSForm();
		alsForm.setDraftFormName("draftFormName 1");
		alsForm.setFormOid("OID-1");
		return alsForm;
	}
	/**
	 * 
	 * @param alsData not null, has one form
	 * @return json string
	 */
	protected String createTestFormUiDataJson(ALSData alsData) {
		String jsonStrFormat = "{\"formsList\":[{\"isValid\":true,\"errors\":[],"
				+ "\"formName\":\"%s\",\"questionsCount\":0}],"
				+ "\"checkUom\":null,\"checkStdCrfCde\":null,\"mustDisplayException\":null}";
		return String.format(jsonStrFormat, alsData.getForms().get(0).getDraftFormName());
	}
	
	/**
	 * Load file from classpath.
	 * 
	 * @param fileName
	 * @return byte[]
	 * @throws IOException 
	 */
	public static byte[] loadFile(String fileName) throws IOException {
		byte[] allBytes;
		File file = ResourceUtils.getFile("classpath:" + fileName);
		allBytes = Files.readAllBytes(file.toPath());
		return allBytes;
	}
	/**
	 * Load ALSData from a json file.
	 * 
	 * @param fileName in classpath
	 * @return ALSData
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public FormsUiData loadJson(String fileName) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		File file = ResourceUtils.getFile("classpath:" + fileName);
		FormsUiData alsFormList = mapper.readValue(file, FormsUiData.class);
		return alsFormList;
	}

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
