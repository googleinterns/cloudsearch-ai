package com.google.cloudsearch;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.cloudsearch.v1.model.Item;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterable;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterableImpl;
import com.google.enterprise.cloudsearch.sdk.RepositoryException;
import com.google.enterprise.cloudsearch.sdk.config.ConfigValue;
import com.google.enterprise.cloudsearch.sdk.config.Configuration;
import com.google.enterprise.cloudsearch.sdk.indexing.Acl;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingItemBuilder;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingService;
import com.google.enterprise.cloudsearch.sdk.indexing.template.ApiOperation;
import com.google.enterprise.cloudsearch.sdk.indexing.template.Repository;
import com.google.enterprise.cloudsearch.sdk.indexing.template.RepositoryContext;
import com.google.enterprise.cloudsearch.sdk.indexing.template.RepositoryDoc;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class LocalRepository implements Repository {

    private static Logger log = Logger.getLogger(LocalRepository.class.getName());
    private List<String> allFileNames = new ArrayList<String>();
    private JSONObject aiConfig = null;
    private JSONObject schema = null;
    private AISkillDriver skillDriver = null;

    @Override
    public void init(RepositoryContext repositoryContext) throws RepositoryException {

        //Read and store file paths from the resource folder
        ConfigValue<String> folderName = Configuration.getValue("resources.names",null,Configuration.STRING_PARSER);
        File[] files = getFileNames(folderName.get());
        for (File f : files) {
            allFileNames.add(f.getPath());
        }
        //Get the Skill Configuration
        ConfigValue<String> aiSkillConfig = Configuration.getValue("enrichment.config", null, Configuration.STRING_PARSER);
        JSONParser parser = new JSONParser();
        try {
            aiConfig = (JSONObject) parser.parse(new FileReader(String.valueOf(aiSkillConfig.get())));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Get the schema
        ConfigValue<String> schemaConfig = Configuration.getValue("enrichment.schema", null, Configuration.STRING_PARSER);
        try {
            schema = (JSONObject) parser.parse(new FileReader(String.valueOf(schemaConfig.get())));
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


        skillDriver = new AISkillDriver(aiConfig, schema);

        Iterator<ApiOperation> allDocs = IntStream.range(0, allFileNames.size())
                .mapToObj(id -> {
                    try {
                        return buildDocument(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .iterator();

        CheckpointCloseableIterable<ApiOperation> iterator =
                new CheckpointCloseableIterableImpl.Builder<>(allDocs).build();

        return iterator;

    }
    private ApiOperation buildDocument(int id){
        Acl acl = new Acl.Builder()
                .setReaders(Collections.singletonList(Acl.getCustomerPrincipal()))
                .build();
        String filepath = this.allFileNames.get(id);

        Multimap<String, Object> structuredData = ArrayListMultimap.create();
        skillDriver.populateStructuredData(structuredData, filepath);

        String content = "";
        try {
            content = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayContent byteContent = ByteArrayContent.fromString("text/plain", content);

        //Set version to timestamp - Change this
        byte[] version = Longs.toByteArray(System.currentTimeMillis());

        String dummyURL = "www.google.com";

        Multimap<String, Object> sentimentProperties;

        Item item = IndexingItemBuilder.fromConfiguration(Integer.toString(id))
                .setItemType(IndexingItemBuilder.ItemType.CONTENT_ITEM)
                .setObjectType(IndexingItemBuilder.FieldOrValue.withValue("Entity"))
                .setAcl(acl)
                .setValues(structuredData)
                .setSourceRepositoryUrl(IndexingItemBuilder.FieldOrValue.withValue(dummyURL))
                .setVersion(version)
                .build();

        RepositoryDoc doc = new RepositoryDoc.Builder()
                .setItem(item)
                .setContent(byteContent, IndexingService.ContentFormat.TEXT)
                .build();

        return doc;

    }
    private static File[] getFileNames(String folderName) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(folderName);
        String path = url.getPath();
        return new File(path).listFiles();
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
