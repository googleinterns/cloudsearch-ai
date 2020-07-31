package com.google.cloudsearch.ai;

import com.google.cloudsearch.exceptions.InvalidConfigException;
import org.apache.log4j.BasicConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Generates a set of skills based on the configuration.
 */
public class AISkillSet {

    private List<AISkill> skills;
    private Logger log = Logger.getLogger(AISkillSet.class);

    /**
     * Constructor
     * @param skillSet      The aiSkillSet object from the Configuration.
     * @param schema        CloudSearch Schema
     * @throws InvalidConfigException   Throws exception if the config in invalid.
     */
    public AISkillSet(JSONObject skillSet, JSONObject schema) throws InvalidConfigException {
        BasicConfigurator.configure();
        this.parse(skillSet, schema);
    }

    /**
     * Set the skill set.
     * @param skills  List of AI Skills
     */
    public void setSkills(List<AISkill> skills) {
        this.skills = skills;
    }

    /**
     * Returns the set of skills.
     * @return
     */
    public List<AISkill> getSkills() {
        return this.skills;
    }

    /**
     * Parse the skill configuration and instantiate the AISkill objects.
     * @param skillSet  The aiSkillSet object from the Configuration.
     * @param schema    CloudSearch Schema
     * @throws InvalidConfigException   Throws exception if the config is invalid.
     */
    public void parse(JSONObject skillSet, JSONObject schema) throws InvalidConfigException {
        List<AISkill> skillList = new ArrayList();
        JSONArray skillsArray = (JSONArray) skillSet.get(Constants.CONFIG_SKILL_SET);
        if(skillsArray == null) {
            throw new InvalidConfigException("AI Skill Set not specified in configuration.");
        }
        log.info("Skills specified :");
        for(Object skillObj : skillsArray) {
            JSONObject currentSkill = (JSONObject) skillObj;
            String skillName;
            String[] nameParts;
            skillName = (String) currentSkill.get(Constants.CONFIG_SKILL_NAME);
            if(skillName == null || skillName.isEmpty()) {
                throw new InvalidConfigException("Skill name not specified.");
            }
            nameParts = skillName.split("\\.");
            if(nameParts.length !=3) {
                throw new InvalidConfigException("Skill name is invalid.");
            }
            else {
                log.info(nameParts[0] + " " + nameParts[1] + " " + nameParts[2]);
            }

            if(nameParts[1].equals(Constants.CONFIG_STANDARD_SKILL)) {
                switch(nameParts[2]) {
                    case Constants.CONFIG_ENTITY_SKILL_NAME: {
                        AISkill skill = new StandardSkillEntityExtraction(currentSkill, schema);
                        skillList.add(skill);
                        break;
                    }
                    case Constants.CONFIG_SENTIMENT_SKILL_NAME: {
                        AISkill skill = new StandardSkillSentimentExtraction(currentSkill, schema);
                        skillList.add(skill);
                        break;
                    }
                    case Constants.CONFIG_CATEGORY_SKILL_NAME: {
                        AISkill skill = new StandardSkillCategoryExtraction(currentSkill, schema);
                        skillList.add(skill);
                        break;
                    }
                    default: {
                        throw new InvalidConfigException("Standard Skill "+ nameParts[2] + " not supported.");
                    }
                }
            }
            else if(nameParts[1].equals(Constants.CONFIG_CUSTOM_SKILL)) {
                AISkill skill = new CustomSkill(currentSkill, schema);
                skillList.add(skill);
            }
            else {
                throw new InvalidConfigException("Invalid Skill name");
            }
        }
        this.setSkills(skillList);
    }
}
