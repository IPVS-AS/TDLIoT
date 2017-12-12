package tdlclient;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

	/**
	 * field of all tdls to which this client should publish
	 */
	private final String TDL_PUBLISH = "tdl_publish";

	/**
	 * field of the json of all tdls which this client should subscribe to
	 */
	private final String TDl_SUBSCRIBE = "tdl_subscribe";
	
	private final String TDL_SERVER = "tdl_catalogue";
	private final String TDL_SERVER_URL = "url";
	private final String TDL_SERVER_PORT = "port";

	private static Configuration config = null;

	private final Logger log = LoggerFactory.getLogger("TDLClient.Configuration");

	private ArrayList<String> subscribeTdls = new ArrayList<>();
	private ArrayList<String> publishTdls = new ArrayList<>();

	/**
	 * Private constructor to force singleton
	 */
	private Configuration() {

	}

	public static Configuration getInstance() {
		if (config == null) {
			config = new Configuration();
		}
		return config;
	}

	public Configuration loadFile(File file) throws IOException {
		clear();
		
		FileReader reader = new FileReader(file);
		JsonReader jreader = Json.createReader(reader);

		try {
			JsonObject obj = jreader.readObject();

			JsonValue publish = obj.get(TDL_PUBLISH);
			JsonValue subscribe = obj.get(TDl_SUBSCRIBE);
			addTdls(publish, publishTdls);
			addTdls(subscribe, subscribeTdls);
		} catch (JsonParsingException e) {
			log.error("Not a valid input json file.");
			throw new IOException("Not a valid json file.");
		}
		jreader.close();
		reader.close();

		return null;
	}

	public Configuration loadFile(String file) throws IOException {
		return loadFile(new File(file));
	}

	private void addTdls(JsonValue values, ArrayList<String> listToAddTo) {
		if (values instanceof JsonArray) {
			JsonArray valueArray = (JsonArray) values;
			for (JsonValue entry : valueArray) {
				if (entry instanceof JsonString) {
					log.debug("Found the tdl: {}", ((JsonString) entry).getString());
					listToAddTo.add(((JsonString) entry).getString());
				}
			}
		} else if (values instanceof JsonString) {
			log.debug("Found the tdl: {}", ((JsonString) values).getString());
			listToAddTo.add(((JsonString) values).getString());
		} else {
			log.error("Input was not a valid json string or list");
		}
	}
	
	private void clear() {
		publishTdls.clear();
		subscribeTdls.clear();
	}

}
