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

public class TestBaseAISkill {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private JSONObject getTestSchema() {
        JSONParser parser = new JSONParser();
        JSONObject schema = null;
        try {
            schema = (JSONObject) parser.parse(new FileReader("./src/test/java/com/google/cloudsearch/ai/testFiles/BaseAISkill/sampleSchema.json"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return schema;
    }

    private JSONObject getTestConfig(String configName) {
        JSONParser parser = new JSONParser();
        JSONObject config = null;
        try {
            config = (JSONObject) parser.parse(new FileReader("./src/test/java/com/google/cloudsearch/ai/testFiles/BaseAISkill/"+configName+".json"));
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

    @Test
    public void testOutputField() throws InvalidConfigException {
        JSONObject config = getTestConfig("testOutputField");
        JSONObject schema = getTestSchema();
        exception.expect(InvalidConfigException.class);
        StandardSkillCategoryExtraction categoryExtraction = new StandardSkillCategoryExtraction(config, schema);
    }

    @Test
    public void testTargetProperty() throws InvalidConfigException {
        JSONObject config = getTestConfig("testTargetProperty");
        JSONObject schema = getTestSchema();
        exception.expect(InvalidConfigException.class);
        StandardSkillCategoryExtraction categoryExtraction = new StandardSkillCategoryExtraction(config, schema);
    }

    @Test
    public void testValidPropertyName() throws InvalidConfigException {
        JSONObject config = getTestConfig("testValidPropertyName");
        JSONObject schema = getTestSchema();
        exception.expect(InvalidConfigException.class);
        StandardSkillCategoryExtraction categoryExtraction = new StandardSkillCategoryExtraction(config, schema);
    }
}
