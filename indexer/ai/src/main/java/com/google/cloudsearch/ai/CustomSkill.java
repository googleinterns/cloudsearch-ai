package com.google.cloudsearch.ai;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomSkill implements AISkill{

    private String aiSkillName = "";
    private List<OutputMapping> outputMappings = new ArrayList<OutputMapping>();
    private JSONObject input = new JSONObject();
    private String url = "";

    @Override
    public void setAISkillName(String aiSkillName) {
        this.aiSkillName = aiSkillName;
    }

    @Override
    public String getAISkillName() {
        return this.aiSkillName;
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
        this.input = input;
    }

    @Override
    public JSONObject getInputs() {

        return this.input;
    }

    @Override
    public void setFilter(JSONObject filter) {
        //Filters not required for Custom functions
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
    public void parse(JSONObject aiSkill) {
        this.setAISkillName((String) aiSkill.get(Constants.configSkillName));
        this.setOutputMappings((JSONArray) aiSkill.get(Constants.configOutputMappings));
        this.setInputs((JSONObject) aiSkill.get(Constants.configInputs));
        this.setURL((String) aiSkill.get(Constants.configCloudFunctionURL));
    }

    @Override
    public Multimap<String, Object> executeSkill(String filePath, Multimap<String, Object> structuredData) {
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
                System.out.println(response.toString());
                JSONParser parser = new JSONParser();
                JSONObject res =  (JSONObject) parser.parse(response.toString());

                for(OutputMapping outputMap : this.outputMappings){
                    structuredData.put(outputMap.getPropertyName().split("\\.")[1], res.get(outputMap.getSkillOutputField()));
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return structuredData;
    }
}
