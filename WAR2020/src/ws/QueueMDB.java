package ws;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destionationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/mojQueue")
})
public class QueueMDB implements MessageListener{
	@EJB WSEndPoint ws;

	@Override
	public void onMessage(Message msg) {
		TextMessage tmsg = (TextMessage)msg;
		try {
			System.out.println("MDB: " + tmsg.getText());
			//ws.echoTextMessage(tmsg.getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
