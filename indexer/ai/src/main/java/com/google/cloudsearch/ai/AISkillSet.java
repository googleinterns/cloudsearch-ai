package com.google.cloudsearch.ai;

import com.google.cloudsearch.exceptions.InvalidConfigException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class AISkillSet {

    private List<AISkill> skillSet;
    private Logger log = Logger.getLogger(AISkillSet.class.getName());

    public AISkillSet(JSONObject skillSet, JSONObject schema) throws InvalidConfigException {
        this.parse(skillSet, schema);
    }
    public void setSkillSet(List<AISkill> skillSet){

        this.skillSet = skillSet;
    }
    public List<AISkill> getSkillSet(){

        return this.skillSet;
    }
    public void parse(JSONObject skillSet, JSONObject schema) throws InvalidConfigException {
        List<AISkill> skillList = new ArrayList();
        JSONArray skills = (JSONArray) skillSet.get(Constants.CONFIG_SKILL_SET);
        log.info("Skills specified :");
        for(Object skillObj : skills){
            JSONObject nextSkill = (JSONObject) skillObj;
            String skillName;
            String[] nameParts;
            try {
                skillName = (String) nextSkill.get(Constants.CONFIG_SKILL_NAME);
                nameParts = skillName.split("\\.");
                System.out.println(nameParts[0] + " " + nameParts[1] + " " + nameParts[2]);
            }
            catch(Exception e){
                throw new InvalidConfigException("Skill name invalid or not specified.");
            }
            if(nameParts[1].equals(Constants.CONFIG_STANDARD_SKILL))
            {
                switch(nameParts[2]){
                    case Constants.CONFIG_ENTITY_SKILL_NAME: {
                        AISkill skill = new StandardSkillEntityExtraction(nextSkill, schema);
                        skillList.add(skill);
                        break;
                    }
                    case Constants.CONFIG_SENTIMENT_SKILL_NAME: {
                        AISkill skill = new StandardSkillSentimentExtraction(nextSkill, schema);
                        skillList.add(skill);
                        break;
                    }
                    case Constants.CONFIG_CATEGORY_SKILL_NAME:{
                        AISkill skill = new StandardSkillCategoryExtraction(nextSkill, schema);
                        skillList.add(skill);
                        break;
                    }
                    default:{
                        throw new InvalidConfigException("Standard Skill "+ nameParts[2] + " not supported.");
                    }
                }
            }
            else if(nameParts[1].equals(Constants.CONFIG_CUSTOM_SKILL)){
                AISkill skill = new CustomSkill(nextSkill, schema);
                skillList.add(skill);
            }
            else{
                throw new InvalidConfigException("Invalid Skill name");
            }
        }
        this.setSkillSet(skillList);
    }
}
