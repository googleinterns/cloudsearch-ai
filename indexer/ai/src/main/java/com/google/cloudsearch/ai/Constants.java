package com.google.cloudsearch.ai;

public class Constants {
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

    /**
     * Constants for Entity Extraction Standard Skill
     */
    public final static String CONFIG_ENTITY_NAME = "entity.name";
    public final static String CONFIG_ENTITY_TYPE_FILTER = "type";
    public final static String CONFIG_ENTITY_SALIENCE_FILTER = "minimumSalience";
    /**
     * Constants for different entity types
     */
    public final static String configEntityTypePerson = "PERSON";
    public final static String configEntityTypeLocation = "LOCATION";
    public final static String configEntityTypeOrganization = "ORGANIZATION";
    public final static String configEntityTypeEvent = "EVENT";
    public final static String configEntityTypeWorkOfArt = "ARTWORK";
    public final static String configEntityTypeConsumerGood = "CONSUMER_GOOD";
    public final static String configEntityTypeOther = "OTHER";
    public final static String configEntityTypePhoneNumber = "PHONE_NUMBER";
    public final static String configEntityTypeAddress = "ADDRESS";
    public final static String configEntityTypeDate= "DATE";
    public final static String configEntityTypeNumber = "NUMBER";
    public final static String configEntityTypePrice= "PRICE";
    public final static String configEntityTypeUNKNOWN = "UNKNOWN";

    /**
     * Constants for Entity Metadata fields : Phone
     */
    public final static String configEntityMetadataPhoneNumber = "entity.metadata.phone.number";
    public final static String configEntityMetadataPhoneNationalPrefix = "entity.metadata.phone.nationalPrefix";
    public final static String configEntityMetadataPhoneAreaCode = "entity.metadata.phone.areaCode";
    public final static String configEntityMetadataPhoneExtension= "entity.metadata.phone.extension";

    /**
     * Constants for Entity Metadata fields : Address
     */
    public final static String configEntityMetadataAddressStreetNumber = "entity.metadata.address.streetNumber";
    public final static String configEntityMetadataAddressLocality = "entity.metadata.address.locality";
    public final static String configEntityMetadataAddressStreetName = "entity.metadata.address.streetName";
    public final static String configEntityMetadataAddressPostalCode = "entity.metadata.address.postalCode";
    public final static String configEntityMetadataAddressCountry = "entity.metadata.address.country";
    public final static String configEntityMetadataAddressBroadRegion = "entity.metadata.address.broadRegion";
    public final static String configEntityMetadataAddressNarrowRegion = "entity.metadata.address.narrowRegion";
    public final static String configEntityMetadataAddressSubLocality = "entity.metadata.address.subLocality";

    /**
     * Constants for Entity Metadata fields : Date
     */
    public final static String configEntityMetadataDateYear = "entity.metadata.date.year";
    public final static String configEntityMetadataDateMonth = "entity.metadata.date.month";
    public final static String configEntityMetadataDateDay = "entity.metadata.date.day";

    /**
     * Constants for Entity Metadata fields : Price
     */
    public final static String configMetadataPriceValue = "entity.metadata.price.value";
    public final static String configMetadataPriceCurrency = "entity.metadata.price.currency";

}
