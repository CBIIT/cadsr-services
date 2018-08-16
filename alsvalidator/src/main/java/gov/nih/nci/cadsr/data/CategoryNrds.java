package gov.nih.nci.cadsr.data;

public class CategoryNrds implements Comparable<Object>{
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cdeId;
		result = prime * result + Float.floatToIntBits(deVersion);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CategoryNrds other = (CategoryNrds) obj;
		if (cdeId != other.cdeId)
			return false;
		if (Float.floatToIntBits(deVersion) != Float.floatToIntBits(other.deVersion))
			return false;
		return true;
	}
	@Override
	public int compareTo(Object o) {
		int res = 0;
		if ((o == null) || (! (o instanceof CategoryNrds))) {
			return -1;
		}
		CategoryNrds other = (CategoryNrds)o;
		if (other.getCdeId() < this.getCdeId()){
			res = 1;
		}
		else if (other.getCdeId() > this.getCdeId()){
			res = -1;
		}
		else if (other.getDeVersion() < this.getDeVersion()) {
			res = -1; 
		}
		else if (other.getDeVersion() > this.getDeVersion()) {
			res = 1; 
		}
		return res;
	}
}
