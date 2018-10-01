package de.uni.stuttgart.ipvs.tdl.verification.policies;

import de.uni.stuttgart.ipvs.tdl.database.MongoDBConnector;
import de.uni.stuttgart.ipvs.tdl.enums.VerificationStatus;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.json.JSONObject;

import java.util.Iterator;

public class VerifyAuthentication implements Runnable {

    private final String policyType = "Authentication";

    private final JSONObject topic;

    public VerifyAuthentication(JSONObject topic) {
        this.topic = topic;
    }

    @Override
    public void run() {
        boolean valid = false;
        switch (topic.getString("protocol").toUpperCase()) {
            case "MQTT":
                valid = verifyMQTTAuthentication();
            case "HTTP":
                valid = verifyHTTPAuthentication();
        }
        storeVerificationResults(valid);
    }

    private boolean verifyMQTTAuthentication() {
        try {
            MqttClient mqttclient = new MqttClient(topic.getString("middleware_endpoint"), MqttClient.generateClientId());
            mqttclient.connect();
            mqttclient.subscribe(topic.getString("path"));
            // We subscribed without authentication. That is wrong.
            return false;
        } catch (MqttSecurityException e) {
            return true;
        } catch (MqttException e) {
            return false;
        }
    }

    private boolean verifyHTTPAuthentication() {
        return false;
    }

    private void storeVerificationResults(boolean valid) {
        JSONObject current = topic.getJSONObject("verification").getJSONObject("current");
        JSONObject policies = current.getJSONObject("policies");

        boolean verificationInProgress = false;
        Iterator<String> keys = policies.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            if (key.equals(policyType)) {
                if (valid) {
                    policies.put(key, VerificationStatus.VALID.toString());
                } else {
                    policies.put(key, VerificationStatus.INVALID.toString());
                }
            }
            if (policies.getString(key).equals(VerificationStatus.IN_PROGRESS.toString())) {
                verificationInProgress = true;
            }
        }
        if (!verificationInProgress) {
            current.put("status", VerificationStatus.FINISHED.toString());
        }
        MongoDBConnector dbConnector = new MongoDBConnector();
        dbConnector.updateTopicDescription(topic.getJSONObject("_id").getString("$oid"), topic.toString());
    }
}
