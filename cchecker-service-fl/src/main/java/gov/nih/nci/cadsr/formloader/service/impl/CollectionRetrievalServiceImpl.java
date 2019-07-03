package gov.nih.nci.cadsr.formloader.service.impl;

import gov.nih.nci.cadsr.formloader.domain.DomainObjectTranslator;
import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.repository.FormLoaderRepository;
import gov.nih.nci.cadsr.formloader.service.CollectionRetrievalService;
import gov.nih.nci.cadsr.formloader.service.common.FormLoaderServiceException;
import gov.nih.nci.ncicb.cadsr.common.dto.FormV2TransferObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionRetrievalServiceImpl implements CollectionRetrievalService {

	private static final Logger logger = LoggerFactory.getLogger(CollectionRetrievalServiceImpl.class.getName());
	
	FormLoaderRepository repository;
	
	public CollectionRetrievalServiceImpl() {}
	
	public CollectionRetrievalServiceImpl(FormLoaderRepository repository) {
		this.repository = repository;
	}

	public FormLoaderRepository getRepository() {
		return repository;
	}

	public void setRepository(FormLoaderRepository repository) {
		this.repository = repository;
	}
	
	@Transactional(readOnly=true)
	@Override
	public List<FormCollection> getAllCollectionsByUser(String userName) throws FormLoaderServiceException {
		//Not check user credential. Check on unload
		
		if (userName == null || userName.length() == 0) 
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_USER_INVALID, 
					"User name is null or empty. Unable to get collections previously loader by user");
		
		List<FormCollection >colls = repository.getAllLoadedCollectionsByUser(userName);
		if (colls == null || colls.size() == 0) {
			logger.info("User [" + userName + "] doesn't seem have loaded any colleciton previously.");
			return new ArrayList<FormCollection>();
		}
		
		getAllFormsForCollections(colls);
		
		return colls;
	}
	
	public List<FormCollection> getAllFormsForCollections(List<FormCollection> colls) {
		
		for (FormCollection coll : colls) {
			//form info from Form Loader table
			List<FormDescriptor> forms = repository.getAllFormsWithCollectionId(coll.getId());
			if (forms == null || forms.size() == 0) {
				logger.warn("Collection " + coll.getId() + " doesn't have any form associated with it in database");
				coll.setForms(new ArrayList<FormDescriptor>());
				continue;
			}
			
			List<FormDescriptor> cadsrforms = getFormDetailsFromCaDsr(coll, forms);
			forms = combineFormInfo(forms, cadsrforms);
			coll.setForms(forms);				
		}
		
		return colls;
	}
	
	protected List<FormDescriptor> getFormDetailsFromCaDsr(FormCollection coll, List<FormDescriptor> forms) {
		List<String> formseqids = new ArrayList<String>();
		for (FormDescriptor form : forms) {
			String seqid = form.getFormSeqId();
			if (seqid != null && seqid.length() > 0)
				formseqids.add(seqid);
		}
		
		if (formseqids.size() == 0)
			return new ArrayList<FormDescriptor>();
		
		List<FormV2TransferObject> formdtos = repository.getFormsInCadsrBySeqids(formseqids);
		List<FormDescriptor> cadsrforms = DomainObjectTranslator.translateIntoFormDescriptors(coll, formdtos);
		
		return cadsrforms;
	}
	
	protected List<FormDescriptor> combineFormInfo(List<FormDescriptor> forms, List<FormDescriptor> cadsrforms) {

		for (FormDescriptor form : forms) {
			String formSeqid = form.getFormSeqId();
			if (formSeqid != null && formSeqid.length() > 0
					&& (form.getLoadStatus() == FormDescriptor.STATUS_LOADED ||
					form.getLoadStatus() == FormDescriptor.STATUS_UNLOADED)) {
				FormDescriptor cadsrForm = getMatchingCadsrForm(formSeqid, cadsrforms);
				if (cadsrForm == null) {
					//This means the form has a record in FL tables but doesn't in cadsr tables
					form.setLoadStatus(FormDescriptor.STATUS_NO_LONGER_EXISTS);
					form.addMessage("Form was previously loaded but it doesn't exist anymore in cadsr database.");
					continue;
				}

				form.setVersion(cadsrForm.getVersion());
				form.setLongName(cadsrForm.getLongName());
				form.setContext(cadsrForm.getContext());
				form.setModifiedBy(cadsrForm.getModifiedBy());
				form.setModifiedDate(cadsrForm.getModifiedDate());
				form.setCreatedBy(cadsrForm.getCreatedBy());
				form.setProtocolName(cadsrForm.getProtocolName());

				form.setType(cadsrForm.getType());
				form.setWorkflowStatusName(cadsrForm.getWorkflowStatusName());

			} else 
				form.setLoadUnloadDate(null);

		}

		return forms;
	}
	

	protected FormDescriptor getMatchingCadsrForm(String targetFormSeqid, List<FormDescriptor> cadsrforms) {
		for (FormDescriptor form : cadsrforms) {
			if (targetFormSeqid.equals(form.getFormSeqId()))
				return form;
		}
		
		return null;
	}
	
}
