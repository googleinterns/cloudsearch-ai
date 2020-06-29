package com.google.cloudsearch.ai;

import java.util.List;
import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
    Interface for all Skills.
 */
public interface AISkill {

    public void setAISkillName(String aiSkillName);
    public String getAISkillName();
    public void parseAISkillName(String aiSkillName) throws InvalidConfigException;

    public void setOutputMappings(List<OutputMapping> outputMappingList);
    public List<OutputMapping> getOutputMappings();
    public void parseOutputMappings(JSONArray outputMapping, JSONObject schema) throws InvalidConfigException;

    public void setInputs(JSONObject input);
    public JSONObject getInputs();
    public void parseInputs(JSONObject input) throws InvalidConfigException;

    public void setFilter(JSONObject filter);
    public JSONObject getFilter();
    public void parseFilter(JSONObject filter) throws InvalidConfigException;

    /**
     * This function executes the corresponding skill and populates structured data for 1 resource at a time.
     *
     * @param contentOrURI      The actual file content or Cloud storage URI
     */
    public void executeSkill(String contentOrURI, Multimap<String, Object> structuredData);
}
