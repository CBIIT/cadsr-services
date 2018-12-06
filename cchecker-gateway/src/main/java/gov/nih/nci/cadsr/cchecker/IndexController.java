package gov.nih.nci.cadsr.cchecker;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    @RequestMapping(method= RequestMethod.GET,value="/")
    String index(){
        return "caDSR CDE Congrruency Checker";
    }
}