package connector;

import javax.ws.rs.core.MediaType;

import java.util.HashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

public class RESTClient extends AbstractClient {

	private final Client client = ClientBuilder.newClient();
	private final WebTarget apiRoot;

	public RESTClient(String serverAddress) {
		apiRoot = client.target(serverAddress);
	}

	@Override
	public void close() {
		client.close();
	}

	/**
	 * GET without response information.
	 * 
	 * @param parameterValuePairs
	 */
	public void get(HashMap<String, Object> parameter) {
		WebTarget query = apiRoot;
		addParameter(query, parameter);
		query.request().get();
	}
	
	public String getString(HashMap<String, Object> parameter) {
		WebTarget query = apiRoot;
		addParameter(query, parameter);
		return query.request(MediaType.TEXT_PLAIN).get(String.class);
	}
	
	public String getString(String endpoint, HashMap<String, Object> parameter) {
		WebTarget query = apiRoot.path(endpoint);
		addParameter(query, parameter);
		return query.request(MediaType.TEXT_PLAIN).get(String.class);
	}
	
	public void addPermanentParameter(String pName, Object pValue) {
		apiRoot.queryParam(pName, pValue);
	}
	
	private void addParameter(WebTarget target, HashMap<String, Object> parameter) {
		if (parameter != null) {
			parameter.entrySet().forEach(pair -> target.queryParam(pair.getKey(), pair.getValue()));
		}
	}
	
	// TODO: Async, POST, ??
}
