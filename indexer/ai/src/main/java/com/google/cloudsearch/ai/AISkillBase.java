package com.google.cloudsearch.ai;

import com.google.cloudsearch.exceptions.InvalidConfigException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.*;

/**
 * Abstract class AISkillBase implements the common functionality required for the different skills.
 */
public abstract class AISkillBase implements AISkill {

    private String aiSkillName = "";
    private List<OutputMapping> outputMappings = new ArrayList<OutputMapping>();
    private Map<String, Boolean> schemaInfo = new HashMap<String, Boolean>();
    private Boolean isSchemaInfoStored = false;

    /**
     * Parses the CloudsSearch Schema and stores the object and property names
     * @param schema    CloudSearch schema
     */
    private void storeSchemaInfo(JSONObject schema){
        JSONArray schemaObjects = (JSONArray) schema.get("objectDefinitions");
        for(Object schemaObj : schemaObjects){
            JSONObject obj = (JSONObject) schemaObj;
            String objName = (String) obj.get("name");
            JSONArray propertyDefinitions = (JSONArray) obj.get("propertyDefinitions");
            for(Object propertyObj : propertyDefinitions){
                JSONObject property = (JSONObject) propertyObj;
                String propertyName = (String) property.get("name");
                schemaInfo.put(objName+"."+propertyName, true);
            }
        }
        this.isSchemaInfoStored = true;
    }

    /**
     * Helper function to validate property names in config
     * @param propertyName  object name + property name specified in configuration
     * @param schema    CloudSearch schema
     * @return
     */
    private boolean isValid(String propertyName, JSONObject schema){

        if(isSchemaInfoStored == false)
            this.storeSchemaInfo(schema);
        if(this.schemaInfo.get(propertyName) == null)
            return false;
        else
            return true;
    }

    /**
     * @param aiSkillName   Name of the AI Skill
     * @throws InvalidConfigException   throws exception if name not specified in the configuration.
     */
    @Override
    public void setAISkillName(String aiSkillName) throws InvalidConfigException {
        if(aiSkillName == null || aiSkillName.equals(""))
            throw new InvalidConfigException("No skill name specified.");

        this.aiSkillName = aiSkillName;
    }

    /**
     * @return  Returns skill name
     */
    @Override
    public String getAISkillName() {
        return this.aiSkillName;
    }

    /**
     * @param outputMapping     JSON Array of Output Mappings
     * @param schema            CloudSearch schema
     * @throws InvalidConfigException   Throws exception if property name not present in schema
     */
    @Override
    public void setOutputMappings(JSONArray outputMapping, JSONObject schema) throws InvalidConfigException {
        if(outputMapping == null)
            throw new InvalidConfigException("Output Mapping not specified.");

        for(Object objMap : outputMapping){
            JSONObject mappingObject = (JSONObject) objMap;
            OutputMapping obj = new OutputMapping();
            if(isValid((String) mappingObject.get(Constants.CONFIG_TARGET_PROPERTY), schema))
                obj.setPropertyName((String) mappingObject.get(Constants.CONFIG_TARGET_PROPERTY));
            else
                throw new InvalidConfigException("Invalid Object or Property Name");

            obj.setSkillOutputField((String) mappingObject.get(Constants.CONFIG_OUTPUT_FILED));
            this.outputMappings.add(obj);
        }
    }

    /**
     * @return  Returns the List of Output Mappings
     */
    @Override
    public List<OutputMapping> getOutputMappings() {
        return this.outputMappings;
    }

    /**
     * Parse the JSONObject for the skill in the Configuration.
     * Call the setter functions and initialize all the data required for the skill.
     * Throws InvalidConfigException if the skill config is invalid.
     *
     * @param aiSkill        JSONObject for the skill
     * @param schema         CloudSearch schema
     * @throws InvalidConfigException   If the configuration for mapping skill outputs
     *                                  to schema objects is invalid then throws InvalidConfigException.
     */
    protected abstract void parse(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException;
}
