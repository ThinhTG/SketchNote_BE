package com.sketchnotes.identityservice.exception;

public class CommentNotAllowedException extends RuntimeException {
    public CommentNotAllowedException(String message) {
        super(message);
    }
    
    public CommentNotAllowedException(int currentProgress) {
        super(String.format("Comments are only allowed after 100%% course completion. Current progress: %d%%", 
            currentProgress));
    }
}
