package de.uni.stuttgart.ipvs.tdl.rest;

import de.uni.stuttgart.ipvs.tdl.database.MongoDBConnector;
import de.uni.stuttgart.ipvs.tdl.enums.VerificationStatus;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@CrossOrigin
@RestController
@RequestMapping(value = "verify")
public class TDLVerifierRestController {

    /**
     * Database connector.
     */
    private MongoDBConnector dbConnector = new MongoDBConnector();

    @Autowired
    private TaskExecutor verificationExecutor;

    private final String verificationKey = "verification";
    private final String currentKey = "current";
    private final String statusKey = "status";
    private final String policiesKey = "policies";
    private final String policyKey = "policy";
    private final String protocolKey = "protocol";
    private final String timestampKey = "timestamp";
    private final String topicKey = "topic";
    private final String messageKey = "messageKey";
    private final String policyTypeKey = "policy_type";

    /**
     * @param id
     * @return
     */
    @RequestMapping(method = GET, value = "/topic/{id}")
    @ResponseBody
    public ResponseEntity isTopicOperational(@PathVariable String id) {
        boolean operational = false;
        String topicDesc = dbConnector.getMatchedTopicDescription(id);
        if (topicDesc != null) {
            JSONObject topic = new JSONObject(topicDesc);
            String middlewareEndpointKey = "middleware_endpoint";
            String pathKey = "path";
            switch (topic.getString(protocolKey).toUpperCase()) {
                case "MQTT":
                    try {
                        operational = connectToMQTTTopic(topic.getString(middlewareEndpointKey), topic.getString(pathKey));
                    } catch (MqttException | NullPointerException e) {
                        operational = false;
                    }
                    break;
                case "HTTP":
                    try {
                        String url = topic.getString(middlewareEndpointKey) + topic.getString(pathKey);
                        operational = connectToHTTPTopic(url);
                    } catch (IOException e) {
                        operational = false;
                    }
                    break;
            }
            if (operational) {
                return ResponseEntity.ok("online");
            } else {
                return ResponseEntity.ok("offline");
            }
        } else {
            return ResponseEntity.badRequest().body("No Topic with id: " + id);
        }
    }

    /**
     * @param id
     * @return
     */
    @RequestMapping(method = POST, value = "/topic/{id}/policies")
    @ResponseBody
    public ResponseEntity verifyPolicies(@PathVariable String id) {
        String topicDesc = dbConnector.getMatchedTopicDescription(id);
        if (topicDesc != null) {
            JSONObject topic = new JSONObject(topicDesc);
            if (topic.has(policyKey)) {
                if (!verificationAlreadyRunning(topic)) {
                    updateLastVerification(id);
                    clearCurrentVerification(id);
                    startVerificationExecutor(id);
                }
                JSONObject href = new JSONObject();
                href.put("href", "/verify/" + id);
                return ResponseEntity.accepted().body(href.toString());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Topic contains no policy");
            }
        } else {
            return ResponseEntity.badRequest().body("No Topic with id: " + id);
        }
    }

    /**
     * @param id
     * @return
     */
    @RequestMapping(method = GET, value = "/{id}")
    @ResponseBody
    public ResponseEntity getVerificationState(@PathVariable String id) {
        String topicDesc = dbConnector.getMatchedTopicDescription(id);
        if (topicDesc != null) {
            JSONObject topic = new JSONObject(topicDesc);
            if (topic.has(verificationKey)) {
                return ResponseEntity.ok(topic.getJSONObject(verificationKey).getJSONObject(currentKey).getString(statusKey));
            } else {
                return ResponseEntity.badRequest().body("Please first start verification with \"/topic/" + id + "/policies\"");
            }
        } else {
            return ResponseEntity.badRequest().body("No Topic with id: " + id);
        }
    }

    /**
     * @param topic
     * @return
     */
    private boolean verificationAlreadyRunning(JSONObject topic) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
        if (topic.has(verificationKey)) {
            JSONObject verification = topic.getJSONObject(verificationKey);
            if (verification.has(currentKey)) {
                JSONObject current = verification.getJSONObject(currentKey);
                if (current.getString(statusKey).equals(VerificationStatus.IN_PROGRESS.toString())) {
                    // Set timestamp to latest verification starting point
                    current.put(timestampKey, sdf.format(new Date()));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param id
     */
    private void updateLastVerification(String id) {
        String topicDesc = dbConnector.getMatchedTopicDescription(id);

        JSONObject topic = new JSONObject(topicDesc);
        if (!topic.has(verificationKey)) {
            topic.put(verificationKey, new JSONObject());
        }

        JSONObject verification = topic.getJSONObject(verificationKey);
        if (verification.has(currentKey)) {
            JSONObject currentVerification = verification.getJSONObject(currentKey);
            String lastKey = "last";
            verification.put(lastKey, currentVerification);
        }
        dbConnector.updateTopicDescription(id, topic.toString());
    }

    /**
     * @param id
     */
    private void clearCurrentVerification(String id) {
        String topicDesc = dbConnector.getMatchedTopicDescription(id);
        JSONObject topic = new JSONObject(topicDesc);
        JSONObject verification = topic.getJSONObject(verificationKey);
        verification.put(currentKey, clearedVerification(topic.getJSONObject(policyKey)));
        dbConnector.updateTopicDescription(id, topic.toString());
    }

    /**
     * @param policy
     * @return
     */
    private JSONObject clearedVerification(JSONObject policy) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
        JSONObject current = new JSONObject();
        current.put(timestampKey, sdf.format(new Date()));
        current.put(statusKey, VerificationStatus.IN_PROGRESS.toString());
        JSONObject policies = new JSONObject();
        if (policy.has(topicKey)) {
            for (int index = 0; index < policy.getJSONArray(topicKey).length(); index++) {
                JSONObject singlePolicy = policy.getJSONArray(topicKey).getJSONObject(index);
                policies.put(singlePolicy.getString(policyTypeKey), VerificationStatus.IN_PROGRESS.toString());
            }
        }
        if (policy.has(messageKey)) {
            for (int index = 0; index < policy.getJSONArray(messageKey).length(); index++) {
                JSONObject singlePolicy = policy.getJSONArray(messageKey).getJSONObject(index);
                policies.put(singlePolicy.getString(policyTypeKey), VerificationStatus.IN_PROGRESS.toString());
            }
        }
        current.put(policiesKey, policies);
        return current;
    }

    /**
     * @param id
     */
    private void startVerificationExecutor(String id) {
        String topicDesc = dbConnector.getMatchedTopicDescription(id);
        JSONObject topic = new JSONObject(topicDesc);
        JSONObject policy = topic.getJSONObject(policyKey);
        ArrayList<String> policyTypes = new ArrayList<>();
        if (policy.has(topicKey)) {
            for (int index = 0; index < policy.getJSONArray(topicKey).length(); index++) {
                JSONObject singlePolicy = policy.getJSONArray(topicKey).getJSONObject(index);
                policyTypes.add(singlePolicy.getString(policyTypeKey));
            }
        }
        if (policy.has(messageKey)) {
            for (int index = 0; index < policy.getJSONArray(messageKey).length(); index++) {
                JSONObject singlePolicy = policy.getJSONArray(messageKey).getJSONObject(index);
                policyTypes.add(singlePolicy.getString(policyTypeKey));
            }
        }
        String protocol = topic.getString(protocolKey).toUpperCase();
        JSONObject policies = topic.getJSONObject(verificationKey).getJSONObject(currentKey).getJSONObject(policiesKey);
        for (String policyType : policyTypes) {
            switch (policyType) {
                case "Authentication":
                    switch (protocol) {
                        case "MQTT":
                            //verificationExecutor.execute();
                            break;
                        case "HTTP":
                            //verificationExecutor.execute();
                            break;
                        default:
                            policies.put(policyType, VerificationStatus.UNKNOWN.toString());
                    }
                    break;
                case "Interval":
                    switch (protocol) {
                        case "MQTT":
                            //verificationExecutor.execute();
                            break;
                        case "HTTP":
                            //verificationExecutor.execute();
                            break;
                        default:
                            policies.put(policyType, VerificationStatus.UNKNOWN.toString());
                    }
                    break;
                default:
                    policies.put(policyType, VerificationStatus.UNKNOWN.toString());
            }
        }

        Iterator<String> keys = policies.keys();
        boolean verificationInProgress = false;
        while(keys.hasNext()) {
            String key = keys.next();
            if (policies.getString(key).equals(VerificationStatus.IN_PROGRESS.toString())) {
                verificationInProgress = true;
            }
        }
        if (!verificationInProgress) {
            JSONObject current = topic.getJSONObject(verificationKey).getJSONObject(currentKey);
            current.put(statusKey, VerificationStatus.FINISHED.toString());
        }
        dbConnector.updateTopicDescription(id, topic.toString());
    }

    /**
     * Try to connect to broker
     *
     * @param broker which serves this topic
     * @param topic  to subscribe to with the standard callback
     * @throws MqttException if an error while subscribing or connecting occurred
     */
    private boolean connectToMQTTTopic(String broker, String topic) throws MqttException {
        if (broker.startsWith("tcp://") || broker.startsWith("ssl://")) {
            MqttClient mqttclient = new MqttClient(broker, MqttClient.generateClientId());
            mqttclient.connect();
            return true;
        } else {
            return false;
        }

    }

    /**
     * @param url
     * @return
     * @throws IOException
     */
    private boolean connectToHTTPTopic(String url) throws IOException {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            URL topicURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) topicURL.openConnection();
            // connection.setRequestMethod("GET");
            connection.disconnect();
            return true;
        } else {
            return false;
        }
    }
}