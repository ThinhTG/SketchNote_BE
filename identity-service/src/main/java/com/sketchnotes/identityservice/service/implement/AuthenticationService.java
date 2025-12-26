package com.sketchnotes.identityservice.service.implement;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sketchnotes.identityservice.client.IdentityClient;
import com.sketchnotes.identityservice.dtos.identity.*;
import com.sketchnotes.identityservice.dtos.request.*;
import com.sketchnotes.identityservice.dtos.response.EmailDetail;
import com.sketchnotes.identityservice.dtos.response.LoginResponse;
import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.exception.ErrorNormalizer;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.VerifyToken;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.repository.IVerifyTokenRepository;
import com.sketchnotes.identityservice.service.interfaces.IAuthService;
import com.sketchnotes.identityservice.service.interfaces.IWalletService;
import com.sketchnotes.identityservice.service.implement.GoogleTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.sketchnotes.identityservice.utils.PasswordEncryptionUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService implements  IAuthService {
    private final IdentityClient identityClient;
    private final IUserRepository userRepository;
    private final ErrorNormalizer errorNormalizer;
    private final IWalletService walletService;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final PasswordEncryptionUtil passwordEncryptionUtil;
    @Value("${idp.client-id}")
    @NonFinal
   private String clientId;

    @Value("${idp.client-secret}")
    @NonFinal
    private String clientSecret;

    @Value("${link.verify-email}")
    @NonFinal
    private String linkVerifyEmail;

    @Value("${link.verify-link}")
    @NonFinal
    private String linkVerifyToken;

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
            // Tìm user trong DB (nếu có)
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

            if (!user.isVerified()) {
                throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
            }

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
    public LoginResponse loginWithGoogleMobile(LoginGoogleMobileRequest request) {
        try {
            
            GoogleIdToken.Payload payload = googleTokenVerifier.verifyToken(request.getIdToken());

            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String pictureUrl = (String) payload.get("picture");

            if (email == null || email.trim().isEmpty()) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            // Sanitize names to remove special characters that Keycloak doesn't accept
            String sanitizedFirstName = sanitizeName(firstName);
            String sanitizedLastName = sanitizeName(lastName);

            User user = userRepository.findByEmail(email).orElse(null);
            String randomPassword = UUID.randomUUID().toString();

            // If user already exists (registered via email/password)
            if (user != null) {
                // Validate user status
                if (!user.isActive()) {
                    throw new AppException(ErrorCode.USER_INACTIVE);
                }
                
                // Decrypt stored password to use for Keycloak login
                String decryptedPassword = passwordEncryptionUtil.decrypt(user.getPassword());
                
                // Use the decrypted password to login
                LoginExchangeResponse tokenResponse = identityClient.login(
                    LoginParam.builder()
                        .grant_type("password")
                        .client_id(clientId)
                        .client_secret(clientSecret)
                        .username(email)
                        .password(decryptedPassword) // Use decrypted password
                        .scope("openid")
                        .build()
                );

                return LoginResponse.builder()
                        .accessToken(tokenResponse.getAccessToken())
                        .refreshToken(tokenResponse.getRefreshToken())
                        .build();
            }

                TokenExchangeResponse adminToken = identityClient.exchangeClientToken(
                        TokenExchangeParam.builder()
                                .grant_type("client_credentials")
                                .client_id(clientId)
                                .client_secret(clientSecret)
                                .scope("openid")
                                .build()
                );

                var creationResponse = identityClient.createUser(
                        "Bearer " + adminToken.getAccessToken(),
                        UserCreationParam.builder()
                                .username(email)
                                .firstName(sanitizedFirstName)
                                .lastName(sanitizedLastName)
                                .email(email)
                                .enabled(true)
                                .emailVerified(true) // Google already verified email
                                .credentials(List.of(Credential.builder()
                                        .type("password")
                                        .temporary(false)
                                        .value(randomPassword)
                                        .build()))
                                .build()
                );

                String keycloakId = extractUserId(creationResponse);
                
                // Encrypt random password before storing (for future Google logins)
                String encryptedPassword = passwordEncryptionUtil.encrypt(randomPassword);
                
                // Create user in local DB
                User newUser = User.builder()
                        .keycloakId(keycloakId)
                        .email(email)
                        .firstName(sanitizedFirstName)
                        .lastName(sanitizedLastName)
                        .password(encryptedPassword) // Store encrypted password for future Google logins
                        .role(Role.CUSTOMER)
                        .isActive(true)
                        .verified(true)
                        .createAt(LocalDateTime.now())
                        .avatarUrl(pictureUrl)
                        .build();

                user = userRepository.save(newUser);
                walletService.createWallet(user.getId());


            LoginExchangeResponse tokenResponse = identityClient.login(
                LoginParam.builder()
                    .grant_type("password")
                    .client_id(clientId)
                    .client_secret(clientSecret)
                    .username(email)
                    .password(randomPassword)
                    .scope("openid")
                    .build()
            );

            return LoginResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .build();

        } catch (FeignException ex) {
            throw errorNormalizer.handleKeyCloakException(ex);
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    public void register(RegisterRequest request) {
        try {
            User existingUser = userRepository.findByEmail(request.getEmail())
                    .orElse(null);
            if (existingUser != null) {
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }
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

            // Encrypt password before storing (so we can decrypt it later for Google login)
            String encryptedPassword = passwordEncryptionUtil.encrypt(request.getPassword());
            
            User user = User.builder()
                    .keycloakId(userId)
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .password(encryptedPassword) // Store encrypted password
                    .createAt(LocalDateTime.now())
                    .role(Role.CUSTOMER)
                    .isActive(true)
                    .verified(true)
                    .avatarUrl(request.getAvatarUrl())
                    .build();
                user = userRepository.save(user);
            walletService.createWallet(user.getId());

            String emailVerifyToken = tokenService.generateNewVerifyToken(user);

            // 4. Create verify link
            String verifyLink =
                    linkVerifyToken + emailVerifyToken;
            // 5. Send mail
            EmailDetail emailDetail = new EmailDetail();
            emailDetail.setRecipient(request.getEmail());
            emailDetail.setSubject("Verify your email");

            Map<String, Object> variables = new HashMap<>();
            variables.put("verifyLink", verifyLink);
            variables.put("name", request.getFirstName() + " " + request.getLastName());

            emailService.sendMailTemplate(emailDetail, variables, "verify-email");
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
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        String emailVerifyToken = tokenService.generateNewVerifyToken(user);

        // 4. Create verify link
        String verifyLink =
                linkVerifyToken + emailVerifyToken;
        // 5. Send mail
        EmailDetail emailDetail = new EmailDetail();
        emailDetail.setRecipient(request.getEmail());
        emailDetail.setSubject("Verify your email");

        Map<String, Object> variables = new HashMap<>();
        variables.put("verifyLink", verifyLink);
        variables.put("name", user.getFirstName() + " " + user.getLastName());

        emailService.sendMailTemplate(emailDetail, variables, "verify-email");
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
                    null,
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

    /**
     * Sanitize name to remove special characters that Keycloak doesn't accept.
     * Keeps only letters, spaces, hyphens, and apostrophes.
     */
    private String sanitizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "User";
        }
        // Remove special characters, keep only letters, spaces, hyphens, and apostrophes
        String sanitized = name.replaceAll("[^a-zA-Z\\s'-]", "").trim();
        return sanitized.isEmpty() ? "User" : sanitized;
    }

}