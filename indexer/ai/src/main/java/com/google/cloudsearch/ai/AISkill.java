package com.google.cloudsearch.ai;


import java.util.List;

import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
    Interface for all Skills.
 */
public interface AISkill {

    public void setAISkillName(String aiSkillName);
    public String getAISkillName();

    public void setOutputMappings(JSONArray outputMapping);
    public List<OutputMapping> getOutputMappings();

    public void setInputs(JSONObject input);
    public JSONObject getInputs();

    public void setFilter(JSONObject filter);
    public JSONObject getFilter();

    /**
     * Parse the JSONObject for the skill in the Configuration.
     * Call the setter functions and initialize all the data required for the skill.
     * TODO: Decide on the type of error or exception if the config is invalid
     *
     * @param aiSkill        JSONObject for the skill
     */
    public void parse(JSONObject aiSkill);

    /**
     * This function executes the corresponding skill and populate structured data for 1 resource at a time.
     *
     * @param filePath      The filepath of the resource
     * @return              Return Structured Data for the resource
     */
    public Multimap<String, Object> executeSkill(String filePath, Multimap<String, Object> structuredData);
}
