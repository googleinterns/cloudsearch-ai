package com.google.cloudsearch;

import com.google.cloudsearch.ai.AISkill;
import com.google.cloudsearch.ai.AISkillSet;
import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.common.collect.Multimap;
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class AISkillDriver {
    private AISkillSet skillSet;
    private Boolean isInvalidConfig = false;
    private static Logger log = Logger.getLogger(AISkillDriver.class.getName());

    public AISkillDriver(JSONObject aiConfig, JSONObject schema) {
        try{
            this.skillSet = new AISkillSet(aiConfig, schema);
        }
        catch(InvalidConfigException e){
            this.isInvalidConfig = true;
            log.warning("AI Skill configuration is invalid. Structured data won't be populated");
            e.printStackTrace();
        }

    }
    public Multimap<String, Object> populateStructuredData(Multimap<String, Object> structuredData, String filepath){

        if(this.isInvalidConfig)
            return structuredData;

        List<AISkill> skillList = (List<AISkill>) skillSet.getSkillSet();
        Iterator<AISkill> skillIterator = skillList.iterator();
        while(skillIterator.hasNext()){
            AISkill skill = skillIterator.next();
            skill.executeSkill(filepath, structuredData);
        }
        return structuredData;
    }
}
