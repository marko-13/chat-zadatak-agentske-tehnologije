package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private ArrayList<UUID> messages; // MOZDA MOZE I BEZ OVOGA
    private String hostIP;

    public User () {
    	
    }
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public User(String username, String password, ArrayList<UUID> messages, String hostIP) {
        this.username = username;
        this.password = password;
        this.messages = messages;
        this.hostIP = hostIP;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username, String password) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
	public ArrayList<UUID> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<UUID> messages) {
		this.messages = messages;
	}
	

	public String getHost() {
		return hostIP;
	}

	public void setHost(String hostIP) {
		this.hostIP = hostIP;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", host=" + hostIP + "]";
	}

}