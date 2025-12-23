package com.sketchnotes.identityservice.service.interfaces;


import com.sketchnotes.identityservice.dtos.request.*;
import com.sketchnotes.identityservice.dtos.response.LoginResponse;


public interface IAuthService {
    LoginResponse login(LoginRequest request);
    void register(RegisterRequest request);
    LoginResponse refreshToken(TokenRequest request);
    LoginResponse loginWithGoogleMobile(LoginGoogleMobileRequest request);
    void sendVerifyEmail(VerifyEmailRequest request);
    void sendResetPasswordEmail(ForgotPasswordRequest request);
    void resetPassword(String userId, ResetPasswordRequest request);
}
