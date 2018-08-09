package gov.nih.nci.cadsr.data;

public class CategoryNrds {
    private int cdeId;
    private float deVersion;
    private String deName;
	public int getCdeId() {
		return cdeId;
	}
	public void setCdeId(int cdeId) {
		this.cdeId = cdeId;
	}
	public float getDeVersion() {
		return deVersion;
	}
	public void setDeVersion(float deVersion) {
		this.deVersion = deVersion;
	}
	public String getDeName() {
		return deName;
	}
	public void setDeName(String deName) {
		this.deName = deName;
	}
	@Override
	public String toString() {
		return "CategoryNrds [cdeId=" + cdeId + ", deVersion=" + deVersion + ", deName=" + deName + "]";
	}

}
