package ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import beans.ChatLocal;

@Singleton
@ServerEndpoint("/ws")
@LocalBean
public class WSEndPoint {
	static List<Session> sessions = new ArrayList<Session>();
	
	
	@EJB
	ChatLocal chat;
	
	// Kad se neko konektuje dodaj ga na spisak
	@OnOpen
	public void onOpen(Session session) {
		if(!sessions.contains(session)) {
			sessions.add(session);
			System.out.println("\n\n-----------------------------------------------------------");
			System.out.println("SESSION ID: " + session.getId());
			try {
				session.getBasicRemote().sendText("ID_MOJE_SESIJE" + session.getId());
			} catch (IOException e) {
				e.printStackTrace();
			}
			/*System.out.println("SESSION SIZE: " + sessions.size());*/
		}
	}
	
	@OnMessage
	public void echoTextMessage(String msg) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("Chat bean returned: " + chat.test());
		try {
			for (Session s : sessions) {
				System.out.println("WSEndPoint: " + msg);
				s.getBasicRemote().sendText(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
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
	
	// Kad neko zatvori browser da ga skloni iz liste sesija
	@OnClose
	public void close(Session session) {
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("SESSION CLOSED. ID:  " + session.getId());
		sessions.remove(session);
	}
	
	// Ako dodje do greske skloni ga iz liste sesija
	@OnError
	public void error(Session session, Throwable t) {
		sessions.remove(session);
		t.printStackTrace();
	}
	

}
