package com.google.cloudsearch.ai;

import com.google.api.services.cloudsearch.v1.model.Item;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterable;
import com.google.enterprise.cloudsearch.sdk.RepositoryException;
import com.google.enterprise.cloudsearch.sdk.config.ConfigValue;
import com.google.enterprise.cloudsearch.sdk.config.Configuration;
import com.google.enterprise.cloudsearch.sdk.indexing.template.ApiOperation;
import com.google.enterprise.cloudsearch.sdk.indexing.template.Repository;
import com.google.enterprise.cloudsearch.sdk.indexing.template.RepositoryContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.io.FileReader;
import java.io.IOException;

public class LocalRepository implements Repository {
    private AISkillSet skillSet;
    @Override
    public void init(RepositoryContext repositoryContext) throws RepositoryException {
        //Read and store file paths from the resource folder
        this.skillSet = new AISkillSet();
        //Parse the JSON
        ConfigValue<String> AISkillConfig = Configuration.getValue("mapping.config", null, Configuration.STRING_PARSER);
        JSONParser parser = new JSONParser();
        try {
            Object aiConfig = parser.parse(new FileReader(String.valueOf(AISkillConfig.get())));
            this.skillSet.parse((JSONObject) aiConfig);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CheckpointCloseableIterable<ApiOperation> getIds(@Nullable byte[] bytes) throws RepositoryException {
        return null;
    }

    @Override
    public CheckpointCloseableIterable<ApiOperation> getChanges(@Nullable byte[] bytes) throws RepositoryException {
        return null;
    }

    @Override
    public CheckpointCloseableIterable<ApiOperation> getAllDocs(@Nullable byte[] bytes) throws RepositoryException {
        //Iterate through all resources and execute skills for each resource
        /*
            Pseudo Code:

            for each resource:
                for each skill in aiSkillSet:
                    executeSkill(resourcePath)
         */
        return null;
    }

    @Override
    public ApiOperation getDoc(Item item) throws RepositoryException {
        return null;
    }

    @Override
    public boolean exists(Item item) throws RepositoryException {
        return false;
    }

    @Override
    public void close() {

    }
}
