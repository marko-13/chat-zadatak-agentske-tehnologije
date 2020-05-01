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
    private long timeStamp;
    private String subject;
    private int category; // 0=salje_svima 1=salje_jednom 2=dodaje_lista_aktivnih 3=brise_lista_aktivnih
    
    public Message() {
    	
    }
    
    
	public Message(UUID id, String content, String sender, ArrayList<String> receivers, long timeStamp, String subject, int category) {
		super();
		this.id = id;
		this.content = content;
		this.sender = sender;
		this.receivers = receivers;
		this.timeStamp = timeStamp;
		this.subject = subject;
		this.category = category;
	}
	
	public Message(String content, int category) {
		super();
		this.id = UUID.randomUUID();
		this.content = content;
		this.sender = "";
		this.receivers = null;
		this.timeStamp = 0;
		this.subject = "";
		this.category = category;
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
	

	public long getTimeStamp() {
		return timeStamp;
	}


	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}


	public String getSubject() {
		return subject;
	}


	public void setSubject(String subject) {
		this.subject = subject;
	}


	public int getCategory() {
		return category;
	}


	public void setCategory(int category) {
		this.category = category;
	}


	@Override
    public String toString() {
        return "Content:" + content;
	}
}
