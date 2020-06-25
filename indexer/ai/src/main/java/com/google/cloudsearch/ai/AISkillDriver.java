package com.google.cloudsearch.ai;

import com.google.cloudsearch.ai.AISkill;
import com.google.cloudsearch.ai.AISkillSet;
import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.common.collect.Multimap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class AISkillDriver {
    private static AISkillSet skillSet;
    private static Logger log = Logger.getLogger(AISkillDriver.class.getName());

    public static void initialize(String aiConfigName, String schemaName) throws InvalidConfigException {
        JSONParser parser = new JSONParser();
        JSONObject aiConfig = null;
        JSONObject schema = null;
        try {
            schema = (JSONObject) parser.parse(new FileReader(schemaName));
            aiConfig = (JSONObject) parser.parse(new FileReader(aiConfigName));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        skillSet = new AISkillSet(aiConfig, schema);
    }
    public static void populateStructuredData(Multimap<String, Object> structuredData, String filepath){
        List<AISkill> skillList = (List<AISkill>) skillSet.getSkillSet();
        for(AISkill skill : skillList){
            skill.executeSkill(filepath, structuredData);
        }
    }
}
