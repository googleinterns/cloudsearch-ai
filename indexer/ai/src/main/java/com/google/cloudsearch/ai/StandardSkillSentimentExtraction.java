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

public class StandardSkillSentimentExtraction implements AISkill {

    private String skillName;
    private List<OutputMapping> outputMappings = new ArrayList<OutputMapping>();
    private String inputLanguage = "";
    private String inputEncoding = "UTF8";
    private Double sentimentScorePositive = 0.2;
    private Double sentimentScoreNegative = -0.2;
    private Double sentimentMagnitudeThreshold = 2.0;
    private String sentimentMagnitudeIgnore = "no";

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
                case Constants.configSentimentScorePositive : {
                    this.sentimentScorePositive = (Double) filter.get(key);
                    break;
                }
                case Constants.configSentimentScoreNegative : {
                    this.sentimentScoreNegative = (Double) filter.get(key);
                    break;
                }
                case Constants.configSentimentMagnitudeThreshold : {
                    this.sentimentMagnitudeThreshold = (Double) filter.get(key);
                    break;
                }
                case Constants.configSentimentMagnitudeIgnore :  {
                    this.sentimentMagnitudeIgnore = (String) filter.get(key);
                    break;
                }
                default :{
                    //TODO: Error
                }
            }
        }
        //Check validity of filter
        if(Math.abs(this.sentimentScorePositive) > 1.0) {
            //TODO: Throw error
        }
        if(Math.abs(this.sentimentScoreNegative) > 1.0){
            //TODO: Throw error
        }
        if( !(this.sentimentMagnitudeIgnore.equals("yes") || this.sentimentMagnitudeIgnore.equals("no"))){
            //TODO: Throw error
        }
        if(this.sentimentScorePositive < this.sentimentScoreNegative) {
            //TODO: Throw error Invalid filter
        }
    }

    @Override
    public JSONObject getFilter() {
        JSONObject obj = new JSONObject();
        obj.put("sentiment.score.positive", this.sentimentScorePositive);
        obj.put("sentiment.score.negative", this.sentimentScoreNegative);
        obj.put("sentiment.magnitude.threshold", this.sentimentMagnitudeThreshold);
        obj.put("sentiment.magnitude.ignore", this.sentimentMagnitudeIgnore);
        return obj;
    }

    private String getSentiment(Double sentimentScore, Double sentimentMagnitude) {
        if(this.sentimentMagnitudeIgnore.equals("yes")){
            if( sentimentScore >= this.sentimentScorePositive){
                return "POSITIVE";
            }
            else if(sentimentScore <= this.sentimentScoreNegative){
                return "NEGATIVE";
            }
            else{
                return "NEUTRAL";
            }
        }
        else{
            if(sentimentMagnitude >= this.sentimentMagnitudeThreshold){

                if( sentimentScore >= this.sentimentScorePositive){
                    return "POSITIVE";
                }
                else if(sentimentScore <= this.sentimentScoreNegative){
                    return "NEGATIVE";
                }
                else{
                    return "MIXED";
                }
            }
            else{

                if( sentimentScore >= this.sentimentScorePositive){
                    return "POSITIVE";
                }
                else if(sentimentScore <= this.sentimentScoreNegative){
                    return "NEGATIVE";
                }
                else{
                    return "NEUTRAL";
                }
            }
        }
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
            }
            else {
                doc = Document.newBuilder().setContent(text).setType(Document.Type.PLAIN_TEXT).build();
            }
            AnalyzeSentimentResponse response = language.analyzeSentiment(doc);
            Sentiment sentiment = response.getDocumentSentiment();

            //Only one mappable output i.e sentiment (also, assuming only one object in schema), therefore only considering first output Mapping.
            OutputMapping outputMap= this.outputMappings.get(0);

            if (sentiment == null) {
                structuredData.put(outputMap.getPropertyName().split("\\.")[1],  "NO SENTIMENT");
            } else {
                float score = sentiment.getScore();
                float magnitude = sentiment.getMagnitude();
                structuredData.put(outputMap.getPropertyName().split("\\.")[1],  this.getSentiment((double)score, (double)magnitude));
                System.out.printf("Sentiment magnitude: %.3f\n", sentiment.getMagnitude());
                System.out.printf("Sentiment score: %.3f\n", sentiment.getScore());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return structuredData;
    }
}
