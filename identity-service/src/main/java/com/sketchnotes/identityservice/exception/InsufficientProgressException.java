package com.sketchnotes.identityservice.exception;

public class InsufficientProgressException extends RuntimeException {
    public InsufficientProgressException(String message) {
        super(message);
    }
    
    public InsufficientProgressException(int currentProgress, int requiredProgress) {
        super(String.format("Insufficient progress: %d%%. You need at least %d%% to submit feedback.", 
            currentProgress, requiredProgress));
    }
}
