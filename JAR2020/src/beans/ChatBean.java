package beans;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
//import javax.jms.ConnectionFactory;
//import javax.jms.Queue;
//import javax.jms.QueueConnection;
//import javax.jms.QueueSender;
//import javax.jms.QueueSession;
//import javax.jms.Session;
//import javax.jms.TextMessage;
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
import models.Message;
import models.User;
import ws.WSEndPoint;

@Stateless
@Path("/chat")
@LocalBean
public class ChatBean implements ChatRemote, ChatLocal {
		
	@EJB
	DBBean db;
	
	@EJB
	WSEndPoint ws;
	
	/*
	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;
	@Resource(mappedName = "java:jboss/exported/jms/queue/mojQueue")
	private Queue queue;
	*/
	
	
	// TEST
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
				
		return "OK";
	}
	
	// NE KORISTI SE
	public String post1(@PathParam("text") String text) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("Received message: " + text);
		
		// OVA LINIJA MENJA ZAKOMENTARISANI JMS JER JE SAD WS USPESNO INJEKTOVAN
		//ws.echoTextMessage(text);
		
		/*try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			// create and publish a message
			TextMessage message = session.createTextMessage();
			message.setText(text);
			sender.send(message);
		} catch (Exception ex) {
			ex.printStackTrace();
		}*/
		
		return "OK";
	}
	
	
	
	// REGISTER NEW USER
	@POST
	@Path("/users/register")
	@Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	public Response register(User myUser) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO REGISTER ENDPOINT");
		
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("New servers IP address: " + ip.getHostAddress());
			System.out.println("New servers host name: " + ip.getHostName());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return Response.status(400).entity("IP address error").build();
		}
		
		for (User u : db.getUsers().values()) {
			if (u.getUsername().equals(myUser.getUsername())) {
				System.out.println("Username already exists");
				return Response.status(400).build();
			}
		}
		
		myUser.setHost(ip.getHostAddress());
		db.getUsers().put(myUser.getUsername(), myUser);
		db.getLoggedInUsers().put(myUser.getUsername(), myUser);
		
		// PROSLEDI I OSTALIM HOSTOVIMA PODATAK O NOVOM KORISNIKU
		for (Host h : db.getHosts().values()) {
			if (h.getAddress().equals(ip.getHostAddress())) {
				continue;
			}
			String hostPath = "http://" + h.getAddress() + ":8080/WAR2020/rest/server/newUser";
			try {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(hostPath);
				Response res = target.request().post(Entity.entity(new User(myUser.getUsername(), myUser.getPassword(), null, ip.getHostAddress()), MediaType.APPLICATION_JSON));
				System.out.println("ADDED USER ON ANOTHER HOST");
			}
			catch (Exception e) {
				System.out.println("ERROR IN REQUEST TO SEND NEW USER TO OTHER NODES");
				return Response.status(400).build();
			}
		}
		
		System.out.println("User registered");
		return Response.status(200).build();
	}
	
	
	// LOGIN EXISTING USER
	@POST
	@Path("/users/login")
	@Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.TEXT_PLAIN)
	public Response login(User myUser) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO LOGIN ENDPOINT");
		
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("New servers IP address: " + ip.getHostAddress());
			System.out.println("New servers host name: " + ip.getHostName());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return Response.status(400).entity("IP address error").build();
		}
		
		for (User u : db.getUsers().values()) {
			if (u.getUsername().equals(myUser.getUsername()) && u.getPassword().equals(myUser.getPassword())) {
				System.out.println("User logged in");

				myUser.setHost(ip.getHostAddress());
				db.getLoggedInUsers().put(myUser.getUsername(), myUser);
				
				
				// PROSLEDI I OSTALIM HOSTOVIMA PODATAK O NOVOM KORISNIKU
				for (Host h : db.getHosts().values()) {
					if (h.getAddress().equals(ip.getHostAddress())) {
						continue;
					}
					String hostPath = "http://" + h.getAddress() + ":8080/WAR2020/rest/server/newUser";
					try {
						ResteasyClient client = new ResteasyClientBuilder().build();
						ResteasyWebTarget target = client.target(hostPath);
						Response res = target.request().post(Entity.entity(new User(myUser.getUsername(), myUser.getPassword(), null, ip.getHostAddress()), MediaType.APPLICATION_JSON));
						System.out.println("ADDED USER ON ANOTHER HOST");
					}
					catch (Exception e) {
						System.out.println("ERROR IN REQUEST TO SEND NEW USER TO OTHER NODES");
						return Response.status(400).build();
					}
				}
				
				return Response.status(200).entity("OK").build();
			}
		}
		
		System.out.println("Incorrect username or password");
		return Response.status(400).entity("Incorrect username or password").build();
	}
	
	
	// prodji kroz sve ostale hostove,IPAK NE MORA SVUDA PROSLEDJUJEM USERA KAD SE LOGUJE PA SU SVE LISTE AZURNE
	// GET ALL LOGGED IN USERS
	@GET
	@Path("users/loggedin")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<String> getLoggedInUsers() {
		
		List<String> usernames = new ArrayList<>();
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET ALL LOGGEDIN ENDPOINT");
		System.out.println("BROJ LOGOVANIH KORISNIKA: " + db.getLoggedInUsers().size());
		for (User u : db.getLoggedInUsers().values()) {
			System.out.println("Username: " + u.getUsername());
			usernames.add(u.getUsername());
		}
		Collection<String> myUsers = usernames;
		return myUsers;
		
	}
	
	// prodji kroz sve ostale hostove,IPAK NE MORA SVUDA PROSLEDJUEJM USERA KAD SE REGISTRUJE PA SU SVE LISTE AZURNE
	// GET ALL REGISTERED USERS
	@GET
	@Path("users/registered")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<String> getRegisteredUsers() {
		
		List<String> usernames = new ArrayList<>();
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET ALL REGISTERED ENDPOINT");
		System.out.println("BROJ REGISTROVANIH KORISNIKA: " + db.getUsers().size());
		for (User u : db.getUsers().values()) {
			System.out.println("Username: " + u.getUsername());
			usernames.add(u.getUsername());
		}
		Collection<String> myUsers = usernames;
		return myUsers;
		
	}

	
	//prodji kroz sve ostale hostove
	// SEND MESSAGE TO ALL LOGGED IN USERS
	@POST
	@Path("/messages/all")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(Message m) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO SEND TO ALL ENDPOINT");
		
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("New servers IP address: " + ip.getHostAddress());
			System.out.println("New servers host name: " + ip.getHostName());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return Response.status(400).entity("IP address error").build();
		}
		
		ArrayList<String> messageReceivers = new ArrayList<>();
		for (User u : db.getLoggedInUsers().values()) {
			messageReceivers.add(u.getUsername());
		}
		m.setReceivers(messageReceivers);
		
		System.out.println("Received message: " + m.getContent());
		System.out.println("Message sender: " + m.getSender());
		System.out.println("Message receivers: " + m.getReceivers());
		System.out.println("Message uuid: " + m.getId());
		
		db.getAllMessages().put(m.getId(), m);
		
		ws.echoTextMessage(m.getId().toString());
		
		for (Host h : db.getHosts().values()) {
			if (h.getAddress().equals(ip.getHostAddress())) {
				continue;
			}
			String hostPath = "http://" + h.getAddress() + ":8080/WAR2020/rest/server/message";
			try {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(hostPath);
				Response res = target.request().post(Entity.entity(new Message(m.getId(), m.getContent(), m.getSender(), m.getReceivers(), m.getTimeStamp(), m.getSubject(), m.getCategory()), MediaType.APPLICATION_JSON));
				System.out.println("FORWARDED MESSAGE TO OTHER HOST");
			}
			catch (Exception e) {
				System.out.println("ERROR IN REQUEST TO FORWAR MESSAGE TO OTHER HOSTS");
				return Response.status(400).build();
			}
		}
		
		return Response.status(200).entity("OK").build();
		/*
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			// create and publish a message
			TextMessage message = session.createTextMessage();
			message.setText(text);
			sender.send(message);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		*/
	}
	
	
	// prodji kroz sve ostale hostove
	// SEND MESSAGE TO ONE SPECIFIC USER
	@POST
	@Path("/messages/user")
	public Response postOne(Message m) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO SEND TO ONE ENDPOINT");
		
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
			System.out.println("New servers IP address: " + ip.getHostAddress());
			System.out.println("New servers host name: " + ip.getHostName());
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return Response.status(400).entity("IP address error").build();
		}
		
		System.out.println("Received message: " + m.getContent());
		System.out.println("Message sender: " + m.getSender());
		System.out.println("Message receivers: " + m.getReceivers());
		System.out.println("Message uuid: " + m.getId());
		System.out.println("Message subject: " + m.getSubject());
		System.out.println("Message timestamp: " + m.getTimeStamp());
		
		db.getAllMessages().put(m.getId(), m);
		
		
		//User u = db.getUsers().get(m.getReceivers().get(0));
		//ws.privateTextMessage(m.getContent(), u.getUsername());
		ws.echoTextMessage(m.getId().toString());
		
		for (Host h : db.getHosts().values()) {
			if (h.getAddress().equals(ip.getHostAddress())) {
				continue;
			}
			String hostPath = "http://" + h.getAddress() + ":8080/WAR2020/rest/server/message";
			try {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(hostPath);
				Response res = target.request().post(Entity.entity(new Message(m.getId(), m.getContent(), m.getSender(), m.getReceivers(), m.getTimeStamp(), m.getSubject(), m.getCategory()), MediaType.APPLICATION_JSON));
				System.out.println("FORWARDED PRIVATE MESSAGE TO OTHER HOST");
			}
			catch (Exception e) {
				System.out.println("ERROR IN REQUEST TO FORWAR PRIVATE MESSAGE TO OTHER HOSTS");
				return Response.status(400).build();
			}
		}
		
		return Response.status(200).entity("OK").build();
	}
	
	
	// GET ALL RECEIVED MESSAGES FROM USER
	@GET
	@Path("messages/{username}")
	public Response getMessages(@PathParam("username") String username) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET MESSAGES ENDPOINT");
		
		for(Message m : db.getAllMessages().values()) {
			System.out.println("UDJE OVDE");
			if (m.getCategory() == 2 || m.getCategory() == 3) {
				System.out.println("SKIP HELP MESSAGES");
				continue;
			}
			if (m.getReceivers().contains(username)) {
				System.out.println("OD: " + m.getSender());
				System.out.println("PORUKA: " + m.getContent());
			}
		}
		
		return Response.status(200).entity("OK").build();
	}
	
	// GET ALL RECEIVED MESSAGES FROM USER
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("messages2/{username}")
	public Collection<Message> getMessages2(@PathParam("username") String username) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET MESSAGES ENDPOINT");
		
		List<Message> messages = new ArrayList<>();
		for(Message m : db.getAllMessages().values()) {
			System.out.println("UDJE OVDE");
			if (m.getCategory() == 2 || m.getCategory() == 3) {
				System.out.println("SKIP HELP MESSAGES");
				continue;
			}
			if (m.getReceivers().contains(username)) {
				messages.add(m);
			}
		}
		Collection<Message> myMessages = messages;
		
		return myMessages;
	}

	
	//LOGOUT USER
	@DELETE
	@Path("/users/logout/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout(@PathParam ("username") String username, User myUser) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO LOGOUT ENDPOINT");
		
		for (User u : db.getLoggedInUsers().values()) {
			if (u.getUsername().equals(myUser.getUsername())) {
				db.getLoggedInUsers().remove(u.getUsername());
				
				//Obavesti i ostale hostove
				InetAddress ip = null;
				try {
					ip = InetAddress.getLocalHost();
					System.out.println("New servers IP address: " + ip.getHostAddress());
					System.out.println("New servers host name: " + ip.getHostName());
					
				} catch (UnknownHostException e) {
					e.printStackTrace();
					return Response.status(400).entity("IP address error").build();
				}
				
				for (Host h : db.getHosts().values()) {
					if (h.getAddress().equals(ip.getHostAddress())) {
						System.out.println("Sam sebi ne mora da salje request da obrise korisnika iz loggout na loggout");
						continue;
					}
					System.out.println(h.getAddress());
					String hostPath = "http://" + h.getAddress() + ":8080/WAR2020/rest/server/logoutUser";
					try {
						ResteasyClient client = new ResteasyClientBuilder().build();
						ResteasyWebTarget target = client.target(hostPath);
						Response res = target.request().post(Entity.entity(new User(myUser.getUsername(), myUser.getPassword(), null, ip.getHostAddress()), MediaType.APPLICATION_JSON));
						System.out.println("DELETED USER FROM ANOTHER HOST");
					}
					catch (Exception e) {
						System.out.println("ERROR IN REQUEST TO DELETE INACTIVE USER FROM OTHER NODES");
						return Response.status(400).build();
					}
				}
				return Response.status(200).entity("OK").build();
			}
		}
		
		return Response.status(200).entity("OK").build();

	}
	
}
