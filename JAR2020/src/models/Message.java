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
    
    public Message() {
    	
    }
    
    
	public Message(UUID id, String content, String sender, ArrayList<String> receivers) {
		super();
		this.id = id;
		this.content = content;
		this.sender = sender;
		this.receivers = receivers;
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


	@Override
    public String toString() {
        return "Content:" + content;
    }
}
