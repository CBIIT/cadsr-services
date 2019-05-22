package gov.nih.nci.cadsr.formloader.service;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderServiceException;

public interface LoadingService {
	
	/**
	 * Load the preprocessed forms in the collection into database. Only forms that are selected (selected flag
	 * is set to true) with loadStatus marked as STATUS_DB_VALIDATED in the content validation step will be loaded.
	 * 
	 * @param aCollection collection containing forms that are xml validated and content validated. <br>
	 *  1. Only forms marked as "selected" will be processed. If FormCollection.selectAllForm is set to true, all forms 
	 * will be considered as selected<br>
	 *  2. Only forms with loadStatus==STATUS_DB_VALIDATED will be processed. <br>
	 *  3. Collection's xml file name and path must be valid. <br>
	 *  4. Collection's createBy, which is the application's loggedin user, must be set.
	 *  
	 * @return the collection with each form marked with latest load status
	 * @throws FormLoaderServiceException
	 */
	public FormCollection loadForms(FormCollection aCollection) 
			throws FormLoaderServiceException;

}
