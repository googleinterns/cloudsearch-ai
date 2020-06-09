package com.google.cloudsearch;

import com.google.enterprise.cloudsearch.sdk.indexing.IndexingApplication;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingConnector;
import com.google.enterprise.cloudsearch.sdk.indexing.template.FullTraversalConnector;
import com.google.enterprise.cloudsearch.sdk.indexing.template.Repository;

public class CloudSearchAIConnector {

    public static void main(String[] args) throws InterruptedException {

        Repository repository = new LocalRepository();
        IndexingConnector connector = new FullTraversalConnector(repository);
        IndexingApplication application = new IndexingApplication.Builder(connector, args).build();
        application.start();
    }
}
