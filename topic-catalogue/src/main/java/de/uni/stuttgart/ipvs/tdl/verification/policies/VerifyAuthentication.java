package de.uni.stuttgart.ipvs.tdl.verification.policies;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class VerifyAuthentication implements IVerifyPolicy {

    private final JSONObject topic;

    public VerifyAuthentication(JSONObject topic) {
        this.topic = topic;
    }

    /**
     *  Starts specific verification of different protocol types
     */
    @Override
    public void run() {
        boolean valid = false;
        switch (topic.getString("protocol").toUpperCase()) {
            case "MQTT":
                valid = verifyMQTTAuthentication();
            case "HTTP":
                valid = verifyHTTPAuthentication();
        }
        storeResults(topic, valid, "Authentication");
    }

    /**
     * Authentication verification of MQTT protocol
     *
     * @return verification success = true, failed = false
     */
    private boolean verifyMQTTAuthentication() {
        try {
            MqttClient mqttclient = new MqttClient(topic.getString("middleware_endpoint"), MqttClient.generateClientId());
            mqttclient.connect();
            mqttclient.subscribe(topic.getString("path"));
            // We subscribed without authentication, validation failed.
            return false;
        } catch (MqttSecurityException e) {
            // Topic expect authentication, validation successfully ended
            return true;
        } catch (MqttException | IllegalArgumentException e) {
            // Validation failed
            return false;
        } catch (Exception e) {
            // Something unexpected went wrong, validation failed
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Authentication verification of HTTP protocol
     *
     * @return verification success = true, failed = false
     */
    private boolean verifyHTTPAuthentication() {
        String url = topic.getString("middleware_endpoint") + topic.getString("path");
        boolean valid = false;
        try {
            URL topicURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) topicURL.openConnection();

            // If responseCode == 403 (Forbidden) -> verification successful
            valid = connection.getResponseCode() == 403;
            connection.disconnect();
        } catch (IOException ignored) {
            // Verification failed
        }
        return valid;
    }
}
