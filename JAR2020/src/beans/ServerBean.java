package beans;

import java.io.IOException;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import models.Host;
import models.Message;
import ws.WSEndPoint;

@Stateless
@Path("/server")
@LocalBean
public class ServerBean {

	@EJB
	DBBean db;
	
	@EJB
	WSEndPoint ws;
	
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public String registerNewNode(Host newHost) {
		System.out.println("\n\nREGISTRUJE NOVI CVOR\n\n");
		
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
	@Consumes(MediaType.TEXT_PLAIN)
	public String informNewNodeAboutAllExistingNodes(String pod) {
		System.out.println("MASTER CVOR SALJE NOVOM CVORU SVE NEMASTER CVOROVE KOJI POSTOJE");
		
		// .....
		
		return "OK";
	}
	
	@POST
	@Path("/users/loggedin")
	@Consumes(MediaType.TEXT_PLAIN)
	public String sendAllLoggedInUsersToNewNode(String pod) {
		System.out.println("NOVOM CVORU SE SALJU SVI ULOGOVANI KORISNICI");
		
		// .....
		
		return "OK";
	}
	
	@DELETE
	@Path("/node/{alias}")
	public String deleteNodeIfHandshakeHasFailed(@PathParam("alias")String alias) {
		System.out.println("AKO JE HANDSHAKE BIO NEUSPESAN, OBRISI NOVI CVOR KOJI JE PROBAO DA SE DODA IZ SVIH LISTI");
		
		db.getHosts().remove(alias);
		
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
