package com.google.cloudsearch.ai;


import java.util.List;

import com.google.common.collect.Multimap;
import org.json.simple.JSONObject;

/**
    Interface for all Skills.
 */
public interface AISkill{

    public void setAISkillName(String aiSkillName);
    public void setOutputMapping(List<Mapping> outputMapping);
    public void setInputs(JSONObject input);
    public void setFilter(JSONObject filter);
    public String getAISkillName();
    public List<Mapping> getOutputMapping();
    public JSONObject getInputs();
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
     * @param filePath      The filpath of the resource if a local resource or the Cloud Storage URI
     * @return              Return Structured Data for the resource
     */
    public Multimap<String, Object> executeSkill(String filePath);
}
