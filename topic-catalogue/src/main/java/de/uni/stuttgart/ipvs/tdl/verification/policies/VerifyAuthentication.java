package de.uni.stuttgart.ipvs.tdl.verification.policies;

import de.uni.stuttgart.ipvs.tdl.enums.VerificationStatus;
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
        switch (topic.getString("protocol").toUpperCase()) {
            case "MQTT":
                storeResults(topic, verifyMQTTAuthentication(), "Authentication");
                break;
            case "HTTP":
                storeResults(topic, verifyHTTPAuthentication(), "Authentication");
                break;
            default:
                storeResults(topic, VerificationStatus.UNKNOWN, "Authentication");

        }
    }

    /**
     * Authentication verification of MQTT protocol
     *
     * @return verification success = true, failed = false
     */
    private VerificationStatus verifyMQTTAuthentication() {
        try {
            MqttClient mqttclient = new MqttClient(topic.getString("middleware_endpoint"), MqttClient.generateClientId());
            mqttclient.connect();
            mqttclient.subscribe(topic.getString("path"));
            // We subscribed without authentication, validation failed.
            return VerificationStatus.INVALID;
        } catch (MqttSecurityException e) {
            // Topic expect authentication, validation successfully ended
            return VerificationStatus.VALID;
        } catch (MqttException | IllegalArgumentException e) {
            // Validation failed
            return VerificationStatus.INVALID;
        } catch (Exception e) {
            // Something unexpected went wrong, validation failed
            e.printStackTrace();
            return VerificationStatus.INVALID;
        }
    }

    /**
     * Authentication verification of HTTP protocol
     *
     * @return verification success = true, failed = false
     */
    private VerificationStatus verifyHTTPAuthentication() {
        String url = topic.getString("middleware_endpoint") + topic.getString("path");
        try {
            URL topicURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) topicURL.openConnection();

            // If responseCode == 403 (Forbidden) -> verification successful
            if (connection.getResponseCode() == 403) {
                connection.disconnect();
                return VerificationStatus.VALID;
            } else {
                connection.disconnect();
                return VerificationStatus.INVALID;
            }
        } catch (IOException ignored) {
            // Verification failed
            return VerificationStatus.INVALID;
        }
    }
}
