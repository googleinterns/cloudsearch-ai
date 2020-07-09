package com.google.cloudsearch.exceptions;

/**
 * InvalidResponseException is thrown when the response from cloud function is not in valid format.
 */
public class InvalidResponseException extends Exception {
    public InvalidResponseException(String e){
        super(e);
    }
}
