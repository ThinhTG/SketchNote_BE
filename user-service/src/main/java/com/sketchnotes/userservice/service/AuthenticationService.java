package com.sketchnotes.userservice.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.sketchnotes.userservice.enums.Role;
import com.sketchnotes.userservice.exception.AuthException;
import com.sketchnotes.userservice.model.User;
import com.sketchnotes.userservice.pojo.request.LoginGoogleRequest;
import com.sketchnotes.userservice.pojo.request.LoginRequest;
import com.sketchnotes.userservice.pojo.request.RegisterRequest;
import com.sketchnotes.userservice.pojo.response.LoginResponse;
import com.sketchnotes.userservice.repository.IUserRepository;
import com.sketchnotes.userservice.service.interfaces.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthenticationService implements  IAuthService {
    private final IUserRepository userRepository;
    private final  AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword()));
        User account = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Not found"));
        String jwtToken = jwtService.generateToken(account);
        return new LoginResponse(jwtToken);
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        // check duplicate email
        User existEmailAccount = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (existEmailAccount != null) {
            throw new AuthException("Email already in use");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreateAt(java.time.LocalDateTime.now());
        user.setActive(true);
        user = userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        return new LoginResponse(jwtToken);
    }

    @Override
    public LoginResponse loginGoogle(LoginGoogleRequest request) {
        LoginResponse response = new LoginResponse();
        try {
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(request.getIdToken());
            String email = firebaseToken.getEmail();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setRole(Role.CUSTOMER);
                user.setFullName(firebaseToken.getName());
                user.setCreateAt(java.time.LocalDateTime.now());
                user.setActive(true);
                user = userRepository.save(user);
            }
            response.setToken(jwtService.generateToken(user));
        } catch (FirebaseAuthException e) {
            throw new AuthException("Invalid ID token");
        }
        return  response;
    }
}