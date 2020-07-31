package com.google.cloudsearch.ai;

import junit.framework.TestCase;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class CloudStorageHandler
 */
public class TestCloudStorageHandler extends TestCase {

    private List<ImmutablePair<String, Boolean>> getTestCases() {
        List<ImmutablePair<String, Boolean>> testCases = new ArrayList<>();
        testCases.add(new ImmutablePair<>("gs://cloudsearch-ai-covid-dataset/test_data.jsonl", true));
        testCases.add(new ImmutablePair<>("gs:", false));
        testCases.add(new ImmutablePair<>("Users/path/xyz.pathl", false));
        return testCases;
    }

    /**
     * Test the function isCloudStorageURI() for different valid or invalid URIs.
     */
    @Test
    public void testCloudStorageHandler() {
        List<ImmutablePair<String, Boolean>> testCases = getTestCases();
        for(ImmutablePair<String, Boolean> testCase : testCases ) {
            boolean result = CloudStorageHandler.isCloudStorageURI(testCase.left);
            assertEquals((boolean) testCase.right, result);
        }
    }
}
