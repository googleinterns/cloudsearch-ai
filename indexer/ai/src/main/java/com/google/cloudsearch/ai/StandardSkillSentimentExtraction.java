package com.google.cloudsearch.ai;

import com.google.cloud.language.v1.*;
import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Class StandardSkillSentimentExtraction implements the methods required for
 * invoking Natural Language API's Sentiment extraction and populating the
 * structured Data for CloudSearch.
 */
public class StandardSkillSentimentExtraction extends BaseAISkill {

    private String inputLanguage = "";
    private double sentimentScorePositive = 0.2;
    private double sentimentScoreNegative = -0.2;
    private double sentimentMagnitudeThreshold = 2.0;
    private String sentimentMagnitudeIgnore = "no";
    private Logger log = Logger.getLogger(StandardSkillEntityExtraction.class.getName());

    /**
     * Constructor
     * @param aiSkill   The JSON Object in Configuration containing Sentiment Extraction skill
     *                  related information.
     * @param schema    The CloudSearch Schema.
     * @throws InvalidConfigException
     */
    public StandardSkillSentimentExtraction(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException {
            this.parse(aiSkill, schema);
    }

    /**
     * Set the Inputs required for Sentiment Extraction skill.
     * @param input JSON Object containing the input fields.
     */
    @Override
    public void setInputs(JSONObject input) {
        this.inputLanguage += (String) input.get("language");
    }

    /**
     * Returns the inputs
     * @return  Returns the Inputs for the skill in JSON format.
     */
    @Override
    public JSONObject getInputs() {
        if(this.inputLanguage == null) {
            return null;
        }
        JSONObject obj = new JSONObject();
        obj.put(Constants.CONFIG_INPUT_LANGUAGE, this.inputLanguage);
        return obj;
    }

    /**
     * Parse the JSON Object containing the inputs for the skill and validate it before
     * calling the setter.
     * @param input     Input in JSON format.
     * @throws InvalidConfigException   Throws exception if the key in the input object is not expected.
     */
    @Override
    public void parseInputs(JSONObject input) throws InvalidConfigException {
        if(input == null) {
            return;
        }
        for(Object key : input.keySet()){
            if(key.equals(Constants.CONFIG_INPUT_LANGUAGE)) {
                setInputs(input);
            }
            else {
                throw new InvalidConfigException("Input "+ key + " not expected for AISkill Sentiment Extraction.");
            }
        }
    }

    /**
     * Set the filters required for Sentiment Extraction.
     * Generates warning and ignores any unexpected filter specified.
     * @param filter    JSON Object Containing the filters required for Sentiment Extraction.
     */
    @Override
    public void setFilter(JSONObject filter) {
        for(Object key : filter.keySet()) {
            switch((String)key) {
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
                case Constants.CONFIG_SENTIMENT_MAGNITUDE_IGNORE: {
                    this.sentimentMagnitudeIgnore = (String) filter.get(key);
                    break;
                }
                default : {
                    log.warning("Filter "+ key + " not expected for AISkill Sentiment Extraction. It will be ignored.");

                }
            }
        }
    }

    /**
     * Returns the filters
     * @return  Returns the filters in JSON Format.
     */
    @Override
    public JSONObject getFilter() {
        JSONObject obj = new JSONObject();
        obj.put(Constants.CONFIG_SENTIMENT_SCORE_POSITIVE, this.sentimentScorePositive);
        obj.put(Constants.CONFIG_SENTIMENT_SCORE_NEGATIVE, this.sentimentScoreNegative);
        obj.put(Constants.CONFIG_SENTIMENT_MAGNITUDE_THRESHOLD, this.sentimentMagnitudeThreshold);
        obj.put(Constants.CONFIG_SENTIMENT_MAGNITUDE_IGNORE, this.sentimentMagnitudeIgnore);
        return obj;
    }

    /**
     * Parses the JSON Object containing the filters for the skill and validate it before calling the setter.
     * @param filter  JSON Object containing the filters
     * @throws InvalidConfigException     Throws exception if the filter name is not supported.
     */
    @Override
    public void parseFilter(JSONObject filter) throws InvalidConfigException {
        if(filter == null){
            return;
        }
        setFilter(filter);
        //Check validity of filter
        if(Math.abs(this.sentimentScorePositive) > 1.0) {
            throw new InvalidConfigException("Filter sentimentScorePositive cannot be greater than 1.0 or less than -1.0");
        }
        if(Math.abs(this.sentimentScoreNegative) > 1.0){
            throw new InvalidConfigException("Filter sentimentScoreNegative cannot be greater than 1.0 or less than -1.0");
        }
        if( !(this.sentimentMagnitudeIgnore.equals("yes") || this.sentimentMagnitudeIgnore.equals("no"))) {
            throw new InvalidConfigException("Filter sentimentMagnitudeIgnore can only be either yes or no.");
        }
        if(this.sentimentScorePositive < this.sentimentScoreNegative) {
            throw new InvalidConfigException("Filter sentimentScorePositive cannot be smaller than sentimentScoreNegative.");
        }
    }

    /**
     * Decide sentiment type (POSITIVE/NEGATIVE) based on score.
     * @param sentimentScore    Sentiment Score
     * @return                  Returns POSITIVE/ NEGATIVE or empty string
     */
    private String decideSentiment(Double sentimentScore){
        if( sentimentScore >= this.sentimentScorePositive) {
            return Constants.CONFIG_SENTIMENT_POSITIVE;
        }
        else if(sentimentScore <= this.sentimentScoreNegative) {
            return Constants.CONFIG_SENTIMENT_NEGATIVE;
        }
        else {
            return "";
        }
    }

    /**
     * Find out the sentiment based on the filters from the given sentiment score and magnitude.
     * @param sentimentScore        Sentiment Score of the document obtained from the API
     * @param sentimentMagnitude    Sentiment Magnitude of the document obtained from the API
     * @return                      Returns the inferred sentiment based on the filter. The returned value can
     *                              be Positive, Negative, Neutral or Mixed.
     */
    private String getSentiment(Double sentimentScore, Double sentimentMagnitude) {
        String sentiment = decideSentiment(sentimentScore);
        if(this.sentimentMagnitudeIgnore.equals("yes")) {
            if(sentiment.equals("")) {
                return Constants.CONFIG_SENTIMENT_NEUTRAL;
            }
            else {
                return sentiment;
            }
        }
        else {
            if(sentimentMagnitude >= this.sentimentMagnitudeThreshold) {
                if(sentiment.equals("")) {
                    return Constants.CONFIG_SENTIMENT_MIXED;
                }
                else {
                    return sentiment;
                }
            }
            else {
                if(sentiment.equals("")) {
                    return Constants.CONFIG_SENTIMENT_NEUTRAL;
                }
                else {
                    return sentiment;
                }
            }
        }
    }

    /**
     * Parse the Configuration for the Sentiment Extraction Skill
     * @param aiSkill        JSONObject for the skill
     * @param schema         CloudSearch schema
     * @throws InvalidConfigException
     */
    @Override
    protected void parse(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException {

        parseAISkillName((String) aiSkill.get(Constants.CONFIG_SKILL_NAME));
        parseOutputMappings((JSONArray) aiSkill.get(Constants.CONFIG_OUTPUT_MAPPINGS), schema);
        parseInputs((JSONObject) aiSkill.get(Constants.CONFIG_INPUTS));
        parseFilter((JSONObject) aiSkill.get(Constants.CONFIG_FILTERS));
    }

    /**
     * Execute the Sentiment Extraction Skill and populates the structured Data.
     * @param contentOrURI      The actual file content or Cloud storage URI
     * @param structuredData    Multimap to store the structured data for indexing.
     */
    @Override
    public void executeSkill(String contentOrURI, Multimap<String, Object> structuredData) {
        try (LanguageServiceClient language = LanguageServiceClient.create()) {
            Document doc = buildNLDocument(language, this.inputLanguage, contentOrURI);

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
        language.shutdown();
        language.awaitTermination(30, TimeUnit.SECONDS);
        language.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
