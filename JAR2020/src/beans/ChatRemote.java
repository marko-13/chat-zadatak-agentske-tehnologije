package beans;

import javax.ejb.Remote;


@Remote
public interface ChatRemote {
	public String post1(String test);
}
