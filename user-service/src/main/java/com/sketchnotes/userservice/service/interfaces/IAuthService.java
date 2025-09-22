package com.sketchnotes.userservice.service.interfaces;

import com.sketchnotes.userservice.pojo.request.LoginGoogleRequest;
import com.sketchnotes.userservice.pojo.request.LoginRequest;
import com.sketchnotes.userservice.pojo.request.RegisterRequest;
import com.sketchnotes.userservice.pojo.response.LoginResponse;


public interface IAuthService {
    public LoginResponse login(LoginRequest request);
    public LoginResponse register(RegisterRequest request);
    public LoginResponse loginGoogle(LoginGoogleRequest request) ;
}
