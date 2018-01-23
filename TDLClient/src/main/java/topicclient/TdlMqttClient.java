package topicclient;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unistuttgart.ipvs.tdl.client.TdlUtil;

/**
 * Topic client for topics supporting the MQTT protocol. Based on a topic
 * description catalogue. Can connect to several brokers and topics at once.
 * 
 */
public class TdlMqttClient {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Collection of brokers this client is connected to at the moment
	 */
	private final HashMap<String, MqttClient> brokers = new HashMap<>();
	/**
	 * Collection of topics with their tdlid as key to avoid duplicates
	 */
	private final HashMap<String, String> topics = new HashMap<>();
	/**
	 * Collection of links to allow publishing to the correct server
	 */
	private final HashMap<String, MqttClient> topicTargets = new HashMap<>();
	/**
	 * Util handling the calls to the catalogue
	 */
	private final TdlUtil catalogueUtil;

	/**
	 * Callback used when handling messages
	 */
	private MqttCallback defaultCallback = null;

	/**
	 * Creates a new mqtt client with a topic server url where topics can be loaded
	 * from.
	 * 
	 * @param catalogueUrl
	 *            url of the server to connect
	 */
	public TdlMqttClient(String catalogueUrl) {

		catalogueUtil = new TdlUtil(catalogueUrl);

		defaultCallback = new MqttCallback() {

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				log.info("Message received");
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				log.info("Message published");
			}

			@Override
			public void connectionLost(Throwable cause) {
				log.warn("Connection lost to broker because of {}.", cause.getMessage());
			}
		};
	}

	/**
	 * Subscribe to a single topic with this id.
	 * 
	 * @param tdl
	 *            topic id to subscribe to
	 */
	public void subscribe(String tdl) {
		subscribe(Arrays.asList(tdl));
	}

	/**
	 * Subscribes to a set of topics with the given ids
	 * 
	 * @param tdlIDs
	 *            topic ids to subscribe to
	 */
	public void subscribe(List<String> tdlIDs) {
		HashSet<String> noDuplicateIDs = new HashSet<>(tdlIDs.size());
		tdlIDs.forEach(tdl -> noDuplicateIDs.add(tdl.trim().toLowerCase()));

		LinkedList<JsonObject> topics = new LinkedList<>();
		for (String tdlId : noDuplicateIDs) {
			String response = catalogueUtil.getTopicDescriptionWithId(tdlId);
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
				log.warn("Failed to subscribe to topic " + topic.toString(), e);
			} catch (NullPointerException e) {
				// thrown if any of the getString fails
				log.warn("Failed to read middleware path from topic " + topic.toString(), e);
			}
		}
	}

	/**
	 * Subscribe to a set of topics described by this search filter
	 * 
	 * @param filterParameter
	 *            parameter to filter by
	 */
	public void subscribe(Map<String, String> filterParameter) {
		JsonObjectBuilder filter = Json.createObjectBuilder();
		filterParameter.forEach((key, value) -> filter.add(key, value));
		JsonObject filterrequest = Json.createObjectBuilder().add("filters", filter.build()).build();
		List<String> foundTDLs = catalogueUtil.searchTopicDescriptions(filterrequest.toString());
		ArrayList<String> foundIDs = new ArrayList<>(foundTDLs.size());
		
		for (String fullTDL : foundTDLs) {
			try {
				// StreamTokenizer to unwrap the String-in-String tdl ("\"{...}\"" to "{}")
				StreamTokenizer parser = new StreamTokenizer(new StringReader(fullTDL));
				parser.nextToken(); // read in "first" and only token
				String unmarshalledtopic = parser.sval;
				JsonObject topic = jsonFromString(unmarshalledtopic);
				JsonObject oidWrapper = topic.getJsonObject("_id");
				String tdlId = oidWrapper.getString("$oid");
				foundIDs.add(tdlId);
			} catch (Exception e) {
				log.error("An error while getting the ID from the tdl "+fullTDL+" occurred.", e);
			}
		}
		subscribe(foundIDs);
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
	 * Closes all subscriptions and disconnects the client from all brokers. This
	 * clears the list of connected brokers.
	 */
	public void disconnect() {
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
	 * Adds a topic to the list of available topics and connects to the serving
	 * broker.
	 * 
	 * @param tdlId
	 *            id of the topic in the tdl catalogue
	 * @return the topic path on the broker, "" if the connection failed (wrong
	 *         protocol, connection error), <code>null</code> if the topic was
	 *         missing or did not have the fields set correctly
	 */
	public String addPublishTdlId(String tdlId) {
		String jsontdl = catalogueUtil.getTopicDescriptionWithId(tdlId);
		return addPublishTdl(jsontdl);
	}

	/**
	 * Adds a topic to the list of available topics and connects to the serving
	 * broker.
	 * 
	 * @param fullTdl
	 *            tdl of the topic as json string
	 * @return the topic path on the broker, "" if the connection failed (wrong
	 *         protocol, connection error), <code>null</code> if the topic was
	 *         missing or did not have the fields set correctly
	 */
	public String addPublishTdl(String fullTdl) {
		JsonObject topic = jsonFromString(fullTdl);

		if (topic.isEmpty() || !hasAllJsonFields(topic)) {
			log.error("Topic did not provide all required fields: {}", topic);
			return null;
		}

		LinkedList<JsonObject> topicWrapper = new LinkedList<>();
		topicWrapper.add(topic);
		if (filterProtocolMQTT(topicWrapper)) {
			log.error("Topic did not support mqtt!");
			return "";
		}

		String url = topic.getString("middleware_endpoint");
		if (!url.startsWith("tcp://") && !url.startsWith("ssl://")) {
			log.error("{} is not a valid mqtt server address. Has to start with tcp or ssl.", url);
			return "";
		}

		JsonObject oidWrapper = topic.getJsonObject("_id");
		String tdlId = oidWrapper.getString("$oid");
		try {
			if (!brokers.containsKey(url)) {
				MqttClient mqttclient = new MqttClient(url, MqttClient.generateClientId());
				mqttclient.setCallback(defaultCallback);
				mqttclient.connect();
				brokers.put(url, mqttclient);
			}
			topics.put(tdlId, topic.getString("path"));
			topicTargets.put(tdlId, brokers.get(url));
		} catch (MqttException e) {
			// thrown while connecting or subscribing
			log.warn("Failed to connect to topic " + topic.toString(), e);
			return "";
		} catch (NullPointerException e) {
			// thrown if any of the getString fails
			log.warn("Failed to read middleware path from topic " + topic.toString(), e);
			return null;
		}

		return topic.getString("path");
	}

	/**
	 * Executes the search with the given search string and tries to add all found
	 * topics to the this client.
	 * 
	 * @param searchJson
	 *            json string of a filter object with key:value pairs of
	 *            field:filter
	 * @return the topic paths on the brokers. No duplicates and no error states.
	 */
	public List<String> addPublishSearch(String searchJson) {
		List<String> tdls = catalogueUtil.searchTopicDescriptions(searchJson);
		final HashSet<String> addedTopics = new HashSet<>();
		tdls.forEach(tdl -> {
			// StreamTokenizer to unwrap the String-in-String tdl ("\"{...}\"" to "{}")
			try {
				StreamTokenizer parser = new StreamTokenizer(new StringReader(tdl));
				parser.nextToken(); // read in "first" and only token
				String unmarshalledtopic = parser.sval;
				addedTopics.add(addPublishTdl(unmarshalledtopic));
			} catch (IOException e) {
				log.warn("Not a valid server response:" + tdl);
			}
		});
		// remove error
		addedTopics.remove(null);
		addedTopics.remove("");

		return new LinkedList<>(addedTopics);
	}

	/**
	 * Publish the message to the topic with the given id. If a publisher for this
	 * topic was not added to this {@link TdlMqttClient} yet, the method will fail
	 * with a {@link NullPointerException}. In most cases it is enough to use
	 * publish() with the topic returned by the addPublish methods. Only if the
	 * topic is not unique, e.g. several brokers with the same topic are stored,
	 * this method is needed to use the correct one.
	 * 
	 * @param topic
	 *            topic id string from the catalogue to publish to on a broker
	 * @param message
	 *            to send as byte array
	 * @return success, <code>true</code> if publish was successful,
	 *         <code>false</code> if sending failed.
	 */
	public boolean publishById(String topicId, byte[] message) {
		if (!topics.containsKey(topicId)) {
			throw new NullPointerException("This topic is not available.");
		}
		MqttMessage wrappedMsg = new MqttMessage(message);
		try {
			topicTargets.get(topicId).publish(topics.get(topicId), wrappedMsg);
			return true;
		} catch (MqttException e) {
			log.error("Sending of the message to " + topicId + " failed.", e);
			return false;
		}
	}

	/**
	 * Publish the message to the given topic. If a publisher for this topic was not
	 * added to this {@link TdlMqttClient} yet, the method will fail with a
	 * {@link NullPointerException}. The method is greedy: If there are more than
	 * one broker for this topic added, the first one found is used.
	 * 
	 * @param topic
	 *            topic string to publish to on a broker
	 * @param message
	 *            to send as byte array
	 * @return success, <code>true</code> if publish was successful,
	 *         <code>false</code> if sending failed.
	 */
	public boolean publish(String topic, byte[] message) {
		for (Entry<String, String> e : topics.entrySet()) {
			if (e.getValue().equals(topic)) {
				return publishById(e.getKey(), message);
			}
		}
		throw new NullPointerException("This topic is not available.");
	}

	/**
	 * Get a list of unique topics with their paths on the broker.
	 * 
	 * @return unmodifiable map of (topicid,topic) pairs currently available
	 */
	public Map<String, String> getAvailableTopics() {
		return Collections.unmodifiableMap(topics);
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
				return !((JsonString) topic.get("protocol")).getString().equalsIgnoreCase("mqtt");
			return false; // unreachable, because every tdl needs to have this field
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
	 * Subscribes to this topic. Reuses brokers to which already a connection to
	 * exists. Otherwise a new connection is established.
	 * 
	 * @param broker
	 *            which serves this topic
	 * @param topic
	 *            to subscribe to with the standard callback
	 * @throws MqttException
	 *             if an error while subscribing or connecting occurred
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
	 * Helper method which checks if the topic has all fields required to use it in
	 * the client
	 * 
	 * @param topic
	 *            to check
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
		} catch (NullPointerException | JsonException e) {
			return false;
		}
		return check;
	}
}
