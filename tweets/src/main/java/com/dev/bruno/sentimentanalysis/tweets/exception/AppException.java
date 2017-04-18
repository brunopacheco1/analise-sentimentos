package com.dev.bruno.sentimentanalysis.tweets.exception;

public class AppException extends Exception {
    
    private static final long serialVersionUID = -4216175342452534457L;
    
    public AppException(String message) {
        super(message);
    }
}