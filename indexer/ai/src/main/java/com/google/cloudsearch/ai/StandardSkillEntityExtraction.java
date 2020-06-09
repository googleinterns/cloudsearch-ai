package com.google.cloudsearch.ai;

import com.google.cloud.language.v1.*;
import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StandardSkillEntityExtraction implements AISkill {

    private String aiSkillName = "";
    private List<OutputMapping> outputMappings = null;
    private Double salienceFilter = 0.0;
    private List<String> typeFilter = new ArrayList<String>();
    private String inputLanguage = "";
    private String inputEncoding = "UTF8";

    public StandardSkillEntityExtraction(JSONObject aiSkill)
    {
        this.parse(aiSkill);
    }

    @Override
    public void setAISkillName(String aiSkillName) {
        this.aiSkillName = aiSkillName;
    }

    @Override
    public String getAISkillName() {
        return this.aiSkillName;
    }

    @Override
    public void setOutputMappings(JSONArray outputMapping) {

        Iterator<JSONObject> mappingIterator = outputMapping.iterator();
        this.outputMappings = new ArrayList<OutputMapping>();
        while(mappingIterator.hasNext()){
            JSONObject mappingObject = mappingIterator.next();
            OutputMapping obj = new OutputMapping();
            obj.setPropertyNames((String) mappingObject.get(Constants.configTargetProperty));
            obj.setSkillOutputField((String) mappingObject.get(Constants.configOutputField));
            this.outputMappings.add(obj);
        }
    }

    @Override
    public List<OutputMapping> getOutputMappings() {
        return this.outputMappings;
    }

    @Override
    public void setInputs(JSONObject input) {
        if(input.get("language") != null){
            this.inputLanguage = (String) input.get("language");
        }

        if(input.get("encoding") != null){
            this.inputEncoding = (String) input.get("encoding");
        }
    }

    @Override
    public JSONObject getInputs() {

        JSONObject obj = new JSONObject();
        obj.put("language", this.inputEncoding);
        obj.put("encoding", this.inputEncoding);
        return obj;
    }

    @Override
    public void setFilter(JSONObject filter) {
        Iterator<String>  keys = filter.keySet().iterator();
        while(keys.hasNext()){
            String key = keys.next();
            switch(key){
                case Constants.configEntityTypeFilter : {
                    this.typeFilter = (List<String>) filter.get(key);
                    break;
                }
                case Constants.configEntitySalienceFilter : {
                    this.salienceFilter = (Double) filter.get(key);
                    break;
                }
                default :{
                    //TODO: Error
                }
            }
        }
        System.out.println("done");

    }

    @Override
    public JSONObject getFilter() {

        JSONObject obj = new JSONObject();
        obj.put("type", this.typeFilter);
        obj.put("salience", this.salienceFilter);
        return obj;
    }

    private Boolean isSatisfyFilter(Entity.Type type, float salience){

        System.out.println(type);
        Boolean ansSalience = Boolean.TRUE;
        Boolean ansType= Boolean.TRUE;

        if(this.salienceFilter > 0.0){
            if(salience >= this.salienceFilter)
                ansSalience = Boolean.TRUE;
            else
                ansSalience = Boolean.FALSE;
        }

        if( !this.typeFilter.isEmpty() ){
            if(this.typeFilter.contains(type.toString())){
                ansType = Boolean.TRUE;
            }
            else{
                ansType = Boolean.FALSE;
            }
        }
        return ansSalience && ansType;
    }

    @Override
    public void parse(JSONObject aiSkill) {

        this.setAISkillName((String) aiSkill.get(Constants.configSkillName));
        this.setOutputMappings((JSONArray) aiSkill.get(Constants.configOutputMappings));
        this.setInputs((JSONObject) aiSkill.get(Constants.configInputs));
        this.setFilter((JSONObject) aiSkill.get(Constants.configFilters));
        System.out.println("Parsed!");
    }

    @Override
    public Multimap<String, Object> executeSkill(String filePath, Multimap<String, Object> structuredData) {

        String text = null;
        try {
            text = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (LanguageServiceClient language = LanguageServiceClient.create()) {
            Document doc;
            if(this.inputLanguage != "") {
                doc = Document.newBuilder().setContent(text).setLanguage(this.inputLanguage).setType(Document.Type.PLAIN_TEXT).build();
            } else{
                doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();

            }
            AnalyzeEntitiesRequest request =
                    AnalyzeEntitiesRequest.newBuilder()
                            .setDocument(doc)
                            .setEncodingType(EncodingType.UTF16)
                            .build();

            AnalyzeEntitiesResponse response = language.analyzeEntities(request);

            for (Entity entity : response.getEntitiesList()){

                if( !isSatisfyFilter(entity.getType(), entity.getSalience()))
                    continue;
                for(OutputMapping outputMap : this.outputMappings){
                    String propertyName = outputMap.getPropertyName();
                    String fieldName = outputMap.getSkillOutputField();
                    switch(fieldName){
                        case Constants.configEntityName:{
                            structuredData.put(propertyName.split("\\.")[1],  entity.getName());
                            break;
                        }
                        default:{
                            //TDOD: Metadata support
                        }
                    }
                }
            }

         } catch (IOException e) {
            e.printStackTrace();
        }

        return structuredData;
    }
}
