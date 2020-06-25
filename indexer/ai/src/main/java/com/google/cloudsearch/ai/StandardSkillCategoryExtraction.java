package com.google.cloudsearch.ai;

import com.google.cloud.language.v1.*;
import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

class StandardSkillCategoryExtraction extends AISkillBase {

    private String inputLanguage = "";
    private double categoryConfidence = 0.0;
    private Logger log = Logger.getLogger(StandardSkillCategoryExtraction.class.getName());


    public StandardSkillCategoryExtraction(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException {
            this.parse(aiSkill, schema);
    }

    @Override
    public void setInputs(JSONObject input) {
        if(input == null)
            return;
        for(Object key : input.keySet()){
            if(key.equals(Constants.CONFIG_INPUT_LANGUAGE)){
                this.inputLanguage = (String) input.get(key);
            }
            else{
                log.info("Input "+key + " not expected for AISkill Category Extraction. It will be ignored.");
            }
        }
    }

    @Override
    public JSONObject getInputs() {
        JSONObject obj = new JSONObject();
        obj.put(Constants.CONFIG_INPUT_LANGUAGE, this.inputLanguage);
        return obj;
    }

    @Override
    public void setFilter(JSONObject filter) {
        if(filter == null)
            return;
        // Only one filter supported
        for(Object key : filter.keySet()) {
            if (key.equals(Constants.CONFIG_CATEGORY_CONFIDENCE))
                this.categoryConfidence = (double) filter.get(Constants.CONFIG_CATEGORY_CONFIDENCE);
            else{
                log.info("Filter "+key+" is not supported. It will be ignored.");
            }
        }
    }

    @Override
    public JSONObject getFilter() {
        JSONObject obj = new JSONObject();
        obj.put(Constants.CONFIG_CATEGORY_CONFIDENCE, this.categoryConfidence);
        return obj;
    }

    private boolean isSatisfyFilter(double confidence){
        if(confidence >= this.categoryConfidence)
            return true;
        else
            return false;
    }

    @Override
    protected void parse(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException {
        this.setAISkillName((String) aiSkill.get(Constants.CONFIG_SKILL_NAME));
        this.setOutputMappings((JSONArray) aiSkill.get(Constants.CONFIG_OUTPUT_MAPPINGS), schema);
        this.setInputs((JSONObject) aiSkill.get(Constants.CONFIG_INPUTS));
        this.setFilter((JSONObject) aiSkill.get(Constants.CONFIG_FILTERS));
    }

    @Override
    public void executeSkill(String contentOrURI, Multimap<String, Object> structuredData) {
        String text = null;
        try {
            if(CloudStorageHandler.isCouldStorageURI(contentOrURI)){
                text = CloudStorageHandler.getObject(contentOrURI);
            }
            else
                text = new String(Files.readAllBytes(Paths.get(contentOrURI)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (LanguageServiceClient language = LanguageServiceClient.create()) {
            Document doc;
            if(this.inputLanguage != "") {
                doc = Document.newBuilder().setContent(text).setLanguage(this.inputLanguage).setType(Document.Type.PLAIN_TEXT).build();
            }
            else {
                doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
            }
            ClassifyTextRequest request = ClassifyTextRequest.newBuilder().setDocument(doc).build();
            // detect categories in the given text
            ClassifyTextResponse response = language.classifyText(request);

            //Only one mappable output i.e sentiment (also, assuming only one object in schema), therefore only considering first output Mapping.
            for(OutputMapping outputMap : getOutputMappings()){
                String propertyName = outputMap.getPropertyName();
                String fieldName = outputMap.getSkillOutputField();
                switch(fieldName){
                    case Constants.CONFIG_CATEGORY:{
                        for (ClassificationCategory category : response.getCategoriesList()) {
                            if(isSatisfyFilter((double)category.getConfidence())){
                                structuredData.put(propertyName.split("\\.")[1],  category.getName());
                            }
                        }
                        break;
                    }
                    default:{
                        log.info("Output Field "+ fieldName + " not supported for AISkill Category Extraction. It will be ignored.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
