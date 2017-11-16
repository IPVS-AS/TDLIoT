package de.uni.stuttgart.ipvs.tdl.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyLoader {

	private static final Log LOG = LogFactory.getLog(PropertyLoader.class);
	private static final String PROPERTIES_FILE = "application.properties";
	private static final String PROPERTY_KEY_MONGODB_SERVER_IP = "mongodb.ip";
	private static final String PROPERTY_KEY_MONGODB_SERVER_PORT = "mongodb.port";

	public void loadPropertyValues() {

		try {

			java.util.Properties prop = getPropertyFile();
			Properties.setMongoDBServerIP(prop.getProperty(PROPERTY_KEY_MONGODB_SERVER_IP));
			Properties.setMongoDBServerPort(Integer.parseInt(prop.getProperty(PROPERTY_KEY_MONGODB_SERVER_PORT)));

		} catch (IOException e) {
			LOG.error("Properties Daten fehlgeschlagen", e);
		}

		LOG.info("Properties erfolgreich geladen");

		try {
			InetAddress.getByName(Properties.getMongoDBServerIP()).isReachable(100);
		} catch (IOException e) {
			throw new IllegalArgumentException("'" + Properties.getMongoDBServerIP() + "' ist nicht erreichbar");
		}
	}

	private java.util.Properties getPropertyFile() throws IOException {

		InputStream inputStream = null;

		File file = new File(PROPERTIES_FILE);

		if (!file.exists()) {
			LOG.info(PROPERTIES_FILE + " nicht im Verzeichnis der JAR gefunden!");
			URL url = getClass().getClassLoader().getResource(PROPERTIES_FILE);
			if (url != null) {
				inputStream = url.openStream();
			}
		} else {
			LOG.info(PROPERTIES_FILE + " im Verzeichnis der JAR gefunden!");
			inputStream = new FileInputStream(file);
		}

		java.util.Properties prop = new java.util.Properties();
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException(PROPERTIES_FILE + " muss im Verzeichnis der JAR liegen!");
		}

		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
			LOG.error("Laden der Properties Datei ist fehlgeschlagen", e);
		}

		return prop;
	}
}
