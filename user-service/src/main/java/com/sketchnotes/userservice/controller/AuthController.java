package com.sketchnotes.userservice.controller;

import com.sketchnotes.userservice.pojo.request.LoginGoogleRequest;
import com.sketchnotes.userservice.pojo.request.LoginRequest;
import com.sketchnotes.userservice.pojo.request.RegisterRequest;
import com.sketchnotes.userservice.pojo.response.LoginResponse;
import com.sketchnotes.userservice.service.interfaces.IAuthService;
import com.sketchnotes.userservice.ultils.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/users/auth")
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success( response,"Login successful"));
    }
    @PostMapping("/login-google")
    public  ResponseEntity<ApiResponse<LoginResponse>> loginGoogle(@RequestBody LoginGoogleRequest request) {
        LoginResponse response = authService.loginGoogle(request);
        return ResponseEntity.ok(ApiResponse.success( response,"Login successful"));
    }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody RegisterRequest request){
        LoginResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success( response,"Register successful"));
    }
}
