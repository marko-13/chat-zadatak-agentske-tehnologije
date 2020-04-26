package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private ArrayList<UUID> messages; // MOZDA MOZE I BEZ OVOGA
    private Host host;

    public User () {
    	
    }
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public User(String username, String password, ArrayList<UUID> messages, Host host) {
        this.username = username;
        this.password = password;
        this.messages = messages;
        this.host = host;
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
	

	public Host getHost() {
		return host;
	}

	public void setHost(Host host) {
		this.host = host;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", host=" + host.getAddress() + "]";
	}

}