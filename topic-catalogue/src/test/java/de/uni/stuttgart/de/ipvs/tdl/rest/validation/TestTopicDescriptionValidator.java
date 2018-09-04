package de.uni.stuttgart.de.ipvs.tdl.rest.validation;

import de.uni.stuttgart.ipvs.tdl.rest.validation.TopicDescriptionValidator;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestTopicDescriptionValidator {

    private TopicDescriptionValidator VALIDATOR = new TopicDescriptionValidator();

    @Test
    public void testValidEmptyPolicy() {
        JSONObject validEmptyPolicy = new JSONObject("{}");
        try {
            JSONObject validationJSON = VALIDATOR.validateAllPolicies(validEmptyPolicy);
            System.out.println(validationJSON);
        } catch (IOException e) {
            Assert.fail("IOExceoption...");
        }
    }

    @Test
    public void testValidPricingPolicy() {
        JSONObject validPricingPolicy = new JSONObject("{\n" +
                "    \"topic\": [\n" +
                "        {\n" +
                "            \"policy_type\": \"Pricing\",\n" +
                "            \"name\": \"Free\",\n" +
                "            \"cost\": 0,\n" +
                "            \"currency\": \"euro\",\n" +
                "            \"cost_period\": \"once\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");
        try {
            JSONObject validationJSON = VALIDATOR.validateAllPolicies(validPricingPolicy);
            System.out.println(validationJSON);
        } catch (IOException e) {
            Assert.fail("IOExceoption...");
        }
    }

    @Test
    public void testInvalidPricingPolicy() {
        JSONObject invalidCostPricingPolicy = new JSONObject("{\n" +
                "    \"topic\": [\n" +
                "        {\n" +
                "            \"policy_type\": \"Pricing\",\n" +
                "            \"name\": \"Free\",\n" +
                "            \"cost\": \"free\",\n" +
                "            \"currency\": \"euro\",\n" +
                "            \"cost_period\": \"once\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        JSONObject invalidCurrencyPricingPolicy = new JSONObject("{\n" +
                "    \"topic\": [\n" +
                "        {\n" +
                "            \"policy_type\": \"Pricing\",\n" +
                "            \"name\": \"small package\",\n" +
                "            \"cost\": 0.99,\n" +
                "            \"currency\": \"yin\",\n" +
                "            \"cost_period\": \"once\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        JSONObject invalidAllPricingPolicy = new JSONObject("{\n" +
                "    \"topic\": [\n" +
                "        {\n" +
                "            \"policy_type\": \"Pricing\",\n" +
                "            \"name\": \"small package\",\n" +
                "            \"cost\": \"ten\",\n" +
                "            \"currency\": \"yin\",\n" +
                "            \"cost_period\": \"never\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        try {
            JSONObject validationJSON = VALIDATOR.validateAllPolicies(invalidCostPricingPolicy);
            System.out.println(validationJSON);
            validationJSON =  VALIDATOR.validateAllPolicies(invalidCurrencyPricingPolicy);
            System.out.println(validationJSON);
            validationJSON = VALIDATOR.validateAllPolicies(invalidAllPricingPolicy);
            System.out.println(validationJSON);
        } catch (IOException e) {
            Assert.fail("IOExceoption...");
        }
    }

    public void testMissingValuePricingPolicy() {

    }

    public void testAdditionalValuePricingPolicy() {

    }

    @Test
    public void testInvalidTestPolicy() {
        JSONObject invalidAllWrongDatatypeTestPolicy = new JSONObject("{\n" +
                "    \"topic\": [\n" +
                "        {\n" +
                "            \"policy_type\": \"Test\",\n" +
                "            \"name\": \"test\",\n" +
                "            \"b1\": \"aoo\",\n" +
                "            \"i1\": \"one\",\n" +
                "            \"n1\": \"ksdjak\",\n" +
                "            \"e1\": 2\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        JSONObject invalidEnumAsWrongStringTestPolicy = new JSONObject("{\n" +
                "    \"topic\": [\n" +
                "        {\n" +
                "            \"policy_type\": \"Test\",\n" +
                "            \"name\": \"test\",\n" +
                "            \"b1\": \"doaks\",\n" +
                "            \"i1\": \"one\",\n" +
                "            \"n1\": \"asdjklsda\",\n" +
                "            \"e1\": \"2\"\n" +
                "        }\n" +
                "    ]\n" +
                "}");

        try {
            JSONObject validationJSON = VALIDATOR.validateAllPolicies(invalidAllWrongDatatypeTestPolicy);
            System.out.println(validationJSON);
            validationJSON = VALIDATOR.validateAllPolicies(invalidEnumAsWrongStringTestPolicy);
            System.out.println(validationJSON);
        } catch (IOException e) {
            Assert.fail("IOExceoption...");
        }
    }
}
