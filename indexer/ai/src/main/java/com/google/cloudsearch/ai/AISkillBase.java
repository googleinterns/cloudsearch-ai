package com.google.cloudsearch.ai;

import com.google.cloudsearch.exceptions.InvalidConfigException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public abstract class AISkillBase implements AISkill {

    private String aiSkillName = "";
    private List<OutputMapping> outputMappings = new ArrayList<OutputMapping>();
    private Map<String, Boolean> schemaInfo = new HashMap<String, Boolean>();
    private Boolean isSchemaInfoStored = false;

    private void storeSchemaInfo(JSONObject schema){
        JSONArray object = (JSONArray) schema.get("objectDefinitions");
        Iterator<JSONObject> objectIterator = object.iterator();
        while(objectIterator.hasNext()){
            JSONObject obj = objectIterator.next();
            String objName = (String) obj.get("name");
            JSONArray propertyDefinitions = (JSONArray) obj.get("propertyDefinitions");
            Iterator<JSONObject> propertyIterator = propertyDefinitions.iterator();

            while(propertyIterator.hasNext()){
                JSONObject property = propertyIterator.next();
                String propertyName = (String) property.get("name");
                schemaInfo.put(objName+"."+propertyName, true);
            }
        }
        this.isSchemaInfoStored = true;
    }
    private boolean isValid(String propertyName, JSONObject schema){

        if(isSchemaInfoStored == false)
            this.storeSchemaInfo(schema);
        if(this.schemaInfo.get(propertyName) == null)
            return false;
        else
            return true;
    }
    @Override
    public void setAISkillName(String aiSkillName) throws InvalidConfigException {
        if(aiSkillName == null || aiSkillName.equals(""))
            throw new InvalidConfigException("No skill name specified.");

        this.aiSkillName = aiSkillName;
    }

    @Override
    public String getAISkillName() {
        return this.aiSkillName;
    }

    @Override
    public void setOutputMappings(JSONArray outputMapping, JSONObject schema) throws InvalidConfigException {
        if(outputMapping == null)
            throw new InvalidConfigException("Output Mapping not specified.");

        Iterator<JSONObject> mappingIterator = outputMapping.iterator();
        while(mappingIterator.hasNext()){
            JSONObject mappingObject = mappingIterator.next();
            OutputMapping obj = new OutputMapping();

            if(isValid((String) mappingObject.get(Constants.CONFIG_TARGET_PROPERTY), schema))
                obj.setPropertyName((String) mappingObject.get(Constants.CONFIG_TARGET_PROPERTY));
            else
                throw new InvalidConfigException("Invalid Object or Property Name");

            obj.setSkillOutputField((String) mappingObject.get(Constants.CONFIG_OUTPUT_FILED));
            this.outputMappings.add(obj);
        }
    }

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
     */
    protected abstract void parse(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException;
}
