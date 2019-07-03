package gov.nih.nci.cadsr.formloader.service.impl;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.repository.impl.LoadServiceRepositoryImpl;
import gov.nih.nci.cadsr.formloader.service.LoadingService;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderServiceException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class LoadingServiceImpl implements LoadingService {
	
	private static final Logger logger = LoggerFactory.getLogger(LoadingServiceImpl.class.getName());
	
	LoadServiceRepositoryImpl loadRepository;
	
	public LoadingServiceImpl() {}
	
	public LoadServiceRepositoryImpl getLoadRepository() {
		return loadRepository;
	}

	public void setLoadRepository(LoadServiceRepositoryImpl loadRepository) {
		this.loadRepository = loadRepository;
	}

	public LoadingServiceImpl(LoadServiceRepositoryImpl repository) {
		this.loadRepository = repository;
	}


	@Override
	//Transactional -- push this back to FormLoaderRepository so that a transactional unit is a form
	//Need to watch this 
	public FormCollection loadForms(FormCollection aCollection) 
		throws FormLoaderServiceException{

		if (aCollection == null)
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_COLLECTION_NULL,
					"Input collection is null");
		
		String loggedinuser = aCollection.getCreatedBy();
		if (loggedinuser == null || loggedinuser.length() == 0) 
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_USER_INVALID,
					"User name for the collection is either null or empty. Unable to load form collection");
		
		String xmlPath = aCollection.getXmlPathOnServer();
		String xmlName = aCollection.getXmlFileName();
		
		if (xmlPath == null || xmlPath.length() == 0 ||
				xmlName == null || xmlName.length() == 0)
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_FILE_INVALID,
					"Xml file path or name is invalid");
		
		String xmlPathName = xmlPath.endsWith("/") ? xmlPath + xmlName : xmlPath + "/" + xmlName;
		
		List<FormDescriptor> forms = aCollection.getForms();
		if (forms == null || forms.size() == 0) {
			logger.error("Input form list is null or empty. Nothing to do");
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_EMPTY_FORM_LIST,
					"Input form list is null or empty. Unable to validate form content.");
		}
		
		if (aCollection.isSelectAllForms()) 
			aCollection.resetAllSelectFlag(true);
			
		loadForms(xmlPathName, forms, loggedinuser);
		
		retrievModifiedDateForForms(forms);
		
		createRecordsForCollection(aCollection, loggedinuser);
		
		aCollection.resetAllSelectFlag(false);
		return aCollection;
	}
	
	/**
	 * Get the modified date in db for the just loaded forms, to be used in Form Loader
	 * related tables.
	 * 
	 * @param forms
	 */
	protected void retrievModifiedDateForForms(List<FormDescriptor> forms) {
		List<String> seqids = new ArrayList<String>();
		for (FormDescriptor form : forms) {
			String seqid = form.getFormSeqId();
			if (seqid != null && seqid.length() > 0)
				seqids.add(seqid);
		}
		
		if (seqids.size() > 0) {
			HashMap<String, Date> formModifiedDates = this.loadRepository.getModifiedDateForForms(seqids);
			assignModifiedDateForForms(forms, formModifiedDates);
		}
		
	}
	
	protected void assignModifiedDateForForms(List<FormDescriptor> forms, HashMap<String, Date> formModifiedDates) {
		for (FormDescriptor form : forms) {
			String seqid = form.getFormSeqId();
			if (seqid != null && seqid.length() > 0)
				form.setModifiedDate(formModifiedDates.get(seqid));
		}
	}
	
	protected void createRecordsForCollection(FormCollection coll, String user) {
		List<FormDescriptor> forms = coll.getForms();
		
		String collSeqid = this.loadRepository.createFormCollectionRecords(coll);
		coll.setId(collSeqid);
		coll.setDateCreated(new Date()); //TODO: This should come from db after create
		
		logger.info("Collection \"" + coll.getNameWithRepeatIndicator() + "\" loaded successfully, with seqid: " + collSeqid);
		
	}
	
	protected void loadForms(String xmlPathName, List<FormDescriptor> forms, String loggedinUser) 
	throws FormLoaderServiceException {
		
		for (FormDescriptor form : forms) {
			if (form.getLoadStatus() < FormDescriptor.STATUS_CONTENT_VALIDATED) {
				form.addMessage("Form didn't pass db validation. Unable to load form");
				logger.debug("Form didn't pass db validation. Unable to load form");
				form.setSelected(false);
				continue;
			}
			
			if (!form.isSelected()) {
				logger.debug("Form is not selected to load. Skip loading");
				continue;
			}
			
			//Until v4.2, we're not loading update form
			if (FormDescriptor.LOAD_TYPE_UPDATE_FORM.equals(form.getLoadType()))
				continue;
			//Until v4.2, we're not loading update form
			
			if (!form.getLoadType().equals(FormDescriptor.LOAD_TYPE_NEW ) &&
					!form.getLoadType().equals(FormDescriptor.LOAD_TYPE_NEW_VERSION ) &&
					!form.getLoadType().equals(FormDescriptor.LOAD_TYPE_UPDATE_FORM)) {
				form.addMessage("Form has undetermined loat type. Unable to load");
				logger.debug("Form has undetermined loat type. Unable to load form");
				form.setSelected(false);
				continue;
			}
			
			//JR423 begin
			//add new logic here
			//JR423 end
			
			logger.debug("========  Start loading form [" + form.getFormIdString() + "] ===============");
				
			if (FormDescriptor.LOAD_TYPE_NEW.equals(form.getLoadType())) {				
				String seqid = this.loadRepository.createForm(form, xmlPathName);
				if (seqid == null || seqid.length() == 0)
					form.setLoadStatus(FormDescriptor.STATUS_LOAD_FAILED);
				else
					form.setLoadStatus(FormDescriptor.STATUS_LOADED);
			} else if (FormDescriptor.LOAD_TYPE_NEW_VERSION.equals(form.getLoadType())) {
				float prevLatestVersion = this.loadRepository.getLatestVersionForForm(form.getPublicId());
				String seqid = this.loadRepository.createFormNewVersion(form, loggedinUser, xmlPathName);
				if (seqid == null || seqid.length() == 0)
					form.setLoadStatus(FormDescriptor.STATUS_LOAD_FAILED);
				else
					form.setLoadStatus(FormDescriptor.STATUS_LOADED);
				form.setPreviousLatestVersion(prevLatestVersion);
			} else if (FormDescriptor.LOAD_TYPE_UPDATE_FORM.equals(form.getLoadType())) {
				this.loadRepository.updateForm(form, loggedinUser, xmlPathName);
				if (form.getFormSeqId() == null || form.getFormSeqId().length() == 0)
					form.setLoadStatus(FormDescriptor.STATUS_LOAD_FAILED);
				else
				 form.setLoadStatus(FormDescriptor.STATUS_LOADED);
			} 
			
			logger.debug("========== Done loading form [" + form.getFormIdString() + "] =============");
			//form_idx++;
		}	
	}
	
	/**
	 * when loading, first check user from xml form. If that's invalid, 
	 * check with the form loader user name. If neither works, inform user
	 *  - confirmed with Denise on 8/7/2013
	 * @param form
	 * @param loggeduser
	 * @return
	 */
	protected boolean userHasRight(FormDescriptor form, String loggeduser) {
		String createdBy = form.getCreatedBy();
		String modifiedBy = form.getModifiedBy();
		String contextName = form.getContext();
		
		if (form.getLoadType().equals(FormDescriptor.LOAD_TYPE_UPDATE_FORM)) {
			//TODO: check with Denise on req. on this.
			if (modifiedBy == null || modifiedBy.length() == 0) {
				if (createdBy != null && createdBy.length() > 0) {
					form.addMessage("ModifiedBy in xml is empty. Reset modifiedBy with createdBy to update form");
					modifiedBy = createdBy;
					
				} else {
					form.addMessage("Both modifiedBy and createdBy in xml is empty. Reset modifiedBy with FormLoader logged-in user to update form");
					modifiedBy = loggeduser;
				}
				
				form.setModifiedBy(modifiedBy);
			}
			
			return checkUserRight(form, modifiedBy, loggeduser, contextName);

		} else {
			if (createdBy == null || createdBy.length() == 0) {
				form.addMessage("Form's createdBy is empty. Will try with Form Loader's logged-in user");
				createdBy = loggeduser;
				form.setCreatedBy(createdBy);
			}
			
			return checkUserRight(form, createdBy, loggeduser, contextName);
		}
		
	}
	
	protected boolean checkUserRight(FormDescriptor form, String formUser, String loggedinUser, String context) {
		if (!this.loadRepository.hasLoadFormRight(form, formUser, context)) {
			form.addMessage("User [" + formUser + "] has no right to load form in context [" + context + 
					"]. Will load with Form Loader logged-in user [" + loggedinUser + "]");
			
			if (!this.loadRepository.hasLoadFormRight(form, loggedinUser, context)) {
				form.addMessage("Form Loader loggedin user has right to load form. Form load failed");
				return false;
			}
		}
			
		return true;
	}
}
