package com.google.cloudsearch.ai;

public class CloudStorageHandler {

    /**
     * Checks if a given string is a Cloud Storage URI
     * @param str   string to check
     * @return  Returns true if the given input is a Cloud Storage URI, else false.
     */
    public static boolean isCloudStorageURI(String str) {
        if(str.startsWith("gs://")) {
            return true;
        }
        else {
            return false;
        }
    }
}
