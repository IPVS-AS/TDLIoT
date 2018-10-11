package de.uni.stuttgart.ipvs.tdl.database;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import de.uni.stuttgart.ipvs.tdl.property.Properties;
import org.json.JSONObject;

public class MongoDBConnector {

	private MongoClient mongoClient = null;
	private static final String DATABASE_NAME = "tdl";
	private static final String DATABASE_TABLE_NAME = "topic";

	/**
	 * Init MongoDBConnector with IP and Port from the properties file.
	 */
	public MongoDBConnector() {
		this(Properties.getMongoDBServerIP(), Properties.getMongoDBServerPort());
	}
	
	/**
	 * Init MongoDBConnector with the provided ip and port.
	 * 
	 * @param serverIP
	 * @param port
	 */
	public MongoDBConnector(final String serverIP, final int port) {

		mongoClient = new MongoClient(serverIP, port);
	}

	/**
	 * Close the connection to the MongoDB.
	 */
	public void closeConnection() {
		mongoClient.close();
	}
	
	/**
	 * Returns the JSON document to the provided id if the id exists.
	 * 
	 * @param id - Searching for this id.
	 * @return At most one JSON document
	 */
	public String getMatchedTopicDescription(final String id) {
		
		Document document = new Document("_id", new ObjectId(id));
		
		Document resultDocument = getTable().find(document).first();
		
		if(resultDocument == null) {
			return null;
		}
		
		return resultDocument.toJson();
	}
	

	/**
	 * Returns all JSON documents with are matching to the provided filter values.
	 * 
	 * @param filter - Searching for this filter values.
	 * @return - All
	 */
	public List<String> getMatchedTopicDescriptions(final JSONObject filter) {

		if(filter == null) {
			throw new IllegalArgumentException("Filter has to be initialized!");
		}

		BasicDBObject queryFilter = BasicDBObject.parse(filter.toString());

		List<String> results = new LinkedList<String>();

		MongoCursor<Document> cursor = getTable().find(queryFilter).iterator();

		try {

		    while (cursor.hasNext()) {
		    	results.add(cursor.next().toJson());
		    }

		} finally {
			cursor.close();
		}

		return results;
	}
	
	/**
	 * Stores the provided JSON document in database.
	 * 
	 * @param description - JSON document of a topic description
	 * @return id of the new stored JSON document
	 */
	public String storeTopicDescription(final String description) {
		Document objectId = Document.parse(description);
		getTable().insertOne(objectId);
		
		return objectId.getObjectId("_id").toString();
	}
	
	/**
	 * Delete the JSON document with the provided id if the id exists.
	 * 
	 * @param id - Delete JSON document with this id.
	 * 
	 * @return get
	 */
	public boolean deleteTopicDescription(final String id) {
		Document objectId = new Document("_id", new ObjectId(id));
		
		return getTable().deleteMany(objectId).wasAcknowledged();
	}
	
	/**
	 * Update the JSON document with the provided id by the provided update parameters.
	 * 
	 * @param id - Update JSON document with this id.
	 * @param updateTopic - New Topic which replaces the old topic
	 */
	public boolean updateTopicDescription(final String id, String updateTopic) {
		
		if(updateTopic == null || updateTopic.isEmpty()) {
			throw new IllegalArgumentException("No update parameter available!");
		}

		Document objectId = new Document("_id", new ObjectId(id));

		Document updateTopicDocument = Document.parse(updateTopic);

		return getTable().replaceOne(objectId, updateTopicDocument).wasAcknowledged();
	}

	public boolean updateVerification(final String id, String policyType, JSONObject policyTypeVerification) {
		if(policyType == null || policyType.isEmpty() || policyTypeVerification == null) {
            throw new IllegalArgumentException("No update parameter available!");
        }

        Document objectId = new Document("_id", new ObjectId(id));
		BasicDBObject setUpdateFields = new BasicDBObject();

		Iterator<String> keys = policyTypeVerification.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            setUpdateFields.append("verification." + policyType + "." + key, policyTypeVerification.getString(key));
        }
        BasicDBObject setVerificationDocument = new BasicDBObject("$set", setUpdateFields);

        return getTable().updateOne(objectId, setVerificationDocument).wasAcknowledged();
	}

	private MongoCollection<Document> getTable() {
		return mongoClient.getDatabase(DATABASE_NAME).getCollection(DATABASE_TABLE_NAME);
	}
}