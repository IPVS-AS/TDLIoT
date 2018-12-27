package de.uni.stuttgart.ipvs.tdl.rest;

import de.uni.stuttgart.ipvs.tdl.database.MongoDBConnector;
import de.uni.stuttgart.ipvs.tdl.enums.VerificationStatus;
import de.uni.stuttgart.ipvs.tdl.verification.policies.VerifyAuthentication;
import de.uni.stuttgart.ipvs.tdl.verification.policies.VerifyInterval;
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
    private final String protocolKey = "protocol";

    /**
     * Check the topic whether it is operational
     *
     * @param id topic description id
     * @return JSONObject with value "msg" that contains "online" or "offline" or failed message
     */
    @RequestMapping(method = GET, value = "/topic/{id}")
    @ResponseBody
    public ResponseEntity isTopicOperational(@PathVariable String id) {
        JSONObject response = new JSONObject();
        response.put("id", id);
        boolean operational = false;
        String topicDesc = dbConnector.getMatchedTopicDescription(id);
        if (topicDesc != null) {
            JSONObject topic = new JSONObject(topicDesc);
            String middlewareEndpointKey = "middleware_endpoint";
            String pathKey = "path";
            switch (topic.getString(protocolKey).toUpperCase()) {
                case "MQTT":
                    try {
                        operational = connectToMQTTTopic(topic.getString(middlewareEndpointKey));
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
                response.put("msg", "online");
            } else {
                response.put("msg", "offline");
            }
            return ResponseEntity.ok(response.toString());
        } else {
            response.put("msg", "No Topic with id: " + id);
            return ResponseEntity.badRequest().body(response.toString());
        }
    }

    /**
     * Starts verification of specific topic and policy type
     *
     * @param id topic id
     * @param policyType policy type
     * @return JSONObject with message of status
     */
    @RequestMapping(method = POST, value = "/topic/{id}/policy/{policyType}")
    @ResponseBody
    public ResponseEntity verifyPolicies(@PathVariable String id, @PathVariable String policyType) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
        String topicDesc = dbConnector.getMatchedTopicDescription(id);
        JSONObject response = new JSONObject();
        HttpStatus responseStatus = HttpStatus.BAD_REQUEST;
        if (topicDesc != null) {
            JSONObject topic = new JSONObject(topicDesc);
            String policyKey = "policy";
            if (topic.has(policyKey)) {
                if (!topic.has(verificationKey)) {
                    topic.put(verificationKey, new JSONObject());
                }
                JSONObject verification = updatePolicyTypes(topic.getJSONObject(verificationKey), topic.getJSONObject(policyKey));
                dbConnector.updateTopicDescription(id, topic.toString());

                //Does policy type exists in topic?
                if (verification.has(policyType)) {
                    JSONObject verifiedPolicy = verification.getJSONObject(policyType);
                    //verification already running?
                    if (!verifiedPolicy.getString(currentKey).equals(VerificationStatus.IN_PROGRESS.toString())) {
                        // Save last verification
                        verifiedPolicy.put("last", verifiedPolicy.getString(currentKey));
                        // Set current verification on "in progress"
                        verifiedPolicy.put("timestamp", sdf.format(new Date()));
                        verifiedPolicy.put(currentKey, VerificationStatus.IN_PROGRESS.toString());
                        // Store changes DB
                        dbConnector.updateVerification(id, policyType, verifiedPolicy);
                        // Start Verification
                        startVerificationExecutor(id, policyType);
                        response.put("msg", "Successfully started verification of policy: " + policyType);
                        responseStatus = HttpStatus.ACCEPTED;
                    } else {
                        response.put("msg", "Verification for " + policyType + " already running!");
                        responseStatus = HttpStatus.ACCEPTED;
                    }
                } else {
                    response.put("msg", "Topic does not contain this policy: " + policyType);
                    responseStatus = HttpStatus.NOT_FOUND;
                }
            } else {
                response.put("msg", "Topic contains no policy");
                responseStatus = HttpStatus.NOT_FOUND;
            }
        }
        else {
            response.put("msg", "No Topic with id: " + id);
        }
        return ResponseEntity.status(responseStatus).body(response.toString());
    }

    /**
     * Check if topic has more policies as last time at verification
     *
     * @param verification JSONObject topic has key verification
     * @param policy JSONObject topic has key policy
     * @return new verification JSONObject
     */
    private JSONObject updatePolicyTypes(JSONObject verification, JSONObject policy) {
        ArrayList<String> policyTypeList = new ArrayList<>();
        String topicKey = "topic";
        String policyTypeKey = "policy_type";
        if (policy.has(topicKey)) {
            for (int index = 0; index < policy.getJSONArray(topicKey).length(); index++) {
                JSONObject singlePolicy = policy.getJSONArray(topicKey).getJSONObject(index);
                policyTypeList.add(singlePolicy.getString(policyTypeKey));
            }
        }
        String messageKey = "message";
        if (policy.has(messageKey)) {
            for (int index = 0; index < policy.getJSONArray(messageKey).length(); index++) {
                JSONObject singlePolicy = policy.getJSONArray(messageKey).getJSONObject(index);
                policyTypeList.add(singlePolicy.getString(policyTypeKey));
            }
        }

        for (String policyType : policyTypeList) {
            if (!verification.has(policyType)) {
                JSONObject newPolicyType = new JSONObject();
                newPolicyType.put(currentKey, VerificationStatus.UNKNOWN.toString());
                verification.put(policyType, newPolicyType);
            }
        }
        return verification;
    }

    /**
     * Starts verification runnable if exists
     *
     * @param id topic id
     * @param policyType verification policy type
     */
    private void startVerificationExecutor(String id, String policyType) {
        String topicDesc = dbConnector.getMatchedTopicDescription(id);
        JSONObject topic = new JSONObject(topicDesc);
        String protocol = topic.getString(protocolKey).toUpperCase();

        JSONObject policyTypeVerification = topic.getJSONObject(verificationKey).getJSONObject(policyType);

        switch (policyType) {
            case "Authentication":
                VerifyAuthentication va = new VerifyAuthentication(topic);
                verificationExecutor.execute(va);
                break;
            case "Interval":
                VerifyInterval vi = new VerifyInterval(topic);
                verificationExecutor.execute(vi);
                break;
            default:
                policyTypeVerification.put(currentKey, VerificationStatus.UNKNOWN.toString());
        }
        System.out.println(policyTypeVerification.toString());
        dbConnector.updateVerification(id, policyType, policyTypeVerification);

    }

    /**
     * Check MQTT Topic operational
     *
     * @param broker middleware endpoint, example: "tcp://192.168.178.2:1883"
     * @return result of verification
     * @throws MqttException if verification failed
     */
    private boolean connectToMQTTTopic(String broker) throws MqttException {
        if (broker.startsWith("tcp://") || broker.startsWith("ssl://")) {
            MqttClient mqttclient = new MqttClient(broker, MqttClient.generateClientId());
            mqttclient.connect();
            return true;
        } else {
            return false;
        }

    }

    /**
     * Check HTTP Topic operational
     *
     * @param url topic url
     * @return result of verification
     * @throws IOException if verification failed
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