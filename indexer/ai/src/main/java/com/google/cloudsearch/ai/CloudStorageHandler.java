package com.google.cloudsearch.ai;

public class CloudStorageHandler {

    /**
     * Checks if a given string is a Cloud Storage URI
     * @param str   string to check
     * @return  Returns true if the given input is a Cloud Storage URI, else false.
     */
    public static boolean isCouldStorageURI(String str){
        if(str.substring(0,5).equals("gs://")) {
            return true;
        }
        else {
            return false;
        }
    }
}
