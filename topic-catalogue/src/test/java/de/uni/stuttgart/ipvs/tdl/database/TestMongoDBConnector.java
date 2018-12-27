package de.uni.stuttgart.ipvs.tdl.database;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni.stuttgart.ipvs.tdl.property.PropertyLoader;

public class TestMongoDBConnector {

	private MongoDBConnector connector = null;
	private static List<String> listObjectIdsInserted = new LinkedList<String>();
	
	@Before
	public void setUp() {
		PropertyLoader loader = new PropertyLoader();
		loader.loadPropertyValues();
		
		connector = new MongoDBConnector();
	}
	
	@After
	public void tearDown() {
		connector.closeConnection();
	}
	
	@AfterClass
	public static void tearDownClass() {
				
		MongoDBConnector connector = new MongoDBConnector();
		for (String id : listObjectIdsInserted) {
			connector.deleteTopicDescription(id);
		}
		connector.closeConnection();
	}
	
	@Test
	public void testStoreTopicDescriptionObjectId() {
		final String TEST_STRING = "{ 'Test':'Test' }";
		String id = storeTopicDescription(TEST_STRING);
		Assert.assertNotNull(id);
		Assert.assertNotEquals("", id);
		
		String jsonResultExisting = connector.getMatchedTopicDescription(id);
		Assert.assertNotNull(jsonResultExisting);
		Assert.assertNotEquals("", jsonResultExisting);

		String expectedResultString = String.format("{ \"_id\" : { \"$oid\" : \"%s\" }, \"Test\" : \"Test\" }", id);
		Assert.assertEquals(expectedResultString, jsonResultExisting);
		
		connector.deleteTopicDescription(id);
		String jsonResultNotExisting = connector.getMatchedTopicDescription(id);
		Assert.assertNull(jsonResultNotExisting);		
	}
	
	@Test
	public void testUpdateTopicDescription() {
		final String TEST_STRING = "{ 'Test':'Test', 'gcgc': '#g#c#g#c#' }";
		String id = storeTopicDescription(TEST_STRING);			
		String jsonResultWithoutUpdate = connector.getMatchedTopicDescription(id);
		
		Map<String, String> mapUpdateParameter = new HashMap<String, String>();
		mapUpdateParameter.put("Test", "Test12345");
		connector.updateTopicDescription(id, mapUpdateParameter);

		String jsonResultWithUpdate = connector.getMatchedTopicDescription(id);
		Assert.assertNotEquals(jsonResultWithoutUpdate, jsonResultWithUpdate);
		
		connector.deleteTopicDescription(id);	
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testUpdateTopicDescriptionWithNotExistingId() {
		connector.updateTopicDescription("012345678901234567890123", new HashMap<String, String>());
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testUpdateTopicDescriptionWithInvalidId() {
		Map<String, String> mapUpdateParameter = new HashMap<String, String>();
		mapUpdateParameter.put("Test", "Test12345");
		connector.updateTopicDescription("xxx", mapUpdateParameter);
	}
	
	@Test
	public void testGetMatchedTopicDescriptionWithNotExistingId() {
		String jsonResult = connector.getMatchedTopicDescription("012345678901234567890123");
		Assert.assertNull(jsonResult);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetMatchedTopicDescriptionWithInvalidId() {
		connector.getMatchedTopicDescription("xxx");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testGetMatchedTopicDescriptionsWithoutFilter() {
		connector.getMatchedTopicDescriptions(null);
	}
	
	@Test
	public void testGetMatchedTopicDescriptionsWithoutExistingElements() {
		JSONObject filter = new JSONObject("{\n" +
				"    \"filters\": {\n" +
				"        \"TestXYZ\": \"#1#2#3#4#5#6#7#8#9#0#\"\n" +
				"    }\n" +
				"}");
		List<String> listJSONResults = connector.getMatchedTopicDescriptions(filter);
		Assert.assertNotNull(listJSONResults);
		Assert.assertEquals(0, listJSONResults.size());
	}
	
	@Test
	public void testGetMatchedTopicDescriptionsWithElements() {
		storeTopicDescription("{ 'Test':'Test', 'Language':'Java', 'Meta': { 'id':'42' } }");
		storeTopicDescription("{ 'Test':'Test', 'Language':'C#', 'Meta': { 'id':'199' } }");
		storeTopicDescription("{ 'Test':'Test', 'Language':'Javascript', 'Meta': { 'id':'42' } }");
		storeTopicDescription("{ 'Test':'Test', 'Language':'Java', 'Meta': { 'id':'123' } }");
		storeTopicDescription("{ 'Test':'Test', 'Language':'Java', 'Meta': { 'id':'200' } }");
		storeTopicDescription("{ 'Test':'Test', 'Language':'Java' }");
		storeTopicDescription("{ 'Test':'Test', 'Meta': { 'id':'42' } }");

		JSONObject filter = new JSONObject("{\n" +
				"    \"filters\": {\n" +
				"        \"Language\": \"Java\",\n" +
				"        \"Meta.id\": \"42\"\n" +
				"    }\n" +
				"}");
		
		List<String> listJSONResults = connector.getMatchedTopicDescriptions(filter);
		Assert.assertNotNull(listJSONResults);
		Assert.assertEquals(1, listJSONResults.size());
	}
	
	private String storeTopicDescription(final String description) {
		String id = connector.storeTopicDescription(description);
		listObjectIdsInserted.add(id);
		return id;
	}
	
}
