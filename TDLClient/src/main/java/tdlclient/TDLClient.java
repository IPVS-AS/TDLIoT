package tdlclient;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import connector.RESTClient;


public class TDLClient {
	
	private static final Logger log = LoggerFactory.getLogger("TDLClient.TDLClient");

	public static void main(String[] args) {

		try {
			Configuration config = Configuration.getInstance().loadFile("configuration.json");
			
		} catch (Exception e) {
			log.error("An uncaught error was raised: {}", e);
		}
		
		RESTClient c = new RESTClient("http://127.0.0.1:8080/api/test");
		c.getString(null);
		
	}

	
	private static ArrayList<String> getTdls(ArrayList<String> ids) {
		
		return null;
	}
}
