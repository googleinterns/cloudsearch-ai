package com.google.cloudsearch.ai;

import com.google.cloudsearch.exceptions.InvalidConfigException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.FileReader;
import java.io.IOException;

/**
 * Test Custom Skill Support
 */
public class TestCustomSkill {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * Returns the Sample Schema JSON file used for testing.
     * @return
     */
    private JSONObject getTestSchema() {
        JSONParser parser = new JSONParser();
        JSONObject schema = null;
        try {
            schema = (JSONObject) parser.parse(new FileReader("./src/test/java/com/google/cloudsearch/ai/testFiles/CustomSkill/sampleSchema.json"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return schema;
    }

    /**
     * Returns the skill configuration file based on input configName.
     * @param configName
     * @return
     */
    private JSONObject getTestConfig(String configName) {
        JSONParser parser = new JSONParser();
        JSONObject config = null;
        try {
            config = (JSONObject) parser.parse(new FileReader("./src/test/java/com/google/cloudsearch/ai/testFiles/CustomSkill/"+configName+".json"));
            for(Object obj : (JSONArray)config.get("aiSkillSet")) {
                return (JSONObject) obj;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Test the behaviour of custom skill when a filter is specified.
     * @throws InvalidConfigException
     */
    @Test
    public void testFilter() throws InvalidConfigException {
        JSONObject config = getTestConfig("testFilter");
        JSONObject schema = getTestSchema();
        exception.expect(InvalidConfigException.class);
        CustomSkill customSkill = new CustomSkill(config, schema);
    }
}
