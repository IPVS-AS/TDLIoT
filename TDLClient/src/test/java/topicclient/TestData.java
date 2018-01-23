package topicclient;

/**
 * Test data for the {@link TdlMqttClient} test classes.
 *
 */
public final class TestData {

	public static final String tdlSimple = 	"{\\\"data_type\\\":\\\"boolean\\\",\\\"hardware_type\\\":\\\"virtual\\\",\\\"topic_type\\\":\\\"demonstration\\\",\\\"message_format\\\":\\\"{}\\\",\\\"protocol\\\":\\\"MQTT\\\",\\\"owner\\\":\\\"development\\\",\\\"middleware_endpoint\\\":\\\"tcp://dev.local\\\",\\\"path\\\":\\\"/test\\\"}";
	public static final String tdldemo = 	"{\\\"data_type\\\":\\\"boolean\\\",\\\"hardware_type\\\":\\\"virtual\\\",\\\"topic_type\\\":\\\"demonstration\\\",\\\"message_format\\\":\\\"{}\\\",\\\"protocol\\\":\\\"MQTT\\\",\\\"owner\\\":\\\"development\\\",\\\"middleware_endpoint\\\":\\\"tcp://192.168.209.199:1883\\\",\\\"path\\\": \\\"/test\\\"}";

	public static final String tdlValid = "{" + "\"data_type\": \"int\","
			+ "\"hardware_type\": \"occupation detection sensor\"," + "\"protocol\": \"MQTT\","
			+ "\"topic_type\": \"subscription\"," + "\"path\": \"/parking-space-monitor\","
			+ "\"middleware_endpoint\": \"http://example.com\"," + "\"owner\": \"test-dep\"" + "}";
	/**
	 * Field missing
	 */
	public static final String tdlInvalid1 = "{" + "\"data_type\": \"int\","
			+ "\"hardware_type\": \"occupation detection sensor\"," + "\"topic_type\": \"subscription\","
			+ "\"path\": \"/invalid1\"," + "\"middleware_endpoint\": \"http://example.com\","
			+ "\"owner\": \"test-dep\"" + "}";
	/**
	 * http as link, MQTT protocol
	 */
	public static final String tdlInvalid2 = "{" + "\"data_type\": \"int\","
			+ "\"hardware_type\": \"occupation detection sensor\"," + "\"protocol\": \"MQTT\","
			+ "\"topic_type\": \"subscription\"," + "\"path\": \"/invalid2\","
			+ "\"middleware_endpoint\": \"http://example.com\"," + "\"owner\": \"test-dep\"" + "}";
	/**
	 * REST protocol
	 */
	public static final String tdlInvalid3 = "{" + "\"data_type\": \"int\","
			+ "\"hardware_type\": \"occupation detection sensor\"," + "\"protocol\": \"REST\","
			+ "\"topic_type\": \"subscription\"," + "\"path\": \"/invalid3\","
			+ "\"middleware_endpoint\": \"http://example.com\"," + "\"owner\": \"test-dep\"" + "}";

	public static final String tdlValidServer = "{\\\"_id\\\": { \\\"$oid\\\": \\\"1\\\" }" + "\"data_type\": \"int\","
			+ "\"hardware_type\": \"occupation detection sensor\"," + "\"protocol\": \"MQTT\","
			+ "\"topic_type\": \"subscription\"," + "\"path\": \"/parking-space-monitor\","
			+ "\"middleware_endpoint\": \"http://example.com\"," + "\"owner\": \"test-dep\"" + "}";
	/**
	 * Field missing
	 */
	public static final String tdlInvalid1Server = "{\\\"_id\\\": { \\\"$oid\\\": \\\"2\\\" }"
			+ "\"data_type\": \"int\"," + "\"hardware_type\": \"occupation detection sensor\","
			+ "\"topic_type\": \"subscription\"," + "\"path\": \"/invalid1\","
			+ "\"middleware_endpoint\": \"http://example.com\"," + "\"owner\": \"test-dep\"" + "}";
	/**
	 * http as link, MQTT protocol
	 */
	public static final String tdlInvalid2Server = "{\\\"_id\\\": { \\\"$oid\\\": \\\"3\\\" }"
			+ "\"data_type\": \"int\"," + "\"hardware_type\": \"occupation detection sensor\","
			+ "\"protocol\": \"MQTT\"," + "\"topic_type\": \"subscription\"," + "\"path\": \"/invalid2\","
			+ "\"middleware_endpoint\": \"http://example.com\"," + "\"owner\": \"test-dep\"" + "}";
	/**
	 * REST protocol
	 */
	public static final String tdlInvalid3Server = "{\\\"_id\\\": { \\\"$oid\\\": \\\"5a671ae924aa9a00076b7239\\\" }"
			+ "\"data_type\": \"int\"," + "\"hardware_type\": \"occupation detection sensor\","
			+ "\"protocol\": \"REST\"," + "\"topic_type\": \"subscription\"," + "\"path\": \"/invalid3\","
			+ "\"middleware_endpoint\": \"http://example.com\"," + "\"owner\": \"test-dep\"" + "}";
}
