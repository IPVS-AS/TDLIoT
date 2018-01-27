package topicclient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import de.unistuttgart.ipvs.tdl.client.TdlUtil;

/**
 * Test of {@link TdlMqttClient} with a catalogue server + broker available.
 * Adapt the configurations strings to your environment.
 */
public class TestTdlMqttClient {

	// Test configuration. Fill in your own environment here
	String catalogue = "http://192.168.209.199:8080/catalogue";
	String mqttbroker = "tcp://192.168.209.199:1883";
	
	// Test variables
	TdlUtil util = null;
	TdlMqttClient testclient = null;
	String validUID = "";
	String notValidUID = "";
	String notValidUIDField = "";
	String demoUID = "";

	@Before
	public void setupMongoDB() {
		// Live server
		util = new TdlUtil(catalogue);

		TestData.tdldemo = TestData.demoStringWithBroker(mqttbroker);
		validUID = util.addTopicDescription(TestData.tdlValid);
		notValidUID = util.addTopicDescription(TestData.tdlInvalid2);
		notValidUIDField = util.addTopicDescription(TestData.tdlInvalid1);
		demoUID = util.addTopicDescription(TestData.tdldemo);

		testclient = new TdlMqttClient(catalogue);
	}

	@After
	public void tearDownMongoDB() {
		testclient.disconnect();
		util.deleteTopicDescription(validUID);
		util.deleteTopicDescription(notValidUID);
		util.deleteTopicDescription(notValidUIDField);
		util.deleteTopicDescription(demoUID);
	}

	@Test
	public void testDemo() {
		// demonstration of connect to publish/subscribe broker
		String topic = testclient.addPublishTdlId(demoUID);
		Assert.assertEquals("/test", topic);
		testclient.subscribe(demoUID);
		// publish something -> receives
		testclient.publishById(demoUID, "Hallo Welt".getBytes());

		// wait for receive
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
		}
		
		// replace callback
		MqttCallback stringCallback = new MqttCallback() {

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				String content = "<no text>";
				try {
					content = new String(message.getPayload());
				} catch (Exception e) {
					// do nothing
				}
				System.out.println("Received message: " + content);

			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				System.out.println("Send message successful");
			}

			@Override
			public void connectionLost(Throwable cause) {
				// TODO Auto-generated method stub

			}
		};
		testclient.updateCallback(stringCallback);
		
		// publish something again
		testclient.publish("/test", "Hallo Welt erneut".getBytes());
	}

	@Test
	public void testListing() {
		String topic = testclient.addPublishTdl(TestData.tdlValid);
		Assert.assertNotNull(testclient.getAvailableTopics().containsValue(topic));
	}

	@Test(expected = NullPointerException.class)
	public void testPublishTopicNotFound() {
		testclient.publish("not existing", "".getBytes());
	}

	@Test(expected = NullPointerException.class)
	public void testPublishIDTopicNotFound() {
		testclient.publishById("not existing", "".getBytes());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSubscribeNotMQTT() {
		testclient.subscribe(Arrays.asList(notValidUID));
	}

	@Test
	public void testSubscribeFieldMissing() {
		testclient.subscribe(Arrays.asList(notValidUIDField));
	}

	@Test
	public void testSubscribeSearch() {
		Map<String, String> filterParameter = new HashMap<>();
		filterParameter.put("path", "/test");
		testclient.subscribe(filterParameter);
	}

	@Test
	public void testPublishSearch() {
		String search = "{\"filters\":{\"path\":\"/test\"}}";
		testclient.addPublishSearch(search);
	}

	@Test
	public void testCallbackNull() {
		Assert.assertNull(testclient.updateCallback(null));
	}

	@Test
	public void testPublishTDLNotMQTT() {
		Assert.assertEquals("", testclient.addPublishTdl(TestData.tdlInvalid3));
		Assert.assertEquals("", testclient.addPublishTdl(TestData.tdlInvalid2));
		Assert.assertEquals(null, testclient.addPublishTdl("{}"));
		Assert.assertEquals(null, testclient.addPublishTdl("{\"}"));
	}
}
