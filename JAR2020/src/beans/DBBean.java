package beans;

import java.util.HashMap;
import java.util.UUID;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;

import models.Message;
import models.User;

@LocalBean
@Singleton
public class DBBean {

	private HashMap<String, User> users = new HashMap<>();
	private HashMap<String, User> loggedInUsers = new HashMap<>(); 
	private HashMap<UUID, Message> allMessages = new HashMap<>();
	
	public DBBean() {
		users = new HashMap<String, User>();
		loggedInUsers = new HashMap<String, User>(); 
		allMessages = new HashMap<UUID, Message>();
	}

	public HashMap<String, User> getUsers() {
		return users;
	}

	public HashMap<String, User> getLoggedInUsers() {
		return loggedInUsers;
	}

	public HashMap<UUID, Message> getAllMessages() {
		return allMessages;
	}

}
