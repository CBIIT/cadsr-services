package gov.nih.nci.cadsr.cchecker;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IndexController {
	// inject via application.properties
	@Value("${welcome.message:test}")
	private String message = "CDE Validator";
    @RequestMapping(method= RequestMethod.GET,value="/")
    String index(Map<String, Object> model){
    	model.put("pageTitle", this.message);;
        return "index";
    }
}