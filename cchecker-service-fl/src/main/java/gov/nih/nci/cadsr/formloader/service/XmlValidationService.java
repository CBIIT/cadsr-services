package gov.nih.nci.cadsr.formloader.service;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderServiceException;

import java.util.List;

public interface XmlValidationService {
	
	/**
	 * Validate an xml file against xsd. The xml file name and path are set in the input
	 * FormCollection object. If successful, a list of FormDescriptors are generated and assigned
	 * to the FormCollection.
	 * <br> <br>
	 * If a form's loadStatus is not FormDescriptor.STATUS_XML_VALIDATED, FormDescriptor.getXmlValidationErrors()
	 * is available to get an error list.
	 * 
	 * @param collection xml file name and path must be set and valid
	 * @return collection with a list of FormDescriptors generated from the xml
	 * @throws FormLoaderServiceException thrown when xml is malformed. An error message can be retrieved
	 * with FormCollection.getMessagesInString()
	 */
	public FormCollection validateXml(FormCollection collection) 
			throws FormLoaderServiceException;

}
