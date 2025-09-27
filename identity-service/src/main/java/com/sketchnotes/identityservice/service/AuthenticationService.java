package com.sketchnotes.identityservice.service;


import com.sketchnotes.identityservice.client.IdentityClient;
import com.sketchnotes.identityservice.dto.identity.Credential;
import com.sketchnotes.identityservice.dto.identity.TokenExchangeParam;
import com.sketchnotes.identityservice.dto.identity.TokenExchangeResponse;
import com.sketchnotes.identityservice.dto.identity.UserCreationParam;
import com.sketchnotes.identityservice.dto.request.LoginRequest;
import com.sketchnotes.identityservice.dto.request.RegisterRequest;
import com.sketchnotes.identityservice.dto.response.LoginResponse;
import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.exception.ErrorNormalizer;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.IAuthService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;


import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthenticationService implements  IAuthService {
    private final IdentityClient identityClient;
    private final IUserRepository userRepository;
    private final ErrorNormalizer errorNormalizer;

    @Value("${idp.client-id}")
    @NonFinal
    String clientId;

    @Value("${idp.client-secret}")
    @NonFinal
    String clientSecret;


    @Override
    public LoginResponse login(LoginRequest request) {
        return  null;

    }

    @Override
    public void register(RegisterRequest request) {
        try {
            TokenExchangeResponse token = identityClient.exchangeClientToken(TokenExchangeParam.builder()
                    .grantType("client_credentials")
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .scope("openid")
                    .build());
            var creationResponse = identityClient.createUser(
                    "Bearer " + token.getAccessToken(),
                    UserCreationParam.builder()
                            .username(request.getEmail())
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .email(request.getEmail())
                            .enabled(true)
                            .emailVerified(false)
                            .credentials(List.of(Credential.builder()
                                    .type("password")
                                    .temporary(false)
                                    .value(request.getPassword())
                                    .build()))
                            .build());

            String userId = extractUserId(creationResponse);
            System.out.println("UserId " + userId);

            User user = User.builder()
                    .keycloakId(userId)
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .createAt(LocalDateTime.now())
                    .role(Role.CUSTOMER)
                    .avatarUrl(request.getAvatarUrl())
                    .build();
            userRepository.save(user);
        } catch (FeignException exception) {
            throw errorNormalizer.handleKeyCloakException(exception);
        }
    }
    private String extractUserId(ResponseEntity<?> response) {
        List<String> locations = response.getHeaders().get("Location");
        if (locations == null || locations.isEmpty()) {
            throw new IllegalStateException("Location header is missing");
        }
        String location = locations.get(0).trim();
        if (location.endsWith("/")) {
            location = location.substring(0, location.length() - 1);
        }
        String[] splitedStr = location.split("/");
        return splitedStr[splitedStr.length - 1];
    }


}