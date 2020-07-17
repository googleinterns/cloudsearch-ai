package com.google.cloudsearch.ai;

import com.google.common.collect.Multimap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * AI Skill Driver manages the creation and execution of skills.
 */
public class AISkillDriver {

    private static AISkillSet skillSet;
    private static Logger log = Logger.getLogger(AISkillDriver.class.getName());

    /**
     * Initialize the Driver
     * @param aiConfigName  File path of the Configuration
     * @param schemaName    File path of CloudSearch Schema
     * @throws Exception    Throws Exception if skill setup fails
     */
    public static void initialize(String aiConfigName, String schemaName) throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject schema = (JSONObject) parser.parse(new FileReader(schemaName));
        JSONObject aiConfig = (JSONObject) parser.parse(new FileReader(aiConfigName));
        skillSet = new AISkillSet(aiConfig, schema);
        for(AISkill skill : skillSet.getSkills()) {
            skill.setupSkill();
        }
    }

    /**
     * Populates the structured Data by executing the skills.
     * @param structuredData    Multimap for storing the structured data.
     * @param contentOrURI      CloudStorage URI or File content in String format.
     */
    public static void populateStructuredData(String contentOrURI, Multimap<String, Object> structuredData) {
        List<AISkill> skillList = (List<AISkill>) skillSet.getSkills();
        if(skillList == null) {
            log.error("No skills Specified. Initialize the driver before calling populateStructuredData.");
            return;
        }
        skillList.forEach(skill -> {
            skill.executeSkill(contentOrURI, structuredData);
        });
    }

    /**
     * Handles skill shutdown.
     */
    public static void closeSkillDriver() throws NullPointerException {
        List<AISkill> skillList = (List<AISkill>) skillSet.getSkills();
        if(skillList == null) {
            log.error("AISkill List is not initialized. Call AISkillDriver.initialize() before calling closeSkillDriver()");
            return;
        }
        skillList.forEach(skill -> {
            skill.shutdownSkill();
        });
    }
}
