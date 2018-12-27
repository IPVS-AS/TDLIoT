package de.uni.stuttgart.ipvs.tdl.verification.policies;

import de.uni.stuttgart.ipvs.tdl.enums.VerificationStatus;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class VerifyInterval implements IVerifyPolicy {

    private final JSONObject topic;

    private ArrayList<Date> messageTimestamps;
    private boolean verificationError = false;

    public VerifyInterval(JSONObject topic) {
        this.topic = topic;
        this.messageTimestamps = new ArrayList<>();
    }

    @Override
    public void run() {
        switch (topic.getString("protocol").toUpperCase()) {
            case "MQTT":
                startMQTTIntervalVerification();
                break;
            default:
                storeResults(topic, VerificationStatus.UNKNOWN, "Interval");
                break;
        }
    }

    private void startMQTTIntervalVerification() {
        try {
            MqttClient mqttclient = new MqttClient(topic.getString("middleware_endpoint"), MqttClient.generateClientId());
            mqttclient.setCallback(new CustomMqttCallback(mqttclient));
            mqttclient.connect();
            mqttclient.subscribe(topic.getString("path"));
        } catch (MqttException e) {
            verificationError = true;
        }
    }

    private class CustomMqttCallback implements MqttCallback {

        private MqttClient mqttclient;
        CustomMqttCallback(MqttClient mqttclient) {
            this.mqttclient = mqttclient;
        }

        @Override
        public void connectionLost(Throwable cause) {
            verificationError = true;
        }

        @Override
        public void messageArrived(String topicString, MqttMessage message) throws Exception {
            Date now = new Date();
            messageTimestamps.add(now);
            System.out.println("message Arrived");

            if (!verificationError) {
                if (messageTimestamps.size() >= 10) {
                    JSONArray messageArray = topic.getJSONObject("policy").getJSONArray("message");
                    long valueMillisec = 0;
                    for (int index = 0; index < messageArray.length(); index++) {
                        JSONObject policy = messageArray.getJSONObject(index);
                        if (policy.getString("policy_type").equals("Interval")) {
                            valueMillisec = policy.getInt("value");
                            switch (policy.getString("unit")) {
                                case "day":
                                    valueMillisec *= 24;
                                case "hour":
                                    valueMillisec *= 60;
                                case "min":
                                    valueMillisec *= 60;
                                case "sec":
                                    valueMillisec *= 1000;
                            }
                        }
                    }
                    System.out.println("Policy-Value: " + valueMillisec);
                    if (valueMillisec != 0) {
                        boolean verificationValid = true;
                        long firstTimestamp = messageTimestamps.get(0).getTime();
                        for (int index = 1; index < messageTimestamps.size(); index++) {
                            long secondTimestamp = messageTimestamps.get(index).getTime();
                            long diff = secondTimestamp - firstTimestamp;
                            System.out.println("Diff: " + diff);
                            if (!verifyDeviation(valueMillisec, diff)) {
                                verificationValid = false;
                                break;
                            }
                            firstTimestamp = secondTimestamp;
                        }
                        if (verificationValid) {
                            storeResults(topic, VerificationStatus.VALID, "Interval");
                            mqttclient.disconnect();
                        } else {
                            storeResults(topic, VerificationStatus.INVALID, "Interval");
                            mqttclient.disconnect();
                        }
                    } else {
                        storeResults(topic, VerificationStatus.INVALID, "Interval");
                        mqttclient.disconnect();
                    }
                }
            } else {
                storeResults(topic, VerificationStatus.INVALID, "Interval");
                mqttclient.disconnect();
            }
        }

        private boolean verifyDeviation(long milliseconds, long diff) {
            long upperThreshold = milliseconds + (long)(milliseconds * 0.1);
            long lowerThreshold = milliseconds - (long)(milliseconds * 0.1);
            return (upperThreshold >= diff) && (diff >= lowerThreshold);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
        }
    }
}
