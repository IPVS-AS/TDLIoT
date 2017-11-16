package de.uni.stuttgart.ipvs.tdl.database;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import de.uni.stuttgart.ipvs.tdl.property.Properties;

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

	public void closeConnection() {
		mongoClient.close();
	}
	
	public String getMatchedTopicDescriptions(final String id) {
		
		Document document = new Document("_id",new ObjectId(id));
		
		return getTable().find(document).first().toJson();
	}
	


	public List<String> getMatchedTopicDescriptions(final HashMap<String, String> filters) {

		Document filterDocument = new Document();
		filterDocument.putAll(filters);

		MongoCursor<Document> cursor = getTable().find(filterDocument).iterator();
		
		List<String> results = new LinkedList<String>();
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
		Document document = Document.parse(description);
		getTable().insertOne(document);
		
		return document.getObjectId("_id").toString();
	}

	private MongoCollection<Document> getTable() {
		return mongoClient.getDatabase(DATABASE_NAME).getCollection(DATABASE_TABLE_NAME);
	}
}