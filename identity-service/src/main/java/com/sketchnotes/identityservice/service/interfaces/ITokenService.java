package com.sketchnotes.identityservice.service.interfaces;

public interface ITokenService {
    void verifyEmail(String token);
}
