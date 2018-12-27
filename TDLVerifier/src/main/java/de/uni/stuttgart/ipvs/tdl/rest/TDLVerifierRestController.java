package de.uni.stuttgart.ipvs.tdl.rest;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "verifier")
public class TDLVerifierRestController {

    @RequestMapping(method = POST, value = "/online")
    @ResponseBody
    public boolean isTopicOnline(@RequestBody JSONObject topicDescription) {
        String id = topicDescription.getJSONObject("_id").getString("$oid");
        System.out.println("Is Topic [" + id + "] online?");
        return true;
    }

    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
}