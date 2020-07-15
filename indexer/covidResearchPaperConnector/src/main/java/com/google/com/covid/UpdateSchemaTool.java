package com.google.com.covid;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.services.cloudsearch.v1.CloudSearch;
import com.google.api.services.cloudsearch.v1.model.Operation;
import com.google.api.services.cloudsearch.v1.model.Schema;
import com.google.api.services.cloudsearch.v1.model.Status;
import com.google.api.services.cloudsearch.v1.model.UpdateSchemaRequest;
import com.google.enterprise.cloudsearch.sdk.CredentialFactory;
import com.google.enterprise.cloudsearch.sdk.LocalFileCredentialFactory;
import com.google.enterprise.cloudsearch.sdk.config.ConfigValue;
import com.google.enterprise.cloudsearch.sdk.config.Configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;

public class UpdateSchemaTool {

    public static final int OPERATION_POLL_INTERVAL = 3 * 1000;

    /**
     * Main entry point for the schema tool.
     */
    public static void main(String[] argv) throws Exception {
        Configuration.initConfig(argv);

        ConfigValue<String> sourceId = Configuration.getString("api.sourceId", null);
        ConfigValue<String> localSchema = Configuration.getString("enrichment.schema", null);

        if (sourceId.get() == null) {
            throw new IllegalArgumentException("Missing api.sourceId value in configuration");
        }
        if (localSchema.get() == null) {
            throw new IllegalArgumentException("Missing enrichment.schema value in configuration");
        }
        updateSchema(sourceId.get(), localSchema.get());
    }

    /**
     * Builds the CloudSearch service using the credentials as configured in the SDK.
     *
     * @return CloudSearch instance
     * @throws Exception if unable to read credentials
     */
    static CloudSearch buildAuthorizedClient() throws Exception {
        CredentialFactory credentialFactory = LocalFileCredentialFactory.fromConfiguration();
        GoogleCredential credential = credentialFactory.getCredential(
                Collections.singletonList("https://www.googleapis.com/auth/cloud_search"));

        // Build the cloud search client
        return new CloudSearch.Builder(
                Utils.getDefaultTransport(),
                Utils.getDefaultJsonFactory(),
                credential)
                .setApplicationName("Connector for COVID-19 Research Papers data using NLP")
                .build();
    }

    /**
     * Updates the schema for a datasource.
     *
     * @param dataSourceId   Unique ID of the datasource.
     * @param schemaFilePath path to JSON file containing the schema
     */
    static void updateSchema(String dataSourceId, String schemaFilePath) throws Exception {
        CloudSearch cloudSearch = buildAuthorizedClient();

        Schema schema;
        try (BufferedReader br = new BufferedReader(new FileReader(schemaFilePath))) {
            schema = cloudSearch.getObjectParser().parseAndClose(br, Schema.class);
        }
        UpdateSchemaRequest updateSchemaRequest = new UpdateSchemaRequest()
                .setSchema(schema);
        String resourceName = String.format("datasources/%s", dataSourceId);
        Operation operation = cloudSearch.indexing().datasources()
                .updateSchema(resourceName, updateSchemaRequest)
                .execute();

        // Wait for the operation to complete.
        while (operation.getDone() == null || operation.getDone() == false) {
            // Wait before polling again
            Thread.sleep(OPERATION_POLL_INTERVAL);
            System.out.printf("Fetching operation: %s\n", operation.getName());
            operation = cloudSearch.operations().get(operation.getName()).execute();
        }

        // Operation is complete, check result
        Status error = operation.getError();
        if (error != null) {
            System.err.printf("Error updating schema: %s\n", error.getMessage());
        } else {
            System.out.println("Schema updated.");
        }
    }
}