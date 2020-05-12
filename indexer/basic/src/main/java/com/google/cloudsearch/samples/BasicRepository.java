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

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHubBuilder;

import javax.activation.FileTypeMap;
import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class BasicRepository implements Repository{
    private int numberOfDocuments;
    private static GitHub github;
    private static String githubOrganizations;
    private static Logger log = Logger.getLogger(BasicRepository.class.getName());
    private static List<GHContent> allcontents  = new ArrayList<GHContent>();
    BasicRepository(){
    }
    @Override
    public void init(RepositoryContext context) throws StartupException{
	//Replace with username
        String user = "";
	//Replace with Token ID
        String token = "";
        githubOrganizations = "madhuparnab/files";

        if (github == null ) {
            try {
                github = new GitHubBuilder()
                        .withPassword(user, token)
                        .build();
            } catch (IOException e) {
                try {
                    throw new IOException("Unable to connect to GitHub", e);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        try {
            github.getMyself();
        } catch (IOException e) {
            try {
                throw new IOException("Unable to connect to GitHub", e);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        GHRepository repo = null;
        try {
            repo = github.getRepository(githubOrganizations);
        } catch (IOException e) {
            e.printStackTrace();
        }
        {
            String sc = repo.getName();
            log.info(sc);
        }
        try {

            collectContentRecursively( repo,"/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        numberOfDocuments = allcontents.size();
    }

    @Override
    public CheckpointCloseableIterable<ApiOperation> getIds(@Nullable byte[] bytes) throws RepositoryException {
        return null;
    }

    @Override
    public CheckpointCloseableIterable<ApiOperation> getChanges(@Nullable byte[] bytes) throws RepositoryException {
        return null;
    }
    private static void collectContentRecursively(GHRepository repo,
                                                  String path) throws IOException {
        log.info("Recurse");
        List<GHContent> contents = repo.getDirectoryContent(path);
        for (GHContent contentItem : contents) {
            log.info("inside");
            if (contentItem.isDirectory()) {
                log.info("here");
                collectContentRecursively(repo, contentItem.getPath());
            } else {
                try {
                    allcontents.add(contentItem);
                }
                catch(Exception e){
                    log.info("Error");
                }

                String resourceName = new URL(contentItem.getHtmlUrl()).getPath();
                log.info(() -> String.format("Adding file %s", resourceName));
            }
        }
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

    private ApiOperation buildDocument(int id) throws IOException {

        Acl acl = new Acl.Builder()
                .setReaders(Collections.singletonList(Acl.getCustomerPrincipal()))
                .build();

        GHContent contentItem = allcontents.get(id);
        String resourceName = new URL(contentItem.getHtmlUrl()).getPath();
        String mimeType = FileTypeMap.getDefaultFileTypeMap()
                .getContentType(contentItem.getName());
        AbstractInputStreamContent fileContent = new InputStreamContent(
                mimeType, contentItem.read())
                .setLength(contentItem.getSize())
                .setCloseInputStream(true);


       //Set version to timestamp - Change this
        byte[] version = Longs.toByteArray(System.currentTimeMillis());


        Item item = IndexingItemBuilder.fromConfiguration(Integer.toString(id))
                .setItemType(IndexingItemBuilder.ItemType.CONTENT_ITEM)
                .setAcl(acl)
                .setSourceRepositoryUrl(IndexingItemBuilder.FieldOrValue.withValue(resourceName))
                .setVersion(version)
                .build();

        RepositoryDoc doc = new RepositoryDoc.Builder()
                .setItem(item)
                .setContent(fileContent, IndexingService.ContentFormat.RAW)
                .build();

        return doc;
    }

}
