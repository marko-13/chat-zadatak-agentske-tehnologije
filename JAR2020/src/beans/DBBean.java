package beans;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;

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
		//System.out.println("\n\nDB BEAN INSTANTIATED\n");
		users = new HashMap<String, User>();
		loggedInUsers = new HashMap<String, User>(); 
		allMessages = new HashMap<UUID, Message>();
		// inicijalno napravi da je host racunar desktop na kom si
		// kasnije stavi pravu ip adresu 192.168.1.10
		hosts = new HashMap<String, Host>();
		/*InetAddress ip = null;
		String hostName = "";
		try {
			ip = InetAddress.getLocalHost();
			hostName = ip.getHostName();
			System.out.println("IP address is: " + ip.getHostAddress());
			System.out.println("Hostname is: " + hostName);
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}*/
		//hosts.put("192.168.1.10", new Host("DesktopMaster", "192.168.1.10", true));
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
