package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private UUID id;
    private String content;
    private String sender;
    private ArrayList<String> receivers;
    private boolean isPrivate;
    
    public Message() {
    	
    }
    
    
	public Message(UUID id, String content, String sender, ArrayList<String> receivers, boolean isPrivate) {
		super();
		this.id = id;
		this.content = content;
		this.sender = sender;
		this.receivers = receivers;
		this.isPrivate = isPrivate;
	}


	public UUID getId() {
		return id;
	}


	public void setId(UUID id) {
		this.id = id;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}


	public String getSender() {
		return sender;
	}


	public void setSender(String sender) {
		this.sender = sender;
	}


	public ArrayList<String> getReceivers() {
		return receivers;
	}


	public void setReceivers(ArrayList<String> receivers) {
		this.receivers = receivers;
	}
	

	public boolean isPrivate() {
		return isPrivate;
	}


	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}


	@Override
    public String toString() {
        return "Content:" + content;
    }
}
