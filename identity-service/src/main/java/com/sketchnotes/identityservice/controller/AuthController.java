package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.*;
import com.sketchnotes.identityservice.dtos.response.LoginResponse;
import com.sketchnotes.identityservice.service.interfaces.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/auth")
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(new  ApiResponse<LoginResponse>(200, "Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request){
        authService.register(request);
        return ResponseEntity.ok(new  ApiResponse<String>(200, "Register successful", null));
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestBody TokenRequest token){
        LoginResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(new  ApiResponse<LoginResponse>(200, "Token refreshed successfully", response));
    }
    @PostMapping("/login-google")
    public ResponseEntity<ApiResponse<LoginResponse>> loginGoogle(@Valid @RequestBody LoginGoogleRequest request) {
        LoginResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(new ApiResponse<LoginResponse>(200, "Login successful", response));
    }

    @PostMapping("/login-google-mobile")
    public ResponseEntity<ApiResponse<LoginResponse>> loginGoogleMobile(@Valid @RequestBody LoginGoogleMobileRequest request) {
        LoginResponse response = authService.loginWithGoogleMobile(request);
        return ResponseEntity.ok(new ApiResponse<LoginResponse>(200, "Login successful", response));
    }

    @PostMapping("/send-verify-email")
    public ResponseEntity<ApiResponse<String>> sendVerifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.sendVerifyEmail(request);
        return ResponseEntity.ok(new ApiResponse<String>(200, "Verification email sent successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.sendResetPasswordEmail(request);
        return ResponseEntity.ok(new ApiResponse<String>(200, "Reset password email sent successfully", null));
    }

    @PutMapping("/reset-password/{userId}")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @PathVariable String userId,
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(userId, request);
        return ResponseEntity.ok(new ApiResponse<String>(200, "Password reset successfully", null));
    }
}
