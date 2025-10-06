package com.sketchnotes.order_service.exception;

public class ResourceTemplateNotFoundException extends RuntimeException {
    public ResourceTemplateNotFoundException(String message) {
        super(message);
    }
    
    public ResourceTemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
