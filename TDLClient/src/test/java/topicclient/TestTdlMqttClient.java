package topicclient;

import org.junit.Assert;
import org.junit.Test;

public class TestTdlMqttClient {

	@Test (expected=IllegalArgumentException.class)
	public void testSubscribeByValue() {
			TdlMqttClient client = new TdlMqttClient("http://localhost:8080/catalogue");
			client.subscribe("5a32971829e6a32884b7c21e");
	}
	
	@Test
	public void testSubscribeByValuetcp() {
			TdlMqttClient client = new TdlMqttClient("http://localhost:8080/catalogue");
			client.subscribe("5a35251a91f115248c68311a");
			Assert.assertTrue(true);
	}
	
	@Test
	public void testSubscribeByValueMqtt() {
		TdlMqttClient client = new TdlMqttClient("http://localhost:8080/catalogue");
		// TODO create tdl for that with localhost as todo
	}
	
	@Test
	public void testSubscribeByFilter() {
		// TODO 
	}
}


