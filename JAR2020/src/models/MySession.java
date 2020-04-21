package models;

import java.io.Serializable;

public class MySession implements Serializable {

	private static final long serialVersionUID = 1L;
	String id;
	
	public MySession () {
		
	}
	
	public MySession (String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Session [id=" + id + "]";
	}
	
}
