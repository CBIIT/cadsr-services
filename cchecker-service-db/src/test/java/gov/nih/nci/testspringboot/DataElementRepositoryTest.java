package gov.nih.nci.testspringboot;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import gov.nih.nci.cadsr.microservices.DataElementRepository;
import gov.nih.nci.cadsr.microservices.DataElements;
import gov.nih.nci.cadsr.microservices.CCheckerDbService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=CCheckerDbService.class)
public class DataElementRepositoryTest {

	    @Autowired
	    private DataElementRepository dataElemenRepository;

	    @Test
	    public void findAllUsers() {
	        List<DataElements> dataElement = dataElemenRepository.findAll();
	        assertNotNull(dataElement);
	        assertTrue(!dataElement.isEmpty());
	        //System.out.println(dataElement.toString());
	    }

}
