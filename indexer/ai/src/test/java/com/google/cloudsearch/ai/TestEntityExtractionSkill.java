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
 * Test Standard Skill Entity Extraction
 */
public class TestEntityExtractionSkill {

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
            schema = (JSONObject) parser.parse(new FileReader("./src/test/java/com/google/cloudsearch/ai/testFiles/EntityExtraction/sampleSchema.json"));
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
            config = (JSONObject) parser.parse(new FileReader("./src/test/java/com/google/cloudsearch/ai/testFiles/EntityExtraction/"+configName+".json"));
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
     * Test the behaviour when the skill config has an empty skill name.
     * @throws InvalidConfigException
     */
    @Test
    public void testParse() throws InvalidConfigException {
        JSONObject config = getTestConfig("skillNameConfig");
        JSONObject schema = getTestSchema();
        exception.expect(InvalidConfigException.class);
        StandardSkillEntityExtraction entityExtraction = new StandardSkillEntityExtraction(config, schema);
    }

    /**
     * Test the behaviour when the filter "type" is of type string instead of list of strings.
     * @throws InvalidConfigException
     */
    @Test
    public void testFilterType() throws InvalidConfigException {
        JSONObject config = getTestConfig("testFilterType");
        JSONObject schema = getTestSchema();
        exception.expect(InvalidConfigException.class);
        StandardSkillEntityExtraction entityExtraction = new StandardSkillEntityExtraction(config, schema);
    }

    /**
     * Test the behaviour when the filter "salience" is of type string instead of double.
     * @throws InvalidConfigException
     */
    @Test
    public void testFilterSalience() throws InvalidConfigException {
        JSONObject config = getTestConfig("testFilterSalience");
        JSONObject schema = getTestSchema();
        exception.expect(InvalidConfigException.class);
        StandardSkillEntityExtraction entityExtraction = new StandardSkillEntityExtraction(config, schema);
    }

    /**
     * Test the behaviour when the filter name is invalid.
     * @throws InvalidConfigException
     */
    @Test
    public void testIncorrectFilterName() throws InvalidConfigException {
        JSONObject config = getTestConfig("testIncorrectFilter");
        JSONObject schema = getTestSchema();
        exception.expect(InvalidConfigException.class);
        StandardSkillEntityExtraction entityExtraction = new StandardSkillEntityExtraction(config, schema);
    }
}
