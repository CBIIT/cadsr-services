/**
 * Copyright (C) 2019 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import static org.mockito.BDDMockito.given;
import org.mockito.ArgumentMatchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.Cookie;

import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.data.CCCReport;
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
	@MockBean
	ServiceValidator serviceValidator;
	
	@Autowired
	WebApplicationContext wContext;
	
	private static final String UNEXPECTED_ERROR = "Unexpected error";
	private static final String ERROR_ON_PARSING_FILE = "Error on parsing file";
	
	private static MockMultipartFile excelFile;
	@BeforeClass
	public static void setUpClass() throws IOException {
		excelFile = buildMultipartFile("RAVE-ALS-10057-VS.xlsx");
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
	public void shouldSaveUploadedFile() throws Exception {

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
			.andExpect(content().json(formUiDataJsonExpected))
			.andExpect(MockMvcResultMatchers.jsonPath("$.formsList[0].formName").value(alsForm.getDraftFormName()))
			.andExpect(status().isOk());
	}
	@Test
	public void parseServiceResponseParserDown() throws Exception {			
		given(this.serviceParser.submitPostRequestParser(Mockito.any(), Mockito.any())).willThrow(new RestClientException("Test Exception"));

		this.mockMvc.perform(multipart("/parseservice").file(excelFile))
			.andExpect(content().string(new StringContains(UNEXPECTED_ERROR)))
			.andExpect(status().is5xxServerError());
	}
	@Test
	public void parseServiceResponseDBDown() throws Exception {
		ALSDataWrapper alsDataWrapper = new ALSDataWrapper();
		alsDataWrapper.setStatusCode(HttpStatus.OK);
		ALSData alsData = new ALSData();
		ALSForm alsForm = createTestALSForm();
		alsData.getForms().add(alsForm);
		
		alsDataWrapper.setAlsData(alsData);
		StringResponseWrapper stringResponseWrapper = new StringResponseWrapper();

		stringResponseWrapper.setStatusCode(HttpStatus.OK);
		
		given(this.serviceParser.submitPostRequestParser(Mockito.any(), Mockito.any())).willReturn(alsDataWrapper);

		given(this.serviceDb.submitPostRequestSaveAls(Mockito.any(ALSData.class), Mockito.any(), Mockito.any())).willThrow(new RestClientException("Test Exception"));

		this.mockMvc.perform(multipart("/parseservice").file(excelFile))
			.andExpect(content().string(new StringContains(UNEXPECTED_ERROR)))
			.andExpect(status().is5xxServerError());
	}
	@Test
	public void parseServiceResponseParserError400() throws Exception {
		ALSDataWrapper alsDataWrapper = new ALSDataWrapper();
		alsDataWrapper.setStatusCode(HttpStatus.BAD_REQUEST);
		
		StringResponseWrapper stringResponseWrapper = new StringResponseWrapper();

		stringResponseWrapper.setStatusCode(HttpStatus.OK);
		
		given(this.serviceParser.submitPostRequestParser(Mockito.any(), Mockito.any())).willReturn(alsDataWrapper);

		this.mockMvc.perform(multipart("/parseservice").file(excelFile))
			.andExpect(content().string(new StringContains(ERROR_ON_PARSING_FILE)))
			.andExpect(status().is4xxClientError());
	}
	@Test
	public void parseServiceResponseParserError503() throws Exception {
		ALSDataWrapper alsDataWrapper = new ALSDataWrapper();
		alsDataWrapper.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		
		StringResponseWrapper stringResponseWrapper = new StringResponseWrapper();

		stringResponseWrapper.setStatusCode(HttpStatus.OK);
		
		given(this.serviceParser.submitPostRequestParser(Mockito.any(), Mockito.any())).willReturn(alsDataWrapper);

		this.mockMvc.perform(multipart("/parseservice").file(excelFile))
			.andExpect(content().string(new StringContains(ERROR_ON_PARSING_FILE)))
			.andExpect(status().isInternalServerError());
	}
	
	private static MockMultipartFile buildMultipartFile(String fileName) throws IOException {
		//read the data
		byte[] allBytes = loadFile("RAVE-ALS-10057-VS.xlsx");
		
		// Mock Request
		MockMultipartFile excelFile = new MockMultipartFile("file", "RAVE-ALS-10057-VS.xlsx", 
			GatewayBootController.MS_EXCEL_MIME_TYPE,
			allBytes);
		return excelFile;
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
	
	@Test
	public void validateServiceResponseValidatorDown() throws Exception {
		given(this.serviceValidator.sendPostRequestValidator(Mockito.any(), Mockito.any(), Mockito.eq(false), Mockito.eq(false), Mockito.eq(false))).willThrow(new RestClientException("Test Exception"));
		this.mockMvc.perform(post("/checkservice").contentType("application/json")
				.content(createFormNameList())
				.cookie(cookie))
			.andExpect(content().string(new StringContains(UNEXPECTED_ERROR)))
			.andExpect(status().is5xxServerError());
	}	
	@Test
	public void validateServiceResponseNoCookie() throws Exception {
		given(this.serviceValidator.sendPostRequestValidator(Mockito.any(), Mockito.any(), Mockito.eq(false), Mockito.eq(false), Mockito.eq(false))).willThrow(new RestClientException("Test Exception"));
		this.mockMvc.perform(post("/checkservice").contentType("application/json")
				.content(createFormNameList())
				)
			.andExpect(content().string(new StringContains(GatewayBootController.SESSION_NOT_VALID)))
			.andExpect(status().is4xxClientError());
	}
	@Test
	public void validateServiceResponseNullFromValidator() throws Exception {
		given(this.serviceValidator.sendPostRequestValidator(Mockito.any(), Mockito.any(), Mockito.eq(false), Mockito.eq(false), Mockito.eq(false))).willReturn(null);
		Cookie cookie = GatewayBootController.generateCookie();
		System.out.println("cookie: " + cookie);
		this.mockMvc.perform(post("/checkservice").contentType("application/json")
				.content(createFormNameList())
				.cookie(cookie))
			.andExpect(content().string(new StringContains(GatewayBootController.SESSION_DATA_NOT_FOUND)))
			.andExpect(status().is5xxServerError());
	}
	@Test
	public void validateServiceResponseOKFromValidator() throws Exception {
		StringResponseWrapper stringResponseWrapper = new StringResponseWrapper();
		stringResponseWrapper.setStatusCode(HttpStatus.OK);
		CCCReport cccReport = createTestCCCReport("testOwner", "testProtocol");
		//DB returns OK
		given(this.serviceDb.submitPostRequestSaveReportError(Mockito.any(CCCReport.class), Mockito.any(), Mockito.any())).willReturn(stringResponseWrapper);
		given(this.serviceValidator.sendPostRequestValidator(Mockito.any(), Mockito.any(), Mockito.eq(false), Mockito.eq(false), Mockito.eq(false))).willReturn(cccReport);
		//TODO use jsonPath instead of string
		ResultMatcher msg = MockMvcResultMatchers.content().string(cccReportExampleJson());
		this.mockMvc.perform(post("/checkservice").contentType("application/json")
				.content(createFormNameList())
				.cookie(cookie))
			.andExpect(msg)
			.andExpect(status().isOk());
	}
	@Test
	public void validateServiceDBRestException() throws Exception {
		StringResponseWrapper stringResponseWrapper = new StringResponseWrapper();
		stringResponseWrapper.setStatusCode(HttpStatus.OK);
		CCCReport cccReport = createTestCCCReport("testOwner", "testProtocol");
		//DB returns OK
		given(this.serviceDb.submitPostRequestSaveReportError(Mockito.any(CCCReport.class), Mockito.any(), Mockito.any())).willThrow(new RestClientException("Test DB Exception"));
		given(this.serviceValidator.sendPostRequestValidator(Mockito.any(), Mockito.any(), Mockito.eq(false), Mockito.eq(false), Mockito.eq(false))).willReturn(cccReport);
		//TODO use jsonPath instead of string
		ResultMatcher msg = MockMvcResultMatchers.content().string(cccReportExampleJson());
		this.mockMvc.perform(post("/checkservice").contentType("application/json")
				.content(createFormNameList())
				.cookie(cookie))
		.andExpect(content().string(new StringContains(UNEXPECTED_ERROR)))
		.andExpect(status().is5xxServerError());
	}
	@Test
	public void validateServiceDBError() throws Exception {
		StringResponseWrapper stringResponseWrapper = new StringResponseWrapper();
		stringResponseWrapper.setStatusCode(HttpStatus.OK);
		CCCReport cccReport = createTestCCCReport("testOwner", "testProtocol");
		//DB returns OK
		given(this.serviceDb.submitPostRequestSaveReportError(Mockito.any(CCCReport.class), Mockito.any(), Mockito.any())).willThrow(new RestClientException("Test DB Exception"));
		given(this.serviceValidator.sendPostRequestValidator(Mockito.any(), Mockito.any(), Mockito.eq(false), Mockito.eq(false), Mockito.eq(false))).willReturn(cccReport);
		//TODO use jsonPath instead of string
		ResultMatcher msg = MockMvcResultMatchers.content().string(cccReportExampleJson());
		this.mockMvc.perform(post("/checkservice").contentType("application/json")
				.content(createFormNameList())
				.cookie(cookie))
		.andExpect(content().string(new StringContains(UNEXPECTED_ERROR)))
		.andExpect(status().is5xxServerError());
	}
	
	protected String createFormNameList() {
		return "[\"BCH\",\"BCR\"]";
	}
	protected CCCReport createTestCCCReport(String reportOwner, String raveProtocolName) {
		CCCReport cccReport = new CCCReport();
		cccReport.setReportOwner(reportOwner);
		cccReport.setRaveProtocolName(raveProtocolName);
		return cccReport;
	}
	protected String cccReportExampleJson() {
		return "{\"reportOwner\":\"testOwner\",\"reportDate\":null,\"fileName\":null,\"raveProtocolName\":\"testProtocol\","
				+ "\"raveProtocolNumber\":null,\"totalFormsCount\":0,\"totalFormsCong\":0,\"countQuestionsChecked\":0,"
				+ "\"countCongruentQuestions\":0,\"countQuestionsWithWarnings\":0,\"countQuestionsWithErrors\":0,"
				+ "\"countQuestionsWithoutCde\":0,\"countManCrfCongruent\":0,\"countManCrfMissing\":0,\"countManCrfwWithWarnings\":0,"
				+ "\"countManCrfWithErrors\":0,\"countOptCrfCongruent\":0,\"countOptCrfMissing\":0,\"countOptCrfwWithWarnings\":0,"
				+ "\"countOptCrfWithErrors\":0,\"countCondCrfCongruent\":0,\"countCondCrfMissing\":0,\"countCondCrfwWithWarnings\":0,"
				+ "\"countCondCrfWithErrors\":0,\"countNrdsCongruent\":0,\"countNrdsMissing\":0,\"countNrdsWithWarnings\":0,"
				+ "\"countNrdsWithErrors\":0,\"cccForms\":[],\"nrdsCdeList\":[],\"missingNrdsCdeList\":[],\"missingStandardCrfCdeList\":[],"
				+ "\"isCheckStdCrfCdeChecked\":null,\"cccError\":{\"alsErrors\":[],\"raveProtocolName\":null,\"raveProtocolNumber\":null}}";
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
