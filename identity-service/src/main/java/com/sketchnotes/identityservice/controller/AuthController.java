package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dto.ApiResponse;
import com.sketchnotes.identityservice.dto.request.LoginRequest;
import com.sketchnotes.identityservice.dto.request.RegisterRequest;
import com.sketchnotes.identityservice.dto.response.LoginResponse;
import com.sketchnotes.identityservice.service.interfaces.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/users/auth")
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
}
