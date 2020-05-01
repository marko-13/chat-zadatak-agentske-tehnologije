package beans;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import models.Host;

public class ServerREST {

	private static final String PATHLOCAL = "http://localhost:8080/WAR2020/rest/server/";
	private static final String PATH = "http://192.168.1.10:8080/WAR2020/rest/server/";
	private static final String MASTERIP = "192.168.1.10";
	
	public static void testiraj () {
			
			InetAddress ip = null;
			try {
				ip = InetAddress.getLocalHost();
				System.out.println("New servers IP address: " + ip.getHostAddress());
				System.out.println("New servers host name: " + ip.getHostName());
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return;
			}
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(PATH+ "nodes");
			Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(new Host(ip.getHostName(), ip.getHostAddress(), true), MediaType.APPLICATION_JSON));
			Host[] ret = res.readEntity(Host[].class);
			
			for (Host h : ret) {
				System.out.println("IME: " + h.getAlias());
			}
		}
}
