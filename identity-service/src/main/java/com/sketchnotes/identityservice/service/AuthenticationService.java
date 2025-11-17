package com.sketchnotes.identityservice.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sketchnotes.identityservice.client.IdentityClient;
import com.sketchnotes.identityservice.dtos.identity.*;
import com.sketchnotes.identityservice.dtos.request.*;
import com.sketchnotes.identityservice.dtos.response.LoginResponse;
import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.exception.ErrorNormalizer;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.IAuthService;
import com.sketchnotes.identityservice.service.KafkaProducerService;
import com.sketchnotes.identityservice.service.interfaces.IWalletService;
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
    private final IWalletService walletService;
    private  final KafkaProducerService kafkaProducerService;
    @Value("${idp.client-id}")
    @NonFinal
   private String clientId;

    @Value("${idp.client-secret}")
    @NonFinal
    private String clientSecret;

    @Value("${link.verify-email}")
    @NonFinal
    private String linkVerifyEmail;



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
            DecodedJWT jwt = JWT.decode(tokenResponse.getAccessToken());
            boolean emailVerified = jwt.getClaim("email_verified").asBoolean();
            if (!emailVerified) {
                throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
            }
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
               var newUser =  userRepository.save(
                        User.builder()
                                .keycloakId(keycloakId)
                                .email(email)
                                .role(Role.CUSTOMER)
                                .isActive(true)
                                .firstName(firstName)
                                .lastName(lastName)
                                .build()
                );
                walletService.createWallet(newUser.getId());

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
            walletService.createWallet(user.getId());

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

    @Override
    public void sendVerifyEmail(VerifyEmailRequest request) {
        try {
            // Get admin token
            TokenExchangeResponse token = identityClient.exchangeClientToken(
                    TokenExchangeParam.builder()
                            .grant_type("client_credentials")
                            .client_id(clientId)
                            .client_secret(clientSecret)
                            .scope("openid")
                            .build()
            );

            // Find user by email in Keycloak
            List<UserInfo> users = identityClient.getUserByEmail(
                    "Bearer " + token.getAccessToken(),
                    request.getEmail()
            );

            if (users == null || users.isEmpty()) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }

            UserInfo userInfo = users.get(0);

            // Send verification email
            // redirect_uri: URL user will be redirected to after email verification
            // Set to null to use default redirect URI from Keycloak client settings
            identityClient.sendVerifyEmail(
                    "Bearer " + token.getAccessToken(),
                    userInfo.getId(),
                    clientId,
                    linkVerifyEmail
            );

            log.info("Verification email sent to: {}", request.getEmail());

        } catch (FeignException ex) {
            log.error("FeignException during send verify email: {}", ex.getMessage());
            throw errorNormalizer.handleKeyCloakException(ex);
        } catch (Exception ex) {
            log.error("Unexpected exception during send verify email", ex);
            throw ex;
        }
    }

    @Override
    public void sendResetPasswordEmail(ForgotPasswordRequest request) {
        try {
            // Get admin token
            TokenExchangeResponse token = identityClient.exchangeClientToken(
                    TokenExchangeParam.builder()
                            .grant_type("client_credentials")
                            .client_id(clientId)
                            .client_secret(clientSecret)
                            .scope("openid")
                            .build()
            );

            // Find user by email in Keycloak
            List<UserInfo> users = identityClient.getUserByEmail(
                    "Bearer " + token.getAccessToken(),
                    request.getEmail()
            );

            if (users == null || users.isEmpty()) {
                throw new AppException(ErrorCode.NOT_FOUND);
            }

            UserInfo userInfo = users.get(0);

            // Send reset password email with UPDATE_PASSWORD action
            // redirect_uri: URL user will be redirected to after resetting password
            // Set to null to use default redirect URI from Keycloak client settings
            identityClient.executeActionsEmail(
                    "Bearer " + token.getAccessToken(),
                    userInfo.getId(),
                    clientId,
                    null,  // or "http://your-frontend-url.com/reset-success"
                    List.of("UPDATE_PASSWORD")
            );

            log.info("Reset password email sent to: {}", request.getEmail());

        } catch (FeignException ex) {
            log.error("FeignException during send reset password email: {}", ex.getMessage());
            throw errorNormalizer.handleKeyCloakException(ex);
        } catch (Exception ex) {
            log.error("Unexpected exception during send reset password email", ex);
            throw ex;
        }
    }

    @Override
    public void resetPassword(String userId, ResetPasswordRequest request) {
        try {
            // Validate passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new AppException(ErrorCode.PASSWORD_MISMATCH);
            }

            // Get admin token
            TokenExchangeResponse token = identityClient.exchangeClientToken(
                    TokenExchangeParam.builder()
                            .grant_type("client_credentials")
                            .client_id(clientId)
                            .client_secret(clientSecret)
                            .scope("openid")
                            .build()
            );

            // Reset password in Keycloak
            identityClient.resetPassword(
                    "Bearer " + token.getAccessToken(),
                    userId,
                    UpdatePasswordParam.builder()
                            .type("password")
                            .value(request.getNewPassword())
                            .temporary(false)
                            .build()
            );

            log.info("Password reset successfully for userId: {}", userId);

        } catch (FeignException ex) {
            log.error("FeignException during reset password: {}", ex.getMessage());
            throw errorNormalizer.handleKeyCloakException(ex);
        } catch (Exception ex) {
            log.error("Unexpected exception during reset password", ex);
            throw ex;
        }
    }

}