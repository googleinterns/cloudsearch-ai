package com.google.cloudsearch.samples;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.cloudsearch.v1.model.Item;
import com.google.api.services.cloudsearch.v1.model.PushItem;
import com.google.common.collect.ArrayListMultimap;
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
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingItemBuilder.FieldOrValue;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingService;
import com.google.enterprise.cloudsearch.sdk.indexing.template.*;

import javax.activation.FileTypeMap;
import javax.annotation.Nullable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class BasicRepository implements Repository{
    private static Logger log = Logger.getLogger(BasicRepository.class.getName());
    private int numberOfDocuments;
    private static List<String> allFiles = new ArrayList<String>();
    BasicRepository(){
    }

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
            log.info(sb.toString());
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
