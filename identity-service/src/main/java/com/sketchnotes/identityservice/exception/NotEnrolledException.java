package com.sketchnotes.identityservice.exception;

public class NotEnrolledException extends RuntimeException {
    public NotEnrolledException(String message) {
        super(message);
    }
    
    public NotEnrolledException(Long userId, Long courseId) {
        super(String.format("User %d is not enrolled in course %d", userId, courseId));
    }
}
