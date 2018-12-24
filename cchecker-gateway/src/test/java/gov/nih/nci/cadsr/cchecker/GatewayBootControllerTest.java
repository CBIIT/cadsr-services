package gov.nih.nci.cadsr.cchecker;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nih.nci.cadsr.data.FormsUiData;
//FIXME decide on tests
//@RunWith(SpringRunner.class)
//@WebMvcTest(GatewayBootController.class)
//@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application.properties")
public class GatewayBootControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayBootControllerTest.class.getName());
	private MockMvc mockMvc;

	@Autowired
	WebApplicationContext wContext;

	@MockBean
	private GatewayBootController gatewayBootController;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wContext).alwaysDo(MockMvcResultHandlers.print()).build();
	}
	//FIXME this is a template to call parseService
	@SuppressWarnings({ "rawtypes", "unchecked" })
	//@Test
	public void testMockParseService() throws IOException, Exception {
		//read the data
		byte[] allBytes = loadFile("RAVE-ALS-10057-VS.xlsx");
		// Mock Request
		MockMultipartFile excelFile = new MockMultipartFile("RAVE-ALS-10057-VS.xlsx", "RAVE-ALS-10057-VS.xlsx", 
			GatewayBootController.MS_EXCEL_MIME_TYPE,
			allBytes);

		// Mock Response
		FormsUiData alsData = loadJson("allForms.json");
		HttpHeaders httpHeaders = gatewayBootController.createHttpOkHeaders();
		ResponseEntity response = new ResponseEntity<>(alsData, httpHeaders, HttpStatus.OK);
		Mockito.when(gatewayBootController.parseService(
				Mockito.any(HttpServletRequest.class), 
				Mockito.any(HttpServletResponse.class), 
				Mockito.anyString(), 
				Mockito.any(MultipartFile.class)))
			.thenReturn(response);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/gateway/parseservice").file("file", excelFile.getBytes())
				.characterEncoding("UTF-8")).andExpect(status().isOk());

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
}
