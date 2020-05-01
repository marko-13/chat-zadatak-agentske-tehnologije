package beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.websocket.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import models.Host;
import models.User;
import ws.WSEndPoint;

@Stateless
@Path("/server")
@LocalBean
public class ServerBean {

	@EJB
	DBBean db;
	
	@EJB
	WSEndPoint ws;
	
	@GET
	@Path("/test")
	public String test() {
		System.out.println("UDJE U TEST");
		
		return "OK";
	}
	
	
	// NOVI CVOR SE JAVLJA MASTERU I MASTER GA DODAJE U LISTU HOSTOVA
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public String registerNewNode(Host newHost) {
		System.out.println("\n\nREGISTRUJE NOVI CVOR:\nAdresa: " + newHost.getAddress() + "\nAlias: " + newHost.getAlias());
		
		
		// prodji kroz sve cvorove i javi im da dodaju novi cvor u svoje liste
		for (Host h: db.getHosts().values()) {
			System.out.println("MASTER OBAVESTAVA CVOR: " + h.getAddress());
			String hostPath = "http://" + h.getAddress() + ":8080/WAR2020/rest/server/node/";
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(hostPath);
			Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Host(newHost.getAlias(), newHost.getAddress(), false), MediaType.APPLICATION_JSON));
			String ret = res.readEntity(String.class);
		}
		db.getHosts().put(newHost.getAlias(), newHost);

		return "Master covr dobio podatke o novom cvoru i dodao ga u listu cvorova";		
	}
	 
	
	
	@POST
	@Path("/node")
	@Consumes(MediaType.APPLICATION_JSON)
	public String informNodesAboutNewNode(Host newHost) {
		System.out.println("\n\nMASTER CVOR JAVLJA OSTALIM NEMASTER CVOROVIMA O DOLASKU NOVOG CVORA");
		
		db.getHosts().put(newHost.getAlias(), newHost);
		
		return "Nemaster cvor dobio podatke o novom cvoru od mastera i dodao ga u listu cvorova";
	}
	
	@POST
	@Path("/nodes")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Host> informNewNodeAboutAllExistingNodes(Host newHost) {
		System.out.println("MASTER CVOR SALJE NOVOM CVORU SVE NEMASTER CVOROVE KOJI POSTOJE");
		
		// .....
		List<Host> hosts = new ArrayList<>();
		for (Host h : db.getHosts().values()) {
			System.out.println("Host: " + h.getAlias());
			hosts.add(h);
		}
		Collection<Host> myHosts = hosts;
		return myHosts;
	}
	
	@POST
	@Path("/users/loggedin")
	@Consumes(MediaType.APPLICATION_JSON)
	public Collection<User> sendAllLoggedInUsersToNewNode(Host newHost) {
		System.out.println("NOVOM CVORU SE SALJU SVI ULOGOVANI KORISNICI");
		
		// .....
		List<User> users = new ArrayList<>();
		for (User u : db.getLoggedInUsers().values()) {
			System.out.println("User: " + u.getUsername());
			users.add(u);
		}
		Collection<User> myUsers = users;
		return myUsers;
	}
	
	@DELETE
	@Path("/node/{alias}")
	public String deleteNodeIfHandshakeHasFailed(@PathParam("alias")String alias) {
		System.out.println("AKO JE HANDSHAKE BIO NEUSPESAN, OBRISI NOVI CVOR KOJI JE PROBAO DA SE DODA IZ SVIH LISTI");
		
		db.getHosts().remove(alias);
		
		for (User u : db.getLoggedInUsers().values()) {
			if (u.getHost().equals(alias)) {
				System.out.println("REMOVING USERS: " + u.getUsername());
				db.getLoggedInUsers().remove(u.getUsername());
			}
		}
		System.out.println("BECAUSE HOST WAS STOPPED");
		
		return "OK";
	}
	
	@GET
	@Path("/node")
	public String heartbeat() {
		System.out.println("PERIODICNO PROVERAVAJ DA LI SU SVI CVOROVI AKTIVNI");
		return "OK";
	}
	
	@GET
	@Path("/message")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response addNewUser(String myMessageJSON) {
		
		for (Session s : ws.getSessions().values()) {
			try {
				s.getBasicRemote().sendText(myMessageJSON);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return Response.status(200).build();
	}
}
