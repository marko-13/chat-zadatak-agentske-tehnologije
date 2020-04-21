package beans;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import models.Message;
import models.MySession;
import models.User;
import ws.WSEndPoint;

@Stateless
@Path("/chat")
@LocalBean
public class ChatBean implements ChatRemote, ChatLocal {
	
	private HashMap<String, User> users = new HashMap<>();
	private HashMap<String, User> loggedInUsers = new HashMap<>(); 
	private HashMap<UUID, Message> allMessages = new HashMap<>();
	
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
		ws.echoTextMessage(text);
		
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
		
		for (User u : users.values()) {
			if (u.getUsername().equals(myUser.getUsername())) {
				System.out.println("Username already exists");
				return Response.status(400).build();
			}
		}
		
		users.put(myUser.getUsername(), myUser);
		loggedInUsers.put(myUser.getUsername(), myUser);
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
		
		for (User u : users.values()) {
			if (u.getUsername().equals(myUser.getUsername()) && u.getPassword().equals(myUser.getPassword())) {
				System.out.println("User logged in");
				loggedInUsers.put(myUser.getUsername(), myUser);
				return Response.status(200).entity("OK").build();
			}
		}
		
		System.out.println("Incorrect username or password");
		return Response.status(400).entity("Incorrect username or password").build();
	}
	
	
	// GET ALL LOGGED IN USERS
	@GET
	@Path("users/loggedin")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<String> getLoggedInUsers() {
		
		List<String> usernames = new ArrayList<>();
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET ALL LOGGEDIN ENDPOINT");
		System.out.println("BROJ LOGOVANIH KORISNIKA: " + loggedInUsers.size());
		for (User u : loggedInUsers.values()) {
			System.out.println("Username: " + u.getUsername());
			usernames.add(u.getUsername());
		}
		Collection<String> myUsers = usernames;
		return myUsers;
		
	}
	
	
	// GET ALL REGISTERED USERS
	@GET
	@Path("users/registered")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<String> getRegisteredUsers() {
		
		List<String> usernames = new ArrayList<>();
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET ALL REGISTERED ENDPOINT");
		System.out.println("BROJ REGISTROVANIH KORISNIKA: " + users.size());
		for (User u : users.values()) {
			System.out.println("Username: " + u.getUsername());
			usernames.add(u.getUsername());
		}
		Collection<String> myUsers = usernames;
		return myUsers;
		
	}

	
	// SEND MESSAGE TO ALL LOGGED IN USERS
	@POST
	@Path("/messages/all")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response post(Message m) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO SEND TO ALL ENDPOINT");
		
		ArrayList<String> messageReceivers = new ArrayList<>();
		for (User u : loggedInUsers.values()) {
			messageReceivers.add(u.getUsername());
		}
		m.setReceivers(messageReceivers);
		
		System.out.println("Received message: " + m.getContent());
		System.out.println("Message sender: " + m.getSender());
		System.out.println("Message receivers: " + m.getReceivers());
		System.out.println("Message uuid: " + m.getId());
		
		allMessages.put(m.getId(), m);
		
		ws.echoTextMessage(m.getContent());
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
	
	
	// SEND MESSAGE TO ONE SPECIFIC USER
	@POST
	@Path("/messages/user")
	public Response postOne(Message m) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO SEND TO ONE ENDPOINT");
		
		System.out.println("Received message: " + m.getContent());
		System.out.println("Message sender: " + m.getSender());
		System.out.println("Message receivers: " + m.getReceivers());
		System.out.println("Message uuid: " + m.getId());
		
		allMessages.put(m.getId(), m);
		
		return Response.status(200).entity("OK").build();
	}
	
	
	// GET ALL RECEIVED MESSAGES FROM USER
	@GET
	@Path("messages/{username}")
	public Response getMessages(@PathParam("username") String username) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO GET MESSAGES ENDPOINT");
		
		for(Message m : allMessages.values()) {
			if (m.getReceivers().contains(username)) {
				System.out.println("OD: " + m.getSender());
				System.out.println("PORUKA: " + m.getContent());
			}
		}
		
		return Response.status(200).entity("OK").build();
	}

	
	//LOGOUT USER
	@DELETE
	@Path("/users/logout/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout(@PathParam ("username") String username, User myUser) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO LOGOUT ENDPOINT");
		
		for (User u : loggedInUsers.values()) {
			if (u.getUsername().equals(myUser.getUsername())) {
				loggedInUsers.remove(u.getUsername());
				return Response.status(200).entity("OK").build();
			}
		}
		return Response.status(200).entity("OK").build();

	}
	
	
	

	//UPDATE SESSION ID 
	// NADJI BOLJI NACIN DA SPOJIS ULOGOVANOG KORISNIKA SA ODGOVARAJUCOM SESSIJOM
	@POST
	@Path("users/update_session_id/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateSessionId (@PathParam("username") String username, MySession s) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("POGODIO UPDATE SESSION ID ENDPOINT");
		User myUser = new User();
		for (User u : loggedInUsers.values()) {
			if (u.getUsername().equals(username)) {
				myUser = u;
				break;
			}
		}
		System.out.println("PROSLA SESIJA: " + myUser.getSessionId());
		myUser.setSessionId(s.getId());
		System.out.println("NOVA SESIJA: " + myUser.getSessionId());
		loggedInUsers.replace(username, myUser);
		
		return Response.status(200).build();
		
	}
	
}
