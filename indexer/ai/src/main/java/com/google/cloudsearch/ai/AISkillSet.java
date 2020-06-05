package com.google.cloudsearch.ai;

import com.sun.org.apache.bcel.internal.generic.ANEWARRAY;
import org.checkerframework.checker.units.qual.C;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AISkillSet {

    private List<AISkill> skillSet;
    public void setSkillSet(List<AISkill> skillSet){

        this.skillSet = skillSet;
    }
    public List<AISkill> getSkillSet(){

        return this.skillSet;
    }
    public void parse(JSONObject skillSet){
        List<AISkill> skillList = new ArrayList();
        JSONArray skills = (JSONArray) skillSet.get("aiSkillSet");
        Iterator<JSONObject> skillIterator = skills.iterator();

        while(skillIterator.hasNext()){
            JSONObject nextSkill = (JSONObject) skillIterator.next();
            String skillName = (String) nextSkill.get("aiSkillName");
            String[] nameParts = skillName.split(".");
            if(nameParts[1] == "StandardAISkills")
            {
                switch(nameParts[3]){
                    case "EntityExtraction": {
                        AISkill skill = new standardSkillEntityExtraction();
                        skill.parse(nextSkill);
                        skillList.add(skill);
                    }
                    case "SentimentExtraction": {
                        AISkill skill = new standardSkillSentimentExtraction();
                        skill.parse(nextSkill);
                        skillList.add(skill);
                    }
                    case "CategoryExtraction":{
                        AISkill skill = new standardSkillCategoryExtraction();
                        skill.parse(nextSkill);
                        skillList.add(skill);
                    }
                    default:{
                        //TODO: Error, No other standard skill supported
                    }
                }
            }
            else if(nameParts[1] == "CustomAISkills"){
                AISkill skill = new customSkill();
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
