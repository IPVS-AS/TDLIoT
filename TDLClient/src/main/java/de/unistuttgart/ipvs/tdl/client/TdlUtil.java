package de.unistuttgart.ipvs.tdl.client;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class TdlUtil {

	private String tdlServerUrl;
	private static final String SERVER_METHOD_GET = "/get";
	private static final String SERVER_METHOD_ADD = "/add";
	private static final String SERVER_METHOD_DELETE = "/delete";
	private static final String SERVER_METHOD_SEARCH = "/search";
	private static final String SERVER_METHOD_UPDATE = "/update";

	/**
	 * Init TdlUtil with the corresponding topic server url.
	 * 
	 * @param tdlServerUrl
	 *            - Url to the topic server
	 */
	public TdlUtil(final String tdlServerUrl) {
		if (tdlServerUrl == null) {
			throw new IllegalArgumentException("Parameter tdlServerUrl is null! An url is required!");
		}

		this.tdlServerUrl = tdlServerUrl;
	}

	/**
	 * Requests the topic description language with the provided topic
	 * description id.
	 * 
	 * @param topicDescriptionId
	 *            - Search for this topic description id.
	 * @return JSON-String of the topic description. If tdlId is not available,
	 *         result is '{}'.
	 */
	public String getTopicDescriptionWithId(final String topicDescriptionId) {

		if (topicDescriptionId == null) {
			throw new IllegalArgumentException("Parameter topicDescriptionId is null! A topic description language id is required!");
		}

		String requestUrl = String.format("%s%s/%s", tdlServerUrl, SERVER_METHOD_GET, topicDescriptionId);

		return this.sendGetRequest(requestUrl);
	}

	/**
	 * Requests topic description languages with the provided topic description
	 * ids.
	 * 
	 * @param topicDescriptionIds
	 *            - List of topic description ids which should be searched.
	 * @return List of JSON-String of the topic descriptions. If tdlIds are not
	 *         available, result is an empty list.
	 */
	public List<String> getTopicDescriptionsWithIds(final List<String> topicDescriptionIds) {

		if (topicDescriptionIds == null || topicDescriptionIds.isEmpty()) {
			throw new IllegalArgumentException("Parameter topicDescriptionIds is null or empty!");
		}

		List<String> resultTdls = new ArrayList<String>();

		Client client = ClientBuilder.newClient();
		for (String tdlId : topicDescriptionIds) {
			String requestUrl = String.format("%s%s/%s", tdlServerUrl, SERVER_METHOD_GET, tdlId);
			String result = this.sendGetRequest(requestUrl, client);

			if (!result.equals("{}")) {
				resultTdls.add(result);
			}
		}
		client.close();

		return resultTdls;
	}

	/**
	 * Adds provided topic description to the topic description catalogue.
	 * 
	 * @param topicDescription
	 *            - Topic description to be stored.
	 * @return - Id of the provided topic description.
	 */
	public String addTopicDescription(final String topicDescription) {

		if (topicDescription == null || topicDescription.trim().isEmpty()) {
			throw new IllegalArgumentException("Parameter topicDescription is null or empty!");
		}

		String requestUrl = String.format("%s%s", tdlServerUrl, SERVER_METHOD_ADD);

		return sendPostRequest(requestUrl, topicDescription);
	}

	/**
	 * Searches for the provided filter in the topic description catalogue.
	 * 
	 * @param topicDescriptionFilter
	 *            - Filter for searching.
	 * @return - List of all matched topic descriptions.
	 */
	public List<String> searchTopicDescriptions(final String topicDescriptionFilter) {

		if (topicDescriptionFilter == null || topicDescriptionFilter.trim().isEmpty()) {
			throw new IllegalArgumentException("Parameter topicDescriptionFilter is null or empty!");
		}

		String requestUrl = String.format("%s%s", tdlServerUrl, SERVER_METHOD_SEARCH);

		String topicDescriptions = sendPostRequest(requestUrl, topicDescriptionFilter);

		List<String> listTopicDescriptions = new ArrayList<String>();

		try {
			JsonReader jsonReader = Json.createReader(new StringReader(topicDescriptions));
			JsonArray array = jsonReader.readArray();
			for (JsonValue jsonValue : array) {
				listTopicDescriptions.add(jsonValue.toString());
			}
			jsonReader.close();
		} catch (JsonException e) {
			throw new IllegalArgumentException("Parameter topicDescriptionFilter was wrong formatted JSON string! Exception: " + e.getLocalizedMessage());
		}

		return listTopicDescriptions;
	}

	/**
	 * Deletes the topic description with the provided id from the catalogue.
	 * 
	 * @param topicDescriptionId
	 *            - Id of the topic description which should be deleted.
	 * @return true if deletion was successful executed.
	 */
	public boolean deleteTopicDescription(final String topicDescriptionId) {

		if (topicDescriptionId == null || topicDescriptionId.trim().isEmpty()) {
			throw new IllegalArgumentException("Parameter topicDescriptionId is null or empty!");
		}

		String requestUrl = String.format("%s%s/%s", tdlServerUrl, SERVER_METHOD_DELETE, topicDescriptionId);

		return sendDeleteRequest(requestUrl);
	}

	/**
	 * Updates the topic description with the provided id and sets the provided
	 * update values.
	 * 
	 * @param topicDescriptionId
	 *            - Id of the topic description which should be updated.
	 * @param topicDescriptionUpdate
	 *            - JSON of update values.
	 * @return true if update was successful executed.
	 */
	public boolean updateTopicDescription(final String topicDescriptionId, final String topicDescriptionUpdate) {

		if (topicDescriptionId == null || topicDescriptionId.trim().isEmpty()) {
			throw new IllegalArgumentException("Parameter topicDescriptionId is null or empty!");
		}

		if (topicDescriptionUpdate == null || topicDescriptionUpdate.trim().isEmpty()) {
			throw new IllegalArgumentException("Parameter topicDescriptionUpdate is null or empty!");
		}

		String requestUrl = String.format("%s%s/%s", tdlServerUrl, SERVER_METHOD_UPDATE, topicDescriptionId);

		return sendPutRequest(requestUrl, topicDescriptionUpdate);
	}

	/**
	 * Does a GET request at the provided server url and returns result as
	 * String.
	 * 
	 * @param requestUrl
	 *            - Server url for request.
	 * @return Result string
	 */
	private String sendGetRequest(final String requestUrl) {

		Client client = ClientBuilder.newClient();
		String response = sendGetRequest(requestUrl, client);
		client.close();

		return response;
	}

	/**
	 * Does a GET request at the provided server url and returns result as
	 * String. Expected client for request.
	 * 
	 * @param requestUrl
	 *            - Server url for request.
	 * @param client
	 *            - Client for the connection.
	 * @return Result string if GET was successful otherwise null.
	 */
	private String sendGetRequest(final String requestUrl, final Client client) {

		Response response = client.target(requestUrl).request(MediaType.APPLICATION_JSON).get();

		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			return null;
		}

		return response.readEntity(String.class);
	}

	/**
	 * Does a POST request at the provided server url and returns result as
	 * String.
	 * 
	 * @param requestUrl
	 *            - Server url for request.
	 * @param postBody
	 *            - Body of request.
	 * @return Result string if POST was successful otherwise null.
	 */
	private String sendPostRequest(final String requestUrl, final String postBody) {

		Client client = ClientBuilder.newClient();
		Response response = targetAndRequestUrl(client, requestUrl).post(Entity.json(postBody));

		String result = null;
		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			result = response.readEntity(String.class);
		}
		client.close();

		return result;
	}

	/**
	 * Does a DELETE request at the provided server url and returns true if
	 * deletion was successful.
	 * 
	 * @param requestUrl
	 *            - Server url for request.
	 * @return True if deletion was successful, otherwise false
	 */
	private boolean sendDeleteRequest(final String requestUrl) {

		Client client = ClientBuilder.newClient();
		Response response = targetAndRequestUrl(client, requestUrl).delete();

		int statusCode = response.getStatus();
		client.close();

		return statusCode == Response.Status.OK.getStatusCode();
	}

	/**
	 * Does a PUT request at the provided server url and returns true if PUT was
	 * successful.
	 * 
	 * @param requestUrl
	 *            - Server url for request.
	 * @param requestBody
	 *            - Content of the body for the request.
	 * @return True if PUT was successful, otherwise false
	 */
	private boolean sendPutRequest(final String requestUrl, final String requestBody) {

		Client client = ClientBuilder.newClient();
		Response response = targetAndRequestUrl(client, requestUrl).put(Entity.json(requestBody));

		int statusCode = response.getStatus();
		client.close();

		return statusCode == Response.Status.ACCEPTED.getStatusCode();
	}

	/**
	 * Targets provided url and builds a request builder which is returned.
	 * 
	 * @param client
	 *            - ClientBuilder for the connection.
	 * @param url
	 *            - Target url.
	 * @return Builder of request.
	 */
	private Builder targetAndRequestUrl(final Client client, final String url) {
		return client.target(url).request();
	}
}
