package gov.nih.nci.cadsr.formloader.service.common;


import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.apache.log4j.Logger;

public class XmlValidationErrorHandler implements ErrorHandler {
	
	private static Logger logger = Logger.getLogger(XmlValidationErrorHandler.class.getName());
	
	private List<XmlValidationError> xmlErrors = new ArrayList<XmlValidationError>();

	public List<XmlValidationError> getXmlErrors() {
		return xmlErrors;
	}
	
	public void setXmlErrors(List<XmlValidationError> xmlErrors) {
		this.xmlErrors = xmlErrors;
	}
	
	protected XmlValidationError initializeError(SAXParseException e) {
		XmlValidationError xmlError = new XmlValidationError();
		xmlError.setLineNumber(e.getLineNumber());
		xmlError.setMessage(e.getMessage());
		return xmlError;
	}
	public void warning(SAXParseException e) {
		
		XmlValidationError xmlError = initializeError(e);
		xmlError.setType(XmlValidationError.XML_WARNING);
		logger.debug(xmlError.toString());
    	
		xmlErrors.add(xmlError);
    }

    public void error(SAXParseException e) {
    	
    	XmlValidationError xmlError = initializeError(e);
		xmlError.setType(XmlValidationError.XML_ERROR);
    	logger.debug(xmlError.toString());
    	
    	xmlErrors.add(xmlError);
    }

    public void fatalError(SAXParseException e) {
    	
    	XmlValidationError xmlError = initializeError(e);
		xmlError.setType(XmlValidationError.XML_FATAL_ERROR);    	
    	logger.debug(xmlError.toString());
    	
    	xmlErrors.add(xmlError);
    }
}