package gov.nih.nci.cadsr.formloader.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.repository.impl.LoadServiceRepositoryImpl;
import gov.nih.nci.cadsr.formloader.service.UnloadingService;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderHelper;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderServiceException;

@Service
public class UnloadingServiceImpl implements UnloadingService {
	
	private static final Logger logger = LoggerFactory.getLogger(UnloadingServiceImpl.class.getName());
	
	LoadServiceRepositoryImpl loadRepository;
	
	public UnloadingServiceImpl() {}
	
	public UnloadingServiceImpl(LoadServiceRepositoryImpl repository) {
		this.loadRepository = repository;
	}
	

	public LoadServiceRepositoryImpl getLoadRepository() {
		return loadRepository;
	}

	public void setLoadRepository(LoadServiceRepositoryImpl loadRepository) {
		this.loadRepository = loadRepository;
	}

	@Override
	@Transactional
	public List<FormCollection> unloadCollections(
			List<FormCollection> collections, String userName) throws FormLoaderServiceException {

		if (collections == null)
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_COLLECTION_NULL, 
					"Form collections is null. Nothing to unload");
		
		if (userName == null || userName.length() == 0) 
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_USER_INVALID, 
					"Login user name is null or empty. Unable to proceed with unloading forms");
		
		//TODO:
		
		//1. Same forms could be in different collections
		//We'll unload forms that belong to multiple collections in the context of the 
		//the one that has the newest date.
		
		//2. When unload new version form, restore the previous latest version.
		
		adjustFormSelections(collections);
		
		for (FormCollection coll : collections) {
			try {
				unloadSelectedForms(coll, userName);
			} catch (FormLoaderServiceException fle) {
				if (fle.getErrorCode() == FormLoaderServiceException.ERROR_EMPTY_FORM_LIST)
					logger.info("Collection [" + coll.getNameWithRepeatIndicator() + "] has 0 form. Nothing to unload.");
				
			}
		}

		return collections;
	}

	public void unloadSelectedForms(FormCollection coll, String userName)
			throws FormLoaderServiceException {
		List<FormDescriptor> forms = coll.getForms();
		
		if (forms == null) 
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_EMPTY_FORM_LIST, 
					"Form list is null");
		
		for (FormDescriptor form : forms) {
			if (!form.isSelected())
				continue;
			
			String context = form.getContext();
			if (!loadRepository.hasLoadFormRight(form, userName, context)) {
				form.addMessage("Loggedin user doesn't have right to unload form with context \"" + context + "\"");
				form.setLoadStatus(FormDescriptor.STATUS_UNLOAD_FAILED);
				continue;
			} 
			
			form.setChangeNote("Unloaded using Form Loader by [" + userName + "]");
			loadRepository.unloadForm(form);
			
			if (form.getLoadStatus() == FormDescriptor.STATUS_UNLOADED) {
				loadRepository.updateFormInCollectionRecord(coll, form);
			}
		}
		
	}

	/**
	 * Same forms could be in different collections. We'll unload forms that belong to multiple collections 
	 * in the context of the the one that has the newest date.
	 * @param collections
	 */
	protected void adjustFormSelections(List<FormCollection> collections) {
		HashSet<String> selectedSeqids = new HashSet<String>();
		List<FormDescriptor> forms;
		
		//Make sure we evaluate newer collections first
		collections = FormLoaderHelper.reversSortCollectionsByDate(collections);
		
		//1st pass: get the unique list of selected forms
		for (FormCollection coll : collections) {
			forms = coll.getForms();
			for (FormDescriptor form : forms) {
				if (form.isSelected())
					selectedSeqids.add(form.getFormSeqId());
			}
		}
		
		//2nd pass: set selected form in the newest collection it belongs to
		for (FormCollection coll : collections) {
			forms = coll.getForms();
			for (FormDescriptor form : forms) {
				String seqid = form.getFormSeqId();
				if (selectedSeqids.contains(seqid)) {
					form.setSelected(true);
					selectedSeqids.remove(seqid);
				} else
					form.setSelected(false);
			}
		}
	}

}
