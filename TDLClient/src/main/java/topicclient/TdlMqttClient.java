package topicclient;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TdlMqttClient {

	private final Client client = ClientBuilder.newClient();
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final HashMap<String, MqttClient> brokers = new HashMap<>();
	private final String catalogue;

	private MqttCallback defaultCallback = null;

	public TdlMqttClient(String catalogueUrl) {

		catalogue = catalogueUrl;

		defaultCallback = new MqttCallback() {

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				log.info("Message received");
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				// do nothing
			}

			@Override
			public void connectionLost(Throwable cause) {
				log.warn("Connection lost to broker because of {}.", cause.getMessage());
			}
		};
	}

	/**
	 * Subscribe to a single topic with this id.
	 * @param tdl topic id to subscribe to
	 */
	public void subscribe(String tdl) {
		subscribe(Arrays.asList(tdl));
	}

	/**
	 * Subscribes to a set of topics with the given ids
	 * @param tdlIDs topic ids to subscribe to
	 */
	public void subscribe(List<String> tdlIDs) {
		HashSet<String> noDuplicateIDs = new HashSet<>(tdlIDs.size());
		tdlIDs.forEach(tdl -> noDuplicateIDs.add(tdl.trim().toLowerCase()));
		
		LinkedList<JsonObject> topics = new LinkedList<>();
		WebTarget getTopicServer = client.target(catalogue);
		for (String tdlId : noDuplicateIDs) {
			String response = getTopicServer.path(tdlId).request(MediaType.TEXT_PLAIN).get(String.class);
			JsonObject jsonResponse = jsonFromString(response);
			if (jsonResponse.isEmpty() || !hasAllJsonFields(jsonResponse)) {
				log.warn("Skip topic without all required fields: {}", response);
			} else {
				topics.add(jsonResponse);
			}
		}
		log.debug("Found {} topic(s)", topics.size());

		if (filterProtocolMQTT(topics)) {
			log.info("Topics which did not support the mqtt protocol where skipped.");
		}

		for (JsonObject topic : topics) {
			try {
				String url = topic.getString("middleware_endpoint");
				subscribeToTopic(url, topic.getString("path"));
				log.trace("Connected to {} at {}", topic.getString("path"), url);
			} catch (MqttException e) {
				// thrown while connecting or subscribing
				log.warn("Failed to subscribe to topic "+ topic.toString(), e);
			} catch (NullPointerException e) {
				// thrown if any of the getString fails
				log.warn("Failed to read middleware path from topic " + topic.toString(), e);
			}
		}
	}

	/**
	 * Subscribe to a set of topics described by this search filter
	 * @param filterParameter parameter to filter by
	 */
	public void subscribe(Map<String, String> filterParameter) {
		Map<String, Object> jsonParameter = new HashMap<>(filterParameter.size());
		filterParameter.forEach((key, value) -> jsonParameter.put(key, value));
		JsonObject filter = Json.createObjectBuilder(jsonParameter).build();
		// TODO: after utils finished
	}

	/**
	 * Replaces the currently used callback by the passed one. Returns the
	 * previously used one. <code>null</code> is returned if the argument was
	 * <code>null</code>.
	 * 
	 * @param callback
	 *            to set as new callback
	 * @return previously used callback or <code>null</code> if <code>null</code>
	 *         was passed as parameter
	 */
	public MqttCallback updateCallback(MqttCallback callback) {
		if (callback == null) {
			return null;
		}
		MqttCallback oldCallback = defaultCallback;
		defaultCallback = callback;
		brokers.values().forEach(b -> b.setCallback(defaultCallback));
		return oldCallback;
	}

	/**
	 * Closes all subscriptions and clears the list of brokers
	 */
	public void unsubscribe() {
		brokers.values().forEach(b -> {
			try {
				b.disconnect();
			} catch (MqttException e) {
				log.warn("Disconnect failed for " + b.getServerURI(), e);
			}
		});
		brokers.clear();
	}

	/**
	 * Removes all topics from the list which do not have the protocol set to mqtt.
	 * Updates the passed list!
	 * 
	 * @param topicDescriptions
	 *            to filter
	 * @return <code>true</code> if elements where removed, otherwise
	 *         <code>false</code>
	 */
	private boolean filterProtocolMQTT(List<JsonObject> topicDescriptions) {
		return topicDescriptions.removeIf(topic -> {
			if (topic.get("protocol") instanceof JsonString)
				return !((JsonString)topic.get("protocol")).getString().equalsIgnoreCase("mqtt");
			else 
				return true;
			});
	}

	/**
	 * Converts a {@link String} to a {@link JsonObject}.
	 * 
	 * @param jsonObjectStr
	 *            a json object string representation in {}
	 * @return a {@link JsonObject} which may be empty
	 */
	private JsonObject jsonFromString(String jsonObjectStr) {
		try {
			JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
			JsonObject object = jsonReader.readObject();
			jsonReader.close();
			return object;
		} catch (JsonException e) {
			log.warn("Json was not correctly formatted: {}", jsonObjectStr);
			return Json.createObjectBuilder().build();
		}
	}

	/**
	 * Subscribes to this topic. Reuses brokers to which already a connection to exists. Otherwise a new connection is established.
	 * @param broker which serves this topic
	 * @param topic to subscribe to with the standard callback
	 * @throws MqttException if an error while subscribing or connecting occurred
	 */
	private void subscribeToTopic(String broker, String topic) throws MqttException {
		if (!broker.startsWith("tcp://") && !broker.startsWith("ssl://")) {
			log.error("{} is not a valid mqtt server address. Has to start with tcp or ssl.", broker);
		}

		if (!brokers.containsKey(broker)) {
			MqttClient mqttclient = new MqttClient(broker, MqttClient.generateClientId());
			mqttclient.setCallback(defaultCallback);
			mqttclient.connect();
			brokers.put(broker, mqttclient);
		}
		brokers.get(broker).subscribe(topic);
	}
	
	/**
	 * Helper method which checks if the topic has all fields required to use it in the client
	 * @param topic to check
	 * @return <code>true</code> if all fields are set, <code>false</code> otherwise
	 */
	private boolean hasAllJsonFields(JsonObject topic) {
		boolean check = true;
		check &= topic.containsKey("protocol");
		check &= topic.containsKey("path");
		check &= topic.containsKey("middleware_endpoint");
		try {
			topic.getString("protocol");
			topic.getString("path");
			topic.getString("middleware_endpoint");
		} catch (NullPointerException|JsonException e) {
			return false;
		}
		return check;
	}
}
