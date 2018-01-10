package de.uni.stuttgart.de.ipvs.tdl.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import de.uni.stuttgart.ipvs.tdl.database.MongoDBConnector;
import de.uni.stuttgart.ipvs.tdl.rest.TDLRestController;

@RunWith(MockitoJUnitRunner.class)
public class TestTDLRestController {

	private static final String dummyID = "hgsdgf7s8dgfs8dfvs8f";
	private static final String dummyDescription = "{\"dummyDesc\":\"true\"}";
	private static final String dummyUpdateJson = "{\"dummyDesc\":\"true\"}";
	private static final String dummyFilters = "{\n" + "	\"filters\":{\n" + "		\"location\":\n" + "		{\n" + "			\"location_type\":\"city name\",\n"
			+ "			\"location_value\": \"Stuttgart\"\n" + "		},\n" + "		\"hardware_type\":\"occopation detection sensor\"\n" + "	}\n" + "}";

	/**
	 * Database connector.
	 */
	@Mock
	private MongoDBConnector dbConnector;

	/**
	 * TDLRestController
	 */
	@InjectMocks
	private TDLRestController restController;

	@Test
	public void testAPIDescription() {
		// TODO
	}

	@Test
	public void testAddNewTopic() {
		when(dbConnector.storeTopicDescription(dummyDescription)).thenReturn(dummyID);
		assertEquals(dummyID, restController.addNewTopic(dummyDescription));
	}

	@Test
	public void testDeleteTopicOK() {
		when(dbConnector.deleteTopicDescription(dummyID)).thenReturn(true);
		assertEquals(new ResponseEntity<HttpStatus>(HttpStatus.OK), restController.deleteTopic(dummyID));
	}

	@Test
	public void testDeleteTopicFails() {
		when(dbConnector.deleteTopicDescription(dummyID)).thenReturn(false);
		assertEquals(new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR), restController.deleteTopic(dummyID));
	}

	@Test
	public void testUpdateTopic() {
		HashMap<String, String> parameter = new HashMap<String, String>();
		parameter.put("dummyDesc", "true");
		when(dbConnector.updateTopicDescription(dummyID, parameter)).thenReturn(true);

		assertEquals(new ResponseEntity<HttpStatus>(HttpStatus.ACCEPTED), restController.updateTopic(dummyID, dummyUpdateJson));
	}

	@Test
	public void testUpdateTopicFails() {
		HashMap<String, String> parameter = new HashMap<String, String>();
		parameter.put("dummyDesc", "true");
		when(dbConnector.updateTopicDescription(dummyID, parameter)).thenReturn(false);

		assertEquals(new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR), restController.updateTopic(dummyID, dummyUpdateJson));
	}

	@Test
	public void testUpdateTopicMalformedJson() {
		assertEquals(new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST), restController.updateTopic(dummyID, "}" + dummyUpdateJson));
	}

	@Test
	public void testSearchTopics() throws JSONException {
		HashMap<String, String> filters = new HashMap<String, String>();
		filters.put("location.location_type", "city name");
		filters.put("location.location_value", "Stuttgart");
		filters.put("hardware_type", "occopation detection sensor");

		JSONArray topicDescriptionJsonArray = new JSONArray();
		topicDescriptionJsonArray.put(dummyDescription);
		ArrayList<String> dummySearchResult = new ArrayList<String>(Arrays.asList(new String[] { dummyDescription }));
		when(dbConnector.getMatchedTopicDescriptions(filters)).thenReturn(dummySearchResult);

		JSONAssert.assertEquals(topicDescriptionJsonArray, (JSONArray) restController.searchTopics(dummyFilters).getBody(), false);
		assertEquals(HttpStatus.OK, restController.searchTopics(dummyFilters).getStatusCode());

	}

	@Test
	public void testSearchTopicsMalformedJSON() {
		HashMap<String, String> filters = new HashMap<String, String>();
		filters.put("location.location_type", "city name");
		filters.put("location.location_value", "Stuttgart");
		filters.put("hardware_type", "occopation detection sensor");

		JSONArray topicDescriptionJsonArray = new JSONArray();
		topicDescriptionJsonArray.put(dummyDescription);
		ArrayList<String> dummySearchResult = new ArrayList<String>(Arrays.asList(new String[] {}));
		when(dbConnector.getMatchedTopicDescriptions(filters)).thenReturn(dummySearchResult);

		assertEquals(HttpStatus.BAD_REQUEST, restController.searchTopics("}" + dummyFilters).getStatusCode());

	}

	@Test
	public void testSearchTopicsMissingFiltersTag() {
		HashMap<String, String> filters = new HashMap<String, String>();
		filters.put("location.location_type", "city name");
		filters.put("location.location_value", "Stuttgart");
		filters.put("hardware_type", "occopation detection sensor");

		String dummyFiltersWithoutFilterTag = dummyDescription;

		JSONArray topicDescriptionJsonArray = new JSONArray();
		topicDescriptionJsonArray.put(dummyDescription);
		ArrayList<String> dummySearchResult = new ArrayList<String>(Arrays.asList(new String[] {}));
		when(dbConnector.getMatchedTopicDescriptions(filters)).thenReturn(dummySearchResult);

		assertEquals(HttpStatus.BAD_REQUEST, restController.searchTopics(dummyFiltersWithoutFilterTag).getStatusCode());

	}

	@Test
	public void testGetTopic() {
		when(dbConnector.getMatchedTopicDescription(dummyID)).thenReturn(dummyDescription);
		assertEquals(new ResponseEntity<String>(dummyDescription, HttpStatus.OK), restController.getTopic(dummyID));
	}

	@Test
	public void testGetTopicNotFound() {
		when(dbConnector.getMatchedTopicDescription(dummyID)).thenReturn(null);
		assertEquals(new ResponseEntity<String>("{}", HttpStatus.OK), restController.getTopic(dummyID));
	}

}
