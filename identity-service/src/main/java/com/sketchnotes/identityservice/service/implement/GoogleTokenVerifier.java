package com.sketchnotes.identityservice.service.implement;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoogleTokenVerifier {
    
    @Value("${google.client-ids:}")
    private String googleClientIds;
    
    /**
     * Verify Google ID Token and return payload
     * Supports multiple client IDs (Android, iOS, Web)
     * @param idTokenString The ID token string from Google
     * @return GoogleIdToken.Payload containing user information
     * @throws AppException if token is invalid
     */
    public GoogleIdToken.Payload verifyToken(String idTokenString) {
        try {
            // Validate input
            if (idTokenString == null || idTokenString.trim().isEmpty()) {
                log.error("❌ ID Token is null or empty!");
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }
            
            log.debug("Received ID token (first 50 chars): {}...", 
                idTokenString.length() > 50 ? idTokenString.substring(0, 50) : idTokenString);
            
            // Parse comma-separated client IDs
            List<String> clientIdList = Arrays.stream(googleClientIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toList());
            
            if (clientIdList.isEmpty()) {
                log.error("❌ No Google Client IDs configured in application.yaml");
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
            
            log.info("🔍 Verifying Google ID token with {} client ID(s)", clientIdList.size());
            log.info("📋 Configured Client IDs: {}", clientIdList);
            
            // Build verifier with Google's public keys
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), 
                GsonFactory.getDefaultInstance()
            )
            // Set expected audiences (support multiple platforms)
            .setAudience(clientIdList)
            .build();
            
            // Verify token signature and claims
            log.info("🔐 Attempting to verify token signature...");
            GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken == null) {
                log.error("❌ Google ID token verification FAILED");
                log.error("Possible reasons:");
                log.error("  1. Token has expired");
                log.error("  2. Token audience (aud) doesn't match any configured client IDs");
                log.error("  3. Token signature is invalid");
                log.error("  4. Token is not from Google");
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }
            
            GoogleIdToken.Payload payload = idToken.getPayload();
            
            log.info("✅ Token signature verified!");
            log.info("📧 Email: {}", payload.getEmail());
            log.info("🎯 Token Audience (aud): {}", payload.getAudience());
            log.info("🏢 Issuer: {}", payload.getIssuer());
            
            // Additional validation
            if (!payload.getEmailVerified()) {
                log.error("❌ Email not verified by Google for: {}", payload.getEmail());
                throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
            }
            
            log.info("✅ Successfully verified Google ID token for email: {}", payload.getEmail());
            return payload;
            
        } catch (AppException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to verify Google ID token: {}", ex.getMessage(), ex);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }
}
