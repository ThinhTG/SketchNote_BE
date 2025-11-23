package com.sketchnotes.identityservice.exception;

public class NotPurchasedException extends RuntimeException {
    public NotPurchasedException(String message) {
        super(message);
    }
    
    public NotPurchasedException(Long userId, Long resourceId) {
        super(String.format("User %d has not purchased resource %d", userId, resourceId));
    }
}
