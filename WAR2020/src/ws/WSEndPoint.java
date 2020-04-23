package ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import beans.ChatLocal;

@Singleton
@ServerEndpoint("/ws/{username}")
@LocalBean
public class WSEndPoint {
	static List<Session> sessions = new ArrayList<Session>();
	static Map<String, Session> sessions1 = Collections.synchronizedMap(new HashMap<>());
	
	
	@EJB
	ChatLocal chat;
	
	// Kad se neko konektuje dodaj ga na spisak
	@OnOpen
	public void onOpen(@PathParam("username")String username, Session session) {
		sessions1.put(username, session);
		// sessions.add(session);
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("USERNAME IZ WS: " + username);
		System.out.println("SESSION ID: " + session.getId());
		/*System.out.println("SESSION SIZE: " + sessions.size());*/
	}
	
	@OnMessage
	public void echoTextMessage(String msg) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("Chat bean returned: " + chat.test());
		try {
			for (Session s : sessions1.values()) {
				System.out.println("WSEndPoint: " + msg);
				s.getBasicRemote().sendText(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
	}
	
	public void privateTextMessage(String msg, String receiver) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("PRIVATE MESSAGE FOR USER: "+ receiver + "\nMESSAGE: " + msg);
			if (sessions1.containsKey(receiver)) {
				try {
					sessions1.get(receiver).getBasicRemote().sendText(msg);
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
		sessions.remove(session);
		sessions1.remove(username);
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("SESSION CLOSED. ID:  " + session.getId());
		System.out.println("SESSION CLOSED FOR USER: "+ username + "\nLIST OF REMAINING ACTIVE USERS:");
		for (String str : sessions1.keySet()) {
			System.out.println(str);
		}
	}
	
	// Ako dodje do greske skloni ga iz liste sesija
	@OnError
	public void error(@PathParam("username")String username, Session session, Throwable t) {
		sessions.remove(session);
		sessions1.remove(username);
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("SESSION ERROR FOR USER: "+ username + "\nLIST OF REMAINING ACTIVE USERS:");
		for (String str : sessions1.keySet()) {
			System.out.println(str);
		}
		t.printStackTrace();
	}
	

}
