package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.LoginGoogleRequest;
import com.sketchnotes.identityservice.dtos.request.LoginRequest;
import com.sketchnotes.identityservice.dtos.request.RegisterRequest;
import com.sketchnotes.identityservice.dtos.request.TokenRequest;
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
    public ResponseEntity<ApiResponse<LoginResponse>> loginGoogle(@RequestBody LoginGoogleRequest request) {
        LoginResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(new ApiResponse<LoginResponse>(200, "Login successful", response));
    }
}
