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

class StandardSkillCategoryExtraction implements AISkill {

    private String skillName;
    private List<OutputMapping> outputMappings = new ArrayList<OutputMapping>();
    private String inputLanguage = "";
    private Double categoryConfidence = 0.0;
    @Override
    public void setAISkillName(String aiSkillName) {
        this.skillName = aiSkillName;
    }

    @Override
    public String getAISkillName() {
        return this.skillName;
    }

    @Override
    public void setOutputMappings(JSONArray outputMapping) {
        Iterator<JSONObject> mappingIterator = outputMapping.iterator();
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
    }

    @Override
    public JSONObject getInputs() {
        JSONObject obj = new JSONObject();
        obj.put("language", this.inputLanguage);
        return obj;
    }

    @Override
    public void setFilter(JSONObject filter) {
        //Only one filter supported
        if(filter.get(Constants.configCategoryConfidence) == null)
            return;
        else
            this.categoryConfidence = (Double) filter.get(Constants.configCategoryConfidence);
    }

    @Override
    public JSONObject getFilter() {
        JSONObject obj = new JSONObject();
        obj.put("category.confidence", this.categoryConfidence);
        return obj;
    }

    private Boolean isSatisfyFilter(Double confidence){
        if(confidence >= this.categoryConfidence)
            return Boolean.TRUE;
        else
            return Boolean.FALSE;
    }

    @Override
    public void parse(JSONObject aiSkill) {
        this.setAISkillName((String) aiSkill.get(Constants.configSkillName));
        this.setOutputMappings((JSONArray) aiSkill.get(Constants.configOutputMappings));
        this.setInputs((JSONObject) aiSkill.get(Constants.configInputs));
        this.setFilter((JSONObject) aiSkill.get(Constants.configFilters));
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
            }
            else {
                doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
            }
            ClassifyTextRequest request = ClassifyTextRequest.newBuilder().setDocument(doc).build();
            // detect categories in the given text
            ClassifyTextResponse response = language.classifyText(request);

            //Only one mappable output i.e sentiment (also, assuming only one object in schema), therefore only considering first output Mapping.
            OutputMapping outputMap= this.outputMappings.get(0);

            for (ClassificationCategory category : response.getCategoriesList()) {
                if(isSatisfyFilter((double)category.getConfidence())){
                    structuredData.put(outputMap.getPropertyName().split("\\.")[1],  category.getName());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return structuredData;
    }
}
