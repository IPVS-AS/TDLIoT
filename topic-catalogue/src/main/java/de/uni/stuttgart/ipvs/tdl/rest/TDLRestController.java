package de.uni.stuttgart.ipvs.tdl.rest;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "catalogue")
public class TDLRestController {

	/**
	 * Returns basic information about the API and provides links to the different
	 * REST methods.
	 * 
	 * @return links to all REST methods.
	 */
	@RequestMapping(method = GET, value = "")
	@ResponseBody
	public String getAPIDescription() {
		// TODO
		return "The api has the following urls";
	}

	/**
	 * Inserts a new topic to the database.
	 * 
	 * @param tdlJSON
	 *            topic description
	 * @return the id of the topic, which is provided by the database
	 */
	@RequestMapping(method = POST, value = "/add")
	@ResponseBody
	public String addNewTopic(String tdlJSON) {
		// TODO
		return "hexstring-ID";
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
	public ResponseEntity<?> deleteTopic(@PathVariable String id) {
		// TODO
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Updates the attributes of a given topic.
	 * 
	 * @param id
	 *            topic description id
	 * @param tdlAttributes
	 *            attributes, which have to be changed
	 * @return status code 200, if successful, 500 else
	 */
	@RequestMapping(method = PUT, value = "/update/{id}")
	@ResponseBody
	public ResponseEntity<?> updateTopic(@PathVariable String id, String tdlAttributes) {
		// TODO
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	/**
	 * Searches for topics based on attributes.
	 * 
	 * @param searchMap
	 *            tag map of attributes, which have to match with all topics of the
	 *            result list
	 * @return list of matching topic descriptions
	 */
	@RequestMapping(method = GET, value = "/search")
	@ResponseBody
	public String searchTopics(String searchMap) {
		// TODO
		return "list of matching tdls";
	}

	/**
	 * Returns a topic with the provided id.
	 * 
	 * @param id
	 *            topic description id
	 * @return topic description
	 */
	@RequestMapping(method = GET, value = "/get/{id}")
	@ResponseBody
	public String getTopic(@PathVariable String id) {
		// TODO
		return "returns topic with id " + id;
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
