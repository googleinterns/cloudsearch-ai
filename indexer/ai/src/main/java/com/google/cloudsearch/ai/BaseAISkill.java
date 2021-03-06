package com.google.cloudsearch.ai;

import com.google.cloud.language.v1.*;
import com.google.cloudsearch.exceptions.InvalidConfigException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.*;

/**
 * Abstract class BaseAISkill implements the common functionality required for the different skills.
 */
public abstract class BaseAISkill implements AISkill {

    private String aiSkillName = "";
    private List<OutputMapping> outputMappings = new ArrayList<OutputMapping>();
    private Map<String, Boolean> schemaInfo = new HashMap<String, Boolean>();
    private Boolean isSchemaInfoStored = false;

    /**
     * Parses the CloudsSearch Schema and stores the object and property names
     * @param schema    CloudSearch schema
     */
    private void storeSchemaInfo(JSONObject schema) {
        JSONArray schemaObjects = (JSONArray) schema.get(Constants.CONFIG_SCHEMA_OBJECT_DEFINITIONS);
        for(Object schemaObj : schemaObjects) {
            JSONObject obj = (JSONObject) schemaObj;
            String objName = (String) obj.get(Constants.CONFIG_SCHEMA_NAME);
            JSONArray propertyDefinitions = (JSONArray) obj.get(Constants.CONFIG_SCHEMA_PROPERTY_DEFINITIONS);
            for(Object propertyObj : propertyDefinitions) {
                JSONObject property = (JSONObject) propertyObj;
                String propertyName = (String) property.get(Constants.CONFIG_SCHEMA_NAME);
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
    private boolean isValidPropertyName(String propertyName, JSONObject schema) {
        if(isSchemaInfoStored == false) {
            this.storeSchemaInfo(schema);
        }
        return (this.schemaInfo.get(propertyName) != null);
    }

    /**
     * @param aiSkillName   Name of the AI Skill
     */
    @Override
    public void setAISkillName(String aiSkillName) { this.aiSkillName = aiSkillName; }

    /**
     * @return  Returns skill name
     */
    @Override
    public String getAISkillName() {
        return this.aiSkillName;
    }

    /**
     *
     * @param aiSkillName   Name of the AI Skill
     * @throws InvalidConfigException   throws exception if name not specified in the configuration.
     */
    @Override
    public void parseAISkillName(String aiSkillName) throws InvalidConfigException {
        if (aiSkillName == null || aiSkillName.equals("")) {
            throw new InvalidConfigException("No skill name specified.");
        }
        else {
            setAISkillName(aiSkillName);
        }
    }

    /**
     *
     * @param outputMappingList     List of output mappings
     */
    @Override
    public void setOutputMappings(List<OutputMapping> outputMappingList) {
        this.outputMappings = outputMappingList;
    }

    /**
     * @return  Returns the List of Output Mappings
     */
    @Override
    public List<OutputMapping> getOutputMappings() {
        return this.outputMappings;
    }

    /**
     * Parse output Mapping
     * @param outputMapping     JSON Array of Output Mappings
     * @param schema            CloudSearch schema
     * @throws InvalidConfigException   Throws exception if property name not present in schema
     */
    @Override
    public void parseOutputMappings(JSONArray outputMapping, JSONObject schema) throws InvalidConfigException {
        if (outputMapping == null) {
            throw new InvalidConfigException("Output Mapping not specified.");
        }
        List<OutputMapping> outputMappingList = new ArrayList<OutputMapping>();
        for(Object objMap : outputMapping) {
            JSONObject mappingObject = (JSONObject) objMap;
            OutputMapping obj = new OutputMapping();
            if(mappingObject.get(Constants.CONFIG_TARGET_PROPERTY) == null) {
                throw new InvalidConfigException("targetField not present in outputMappings.");
            }
            else {
                if (isValidPropertyName((String) mappingObject.get(Constants.CONFIG_TARGET_PROPERTY), schema)) {
                    obj.setPropertyName((String) mappingObject.get(Constants.CONFIG_TARGET_PROPERTY));
                }
                else {
                    throw new InvalidConfigException("Invalid Object or Property Name");
                }
            }

            if (mappingObject.get(Constants.CONFIG_OUTPUT_FILED) == null) {
                throw new InvalidConfigException("outputField not present in outputMappings.");
            }
            else {
                obj.setSkillOutputField((String) mappingObject.get(Constants.CONFIG_OUTPUT_FILED));
                outputMappingList.add(obj);
            }
        }
        setOutputMappings(outputMappingList);
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

    /**
     * Build Natural Language API Document.
     * @param inputLanguage     Language specified in Skill Configuration
     * @param contentOrURI      Content or Cloud Storage URI
     * @return
     */
    protected Document buildNLDocument(String inputLanguage, String contentOrURI) {
        Document doc;
        Document.Builder docBuilder = Document.newBuilder();

        if(CloudStorageHandler.isCloudStorageURI(contentOrURI)) {
            docBuilder.setGcsContentUri(contentOrURI);
        }
        else {
            docBuilder.setContent(contentOrURI);
        }

        if(inputLanguage != null && !inputLanguage.isEmpty()) {
            docBuilder.setLanguage(inputLanguage);
        }
        doc = docBuilder.setType(Document.Type.PLAIN_TEXT).build();
        return doc;
    }
}
