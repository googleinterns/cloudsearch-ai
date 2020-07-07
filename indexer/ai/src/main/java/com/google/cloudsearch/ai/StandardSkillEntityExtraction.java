package com.google.cloudsearch.ai;

import com.google.cloud.language.v1.*;
import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * Class StandardSkillEntityExtraction implements the methods required for
 * invoking Natural Language API's Entity extraction and populating the
 * structured Data for CloudSearch.
 */
public class StandardSkillEntityExtraction extends BaseAISkill {

    private double salienceFilter = 0.0;
    private List<String> typeFilter = new ArrayList<String>();
    private String inputLanguage = "";
    private EncodingType inputEncoding = EncodingType.NONE;
    LanguageServiceClient languageService;
    private Logger log = Logger.getLogger(StandardSkillEntityExtraction.class.getName());

    /**
     * Constructor
     * @param aiSkill   The JSON Object in Configuration containing Entity Extraction skill
     *                  related information.
     * @param schema    The CloudSearch Schema.
     * @throws InvalidConfigException
     */
    public StandardSkillEntityExtraction(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException {
            this.parse(aiSkill, schema);
    }

    /**
     * Set the Inputs required for Entity Extraction skill.
     * If the specified encoding is not supported then generates a warning and the encoding remains to NONE.
     * @param input JSON Object containing the input fields.
     */
    @Override
    public void setInputs(JSONObject input) {
        for (Object key : input.keySet()) {
            if (key.equals(Constants.CONFIG_INPUT_LANGUAGE)) {
                this.inputLanguage = (String) input.get(Constants.CONFIG_INPUT_LANGUAGE);
            }
            else if (key.equals(Constants.CONFIG_INPUT_ENCODING)) {
                switch ((String) input.get(Constants.CONFIG_INPUT_ENCODING)) {
                    case Constants.CONFIG_ENTITY_ENCODING_UTF8:
                        {
                          this.inputEncoding = EncodingType.UTF8;
                          break;
                        }
                    case Constants.CONFIG_ENTITY_ENCODING_UTF16:
                        {
                          this.inputEncoding = EncodingType.UTF16;
                          break;
                        }
                    case Constants.CONFIG_ENTITY_ENCODING_UTF32:
                        {
                          this.inputEncoding = EncodingType.UTF32;
                          break;
                        }
                    default:
                        log.warn("Encoding not supported. Input Encoding set to None");
                }
            }
        }
    }

    /**
     * Returns the inputs
     * @return  Returns the Inputs for the skill in JSON format.
     */
    @Override
    public JSONObject getInputs() {
        if(this.inputEncoding == null) {
            return null;
        }
        JSONObject obj = new JSONObject();
        obj.put(Constants.CONFIG_INPUT_LANGUAGE, this.inputLanguage);
        obj.put(Constants.CONFIG_INPUT_ENCODING, this.inputEncoding);
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
            if(key.equals(Constants.CONFIG_INPUT_LANGUAGE) || key.equals(Constants.CONFIG_INPUT_ENCODING)) {
                setInputs(input);
            }
            else{
                throw new InvalidConfigException("Input "+ key + " not expected for AISkill Entity Extraction");
            }
        }
    }

    /**
     * Set the filters required for Entity Extraction.
     * @param filter    JSON Object Containing the filters required for Entity Extraction.
     */
    @Override
    public void setFilter(JSONObject filter) {
        if(filter == null)
            return;
        for(Object key : filter.keySet()) {
            switch((String)key){
                case Constants.CONFIG_ENTITY_TYPE_FILTER: {
                    this.typeFilter = (List<String>) filter.get(key);
                    break;
                }
                case Constants.CONFIG_ENTITY_SALIENCE_FILTER : {
                    this.salienceFilter = (Double) filter.get(key);
                    break;
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
        obj.put("type", this.typeFilter);
        obj.put("salience", this.salienceFilter);
        return obj;
    }

    /**
     * Parses the JSON Object containing the filters for the skill and validate it before calling the setter.
     * @param filter  JSON Object containing the filters
     * @throws InvalidConfigException     Throws exception if the filter name is not supported.
     */
    @Override
    public void parseFilter(JSONObject filter) throws InvalidConfigException {
        if(filter == null)
            return;
        for(Object key : filter.keySet()){
            if(!(key.equals(Constants.CONFIG_ENTITY_TYPE_FILTER) || key.equals(Constants.CONFIG_ENTITY_SALIENCE_FILTER)))
                throw new InvalidConfigException("Filter "+ key + " not expected for AISkill Entity Extraction.");
        }
        setFilter(filter);
    }


    /**
     * Check if the entity type and salience score satisfies the specified filter.
     * @param type      Entity Type
     * @param salience  Salience score
     * @return  Returns true if the type and salience score satisfies the filter, else false.
     */
    private boolean isFilterSatisfied(Entity.Type type, double salience) {
        boolean ansSalience = true;
        boolean ansType= true;

        if(this.salienceFilter > 0.0 && salience < this.salienceFilter) {
                ansSalience = false;
        }
        if( !this.typeFilter.isEmpty() && !this.typeFilter.contains(type.toString())) {
                ansType = false;
        }
        return ansSalience && ansType;
    }

    /**
     * Parse the Configuration for the Entity Extraction Skill
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
     * Execute the Entity Extraction Skill and populates the structured Data.
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
            AnalyzeEntitiesRequest request =
                    AnalyzeEntitiesRequest.newBuilder()
                            .setDocument(doc)
                            .setEncodingType(this.inputEncoding)
                            .build();
            AnalyzeEntitiesResponse response = languageService.analyzeEntities(request);

            for (Entity entity : response.getEntitiesList()) {
                if( !isFilterSatisfied(entity.getType(), entity.getSalience()))
                    continue;
                for(OutputMapping outputMap : getOutputMappings()) {
                    String propertyName = outputMap.getPropertyName();
                    String fieldName = outputMap.getSkillOutputField();
                    switch(fieldName) {
                        case Constants.CONFIG_ENTITY_NAME: {
                            structuredData.put(propertyName.split("\\.")[1],  entity.getName());
                            break;
                        }
                        default: {
                            log.info("Output Field "+ fieldName + " not supported for AISkill Entity Extraction. It will be ignored.");
                            //TODO: Metadata support
                        }
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
