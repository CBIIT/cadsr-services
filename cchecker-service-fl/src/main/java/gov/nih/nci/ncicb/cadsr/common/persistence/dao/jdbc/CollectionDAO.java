package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;

import java.util.Date;
import java.util.List;

public interface CollectionDAO {
	
	public String createCollectionRecord(String name, String desc, String fileName, String filePath, String createdBy, int name_repeat);
	public int createCollectionFormMappingRecord(String collectionseqid, String formseqid, 
			int formpublicid, float formversion, String loadType, int loadStatus, 
			String longName, float prevLatestVersion, Date loadDate);
	
	public List<FormCollection> getAllLoadedCollectionsByUser(String userName);
	public List<FormDescriptor> getAllFormInfoForCollection(String collseqid);
}
