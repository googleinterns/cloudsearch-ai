package com.google.cloudsearch.ai;

import com.google.cloud.language.v1.*;
import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class StandardSkillEntityExtraction extends AISkillBase {

    private Double salienceFilter = 0.0;
    private List<String> typeFilter = new ArrayList<String>();
    private String inputLanguage = "";
    private EncodingType inputEncoding = EncodingType.NONE;
    private Logger log = Logger.getLogger(StandardSkillEntityExtraction.class.getName());


    public StandardSkillEntityExtraction(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException {
            this.parse(aiSkill, schema);
    }

    @Override
    public void setInputs(JSONObject input) {

        if(input == null)
            return;

        Iterator<String> inputIterator = input.keySet().iterator();

        while(inputIterator.hasNext()){
            String key = (String) inputIterator.next();
            if(key.equals(Constants.CONFIG_INPUT_LANGUAGE)){
                this.inputLanguage = (String) input.get(Constants.CONFIG_INPUT_LANGUAGE);
            }
            else if(key.equals(Constants.CONFIG_INPUT_ENCODING)){
                switch((String) input.get(Constants.CONFIG_INPUT_ENCODING)){
                    case "UTF8" : {
                        this.inputEncoding = EncodingType.UTF8;
                        break;
                    }
                    case "UTF16" : {
                        this.inputEncoding = EncodingType.UTF16;
                        break;
                    }
                    case "UTF32" : {
                        this.inputEncoding = EncodingType.UTF32;
                        break;
                    }
                }
            }
            else{
                log.info("Input "+key + " not expected for AISkill Entity Extraction. It will be ignored.");

            }
        }

    }

    @Override
    public JSONObject getInputs() {

        JSONObject obj = new JSONObject();
        obj.put(Constants.CONFIG_INPUT_LANGUAGE, this.inputLanguage);
        obj.put(Constants.CONFIG_INPUT_ENCODING, this.inputEncoding);
        return obj;
    }

    @Override
    public void setFilter(JSONObject filter) {
        Iterator<String>  keys = filter.keySet().iterator();
        while(keys.hasNext()){
            String key = keys.next();
            switch(key){
                case Constants.CONFIG_ENTITY_TYPE_FILTER: {
                    this.typeFilter = (List<String>) filter.get(key);
                    break;
                }
                case Constants.CONFIG_ENTITY_SALIENCE_FILTER : {
                    this.salienceFilter = (Double) filter.get(key);
                    break;
                }
                default :{
                    log.info("Filter "+key + " not expected for AISkill Entity Extraction. It will be ignored.");
                }
            }
        }
    }

    @Override
    public JSONObject getFilter() {

        JSONObject obj = new JSONObject();
        obj.put("type", this.typeFilter);
        obj.put("salience", this.salienceFilter);
        return obj;
    }

    private boolean isFilterSatisfied(Entity.Type type, double salience){

        boolean ansSalience = true;
        boolean ansType= true;

        if(this.salienceFilter > 0.0){
            if(salience < this.salienceFilter)
                ansSalience = false;
        }

        if( !this.typeFilter.isEmpty() ){
            if(!this.typeFilter.contains(type.toString()))
                ansType = false;
        }
        return ansSalience && ansType;
    }

    @Override
    protected void parse(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException {

        this.setAISkillName((String) aiSkill.get(Constants.CONFIG_SKILL_NAME));
        this.setOutputMappings((JSONArray) aiSkill.get(Constants.CONFIG_OUTPUT_MAPPINGS), schema);
        this.setInputs((JSONObject) aiSkill.get(Constants.CONFIG_INPUTS));
        this.setFilter((JSONObject) aiSkill.get(Constants.CONFIG_FILTERS));
    }

    @Override
    public void executeSkill(String filePath, Multimap<String, Object> structuredData) {

        String text = null;
        try {
            text = new String(Files.readAllBytes(Paths.get(filePath)));

            LanguageServiceClient language = LanguageServiceClient.create();
            Document doc;
            if(this.inputLanguage != "") {
                doc = Document.newBuilder().setContent(text).setLanguage(this.inputLanguage).setType(Document.Type.PLAIN_TEXT).build();
            } else{
                doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
            }
            AnalyzeEntitiesRequest request =
                    AnalyzeEntitiesRequest.newBuilder()
                            .setDocument(doc)
                            .setEncodingType(this.inputEncoding)
                            .build();

            AnalyzeEntitiesResponse response = language.analyzeEntities(request);

            for (Entity entity : response.getEntitiesList()){

                if( !isFilterSatisfied(entity.getType(), entity.getSalience()))
                    continue;
                for(OutputMapping outputMap : getOutputMappings()){
                    String propertyName = outputMap.getPropertyName();
                    String fieldName = outputMap.getSkillOutputField();
                    switch(fieldName){
                        case Constants.CONFIG_ENTITY_NAME:{
                            structuredData.put(propertyName.split("\\.")[1],  entity.getName());
                            break;
                        }
                        default:{
                            log.info("Output Field "+ fieldName + " not supported for AISkill Entity Extraction. It will be ignored.");
                            //TODO: Metadata support
                        }
                    }
                }
            }

         } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
