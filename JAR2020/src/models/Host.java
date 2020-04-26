package models;

public class Host {

	private String alias;
	private String address;
	private boolean isMaster;
	
	public Host() {
		
	}

	public Host(String alias, String address, boolean isMaster) {
		this.alias = alias;
		this.address = address;
		this.isMaster = isMaster;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	@Override
	public String toString() {
		return "Host [alias=" + alias + ", address=" + address + ", isMaster=" + isMaster + "]";
	}
	
	
}
