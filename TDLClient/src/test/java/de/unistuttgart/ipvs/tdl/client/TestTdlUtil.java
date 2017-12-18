package de.unistuttgart.ipvs.tdl.client;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.contains;
import static org.powermock.api.mockito.PowerMockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TdlUtil.class)
public class TestTdlUtil {

	private TdlUtil tdlUtil = null;
	private static final String TDL_SERVER_URL = "http://localhost:8080/catalogue";
	
	@Before
	public void setUp() {
		tdlUtil = new TdlUtil(TDL_SERVER_URL);
		this.mockTopicServerRequest();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInitTdlUtilWithNull() {
		new TdlUtil(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetTdlWithIdWithNull() {
		tdlUtil.getTopicDescriptionWithId(null);	
	}
	
	@Test
	public void testGetTdlWithIdWithoutExistingId() {
		String result = tdlUtil.getTopicDescriptionWithId("111111111111111111111111");	
		Assert.assertEquals("Result has to be empty!", "{}", result);
	}
	
	@Test
	public void testGetTdlWithIdWithExistingId() {
		String result = tdlUtil.getTopicDescriptionWithId("211111111111111111111111");	
		Assert.assertEquals("Wrong result is returned", "{\"_id\":{\"$oid\":\"211111111111111111111111\"}}", result);
	}
	
	@Test
	public void testGetTdlsWithIds() {
		List<String> requestTdlIds = new ArrayList<String>();
		requestTdlIds.add("a11111111111111111111111");
		requestTdlIds.add("a21111111111111111111111");
		requestTdlIds.add("a31111111111111111111111");
		requestTdlIds.add("a41111111111111111111111");
		requestTdlIds.add("a51111111111111111111111");
		
		List<String> results = tdlUtil.getTopicDescriptionsWithIds(requestTdlIds);	
		Assert.assertEquals("Wrong number of results!", 3, results.size());
		Assert.assertTrue("Wrong element in results!", results.get(0).contains("a11111111111111111111111"));
		Assert.assertTrue("Wrong element in results!", results.get(1).contains("a31111111111111111111111"));
		Assert.assertTrue("Wrong element in results!", results.get(2).contains("a41111111111111111111111"));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testAddTopicDescriptionWithNullTdl() {
		tdlUtil.addTopicDescription(null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testAddTopicDescriptionWithEmptyTdl() {
		tdlUtil.addTopicDescription("  ");
	}
	
	@Test
	public void testAddTopicDescriptionWithCorrectTdl() {
		String tdl = "{\"data_type\":\"boolean\",\"hardware_type\":\"Sensor 123\"}";
		String result = tdlUtil.addTopicDescription(tdl);
		Assert.assertEquals("Wrong tdl id is returned!", "11111111111111111111111a", result);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testSearchTopicDescriptionsWithNullFilter() {
		tdlUtil.searchTopicDescriptions(null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testSearchTopicDescriptionsWithEmptyFilter() {
		tdlUtil.searchTopicDescriptions("  ");
	}
	
	@Test
	public void testSearchTopicDescriptionsWithCorrectFilter() {
		String filter = "{\"filters\":{\"data_type\":\"int\"}}";
		List<String> result = tdlUtil.searchTopicDescriptions(filter);
		Assert.assertEquals("Wrong number of elements in results!", 2, result.size());
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testDeleteTopicDescriptionWithNullFilter() {
		tdlUtil.deleteTopicDescription(null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testDeleteTopicDescriptionWithEmptyFilter() {
		tdlUtil.deleteTopicDescription("  ");
	}
	
	@Test
	public void testDeleteTopicDescriptionWithExistingTD() {
		boolean result = tdlUtil.deleteTopicDescription("d11111111111111111111111");
		Assert.assertTrue("Execution of deletion should be successful!", result);
	}
	
	@Test
	public void testDeleteTopicDescriptionWithoutExistingTD() {
		boolean result = tdlUtil.deleteTopicDescription("d11111111111111111111112");
		Assert.assertFalse("Execution of deletion should be failed!", result);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testUpdateTopicDescriptionWithNullId() {
		tdlUtil.updateTopicDescription(null, "{}");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testUpdateTopicDescriptionWithNullBody() {
		tdlUtil.updateTopicDescription("u11111111111111111111111", null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testUpdateTopicDescriptionWithEmptyId() {
		tdlUtil.updateTopicDescription("   ", "{}");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testUpdateTopicDescriptionWithEmptyBody() {
		tdlUtil.updateTopicDescription("u11111111111111111111111", "   ");
	}
	
	@Test
	public void testUpdateTopicDescriptionWithExistingTD() {
		boolean result = tdlUtil.updateTopicDescription("u11111111111111111111111", "{}");
		Assert.assertTrue("Execution of update should be successful!", result);
	}
	
	@Test
	public void testUpdateTopicDescriptionWithoutExistingTD() {
		boolean result = tdlUtil.updateTopicDescription("u11111111111111111111112", "{}");
		Assert.assertFalse("Execution of update should be failed!", result);
	}

	private void mockTopicServerRequest() {
		tdlUtil = PowerMockito.spy(tdlUtil);
		try {
			
			doReturn("{}").when(tdlUtil, "sendGetRequest", anyString());		
			doReturn("{\"_id\":{\"$oid\":\"211111111111111111111111\"}}").when(tdlUtil, "sendGetRequest", endsWith("211111111111111111111111"));
			
			doReturn("{}").when(tdlUtil, "sendGetRequest", anyString(), any());
			doReturn("{\"_id\":{\"$oid\":\"a11111111111111111111111\"}}").when(tdlUtil, "sendGetRequest", endsWith("a11111111111111111111111"), any());
			doReturn("{\"_id\":{\"$oid\":\"a31111111111111111111111\"}}").when(tdlUtil, "sendGetRequest", endsWith("a31111111111111111111111"), any());
			doReturn("{\"_id\":{\"$oid\":\"a41111111111111111111111\"}}").when(tdlUtil, "sendGetRequest", endsWith("a41111111111111111111111"), any());
	
			doReturn("").when(tdlUtil, "sendPostRequest", contains("add"), anyString());
			doReturn("11111111111111111111111a").when(tdlUtil, "sendPostRequest", contains("add"), eq("{\"data_type\":\"boolean\",\"hardware_type\":\"Sensor 123\"}"));
			
			doReturn("").when(tdlUtil, "sendPostRequest", contains("search"), anyString());
			doReturn("[{\"hardware_type\":\"Sensor 123\"},{\"hardware_type\":\"Test Sensor\"}]").when(tdlUtil, "sendPostRequest", contains("search"), eq("{\"filters\":{\"data_type\":\"int\"}}"));
				
			doReturn(false).when(tdlUtil, "sendDeleteRequest", anyString());
			doReturn(true).when(tdlUtil, "sendDeleteRequest", endsWith("d11111111111111111111111"));	
			
			doReturn(false).when(tdlUtil, "sendPutRequest", anyString(), anyString());
			doReturn(true).when(tdlUtil, "sendPutRequest", endsWith("u11111111111111111111111"), anyString());	
			
		} catch(Exception e) {
			Assert.fail("Mocking fail! " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		//{"_id":{"$oid":"5a3271a0117e7a1354646e57"},"data_type":"boolean","hardware_type":"Awesome sensor","location":{"location_type":"city name","location_value":"Stuttgart"},"message_format":"JSON","message_structure":{"metamodel_type":"JSON schema","metamodel":"{\n\t\t\t\"title\":\"provider_schema\",\n\t\t\t\"type\":\"object\",\n\t\t\t\"properties\":{\n\t\t\t\"value\":{\"type\":\"boolean\"},\n\t\t\t\"timestamp\":{\"type\":\"integer\"},\n\t\t\t\"time_up\":{\"type\":\"string\"}\n\t\t},\n\t\t\"required\":[\"value\",\"timestamp\"]\n\t\t}"},"middleware_endpoint":"http://example.com","owner":"city-of-stuttgart","path":"/my-sensor","protocol":"MQTT","topic_type":"subscription"}
	}
	
}
