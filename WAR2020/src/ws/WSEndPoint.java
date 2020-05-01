package ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import beans.ChatLocal;
import beans.DBBean;
import models.Message;


@Singleton
@ServerEndpoint("/ws/{username}")
@LocalBean
public class WSEndPoint {
	static HashMap<String, Session> sessions = new HashMap<>();
	
	public static HashMap<String, Session> getSessions() {
		return sessions;
	}

	public static void setSessions(HashMap<String, Session> sessions) {
		WSEndPoint.sessions = sessions;
	}

	@EJB
	ChatLocal chat;
	
	@EJB
	DBBean db;
	
	// Kad se neko konektuje dodaj ga na spisak
	@OnOpen
	public void onOpen(@PathParam("username")String username, Session session) {
		sessions.put(username, session);
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("USERNAME IZ WS: " + username);
		System.out.println("SESSION ID: " + session.getId());
		/*System.out.println("SESSION SIZE: " + sessions.size());*/
		
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String msgJSON = "";
		try {
			msgJSON = ow.writeValueAsString(new Message(username, 2));
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}		
		
		try {
			for (Session s : sessions.values()) {
				if(!s.getId().equals(sessions.get(username).getId())) {
					System.out.println("Add to logged in users list on frontend: " + username);
					s.getBasicRemote().sendText(msgJSON);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@OnMessage
	public void echoTextMessage(String msgId) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("Chat bean returned: " + chat.test());
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String msgJSON = "";
		try {
			msgJSON = ow.writeValueAsString(db.getAllMessages().get(UUID.fromString(msgId)));
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		Message myMessage = db.getAllMessages().get(UUID.fromString(msgId));
		// ako je kategorija 1 saljes svim aktivnim korisnicima poruku
		if (myMessage.getCategory() == 0) {
			System.out.println("\n\n-----------------------------------------------------------");
			System.out.println("PUBLIC MESSAGE");
			try {
				for (Session s : sessions.values()) {
					System.out.println("WSEndPoint: " + msgJSON);
					s.getBasicRemote().sendText(msgJSON);
	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		// ako je kategorija 1 saljes samo jednom korisniku poruku
		else if (myMessage.getCategory() == 1) {
			System.out.println("\n\n-----------------------------------------------------------");
			System.out.println("PRIVATE MESSAGE FOR USER: "+ myMessage.getReceivers().get(0) + "\nMESSAGE: " + myMessage.getContent());
			if (sessions.containsKey(myMessage.getReceivers().get(0))) {
				try {
					sessions.get(myMessage.getReceivers().get(0)).getBasicRemote().sendText(msgJSON);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
			return;
		}
		// ako je kategorija 2 ili 3(dodaj i obrisi iz liste aktivnih korisnika) to se radi u onopen onclose
		else {
			System.out.println("\n\n-----------------------------------------------------------");
			System.out.println("Category error in method ws.echoTextMessage");
			return;
		}
	
		
	}
	
	
	// NE KORISTI SE VISE
	public void privateTextMessage(String msg, String receiver) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("PRIVATE MESSAGE FOR USER: "+ receiver + "\nMESSAGE: " + msg);
			if (sessions.containsKey(receiver)) {
				try {
					sessions.get(receiver).getBasicRemote().sendText(msg);
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		
	}
	
	/*@OnMessage
	public void echoTextMessage(Session session, String msg, boolean last) {
		try {
			if (session.isOpen()) {
				for (Session s: sessions) {
					if (!s.getId().equals(session.getId())) {
						s.getBasicRemote().sendText(msg, last);
					}
				}
			}
		} catch (IOException e) {
			try {
				session.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}*/
	
	// Kad neko zatvori browser skloni ga iz liste sesija
	@OnClose
	public void close(@PathParam("username")String username, Session session) {
		sessions.remove(username);
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("SESSION CLOSED. ID:  " + session.getId());
		System.out.println("SESSION CLOSED FOR USER: "+ username + "\nLIST OF REMAINING ACTIVE USERS:");
		
		// delete from list of logged in users(same action on logout button and onerror session)
		db.getLoggedInUsers().remove(username);

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String msgJSON = "";
		try {
			msgJSON = ow.writeValueAsString(new Message(username, 3));
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}		
		
		for (String str : sessions.keySet()) {
			System.out.println(str);
		}
		
			try {
				for (Session s : sessions.values()) {
					System.out.println("Delete from logged in users list on frontend: " + username);
					s.getBasicRemote().sendText(msgJSON);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	// Ako dodje do greske skloni ga iz liste sesija
	@OnError
	public void error(@PathParam("username")String username, Session session, Throwable t) {
		sessions.remove(username);
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("SESSION ERROR FOR USER: "+ username + "\nLIST OF REMAINING ACTIVE USERS:");
		for (String str : sessions.keySet()) {
			System.out.println(str);
		}
		
		// delete from list of logged in users(same action on logout button and onclose session)
		db.getLoggedInUsers().remove(username);

		
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String msgJSON = "";
		try {
			msgJSON = ow.writeValueAsString(new Message(username, 3));
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}		
		
		try {
			for (Session s : sessions.values()) {
				System.out.println("Delete from logged in user lists on frontend: " + username);
				s.getBasicRemote().sendText(msgJSON);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		t.printStackTrace();
	}
	

}
