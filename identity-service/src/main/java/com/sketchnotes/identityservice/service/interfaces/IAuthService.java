package com.sketchnotes.identityservice.service.interfaces;


import com.sketchnotes.identityservice.dtos.request.LoginGoogleRequest;
import com.sketchnotes.identityservice.dtos.request.LoginRequest;
import com.sketchnotes.identityservice.dtos.request.RegisterRequest;
import com.sketchnotes.identityservice.dtos.request.TokenRequest;
import com.sketchnotes.identityservice.dtos.response.LoginResponse;


public interface IAuthService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    LoginResponse refreshToken(TokenRequest request);
    LoginResponse loginWithGoogle(LoginGoogleRequest request);

}
