package de.uni.stuttgart.ipvs.tdl.verification.policies;

import de.uni.stuttgart.ipvs.tdl.database.MongoDBConnector;
import de.uni.stuttgart.ipvs.tdl.enums.VerificationStatus;
import org.json.JSONObject;

public interface IVerifyPolicy extends Runnable {

    /**
     * Every verification has to be store into database.
     * Therefore this default method is needed for every verification.
     *
     * @param topic - verified topic
     * @param status - status of the finished verification
     * @param policyType - topic policy type
     */
    default void storeResults(JSONObject topic, VerificationStatus status, String policyType) {
        JSONObject policyTypeVerification = topic.getJSONObject("verification").getJSONObject(policyType);
        String currentKey = "current";
        policyTypeVerification.put(currentKey, status.toString());

        MongoDBConnector dbConnector = new MongoDBConnector();
        dbConnector.updateVerification(topic.getJSONObject("_id").getString("$oid"), policyType, policyTypeVerification);
    }
}
