package com.sketchnotes.userservice.exception;

public class AuthException extends  RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
