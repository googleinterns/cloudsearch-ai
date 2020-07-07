package com.google.cloudsearch.samples;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.cloudsearch.v1.model.Item;
import com.google.cloud.language.v1.*;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterable;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterableImpl;
import com.google.enterprise.cloudsearch.sdk.InvalidConfigurationException;
import com.google.enterprise.cloudsearch.sdk.RepositoryException;
import com.google.enterprise.cloudsearch.sdk.StartupException;
import com.google.enterprise.cloudsearch.sdk.config.ConfigValue;
import com.google.enterprise.cloudsearch.sdk.config.Configuration;
import com.google.enterprise.cloudsearch.sdk.indexing.Acl;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingItemBuilder;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingService;
import com.google.enterprise.cloudsearch.sdk.indexing.template.*;
import com.google.cloud.language.v1.Document.Type;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import java.io.FileWriter;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.google.cloudsearch.samples.naturalLanguageFunctions.getNaturalLanguageFeatures;

public class BasicRepository implements Repository{
    private static Logger log = Logger.getLogger(BasicRepository.class.getName());
    private int numberOfDocuments;
    private static List<String> allFiles = new ArrayList<String>();
    BasicRepository(){
    }
    /*
    public ItemReader<String> reader() {
        Resource[] resources = null;
        ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        try {
            resources = patternResolver.getResources("/myfolder/*.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
        MultiResourceItemReader<String> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setDelegate(new FlatFileItemReader<>(..));
        return reader;

        }
    */
    public void readAllFiles() throws IOException {

        ConfigValue<List<String>> names = Configuration.getMultiValue(
                "resources.names",
                Collections.emptyList(),
                Configuration.STRING_PARSER);

        List<String> fileNames = names.get();

        if (fileNames.isEmpty()) {
            log.info("Empty");
            throw new InvalidConfigurationException(
                    "No resources configured. Set 'resources.names' in the configuration" +
                            " to one or more resources names."
            );
        }
        log.info("getting config");
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        for(String fileName : fileNames)
        {
            log.info(fileName);
            InputStream is = classloader.getResourceAsStream(fileName);
            InputStreamReader isReader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isReader);
            StringBuffer sb = new StringBuffer();
            String content;
            while((content = reader.readLine())!= null){
                sb.append(content);
            }
            //log.info(sb.toString());
            allFiles.add(sb.toString());
        }

    }



    @Override
    public void init(RepositoryContext context) throws StartupException{

        try {
            readAllFiles();
        }
        catch(Exception e){

            log.info("Here");

        }
        numberOfDocuments = allFiles.size();
    }




    @Override
    public CheckpointCloseableIterable<ApiOperation> getAllDocs(byte[] checkpoint){
        Iterator<ApiOperation> allDocs = IntStream.range(0, numberOfDocuments)
                .mapToObj(id -> {
                    try {
                        return buildDocument(id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .iterator();

        CheckpointCloseableIterable<ApiOperation> iterator =
                new CheckpointCloseableIterableImpl.Builder<>(allDocs).build();

        return iterator;
    }



    private ApiOperation buildDocument(int id) throws IOException {

        Acl acl = new Acl.Builder()
                .setReaders(Collections.singletonList(Acl.getCustomerPrincipal()))
                .build();

       String cont = allFiles.get(id);
        ByteArrayContent byteContent = ByteArrayContent.fromString("text/plain", cont);
       //Set version to timestamp - Change this
        byte[] version = Longs.toByteArray(System.currentTimeMillis());

        String dummyURL = "www.google.com";
        Item item = IndexingItemBuilder.fromConfiguration(Integer.toString(id))
                .setItemType(IndexingItemBuilder.ItemType.CONTENT_ITEM)
                .setAcl(acl)
                .setSourceRepositoryUrl(IndexingItemBuilder.FieldOrValue.withValue(dummyURL))
                .setVersion(version)
                .build();

        try {
            Multimap<String, Object> structuredData = getNaturalLanguageFeatures(cont);
            log.info(String.valueOf(structuredData.get("sentiment")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        RepositoryDoc doc = new RepositoryDoc.Builder()
                    .setItem(item)
                    .setContent(byteContent, IndexingService.ContentFormat.TEXT)
                    .build();

        return doc;
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
