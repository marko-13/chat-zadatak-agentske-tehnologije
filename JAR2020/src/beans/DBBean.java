package beans;

import java.util.HashMap;
import java.util.UUID;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;

import models.Host;
import models.Message;
import models.User;

@LocalBean
@Singleton
public class DBBean {

	private HashMap<String, User> users = new HashMap<>();
	private HashMap<String, User> loggedInUsers = new HashMap<>(); 
	private HashMap<UUID, Message> allMessages = new HashMap<>();
	private HashMap<String, Host> hosts = new HashMap<>();
	
	public DBBean() {
		users = new HashMap<String, User>();
		loggedInUsers = new HashMap<String, User>(); 
		allMessages = new HashMap<UUID, Message>();
		hosts = new HashMap<String, Host>();
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

	public HashMap<String, Host> getHosts() {
		return hosts;
	}

	public void setHosts(HashMap<String, Host> hosts) {
		this.hosts = hosts;
	}


	@Override
	public String toString() {
		return "DBBean [users=" + users + ", loggedInUsers=" + loggedInUsers + ", allMessages=" + allMessages
				+ ", hosts=" + hosts + "]";
	}
	
	public String ispisSvega() {
		String korisnici = "REGISTROVANI KORISNICI:\n";
		for (User u : users.values()) {
			korisnici += u.getUsername() + "\n";
		}
		
		String korisnici2 = "AKTIVNI KORISNICI:\n";
		for (User u : loggedInUsers.values()) {
			korisnici2 += u.getUsername() + "\n";
		}
		
		String cvorovi = "CVOROVI:\n";
		for (Host h : hosts.values()) {
			cvorovi += h.getAlias() + "\n";
		}
		
		return "PODACI U DB beanu:\n" + korisnici + korisnici2 + cvorovi;
		
	}

}
