package com.google.cloudsearch.ai;

import com.google.cloudsearch.exceptions.InvalidConfigException;
import org.json.simple.JSONArray;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.json.simple.JSONObject;

/**
 * Test class AISkillSet.
 */
public class TestAISkillSet {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * Test Constructor when the passed JSONObject is not in desired format.
     * @throws InvalidConfigException
     */
    @Test
    public void testConstructor() throws InvalidConfigException {
        exception.expect(InvalidConfigException.class);
        JSONObject dummyObj = new JSONObject();
        dummyObj.put("key", "value");
        AISkillSet skillSet = new AISkillSet(dummyObj, dummyObj);
    }

    /**
     * Test the parse function when a skill with invalid skill name is passed to the function.
     * @throws InvalidConfigException
     */
    @Test
    public void testParseInvalidSkillName() throws InvalidConfigException {
        exception.expect(InvalidConfigException.class);
        JSONObject skill1 = new JSONObject();
        skill1.put("aiSkillName", "CloudSearch.InvalidName");

        JSONArray skills = new JSONArray();
        skills.add(skill1);

        JSONObject dummyObj = new JSONObject();
        dummyObj.put("aiSkillSet", skills);

        AISkillSet skillSet = new AISkillSet(dummyObj, dummyObj);
    }

    /**
     * Test the parse function when a Standard skill with invalid skill name is passed to the function.
     * @throws InvalidConfigException
     */
    @Test
    public void testParseInvalidStandardSkillName() throws InvalidConfigException {
        exception.expect(InvalidConfigException.class);
        JSONObject skill1 = new JSONObject();
        skill1.put("aiSkillName", "CloudSearch.StandardAISkills.newSkill");

        JSONArray skills = new JSONArray();
        skills.add(skill1);

        JSONObject dummyObj = new JSONObject();
        dummyObj.put("aiSkillSet", skills);

        AISkillSet skillSet = new AISkillSet(dummyObj, dummyObj);
    }
}
