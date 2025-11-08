package com.sketchnotes.identityservice.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sketchnotes.identityservice.client.IdentityClient;
import com.sketchnotes.identityservice.dtos.identity.*;
import com.sketchnotes.identityservice.dtos.request.LoginGoogleRequest;
import com.sketchnotes.identityservice.dtos.request.LoginRequest;
import com.sketchnotes.identityservice.dtos.request.RegisterRequest;
import com.sketchnotes.identityservice.dtos.response.LoginResponse;
import com.sketchnotes.identityservice.dtos.request.TokenRequest;
import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.exception.ErrorNormalizer;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.IAuthService;
import com.sketchnotes.identityservice.service.KafkaProducerService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService implements  IAuthService {
    private final IdentityClient identityClient;
    private final IUserRepository userRepository;
    private final ErrorNormalizer errorNormalizer;
    private  final KafkaProducerService kafkaProducerService;
    @Value("${idp.client-id}")
    @NonFinal
    String clientId;

    @Value("${idp.client-secret}")
    @NonFinal
    String clientSecret;


    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            // Gọi Keycloak để lấy token theo grant_type=password
            LoginExchangeResponse tokenResponse = identityClient.login(
                    LoginParam.builder()
                            .grant_type("password")
                            .client_id(clientId)
                            .client_secret(clientSecret)
                            .username(request.getEmail())
                            .password(request.getPassword())
                            .scope("openid")
                            .build()
            );

            // Tìm user trong DB (nếu có)
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            if (!user.isActive()) {
                throw new AppException(ErrorCode.USER_INACTIVE);
            }
            // Trả response
            return LoginResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .build();

        } catch (FeignException ex) {
            log.error("FeignException caught during login: Status={}, Message={}", ex.status(), ex.getMessage());
            throw errorNormalizer.handleKeyCloakException(ex);
        } catch (Exception ex) {
            log.error("Unexpected exception during login", ex);
            throw ex;
        }
    }
    @Override
    public LoginResponse refreshToken(TokenRequest request) {
        try {

            LoginExchangeResponse tokenResponse = identityClient.refreshToken(
                    RefreshTokenParam.builder()
                            .grant_type("refresh_token")
                            .client_id(clientId)
                            .client_secret(clientSecret)
                            .refresh_token(request.getRefreshToken())
                            .build());

            return LoginResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .build();

        } catch (FeignException ex) {
            throw errorNormalizer.handleKeyCloakException(ex);
        }
    }

    @Override
    public LoginResponse loginWithGoogle(LoginGoogleRequest request) {
        try {
            // 1. Đổi code -> token
            LoginExchangeResponse tokenResponse = identityClient.loginWithGoogle(
                    GoogleLoginParam.builder()
                            .grant_type("authorization_code")
                            .client_id(clientId)
                            .client_secret(clientSecret)
                            .code(request.getCode())
                            .redirect_uri(request.getRedirectUri())
                            .build()
            );

            // 2. Giải ID Token
            String idToken = tokenResponse.getIdToken();

            DecodedJWT jwt = JWT.decode(idToken);

            String keycloakId = jwt.getSubject(); // "sub" claim
            System.out.println("KeycloakId: " + keycloakId);
            String email = jwt.getClaim("email").asString();
            String firstName = jwt.getClaim("given_name").asString();
            String lastName = jwt.getClaim("family_name").asString();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                 userRepository.save(
                        User.builder()
                                .keycloakId(keycloakId)
                                .email(email)
                                .role(Role.CUSTOMER)
                                .isActive(true)
                                .firstName(firstName)
                                .lastName(lastName)
                                .build()
                );
            }
            return LoginResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .build();

        } catch (FeignException ex) {
            throw errorNormalizer.handleKeyCloakException(ex);
        }
    }

    @Override
    public void register(RegisterRequest request) {
        try {
            TokenExchangeResponse token = identityClient.exchangeClientToken(TokenExchangeParam.builder()
                    .grant_type("client_credentials")
                    .client_id(clientId)
                    .client_secret(clientSecret)
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
                    .isActive(true)
                    .avatarUrl(request.getAvatarUrl())
                    .build();
                user = userRepository.save(user);

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