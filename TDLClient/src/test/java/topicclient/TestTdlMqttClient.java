package topicclient;

import org.junit.Assert;
import org.junit.Test;

public class TestTdlMqttClient {

	@Test
	public void testSubscribeByValue() {
		
		try {
			TdlMqttClient client = new TdlMqttClient("http://localhost:8080/catalogue/get/");
			client.subscribe("5a32971829e6a32884b7c21e");
			Assert.assertTrue(true);
		} catch (Exception e) {
			Assert.fail();
		} 
	}
}
