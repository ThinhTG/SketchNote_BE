package com.sketchnotes.identityservice.exception;

public class AuthException extends  RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
