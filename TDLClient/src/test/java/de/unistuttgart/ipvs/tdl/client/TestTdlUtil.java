package de.unistuttgart.ipvs.tdl.client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestTdlUtil {

	private TdlUtil tdlUtil = null;
	private static final String TDL_SERVER_URL = "http://192.168.209.199:8080/catalogue";
	private static List<String> listObjectIdsInserted = new LinkedList<String>();

	@Before
	public void setUp() {
		tdlUtil = new TdlUtil(TDL_SERVER_URL);
	}

	@AfterClass
	public static void tearDownClass() {
		TdlUtil util = new TdlUtil(TDL_SERVER_URL);
		for (String id : listObjectIdsInserted) {
			util.deleteTopicDescription(id);
		}
	}

	@Test
	public void testAddTopicDescription() {
		String id = storeTopicDescription("{ 'Test':'Test', 'Language':'Java', 'Meta': { 'id':'42' } }");
		Assert.assertNotNull(id);
	}

	@Test
	public void testDeleteTopicDescription() {
		String id = storeTopicDescription("{ 'Test':'Test', 'Language':'Java', 'Meta': { 'id':'42' } }");
		Assert.assertNotNull(id);
		boolean status = tdlUtil.deleteTopicDescription(id);
		Assert.assertTrue(status);
	}

	@Test
	public void testSearchTopicDescriptions() {
		String id1 = storeTopicDescription("{ 'Test':'Test1_', 'Language':'Java', 'Meta': { 'id':'42' } }");
		storeTopicDescription("{ 'Test':'Test1_', 'Language':'C#', 'Meta': { 'id':'199' } }");
		storeTopicDescription("{ 'Test':'Test1_', 'Language':'Javascript', 'Meta': { 'id':'42' } }");
		String id2 = storeTopicDescription("{ 'Test':'Test1_', 'Language':'Java', 'Meta': { 'id':'123' } }");

		List<String> resultTopicDescriptions = tdlUtil.searchTopicDescriptions("{'filters':{'Test':'Test1_', 'Language': 'Java'} }");
		Assert.assertNotNull(resultTopicDescriptions);
		Assert.assertEquals(2, resultTopicDescriptions.size());
		for (String result : resultTopicDescriptions) {
			Assert.assertTrue(result.contains("Test1_"));
			Assert.assertTrue(result.contains("Java"));
			Assert.assertTrue(result.contains(id1) || result.contains(id2));
		}
	}

	@Test
	public void testGetTopicDescriptionWithId() {
		String id = storeTopicDescription("{ 'Test':'Test2_', 'Language':'Java', 'Meta': { 'id':'42' } }");
		String topicDescription = tdlUtil.getTopicDescriptionWithId(id);
		Assert.assertTrue(topicDescription.contains("Test2_"));
		Assert.assertTrue(topicDescription.contains("Java"));
		Assert.assertTrue(topicDescription.contains(id));
	}

	@Test
	public void testUpdateTopicDescription() {
		String id = storeTopicDescription("{ 'Test':'Test3_', 'Language':'Java', 'Meta': { 'id':'42' } }");
		boolean status = tdlUtil.updateTopicDescription(id, "{'Language':'C#'}");
		Assert.assertTrue(status);
		String topicDescription = tdlUtil.getTopicDescriptionWithId(id);
		Assert.assertTrue(topicDescription.contains("Test3_"));
		Assert.assertTrue(topicDescription.contains("C#"));
		Assert.assertFalse(topicDescription.contains("Java"));
		Assert.assertTrue(topicDescription.contains(id));
	}

	@Test
	public void testGetTopicDescriptionWithIds() {
		String id1 = storeTopicDescription("{ 'Test':'Test11_', 'Language':'Java', 'Meta': { 'id':'7654321' } }");
		storeTopicDescription("{ 'Test':'Test11_', 'Language':'C++', 'Meta': { 'id':'199' } }");
		storeTopicDescription("{ 'Test':'Test11_', 'Language':'SQL', 'Meta': { 'id':'42' } }");
		String id2 = storeTopicDescription("{ 'Test':'Test11_', 'Language':'C#', 'Meta': { 'id':'1234567' } }");
		List<String> topicDescriptionIds = new ArrayList<String>();
		topicDescriptionIds.add(id1);
		topicDescriptionIds.add(id2);
		List<String> resultTopicDescriptions = tdlUtil.getTopicDescriptionsWithIds(topicDescriptionIds);
		Assert.assertNotNull(resultTopicDescriptions);
		Assert.assertEquals(2, resultTopicDescriptions.size());
		for (String result : resultTopicDescriptions) {
			Assert.assertTrue(result.contains("Test11_"));

			boolean topicDescription1Condition = (result.contains("Java") && result.contains("7654321") && result.contains(id1));
			boolean topicDescription2Condition = (result.contains("C#") && result.contains("1234567") && result.contains(id2));
			Assert.assertTrue(topicDescription1Condition || topicDescription2Condition);
		}
	}

	private String storeTopicDescription(final String description) {
		String id = tdlUtil.addTopicDescription(description);
		listObjectIdsInserted.add(id);
		return id;
	}
}
