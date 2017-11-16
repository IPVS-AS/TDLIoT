package de.uni.stuttgart.ipvs.tdl.database;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.uni.stuttgart.ipvs.tdl.property.PropertyLoader;

public class TestMongoDBConnector {

	private MongoDBConnector connector = null;
	
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
	
	@Test
	public void testStoreTopicDescriptionObjectId() {
		final String TEST_STRING = "{ 'Test':'Test' }";
		String id = connector.storeTopicDescription(TEST_STRING);
		Assert.assertNotNull(id);
		Assert.assertNotEquals("", id);
		String jsonResult = connector.getMatchedTopicDescriptions(id);
		Assert.assertNotNull(jsonResult);
		Assert.assertNotEquals("", jsonResult);
		
		String expectedResultString = String.format("{ \"_id\" : { \"$oid\" : \"%s\" }, \"Test\" : \"Test\" }", id);
		Assert.assertEquals(expectedResultString, jsonResult);
	}
	
}
