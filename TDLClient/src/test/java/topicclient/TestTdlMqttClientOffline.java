package topicclient;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doReturn;

import java.util.Arrays;
import java.util.HashMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.unistuttgart.ipvs.tdl.client.TdlUtil;

/**
 * Tests for the {@link TdlMqttClient} without a catalogue and broker available.
 * Covers less than the online counterpart.
 * May throw some errors because the logging does not like the mocking.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ TdlUtil.class, TdlMqttClient.class })
public class TestTdlMqttClientOffline {

	boolean isLive = true;
	TdlUtil util = null;
	TdlMqttClient testclient = null;
	String validUID = "5a6718fc24aa9a00076b7238";
	String notValidUID = "";
	String notValidUIDField = "";

	@Before
	public void setupMongoDB() throws Exception {
		// Mockito
		util = new TdlUtil("http://localhost:8080/catalogue");
		this.mockRequests();

		validUID = util.addTopicDescription(TestData.tdlValid);
		notValidUID = util.addTopicDescription(TestData.tdlInvalid2);
		notValidUIDField = util.addTopicDescription(TestData.tdlInvalid1);

		PowerMockito.mockStatic(TdlUtil.class);
		PowerMockito.whenNew(TdlUtil.class).withAnyArguments().thenReturn(util);

		testclient = new TdlMqttClient("");

	}

	@After
	public void tearDownMongoDB() {
		testclient.disconnect();
		util.deleteTopicDescription(validUID);
		util.deleteTopicDescription(notValidUID);
		util.deleteTopicDescription(notValidUIDField);
	}

	// no broker available
	// @Test
	// public void testDemo() {
	// String topic = testclient.addPublishTdlId("5a671ae924aa9a00076b7239");
	// Assert.assertEquals("/test", topic);
	// testclient.subscribe("5a671ae924aa9a00076b7239");
	// testclient.publishById("5a671ae924aa9a00076b7239", "Hallo Welt".getBytes());
	//
	// MqttCallback stringCallback = new MqttCallback() {
	//
	// @Override
	// public void messageArrived(String topic, MqttMessage message) throws
	// Exception {
	// String content = "<no text>";
	// try {
	// content = new String(message.getPayload());
	// } catch (Exception e) {
	// // do nothing
	// }
	// System.out.println("Received message: " + content);
	//
	// }
	//
	// @Override
	// public void deliveryComplete(IMqttDeliveryToken token) {
	// System.out.println("Send message successful");
	// }
	//
	// @Override
	// public void connectionLost(Throwable cause) {
	// // TODO Auto-generated method stub
	//
	// }
	// };
	//
	// testclient.updateCallback(stringCallback);
	// testclient.publish("/test", "Hallo Welt erneut".getBytes());
	// }

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

	@Test
	public void testSubscribeNotMQTT() {
		testclient.subscribe(Arrays.asList(notValidUID));
	}

	@Test
	public void testSubscribeFieldMissing() {
		testclient.subscribe(Arrays.asList(notValidUIDField));
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

	private void mockRequests() {
		util = PowerMockito.spy(util);

		HashMap<String, String> mockedTdls = new HashMap<>();
		mockedTdls.put("1", TestData.tdlValid);
		mockedTdls.put("2", TestData.tdlInvalid2);
		mockedTdls.put("3", TestData.tdlInvalid1);

		try {
			doReturn("1").when(util, "addTopicDescription", TestData.tdlValid);
			doReturn("2").when(util, "addTopicDescription", TestData.tdlInvalid2);
			doReturn("3").when(util, "addTopicDescription", TestData.tdlInvalid1);
			doReturn("-1").when(util, "addTopicDescription", anyString());

			doReturn(true).when(util).deleteTopicDescription(anyString());
			doReturn(TestData.tdlValidServer).when(util).getTopicDescriptionWithId("1");
			doReturn(TestData.tdlInvalid2Server).when(util).getTopicDescriptionWithId("2");
			doReturn(TestData.tdlInvalid1Server).when(util).getTopicDescriptionWithId("3");
			doReturn(TestData.tdlInvalid3Server).when(util).getTopicDescriptionWithId("-1");

		} catch (Exception e) {
			Assert.fail("Mocking fail! " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
}
