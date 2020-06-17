package com.google.cloudsearch.ai;

import com.google.cloud.language.v1.*;
import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.logging.Logger;

public class StandardSkillSentimentExtraction extends AISkillBase {

    private String inputLanguage = "";
    private double sentimentScorePositive = 0.2;
    private double sentimentScoreNegative = -0.2;
    private double sentimentMagnitudeThreshold = 2.0;
    private String sentimentMagnitudeIgnore = "no";
    private Logger log = Logger.getLogger(StandardSkillEntityExtraction.class.getName());

    public StandardSkillSentimentExtraction(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException{
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
                this.inputLanguage = (String) input.get("language");
            }
            else{
                log.info("Input "+key + " not expected for AISkill Sentiment Extraction. It will be ignored.");
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
    public void setFilter(JSONObject filter) throws InvalidConfigException {

        Iterator<String>  keys = filter.keySet().iterator();
        while(keys.hasNext()){
            String key = keys.next();
            switch(key){
                case Constants.CONFIG_SENTIMENT_SCORE_POSITIVE: {
                    this.sentimentScorePositive = (double) filter.get(key);
                    break;
                }
                case Constants.CONFIG_SENTIMENT_SCORE_NEGATIVE: {
                    this.sentimentScoreNegative = (double) filter.get(key);
                    break;
                }
                case Constants.CONFIG_SENTIMENT_MAGNITUDE_THRESHOLD: {
                    this.sentimentMagnitudeThreshold = (double) filter.get(key);
                    break;
                }
                case Constants.CONFIG_SENTIMENT_MAGNITUDE_IGNORE:  {
                    this.sentimentMagnitudeIgnore = (String) filter.get(key);
                    break;
                }
                default :{
                    log.info("Filter "+key + " not expected for AISkill Sentiment Extraction. It will be ignored.");

                }
            }
        }
        //Check validity of filter
        if(Math.abs(this.sentimentScorePositive) > 1.0) {
            throw new InvalidConfigException("Filter sentimentScorePositive cannot be greater than 1.0 or less than -1.0");
        }
        if(Math.abs(this.sentimentScoreNegative) > 1.0){
            throw new InvalidConfigException("Filter sentimentScoreNegative cannot be greater than 1.0 or less than -1.0");
        }
        if( !(this.sentimentMagnitudeIgnore.equals("yes") || this.sentimentMagnitudeIgnore.equals("no"))){
            throw new InvalidConfigException("Filter sentimentMagnitudeIgnore can only be either yes or no.");
        }
        if(this.sentimentScorePositive < this.sentimentScoreNegative) {
            throw new InvalidConfigException("Filter sentimentScorePositive cannot be smaller than sentimentScoreNegative.");
        }
    }

    @Override
    public JSONObject getFilter() {
        JSONObject obj = new JSONObject();
        obj.put(Constants.CONFIG_SENTIMENT_SCORE_POSITIVE, this.sentimentScorePositive);
        obj.put(Constants.CONFIG_SENTIMENT_SCORE_NEGATIVE, this.sentimentScoreNegative);
        obj.put(Constants.CONFIG_SENTIMENT_MAGNITUDE_THRESHOLD, this.sentimentMagnitudeThreshold);
        obj.put(Constants.CONFIG_SENTIMENT_MAGNITUDE_IGNORE, this.sentimentMagnitudeIgnore);
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

            for(OutputMapping outputMap : getOutputMappings()){
                String fieldName = outputMap.getSkillOutputField();
                switch(fieldName){
                    case Constants.CONFIG_SENTIMENT:{
                        if (sentiment == null) {
                            structuredData.put(outputMap.getPropertyName().split("\\.")[1],  "NO SENTIMENT");
                        } else {
                            float score = sentiment.getScore();
                            float magnitude = sentiment.getMagnitude();
                            structuredData.put(outputMap.getPropertyName().split("\\.")[1],  this.getSentiment((double)score, (double)magnitude));
                        }
                        break;
                    }
                    default:{
                        log.info("Output Field "+ fieldName + " not supported for AISkill Sentiment Extraction. It will be ignored.");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
