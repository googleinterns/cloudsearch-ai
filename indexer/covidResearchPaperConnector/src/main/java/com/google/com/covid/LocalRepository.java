package com.google.com.covid;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.cloudsearch.v1.model.Item;
import com.google.cloudsearch.exceptions.InvalidConfigException;
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
import com.google.cloudsearch.ai.AISkillDriver;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import com.opencsv.*;

public class LocalRepository implements Repository {

    private static Logger log = Logger.getLogger(LocalRepository.class.getName());
    private List<String> allFileNames = new ArrayList<String>();
    private String aiConfigName = null;
    private String schemaName = null;
    private AISkillDriver skillDriver = null;
    private Map<String, Multimap<String, String >> idMap = new HashMap<>();


    @Override
    public void init(RepositoryContext repositoryContext) throws RepositoryException {

        //Read and store file paths from the resource folder
        ConfigValue<String> folderName = Configuration.getValue("resources.names",null,Configuration.STRING_PARSER);
        File[] files = getFileNames(folderName.get());
        for (File f : files) {
            allFileNames.add(f.getPath());
        }
        readMetadata("covid_metadata.csv");

        //Get the Skill Configuration name
        ConfigValue<String> aiSkillConfig = Configuration.getValue("enrichment.config", null, Configuration.STRING_PARSER);
        aiConfigName = aiSkillConfig.get();

        //Get the schema name
        ConfigValue<String> schemaConfig = Configuration.getValue("enrichment.schema", null, Configuration.STRING_PARSER);
        schemaName = schemaConfig.get();

        //Initialize AI Skill Driver
        try {
            AISkillDriver.initialize(aiConfigName, schemaName);
        }
        catch(InvalidConfigException e){
            log.warning("Invalid Config");
        }
        catch (Exception e) {
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

    /**
     * Read the  Metadata file
     * @param fileName
     */
    private void readMetadata(String fileName){
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(fileName);
        String path = url.getPath();
        FileReader filereader = null;
        try {
            filereader = new FileReader(path);
            CSVReader csvReader = new CSVReader(filereader);
            List<String[]> allData = csvReader.readAll();
            for (String[] row : allData) {
                Multimap<String, String> map = ArrayListMultimap.create();
                map.put("title", row[1]);
                map.put("uri", row[2]);
                map.put("url", row[3]);
                map.put("cord_uid", row[0]);
                for(String name : row[4].split(";")) {
                    map.put("author", name);
                }
                idMap.put(row[0],map);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param id Document ID
     * @return
     */
    private ApiOperation buildDocument(int id) {
        log.info(String.valueOf(id));
        Acl acl = new Acl.Builder()
                .setReaders(Collections.singletonList(Acl.getCustomerPrincipal()))
                .build();
        Multimap<String, Object> structuredData = ArrayListMultimap.create();

        String filepath = this.allFileNames.get(id);
        String[] temp = filepath.split("/");
        String key = temp[temp.length-1].split("\\.")[0];
        String title = (String) idMap.get(key).get("title").toArray()[0];
        for(Object author : idMap.get(key).get("author").toArray()) {
            structuredData.put("authorName", (String) author);
        }
        log.info(key);
        log.info((String) idMap.get(key).get("uri").toArray()[0]);
        String URI = (String) idMap.get(key).get("uri").toArray()[0];
        String URL = (String) idMap.get(key).get("url").toArray()[0];
        AISkillDriver.populateStructuredData(URI, structuredData);
        byte[] pdf = new byte[0];
        try {
            pdf = Files.readAllBytes(Paths.get(filepath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayContent byteContent = new ByteArrayContent("application/pdf",pdf);

        byte[] version = Longs.toByteArray(System.currentTimeMillis());

        Item item = IndexingItemBuilder.fromConfiguration(key)
                .setItemType(IndexingItemBuilder.ItemType.CONTENT_ITEM)
                .setObjectType(IndexingItemBuilder.FieldOrValue.withValue("researchPaper"))
                .setAcl(acl)
                .setTitle(IndexingItemBuilder.FieldOrValue.withValue(title))
                .setValues(structuredData)
                .setSourceRepositoryUrl(IndexingItemBuilder.FieldOrValue.withValue(URL))
                .setVersion(version)
                .build();

        RepositoryDoc doc = new RepositoryDoc.Builder()
                .setItem(item)
                .setContent(byteContent, IndexingService.ContentFormat.RAW)
                .build();

        return doc;
    }

    /**
     *  Get all the file names from a folder.
     * @param folderName    Folder containing the required files.
     * @return
     */
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

    /**
     * Close Skill Driver.
     */
    @Override
    public void close() {
        AISkillDriver.closeSkillDriver();
    }
}
