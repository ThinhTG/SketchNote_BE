package com.sketchnotes.identityservice.service.interfaces;


import com.sketchnotes.identityservice.dto.request.LoginRequest;
import com.sketchnotes.identityservice.dto.request.RegisterRequest;
import com.sketchnotes.identityservice.dto.response.LoginResponse;


public interface IAuthService {
    public LoginResponse login(LoginRequest request);
    public void register(RegisterRequest request);

}
