package com.google.cloudsearch.ai;

import com.google.cloudsearch.exceptions.InvalidConfigException;
import com.google.cloudsearch.exceptions.InvalidResponseException;
import com.google.common.collect.Multimap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class CustomSkill extends AISkillBase {

    private JSONObject input = new JSONObject();
    private String url = "";
    private Logger log = Logger.getLogger(StandardSkillCategoryExtraction.class.getName());

    public CustomSkill(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException{

            this.parse(aiSkill, schema);
    }
    @Override
    public void setInputs(JSONObject input) {
        this.input = input;
    }

    @Override
    public JSONObject getInputs() {

        return this.input;
    }

    @Override
    public void setFilter(JSONObject filter) {
        //Filters not required for Custom functions
        if(filter!=null){
            log.info("Filters not supported in Custom AI Skills. Any filters specified will be ignored.");
        }
    }

    @Override
    public JSONObject getFilter() {

        return null;
    }

    public void setURL(String url){
        this.url = url;
    }

    public String getURL(){
        return this.url;
    }

    @Override
    protected void parse(JSONObject aiSkill, JSONObject schema) throws InvalidConfigException {
        this.setAISkillName((String) aiSkill.get(Constants.CONFIG_SKILL_NAME));
        this.setOutputMappings((JSONArray) aiSkill.get(Constants.CONFIG_OUTPUT_MAPPINGS), schema);
        this.setInputs((JSONObject) aiSkill.get(Constants.CONFIG_INPUTS));
        this.setURL((String) aiSkill.get(Constants.CONFIG_CLOUD_FUNCTION_URL));
    }

    @Override
    public void executeSkill(String filePath, Multimap<String, Object> structuredData){
        String text = null;
        try {
            text = new String(Files.readAllBytes(Paths.get(filePath)));

            //Data is the first parameter of the input JSON
            JSONObject obj = getInputs();
            obj.put("data", text);
            this.setInputs(obj);

            URL cloudFunction = new URL(this.getURL());
            HttpURLConnection connection = (HttpURLConnection) cloudFunction.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("content-type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

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
                JSONObject res = new JSONObject();
                try{
                    res =  (JSONObject) parser.parse(response.toString());
                }
                catch(ParseException e){
                    throw new InvalidResponseException("Invalid Response from CloudFunction");
                }

                for(OutputMapping outputMap : getOutputMappings()){
                    if(res.get(outputMap.getSkillOutputField()) == null)
                        continue;
                    structuredData.put(outputMap.getPropertyName().split("\\.")[1], res.get(outputMap.getSkillOutputField()));
                }
            }

        } catch (IOException | InvalidResponseException e) {
            e.printStackTrace();
        }
    }
}
