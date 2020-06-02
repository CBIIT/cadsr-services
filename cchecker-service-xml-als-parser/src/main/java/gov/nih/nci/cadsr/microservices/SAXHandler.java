package gov.nih.nci.cadsr.microservices;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nci.cadsr.data.XmlRow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXHandler extends DefaultHandler {

	private final static Logger logger = LoggerFactory.getLogger(SAXHandler.class);
    public List<XmlRow> xmlRowList = new ArrayList<>();
    public List<String> sheetsList = new ArrayList<String>();
    XmlRow xmlRow = null;
    String content = null;
    String sheetName = new String();
    

    @Override
    //Triggered when the start of tag is found.
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        switch (qName) {
            //Create a new Row object when the start tag is found
            case "Row":
                xmlRow = new XmlRow();
                break;
            case "Worksheet":
            	sheetName = new String();
            	int length = attributes.getLength();
            for (int i=0; i<length; i++) {
            	// get qualified (prefixed) name by index
            	String name = attributes.getQName(i);
                //logger.debug("Name:" + name);
            	// get attribute's value by index.
            	String value = attributes.getValue(i);
            	//logger.debug("Value:" + value);
                sheetName = value;
            }            	         
            	break;
        }
    }

    @Override
    public void endElement(String uri, String localName,
                           String qName) throws SAXException {
        switch (qName) {
            case "Row":
                xmlRowList.add(xmlRow);
                break;
            case "Data":
                xmlRow.cellList.add(content);
                break;
            case "Worksheet":
            	sheetsList.add(sheetName);
            	sheetName = new String();
            	break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        content = String.copyValueOf(ch, start, length).trim();
    }
}
