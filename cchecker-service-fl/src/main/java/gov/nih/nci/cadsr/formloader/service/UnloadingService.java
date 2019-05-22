package gov.nih.nci.cadsr.formloader.service;

import java.util.List;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderServiceException;

public interface UnloadingService {
	
	/**
	 * Unload previously loaded form collections. Forms will not be deleted from database but only their workflow status
	 * will be set to RETIRED UNLOADED
	 * <br>
	 * @param collections collections to be unloaded
	 * @param userName user that has right to unload forms
	 * 
	 * @return collections with their forms's loadStatus set to latest status (STATUS_UNLOADED or STATUS_UNLOAD_FAILED)
	 * @throws FormLoaderServiceException
	 */
	public List<FormCollection> unloadCollections(List<FormCollection> collections, String userName) 
			throws FormLoaderServiceException;
	
}
