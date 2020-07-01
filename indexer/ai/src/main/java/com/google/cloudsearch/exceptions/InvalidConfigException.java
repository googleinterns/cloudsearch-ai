package com.google.cloudsearch.exceptions;

/**
 * InvalidConfigException is thrown for all errors in skill configuration.
 */
public class InvalidConfigException extends Exception {
        public InvalidConfigException(String e){
            super(e);
        }
}
