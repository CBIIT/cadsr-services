package gov.nih.nci.ncicb.cadsr.common.persistence.dao;


import java.util.HashMap;
import java.util.List;

import gov.nih.nci.ncicb.cadsr.common.dto.PermissibleValueV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueDomainV2;

/**
 * Mod. from  ValueDomainV2DAO
 * @author yangs8
 *
 */
public interface ValueDomainDAOV2 extends AdminComponentDAOV2 {
  
	public ValueDomainV2 getValueDomainV2ById(String vdId);
	public HashMap<String, List<PermissibleValueV2TransferObject>> getPermissibleValuesByVdIds(List<String> vdSeqIds);
	//public List<PermissibleValueV2TransferObject> getPermissibleValuesByVdIds(List<String> vdSeqIds);
  
}
