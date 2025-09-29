package com.sketchnotes.identityservice.service.interfaces;


import com.sketchnotes.identityservice.dto.request.LoginGoogleRequest;
import com.sketchnotes.identityservice.dto.request.LoginRequest;
import com.sketchnotes.identityservice.dto.request.RegisterRequest;
import com.sketchnotes.identityservice.dto.request.TokenRequest;
import com.sketchnotes.identityservice.dto.response.LoginResponse;


public interface IAuthService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    LoginResponse refreshToken(TokenRequest request);
    LoginResponse loginWithGoogle(LoginGoogleRequest request);

}
