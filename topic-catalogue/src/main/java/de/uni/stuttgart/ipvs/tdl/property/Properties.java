package de.uni.stuttgart.ipvs.tdl.property;

public class Properties {

	private static String mongodb_serverIP = "";
	private static int mongodb_port = -1;

	protected static void setMongoDBServerIP(final String mongoDBServerIP) {

		Properties.mongodb_serverIP = mongoDBServerIP;
	}

	public static String getMongoDBServerIP() {

		return Properties.mongodb_serverIP;
	}

	protected static void setMongoDBServerPort(final int mongoDBServerPort) {

		Properties.mongodb_port = mongoDBServerPort;
	}

	public static int getMongoDBServerPort() {

		return Properties.mongodb_port;
	}
}
