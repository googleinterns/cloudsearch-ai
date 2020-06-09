package com.google.cloudsearch.ai;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AISkillSet {

    private List<AISkill> skillSet;

    public AISkillSet(JSONObject skillSet){
        this.parse(skillSet);
    }
    public void setSkillSet(List<AISkill> skillSet){

        this.skillSet = skillSet;
    }
    public List<AISkill> getSkillSet(){

        return this.skillSet;
    }
    public void parse(JSONObject skillSet){
        List<AISkill> skillList = new ArrayList();
        JSONArray skills = (JSONArray) skillSet.get(Constants.configSkillSet);
        Iterator<JSONObject> skillIterator = skills.iterator();

        while(skillIterator.hasNext()){
            JSONObject nextSkill = (JSONObject) skillIterator.next();
            String skillName = (String) nextSkill.get(Constants.configSkillName);

            System.out.println(skillName);
            String[] nameParts = skillName.split("\\.");
            System.out.println(nameParts[0] + " " + nameParts[1] + " " + nameParts[2]);
            if(nameParts[1].equals("StandardAISkills"))
            {
                switch(nameParts[2]){
                    case "EntityExtraction": {
                        System.out.println("Inside");
                        AISkill skill = new StandardSkillEntityExtraction(nextSkill);
                        skillList.add(skill);
                        break;
                    }
                    case "SentimentExtraction": {
                        AISkill skill = new StandardSkillSentimentExtraction();
                        skill.parse(nextSkill);
                        skillList.add(skill);
                        break;
                    }
                    case "CategoryExtraction":{
                        AISkill skill = new StandardSkillCategoryExtraction();
                        skill.parse(nextSkill);
                        skillList.add(skill);
                        break;
                    }
                    default:{
                        //TODO: Error, No other standard skill supported
                    }
                }
            }
            else if(nameParts[1] == "CustomAISkills"){
                AISkill skill = new CustomSkill();
                skill.parse(nextSkill);
                skillList.add(skill);
            }
            else{
                //TODO: Error
            }
        }
        this.setSkillSet(skillList);
    }
}
