package gov.nih.nci.ncicb.cadsr.common.dto;

public class ClassificationTransferObject
{
	public String csCsiIdSeq;
	public String idSeq;
	public String version;
	public String preferredDefinition;
	public String name;
	public String publicID;

	public String csiIdSeq;
	public String csiName;
	public String csiPublicID;
	public String csiVersion;
	public String csiType;
	
	public ClassificationTransferObject()
	{
		super();
	}

	public ClassificationTransferObject(String csCsiIdSeq, String idSeq, String version, String preferredDefinition, String name,
			String publicID, String csiIdSeq, String csiName, String csiPublicID, String csiVersion, String csiType) {
		super();
		this.csCsiIdSeq = csCsiIdSeq;
		this.idSeq = idSeq;
		this.version = version;
		this.preferredDefinition = preferredDefinition;
		this.name = name;
		this.publicID = publicID;
		this.csiIdSeq = csiIdSeq;
		this.csiName = csiName;
		this.csiPublicID = csiPublicID;
		this.csiVersion = csiVersion;
		this.csiType = csiType;
	}

	public String getCsCsiIdSeq() {
		return csCsiIdSeq;
	}

	public void setCsCsiIdSeq(String csCsiIdSeq) {
		this.csCsiIdSeq = csCsiIdSeq;
	}

	public String getIdSeq() {
		return idSeq;
	}

	public void setIdSeq(String idSeq) {
		this.idSeq = idSeq;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPreferredDefinition() {
		return preferredDefinition;
	}
	            
	public void setPreferredDefinition(String preferredDefinition) {
		this.preferredDefinition = preferredDefinition;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPublicID() {
		return publicID;
	}

	public void setPublicID(String publicID) {
		this.publicID = publicID;
	}

	public String getCsiIdSeq() {
		return csiIdSeq;
	}

	public void setCsiIdSeq(String csiIdSeq) {
		this.csiIdSeq = csiIdSeq;
	}

	public String getCsiName() {
		return csiName;
	}

	public void setCsiName(String csiName) {
		this.csiName = csiName;
	}

	public String getCsiPublicID() {
		return csiPublicID;
	}

	public void setCsiPublicID(String csiPublicID) {
		this.csiPublicID = csiPublicID;
	}

	public String getCsiVersion() {
		return csiVersion;
	}

	public void setCsiVersion(String csiVersion) {
		this.csiVersion = csiVersion;
	}

	public String getCsiType() {
		return csiType;
	}

	public void setCsiType(String csiType) {
		this.csiType = csiType;
	}

}
