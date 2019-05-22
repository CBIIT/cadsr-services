package gov.nih.nci.cadsr.formloader.service;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderServiceException;

import java.util.List;

public interface CollectionRetrievalService {

	/**
	 * Retrieve all previously loaded collections, as well as their forms, by the user
	 * 
	 * @param userName user that previously has loaded form collections
	 * 
	 * @return list of form collections previously loaded by the user.
	 * @throws FormLoaderServiceException
	 */
	public List<FormCollection> getAllCollectionsByUser(String userName) throws FormLoaderServiceException;
}
