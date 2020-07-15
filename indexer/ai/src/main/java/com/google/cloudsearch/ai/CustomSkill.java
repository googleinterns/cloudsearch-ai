package com.google.cloudsearch.ai;

import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 * Class Custom Skill provides the support for invoking
 * custom function written in Cloud Functions.
 */
public class CustomSkill extends BaseAISkill {

    private JSONObject input = new JSONObject();
    private String cloudFunctionURL = "";
    HttpURLConnection connection;
    private Logger log = Logger.getLogger(StandardSkillCategoryExtraction.class.getName());

  /**
   * Constructor
   * @param aiSkill     The JSON Object in Configuration containing Category Extraction skill related
   *                    information.
   * @param schema      The CloudSearch Schema.
   * @throws InvalidConfigException
   */
    public CustomSkill(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException {
            this.parse(aiSkill, schema);
    }

    /**
     * Set the Inputs for the Cloud Function
     * @param input     JSON Input Object
     */
    @Override
    public void setInputs(JSONObject input) {
        this.input = input;
    }

    /**
     * Return the Inputs
     * @return     Returns the Inputs for the skill in JSON format.
     */
    @Override
    public JSONObject getInputs() {
        return this.input;
    }

    /**
     * Parse the JSON Object containing the inputs for the skill.
     * @param input     Input in JSON format.
     * @throws InvalidConfigException   Throws exception if the key in the input object is not expected.
     */
    @Override
    public void parseInputs(JSONObject input) throws InvalidConfigException {
        if(input == null) {
            setInputs(new JSONObject());
        }
        else {
            setInputs(input);
        }
    }

    /**
     * No Filters are required for the Custom Skill.
     * @param filter
     */
    @Override
    public void setFilter(JSONObject filter) { return; }

    /**
     * Returns the filters.
     * @return
     */
    @Override
    public JSONObject getFilter() {
        return null;
    }

    /**
     * Filters not required for Custom AI Skill.
     * @param filter
     * @throws InvalidConfigException
     */
    @Override
    public void parseFilter(JSONObject filter) throws InvalidConfigException {
        if(filter != null) {
            throw new InvalidConfigException("Filters not supported in Custom AI Skill.");
        }
    }

    /**
     * Set the Cloud Function URL.
     * @param url   URL for invoking the Cloud Function.
     */
    public void setURL(String url) {
        this.cloudFunctionURL = url;
    }

    /**
     * Return the Cloud Function URL
     * @return
     */
    public String getURL() {
        return this.cloudFunctionURL;
    }

    /**
     * Parses the Configuration for the Custom Skill.
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
        setURL((String) aiSkill.get(Constants.CONFIG_CLOUD_FUNCTION_URL));
    }

    /**
     *  No specific setup required.
     */
    @Override
    public void setupSkill() throws IOException {
        URL cloudFunction = new URL(this.getURL());
        connection = (HttpURLConnection) cloudFunction.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("content-type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
    }

    /**
     * Execute the Cloud Function.
     * JSON Response Format: { "key1" : [List of Values], "key2" : [List of Values] }
     * key1 and key2 are output field names in the mapping.
     * @param contentOrURI      The actual file content or Cloud storage URI
     * @param structuredData    For storing CloudSearch Structured Data.
     */
    @Override
    public void executeSkill(String contentOrURI, Multimap<String, Object> structuredData) {
        try {
            if(connection != null) {
                throw new IllegalStateException("Connection for Cloud Function not initialized. Call setupSkill() before calling executeSkill().");
            }
            //Data (Content or URI) is the first parameter of the input JSON
            JSONObject obj = getInputs();
            obj.put("data", contentOrURI);
            this.setInputs(obj);

            String jsonInputString = this.input.toJSONString();
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                JSONParser parser = new JSONParser();
                JSONObject res;
                res =  (JSONObject) parser.parse(response.toString());
                for(OutputMapping outputMap : getOutputMappings()) {
                    if(res.get(outputMap.getSkillOutputField()) == null)
                        continue;
                    String property = outputMap.getPropertyName().split("\\.")[1];
                    for(Object element : (JSONArray)res.get(outputMap.getSkillOutputField())) {
                        structuredData.put(property, element);
                    }
                }
            }
        }
        catch (IOException | IllegalStateException e) {
            log.error(e);
        }
        catch (Exception e) {
            log.info("Invalid Response from CloudFunction " + getAISkillName());
            log.error(e);
        }
    }

    /**
     * No Specific Shut Down required.
     */
    @Override
    public void shutdownSkill() {
        connection.disconnect();
    }
}
