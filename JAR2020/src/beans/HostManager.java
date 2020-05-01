package beans;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import models.Host;
import models.User;
import ws.WSEndPoint;

@Singleton
@Startup
//@LocalBean
public class HostManager {

	private static final String PATHLOCAL = "http://localhost:8080/WAR2020/rest/server/";
	private static final String PATH = "http://192.168.1.10:8080/WAR2020/rest/server/";
	private static final String MASTERIP = "192.168.1.10";
	
	private String myIP = "";
	private String myAlias = "";
	
	@EJB
	DBBean db;
	
	@EJB
	WSEndPoint ws;

	
	@PostConstruct
	public void init() {
		System.out.println("\n\n------------------------------\nNEW SERVER STARTUP");
		
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("New servers IP address: " + ip.getHostAddress());
			System.out.println("New servers host name: " + ip.getHostName());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		myIP = ip.getHostAddress();
		myAlias = ip.getHostName();
		
		// Ako nije na master cvoru, uradi handshake
		if (!ip.getHostAddress().equals(MASTERIP)) {
			System.out.println("NOT ON MASTER NODE, PERFORMING HANDSHAKE...");
			if (handshake(new Host(ip.getHostName(), ip.getHostAddress(), false))) {
				System.out.println("HANDSHAKED SUCCEEDED");
			}
			else {
				System.out.println("HANDSHAKE ERROR, DELETE NODE FROM HOST LIST ON REMAINING NODES");
				// ...
			}
		}
		// Ako je na master cvoru samo se dodaj u listu hostova
		else {
			System.out.println("MASTER HOST STARTUP, NO FURTHER ACTIONS ARE NEEDED");
			db.getHosts().put(ip.getHostName(), new Host(ip.getHostName(), ip.getHostAddress(), true));
		}
		
	}
	
	@PreDestroy
	public void preDestroy() {
		System.out.println("\n\n--------------------------------------\nPREDESTROY");
				
		// za svaki host pozovi brisanje cvora iz liste hosotva i obrisi i usere sa tog cvora
		for (Host h : db.getHosts().values()) {
			String hostPath = "http://" + h.getAddress() + ":8080/WAR2020/rest/server/node/" + myAlias;
			
			try {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(hostPath);
				Response res = target.request().delete();
				String ret = res.readEntity(String.class);
				System.out.println("DELETE HOST RET: " + ret);
			}
			catch (Exception e) {
				System.out.println("ERROR IN NODE DELETION");
				return;
			}
		}
	}
	
	
	
	public boolean handshake(Host newHost) {
		// STEP 1 
		// Javi masteru da je podignut novi cvor i master ce ga dodati u listu svojih hostova 
		try {
			System.out.println("STEP1:\nNEW NODE WITH IP: " + myIP + " SENDING REQUEST TO MASTER WHERE MASTER WILL ADD SAID NODE TO HOSTS LIST AND PASS IT TO OTHER NODES TO DO THE SAME");
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(PATH+ "register");
			Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Host(newHost.getAlias(), newHost.getAddress(), false), MediaType.APPLICATION_JSON));
			String ret = res.readEntity(String.class);
			System.out.println(ret);
		}
		catch (Exception e) {
			System.out.println("ERROR IN STEP 1");
			return false;
		}
		
		// STEP 2
		// Nalazi se u register new node 
		
		// STEP 3
		// Novi cvor pita master cvor za spisak svih cvorova i master mu u odgovoru salje
		try {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(PATH+ "nodes");
			Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Host(newHost.getAlias(), newHost.getAddress(), false), MediaType.APPLICATION_JSON));
			Host[] ret = res.readEntity(Host[].class);
			System.out.println("ALL EXISTING NODES ARE PASSED TO NEW NODE");
			for (Host h : ret) {
				System.out.println("IME: " + h.getAlias() + "\nIP: " + h.getAddress());
				db.getHosts().put(h.getAlias(), h);
			}
		}
		catch (Exception e) {
			System.out.println("ERROR IN STEP 3");
			return false;
		}
		
		// STEP 4
		// Novi cvor pita master za spisak svih ulogovanih korisnika i master mu u odgovoru salje
		try {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(PATH+ "users/loggedin");
			Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Host(newHost.getAlias(), newHost.getAddress(), false), MediaType.APPLICATION_JSON));
			User[] ret = res.readEntity(User[].class);
			System.out.println("ALL USERS ARE BEING PASSED TO NEW NODE");
			for (User u : ret) {
				System.out.println("USERNAME: " + u.getUsername());
				db.getLoggedInUsers().put(u.getUsername(), u);
			}
		}
		catch (Exception e) {
			System.out.println("ERROR IN STEP 4");
			return false;
		}
		
		return true;
	}
	
	@Schedule(hour = "*", minute = "*/1", persistent = false)
	private void heartbeat() {
		if (myIP.equals(MASTERIP)) {
			System.out.println("SCHEDULED EVENT HAPPENED ON MASTER, CHECK HEARTBEAT");
			for (Host h : db.getHosts().values()) {
				String hostPath = "http://" + h.getAddress() + ":8080/WAR2020/rest/server/node/";
				try {
					ResteasyClient client = new ResteasyClientBuilder().build();
					ResteasyWebTarget target = client.target(hostPath);
					Response res = target.request().get();
					String ret = res.readEntity(String.class);
					System.out.println("HEARTBEAT: " + ret);
				}
				catch (Exception e) {
					try {
						ResteasyClient client = new ResteasyClientBuilder().build();
						ResteasyWebTarget target = client.target(hostPath);
						Response res = target.request().get();
						String ret = res.readEntity(String.class);
						System.out.println("HEARTBEAT: " + ret);
					}
					catch (Exception e1){
						System.out.println("DELETE THIS NODE..." + h.getAlias());
						for (Host h2 : db.getHosts().values()) {
							String hostPath2 = "http://" + h2.getAddress() + ":8080/WAR2020/rest/server/node/" + myAlias;
							
							try {
								ResteasyClient client = new ResteasyClientBuilder().build();
								ResteasyWebTarget target = client.target(hostPath2);
								Response res = target.request().delete();
								String ret = res.readEntity(String.class);
								System.out.println("DELETE HOST RET: " + ret);
							}
							catch (Exception e2) {
								System.out.println("ERROR IN NODE DELETION");
								return;
							}
						}
					}
				}
			}
		}
		else {
			System.out.println("SCHEDULED EVENT HAPPENED ON REGULAR HOST, DO NOTHING");
		}
	}

	
	
	
}