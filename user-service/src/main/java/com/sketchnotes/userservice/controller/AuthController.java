package com.sketchnotes.userservice.controller;

import com.sketchnotes.userservice.pojo.request.LoginGoogleRequest;
import com.sketchnotes.userservice.pojo.request.LoginRequest;
import com.sketchnotes.userservice.pojo.request.RegisterRequest;
import com.sketchnotes.userservice.pojo.response.LoginResponse;
import com.sketchnotes.userservice.service.interfaces.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/api/users/auth")
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/login-google")
    public ResponseEntity<LoginResponse> loginGoogle(@RequestBody LoginGoogleRequest request) {
        return ResponseEntity.ok(authService.loginGoogle(request));
    }
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }
}
