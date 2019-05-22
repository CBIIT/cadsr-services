package gov.nih.nci.cadsr.formloader.service;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderServiceException;

public interface ContentValidationService {

	/**
	 * Validate the content of forms in a collection against data in database, after a successful xml wellness/ xsd validation.
	 * 
	 * @param aCollection form collection that went through xml validation. <br>
	 * 1. Only forms marked as "selected" will be processed. If FormCollection.selectAllForm is set to true, all forms 
	 * will be considered as selected<br>
	 * 2. Collection's xml file name and path must be valid. <br>
	 * 3. Collection's createBy, which is the application's loggedin user, must be set.
	 *  
	 * @return collection with forms stamped with content validation result.
	 * @throws FormLoaderServiceException
	 */
	public FormCollection validateXmlContent(FormCollection aCollection) 
			throws FormLoaderServiceException;
}
