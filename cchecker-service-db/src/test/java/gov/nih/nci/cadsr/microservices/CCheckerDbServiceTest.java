package gov.nih.nci.cadsr.microservices;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
//import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
/**
 * This is an integration test.
 * It is expected to connect to DB.
 * Environment shall be set.
 * 
 * @author asafievan
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CCheckerDbService.class, webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
//For a chance of restriction of tests.
//@IfProfileValue(name = "spring-boot.run.profiles", values = {"test"})
public class CCheckerDbServiceTest {
    @Autowired
    private MockMvc mvc;
    /**
     * We need to add more specific asserts in this test.
     * 
     * @throws Exception
     */
	@Test
	public void testCde10() throws Exception {
		//System.out.println("...In testCde10");
		mvc.perform(get("/rest/retrievecategorycde")
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content()
				.contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0].cdeId").exists())
				.andExpect(jsonPath("$[1].cdeId").exists())
				.andExpect(jsonPath("$[2].cdeId").exists());
	}

}
