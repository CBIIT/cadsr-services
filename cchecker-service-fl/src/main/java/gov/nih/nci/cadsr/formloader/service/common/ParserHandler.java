/**
 * 
 */
package gov.nih.nci.cadsr.formloader.service.common;

import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;

import java.util.List;

import javax.xml.stream.XMLStreamReader;

/**
 * @author yangs8
 *
 */
public abstract class ParserHandler {
	
	List<FormDescriptor> formList;
	
	public ParserHandler(List<FormDescriptor> forms) {
		formList = forms;
	}
	
	public List<FormDescriptor> getFormList() {
		return formList;
	}
	
	public void handleStartElement(XMLStreamReader xmlreader) {}
	public void handleEndElement(XMLStreamReader xmlreader) {}
	public void handleCharacterElement(XMLStreamReader xmlreader){}
	
	
	/**
	 * 
	 * @param xmlElemName assumption for xmlElemName format: Camel case with 1st letter in first word in lower case
	 * @return
	 */
	protected String getMethodName(String xmlElemName) {
		String nameToUse = (StaXParser.MAP_XML_ELEM_TO_DTO_PROPERTY.containsKey(xmlElemName)) ?
				StaXParser.MAP_XML_ELEM_TO_DTO_PROPERTY.get(xmlElemName) : xmlElemName;
		char[] stringArray = nameToUse.toCharArray();
		stringArray[0] = Character.toUpperCase(stringArray[0]);
		return "set" + new String(stringArray);
	}
}
