package com.google.cloudsearch.ai;

public class Constants {
    /**
     * CloudSearch Schema Related Constants
     */
    public final static String CONFIG_SCHEMA_OBJECT_DEFINITIONS = "objectDefinitions";
    public final static String CONFIG_SCHEMA_PROPERTY_DEFINITIONS = "propertyDefinitions";
    public final static String CONFIG_SCHEMA_NAME = "name";

    /**
     * General Constants for AI Skill Configuration
     */
    public final static String CONFIG_SKILL_SET = "aiSkillSet";
    public final static String CONFIG_SKILL_NAME = "aiSkillName";
    public final static String CONFIG_ENTITY_SKILL_NAME = "EntityExtraction";
    public final static String CONFIG_SENTIMENT_SKILL_NAME = "SentimentExtraction";
    public final static String CONFIG_CATEGORY_SKILL_NAME = "CategoryExtraction";
    public final static String CONFIG_CUSTOM_SKILL = "CustomAISkills";
    public final static String CONFIG_STANDARD_SKILL = "StandardAISkills";
    public final static String CONFIG_FILTERS = "filters";
    public final static String CONFIG_OUTPUT_MAPPINGS = "outputMappings";
    public final static String CONFIG_TARGET_PROPERTY = "targetProperty";
    public final static String CONFIG_OUTPUT_FILED = "outputField";
    public final static String CONFIG_INPUTS = "inputs";
    public final static String CONFIG_INPUT_LANGUAGE = "language";
    public final static String CONFIG_INPUT_ENCODING = "encoding";
    public final static String CONFIG_CLOUD_FUNCTION_URL = "url";

    /**
     * Constants for Category Extraction
     */
    public final static String CONFIG_CATEGORY = "category";
    public final static String CONFIG_CATEGORY_CONFIDENCE = "confidence";

    /**
     * Constants for Sentiment Extraction Standard Skill
     */
    public final static String CONFIG_SENTIMENT = "sentiment";
    public final static String CONFIG_SENTIMENT_SCORE_POSITIVE = "positiveScore";
    public final static String CONFIG_SENTIMENT_SCORE_NEGATIVE = "negativeScore";
    public final static String CONFIG_SENTIMENT_MAGNITUDE_IGNORE = "ignoreMagnitude";
    public final static String CONFIG_SENTIMENT_MAGNITUDE_THRESHOLD = "magnitudeThreshold";
    public final static String CONFIG_SENTIMENT_POSITIVE = "POSITIVE";
    public final static String CONFIG_SENTIMENT_NEGATIVE = "NEGATIVE";
    public final static String CONFIG_SENTIMENT_MIXED = "MIXED";
    public final static String CONFIG_SENTIMENT_NEUTRAL = "NEUTRAL";

    /**
     * Constants for Entity Extraction Standard Skill
     */
    public final static String CONFIG_ENTITY_NAME = "entity.name";
    public final static String CONFIG_ENTITY_TYPE_FILTER = "type";
    public final static String CONFIG_ENTITY_SALIENCE_FILTER = "minimumSalience";
    public final static String CONFIG_ENTITY_ENCODING_UTF8 = "UTF8";
    public final static String CONFIG_ENTITY_ENCODING_UTF16 = "UTF16";
    public final static String CONFIG_ENTITY_ENCODING_UTF32 = "UTF32";

}
