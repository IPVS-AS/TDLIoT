package de.uni.stuttgart.ipvs.tdl.rest.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

public class TopicDescriptionValidator {
    /**
     * return value json properties keys
     */
    private final String success = "success";
    private final String msg = "msg";

    /**
     * return value
     */
    private JSONObject validationPolicyJSON = new JSONObject();

    /**
     * Validate the topic description with the TDL-Json-Scheme
     *
     * @param topicDescription the topic description json as String
     * @return the result of the scheme validation as ProcessingReport
     * @throws IOException         when the scheme could not load from the file
     * @throws ProcessingException when the validation crashes
     */
    public synchronized ProcessingReport validateTopicOnJsonScheme(String topicDescription) throws IOException, ProcessingException {
        final JsonValidator VALIDATOR = JsonSchemaFactory.byDefault().getValidator();
        JsonNode schemeNode = JsonLoader.fromFile(new File("src/main/resources/jsonTDLscheme.json"));
        JsonNode newTopicNode = JsonLoader.fromString(topicDescription);
        return VALIDATOR.validate(schemeNode, newTopicNode);
    }

    /**
     * @param policiesJSON the policies as JSONObject. ex:
     *                     "policy": {
     *                     "message": [
     *                     {
     *                     "policy_type": "Accuracy",
     *                     "name": "Accuracy",
     *                     "accuracy": 0.0001
     *                     }
     *                     ],
     *                     "topic": [
     *                     {
     *                     "policy_type": "Pricing",
     *                     "name": "small Package",
     *                     "cost": 0.99,
     *                     "currency": "euro",
     *                     "cost_period": "month"
     *                     }
     *                     ]
     *                     }
     * @return JSON with format:
     * {
     * "success": true or false"
     * "msg": [
     * success = true -> messages empty. ELSE: (example)
     * "pricing: \"Free\" {cost, [free]} is not a number"
     * ]
     * }
     * @throws IOException when the connection to github breaks off or gets disturbed
     */
    public synchronized JSONObject validateAllPolicies(JSONObject policiesJSON) throws IOException {
        validationPolicyJSON.put(success, true);
        if (validationPolicyJSON.has(msg)) {
            validationPolicyJSON.remove(msg);
        }
        URL githubTDLPolicyTypesURL = new URL("https://api.github.com/repos/lehmansn/TDLPolicy/contents/policy_types");
        HttpURLConnection connection = (HttpURLConnection) githubTDLPolicyTypesURL.openConnection();
        connection.setRequestMethod("GET");

        InputStream policyTypesStream = connection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(policyTypesStream));

        StringBuilder streamResponse = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            streamResponse.append(line);
        }
        in.close();
        JSONArray policyTypesJSON = new JSONArray(streamResponse.toString());
        connection.disconnect();

        // Validation
        validateCategory(policiesJSON, "topic", policyTypesJSON);
        validateCategory(policiesJSON, "message", policyTypesJSON);

        return validationPolicyJSON;
    }

    /**
     * Validates a single category (topic or message)
     *
     * @param policiesJSON        The current policy
     * @param category            The category
     * @param policyTypesRepoJSON The policy types of the repo
     * @throws IOException when the connection to github breaks off or gets disturbed
     */
    private void validateCategory(JSONObject policiesJSON, String category, JSONArray policyTypesRepoJSON) throws IOException {
        String policyTypeKey = "policy_type";
        String nameKey = "name";
        String downloadURLKey = "download_url";
        // Example: "accuracy.json" -> remove 5 digits = "accuracy"
        int removeDigits = 5;

        // If category not exists skip this category
        if (policiesJSON.has(category)) {
            JSONArray categoryPoliciesJSON = policiesJSON.getJSONArray(category);
            validateDuplicatePolicies(categoryPoliciesJSON);

            for (int policyIndex = 0; policyIndex < categoryPoliciesJSON.length(); policyIndex++) {
                JSONObject currentPolicyJSON = categoryPoliciesJSON.getJSONObject(policyIndex);
                String currentPolicyTypeValue = currentPolicyJSON.getString(policyTypeKey).toLowerCase();
                String currentPolicyTypeValueLowerCase = currentPolicyJSON.getString(policyTypeKey).toLowerCase();

                boolean policyTypeExists = false;
                for (int policyTypeIndex = 0; policyTypeIndex < policyTypesRepoJSON.length(); policyTypeIndex++) {
                    JSONObject policyFileTypeJSON = policyTypesRepoJSON.getJSONObject(policyTypeIndex);
                    String policyTypeName = policyFileTypeJSON.getString(nameKey).toLowerCase();
                    policyTypeName = policyTypeName.substring(0, policyTypeName.length() - removeDigits);

                    if (currentPolicyTypeValueLowerCase.equals(policyTypeName)) {
                        validatePolicyInputValues(policyFileTypeJSON.getString(downloadURLKey), category, currentPolicyJSON);
                        policyTypeExists = true;
                    }
                }
                if (!policyTypeExists) {
                    // PolicyType is invalid
                    invalidPolicy("PolicyType: \"" + currentPolicyTypeValue + "\" is not a valid policy type. Check the GitHub repository!");
                }
            }
        }
    }

    /**
     * Validates the policies of one category and checks for duplicated policy types
     *
     * @param categoryPoliciesJSON The category of the current policy
     */
    private void validateDuplicatePolicies(JSONArray categoryPoliciesJSON) {
        String policyTypeKey = "policy_type";
        HashSet<String> existingPolicyTypes = new HashSet<>();
        for (int policyIndex = 0; policyIndex < categoryPoliciesJSON.length(); policyIndex++) {
            String policyTypeLowerCase = categoryPoliciesJSON.getJSONObject(policyIndex).getString(policyTypeKey).toLowerCase();
            if (existingPolicyTypes.contains(policyTypeLowerCase)) {
                String policyType = categoryPoliciesJSON.getJSONObject(policyIndex).getString(policyTypeKey);
                // PolicyType duplicates
                invalidPolicy("PolicyType: \"" + policyType + "\" exists already. No duplicates allowed!");
            } else {
                existingPolicyTypes.add(policyTypeLowerCase);
            }
        }
    }

    /**
     * Validates input values of one policy with its definition on the GitHub repo TDLPolicy "https://github.com/lehmansn/TDLPolicy"
     *
     * @param downloadUrlPolicyType URL to download the policy type json file
     * @param currentPolicyJSON     current policy to validate with the right policy type of the GitHub repo
     * @throws IOException when the connection to github breaks off or gets disturbed
     */
    private void validatePolicyInputValues(String downloadUrlPolicyType, String category, JSONObject currentPolicyJSON) throws IOException {
        URL githubTDLPolicyTypesURL = new URL(downloadUrlPolicyType);
        HttpURLConnection connection = (HttpURLConnection) githubTDLPolicyTypesURL.openConnection();
        connection.setRequestMethod("GET");
        InputStream policyTypesStream = connection.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(policyTypesStream));

        StringBuilder streamResponse = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            streamResponse.append(line);
        }
        in.close();
        JSONObject policyTypeJSON = new JSONObject(streamResponse.toString());

        String policyType = policyTypeJSON.getString("policy_type");
        String name = currentPolicyJSON.getString("name");
        String rightPolicyCategory = policyTypeJSON.getString("policy_category");
        if (category.equals(rightPolicyCategory)) {
            for (int inputValueIndex = 0; inputValueIndex < policyTypeJSON.getJSONArray("input").length(); inputValueIndex++) {
                JSONObject inputValueJSON = policyTypeJSON.getJSONArray("input").getJSONObject(inputValueIndex);
                String valueName = inputValueJSON.getString("value");
                String datatype = inputValueJSON.getString("datatype");
                if (currentPolicyJSON.has(valueName)) {
                    Object value = currentPolicyJSON.get(valueName);
                    try {
                        switch (inputValueJSON.getString("datatype")) {
                            case "boolean":
                                currentPolicyJSON.getBoolean(inputValueJSON.getString("value"));
                                break;
                            case "int":
                                currentPolicyJSON.getInt(inputValueJSON.getString("value"));
                                break;
                            case "number":
                                currentPolicyJSON.getDouble(inputValueJSON.getString("value"));
                                break;
                            case "enum":
                                String enumValue = currentPolicyJSON.getString(inputValueJSON.getString("value"));
                                boolean validEnumValue = false;
                                for (int index = 0; index < inputValueJSON.getJSONArray("enum").length(); index++) {
                                    if (inputValueJSON.getJSONArray("enum").getString(index).equals(enumValue)) {
                                        validEnumValue = true;
                                        break;
                                    }
                                }
                                if (!validEnumValue) {
                                    throw new JSONException("JSONObject[\"" + valueName + "\"] is not an valid enum value.");
                                }
                                break;
                            case "string":
                                currentPolicyJSON.getString(inputValueJSON.getString("value"));
                                break;
                        }
                    } catch (JSONException e) {
                        // Value invalid data type
                        invalidPolicy(createInvalidDataTypeMessage(policyType, name, valueName, value, datatype));
                    }
                } else {
                    // Missing Input Value
                    invalidPolicy(policyType + ": \"" + name + "\" {" + valueName + "} is missing!");
                }
            }
        } else {
            // Category is invalid
            invalidPolicy(policyType + ": \"" + name + "\" wrong policy category (" + category + "), right category is " + rightPolicyCategory);
        }
    }

    /**
     * Changes return value to invalid and put the validation messages into the return value
     *
     * @param invalidMessage Validation message for the user
     */
    private void invalidPolicy(String invalidMessage) {
        validationPolicyJSON.put(success, false);
        if (!validationPolicyJSON.has(msg)) {
            validationPolicyJSON.put(msg, new JSONArray());
        }
        validationPolicyJSON.getJSONArray(msg).put(invalidMessage);
    }

    /**
     * Creates a message for the return json value
     *
     * @param policyType the policy type of the invalid value
     * @param name       the name of the policy of the invalid value
     * @param valueName  the value name of the invalid value
     * @param value      the invalid value
     * @param datatype   the data type of the invalid value
     * @return a String with all information of the invalid value
     */
    private String createInvalidDataTypeMessage(String policyType, String name, String valueName, Object value, String datatype) {
        String msg;
        switch (datatype) {
            case "int":
                msg = policyType + ": \"" + name + "\" {" + valueName + ", [" + value + "]} is not an " + datatype;
                break;
            case "enum":
                msg = policyType + ": \"" + name + "\" {" + valueName + ", [" + value + "]} is not a valid enum value";
                break;
            default:
                msg = policyType + ": \"" + name + "\" {" + valueName + ", [" + value + "]} is not a " + datatype;
                break;
        }
        return msg;
    }
}
