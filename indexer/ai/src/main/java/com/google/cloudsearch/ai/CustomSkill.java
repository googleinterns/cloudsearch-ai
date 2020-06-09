package com.google.cloudsearch.ai;

import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

public class CustomSkill implements AISkill{


    @Override
    public void setAISkillName(String aiSkillName) {

    }

    @Override
    public String getAISkillName() {
        return null;
    }

    @Override
    public void setOutputMappings(JSONArray outputMapping) {

    }

    @Override
    public List<OutputMapping> getOutputMappings() {
        return null;
    }

    @Override
    public void setInputs(JSONObject input) {

    }

    @Override
    public JSONObject getInputs() {
        return null;
    }

    @Override
    public void setFilter(JSONObject filter) {

    }

    @Override
    public JSONObject getFilter() {
        return null;
    }

    @Override
    public void parse(JSONObject aiSkill) {

    }

    @Override
    public Multimap<String, Object> executeSkill(String filePath, Multimap<String, Object> structuredData) {
        return null;
    }
}
