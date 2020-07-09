package com.google.cloudsearch.ai;

import com.google.cloud.language.v1.*;
import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * Class StandardSkillCategoryExtraction implements the methods required for
 * invoking Natural Language API's Category extraction and populating the
 * structured Data for CloudSearch.
 */
class StandardSkillCategoryExtraction extends BaseAISkill {

    private String inputLanguage = "";
    private double categoryConfidence = 0.0;
    LanguageServiceClient languageService;
    private Logger log = Logger.getLogger(StandardSkillCategoryExtraction.class.getName());

    /**
     * Constructor
     * @param aiSkill   The JSON Object in Configuration containing Category Extraction skill
     *                  related information.
     * @param schema    The CloudSearch Schema.
     * @throws InvalidConfigException
     */
    public StandardSkillCategoryExtraction(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException {
            this.parse(aiSkill, schema);
    }

    /**
     * Set the Inputs required for Category Extraction skill
     * @param input JSON Object containing the input fields.
     */
    @Override
    public void setInputs(JSONObject input) {
        this.inputLanguage = (String) input.get(Constants.CONFIG_INPUT_LANGUAGE);
    }

    /**
     * Returns the inputs
     * @return  Returns the Inputs for the skill in JSON format.
     */
    @Override
    public JSONObject getInputs() {
        if (this.inputLanguage == null) {
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
        for(Object key : input.keySet()) {
            if(key.equals(Constants.CONFIG_INPUT_LANGUAGE)) {
                setInputs(input);
            }
            else {
                throw new InvalidConfigException("Input " + key + " not expected for AISkill Category Extraction.");
            }
        }
    }

    /**
     * Set the filters required for Category Extraction.
     * @param filter    JSON Object Containing the filters required for Category Extraction.
     */
    @Override
    public void setFilter(JSONObject filter) {
        this.categoryConfidence = (double) filter.get(Constants.CONFIG_CATEGORY_CONFIDENCE);
    }

    /**
     * Returns the filters
     * @return  Returns the filters in JSON Format.
     */
    @Override
    public JSONObject getFilter() {
        JSONObject obj = new JSONObject();
        obj.put(Constants.CONFIG_CATEGORY_CONFIDENCE, this.categoryConfidence);
        return obj;
    }

  /**
   * Parse the JSON Object containing the filters for the skill and validate it before calling the setter.
   * @param filter  JSON Object containing the filters
   * @throws InvalidConfigException     Throws exception if the filter name is not supported.
   */
  @Override
  public void parseFilter(JSONObject filter) throws InvalidConfigException {
        if(filter == null) {
            return;
        }
        for(Object key : filter.keySet()) {
            if (key.equals(Constants.CONFIG_CATEGORY_CONFIDENCE))
                setFilter(filter);
            else {
                throw new InvalidConfigException("Filter " + key + " is not supported for AISkill Category Extraction.");
            }
        }
    }

    /**
     * Check if the confidence score satisfied the specified filter.
     * @param confidence    Confidence score obtained for each category returned by the API
     * @return              Returns true if the condition is satisfied, else returns false.
     */
    private boolean isFilterSatisfied(double confidence) {
        return (confidence >= this.categoryConfidence);
    }

    /**
     * Parses the Configuration for the Category Extraction Skill
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
     * Set up the language service client.
     * @throws Exception    Throws IOException if client creation fails.
     */
    @Override
    public void setupSkill() throws Exception {
        languageService = LanguageServiceClient.create();
    }

    /**
     * Execute the Category Extraction Skill and populates the structured Data.
     * @param contentOrURI      The actual file content or Cloud storage URI
     * @param structuredData    Multimap to store the structured data for indexing.
     */
    @Override
    public void executeSkill(String contentOrURI, Multimap<String, Object> structuredData) {
        try {
            if(languageService == null) {
                throw new IllegalStateException("Language Service client not initialized. Call setupSkill() before executing the skill.");
            }
            Document doc = buildNLDocument(this.inputLanguage, contentOrURI);
            ClassifyTextRequest request = ClassifyTextRequest.newBuilder().setDocument(doc).build();
            ClassifyTextResponse response = languageService.classifyText(request);

            for(OutputMapping outputMap : getOutputMappings()) {
                String propertyName = outputMap.getPropertyName();
                String fieldName = outputMap.getSkillOutputField();
                switch(fieldName) {
                    case Constants.CONFIG_CATEGORY: {
                        for (ClassificationCategory category : response.getCategoriesList()) {
                            if(isFilterSatisfied((double)category.getConfidence())) {
                                structuredData.put(propertyName.split("\\.")[1],  category.getName());
                            }
                        }
                        break;
                    }
                    default: {
                        log.info("Output Field "+ fieldName + " not supported for AISkill Category Extraction. It will be ignored.");
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Shutdown LanguageService Client
     */
    @Override
    public void shutdownSkill() {
        try {
            languageService.shutdown();
            languageService.awaitTermination(30, TimeUnit.SECONDS);
            languageService.close();
        } catch (InterruptedException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(e);
        }
    }
}
