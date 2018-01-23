package de.uni.stuttgart.ipvs.tdl.rest;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.minidev.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.uni.stuttgart.ipvs.tdl.database.MongoDBConnector;

@CrossOrigin
@RestController
@RequestMapping(value = "catalogue")
public class TDLRestController {
	/**
	 * Database connector.
	 */
	public MongoDBConnector dbConnector = new MongoDBConnector();

	/**
	 * Inserts a new topic to the database.
	 * 
	 * @param tdlJSON
	 *            topic description
	 * @return the id of the topic, which is provided by the database
	 */
	@RequestMapping(method = POST, value = "/add")
	@ResponseBody
	public String addNewTopic(@RequestBody String topicDescription) {
		return dbConnector.storeTopicDescription(topicDescription);
	}

	/**
	 * Deletes a topic based on the given topic id.
	 * 
	 * @param id
	 *            topic description id
	 * @return Status code 200, if successful, 500 else
	 */
	@RequestMapping(method = DELETE, value = "/delete/{id}")
	@ResponseBody
	public ResponseEntity<HttpStatus> deleteTopic(@PathVariable String id) {
		if (dbConnector.deleteTopicDescription(id)) {
			return new ResponseEntity<HttpStatus>(HttpStatus.OK);
		} else {
			return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Updates the attributes of a given topic.
	 * 
	 * @param id
	 *            topic description id
	 * @param tdlAttributes
	 *            attributes, which have to be changed
	 * @return status code 200, if successful, 500 else
	 * 
	 */
	@RequestMapping(method = PUT, value = "/update/{id}")
	@ResponseBody
	public ResponseEntity<HttpStatus> updateTopic(@PathVariable String id, @RequestBody String tdlAttributes) {
		Map<String, String> updateParameter = new HashMap<String, String>();
		try {
			JSONObject updateParameterJson = new JSONObject(tdlAttributes);
			Iterator<String> keysIterator = updateParameterJson.keys();
			// Iterate over all update parameter
			while (keysIterator.hasNext()) {
				String key = (String) keysIterator.next();
				updateParameter.put(key, updateParameterJson.getString(key));
			}
			if (dbConnector.updateTopicDescription(id, updateParameter)) {
				return new ResponseEntity<HttpStatus>(HttpStatus.ACCEPTED);
			} else {
				return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (JSONException e) {
			return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
		}

	}

	/**
	 * Searches for topics based on attributes.
	 * 
	 * @param filters
	 *            tag map of attributes, which have to match with all topics of
	 *            the result list
	 * @return list of matching topic descriptions
	 * 
	 *         We directly throw the JSON exception to the user :)
	 */
	@RequestMapping(method = POST, value = "/search", produces = "application/json; charset=utf-8")
	@ResponseBody
	public ResponseEntity searchTopics(@RequestBody String filters) {
		HashMap<String, String> filterMap = new HashMap<String, String>();
		JSONObject filterJson;
		try {
			filterJson = new JSONObject(filters);
			if (filterJson.has("filters")) {
				filterJson = filterJson.getJSONObject("filters");
			} else {
				return new ResponseEntity<HttpStatus>(HttpStatus.BAD_REQUEST);
			}
			Iterator<String> keysIterator = filterJson.keys();

			while (keysIterator.hasNext()) {
				String key = (String) keysIterator.next();

				// Check if there is a nested JSONObject
				if (filterJson.optJSONObject(key) != null) {
					JSONObject childJson = filterJson.getJSONObject(key);
					Iterator<String> childKeysIterator = childJson.keys();

					// Iterate over child keys and put the keys together
					while (childKeysIterator.hasNext()) {
						String childKey = (String) childKeysIterator.next();
						filterMap.put(key + "." + childKey, childJson.getString(childKey));
					}
				} else {
					filterMap.put(key, filterJson.getString(key));
				}
			}

			List<String> descriptionList = dbConnector.getMatchedTopicDescriptions(filterMap);
			JSONArray topicDescriptionJsonArray = new JSONArray();
			for (String topicDescription : descriptionList) {
				topicDescriptionJsonArray.add(topicDescription);
			}
			return new ResponseEntity<>(topicDescriptionJsonArray, HttpStatus.OK);
		} catch (JSONException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	/**
	 * Returns a topic with the provided id.
	 * 
	 * @param id
	 *            topic description id
	 * @return topic description
	 */
	@RequestMapping(method = GET, value = "/get/{id}", produces = "application/json; charset=utf-8")
	@ResponseBody
	public ResponseEntity<String> getTopic(@PathVariable String id) {
		String topicDescription = dbConnector.getMatchedTopicDescription(id);
		if (null != topicDescription) {
			return new ResponseEntity<String>(topicDescription, HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("{}", HttpStatus.OK);
		}
	}

	/**
	 * Header information for swagger requests.
	 *
	 * @param response
	 *            Response information
	 */
	@ModelAttribute
	public void setVaryResponseHeader(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
	}

}
