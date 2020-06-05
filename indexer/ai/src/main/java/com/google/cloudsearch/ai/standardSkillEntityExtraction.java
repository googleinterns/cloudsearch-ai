package com.google.cloudsearch.ai;

import com.google.common.collect.Multimap;
import org.json.simple.JSONObject;
import java.util.List;

public class standardSkillEntityExtraction implements AISkill {
    @Override
    public void setAISkillName(String aiSkillName) {

    }

    @Override
    public void setOutputMapping(List<Mapping> outputMapping) {

    }

    @Override
    public void setInputs(JSONObject input) {

    }

    @Override
    public void setFilter(JSONObject filter) {

    }

    @Override
    public String getAISkillName() {
        return null;
    }

    @Override
    public List<Mapping> getOutputMapping() {
        return null;
    }

    @Override
    public JSONObject getInputs() {
        return null;
    }

    @Override
    public JSONObject getFilter() {
        return null;
    }

    @Override
    public void parse(JSONObject aiSkill) {

    }

    @Override
    public Multimap<String, Object> executeSkill(String filePath) {
        return null;
    }
}
