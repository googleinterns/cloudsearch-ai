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

    public void setAISkillName(String aiSkillName) throws InvalidConfigException;
    public String getAISkillName();

    public void setOutputMappings(JSONArray outputMapping, JSONObject schema) throws InvalidConfigException;
    public List<OutputMapping> getOutputMappings();

    public void setInputs(JSONObject input);
    public JSONObject getInputs();

    public void setFilter(JSONObject filter) throws InvalidConfigException;
    public JSONObject getFilter();

    /**
     * This function executes the corresponding skill and populate structured data for 1 resource at a time.
     *
     * @param contentOrURI      The actual file content or Cloud storage URI
     */
    public void executeSkill(String contentOrURI, Multimap<String, Object> structuredData);
}
