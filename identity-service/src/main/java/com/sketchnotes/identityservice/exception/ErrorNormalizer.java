package com.sketchnotes.identityservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sketchnotes.identityservice.dtos.identity.KeyCloakError;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ErrorNormalizer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, ErrorCode> errorCodeMap = new HashMap<>();

    public ErrorNormalizer() {
        // ===== Admin API errors =====
        errorCodeMap.put("User exists with same username", ErrorCode.USER_EXISTED);
        errorCodeMap.put("User exists with same email", ErrorCode.EMAIL_EXISTED);
        errorCodeMap.put("User name is missing", ErrorCode.EMAIL_IS_MISSING);
        errorCodeMap.put("Invalid password", ErrorCode.INVALID_PASSWORD);
        errorCodeMap.put("Client not found", ErrorCode.CLIENT_NOT_FOUND);
        errorCodeMap.put("Role not found", ErrorCode.ROLE_NOT_FOUND);

        // ===== OAuth2 / Token API errors =====
        errorCodeMap.put("invalid_grant", ErrorCode.INVALID_GRANT);
        errorCodeMap.put("invalid_client", ErrorCode.INVALID_CLIENT);
        errorCodeMap.put("invalid_scope", ErrorCode.INVALID_SCOPE);
        errorCodeMap.put("unauthorized_client", ErrorCode.UNAUTHORIZED_CLIENT);
        errorCodeMap.put("invalid_request", ErrorCode.INVALID_REQUEST);
        errorCodeMap.put("invalid_token", ErrorCode.INVALID_TOKEN);
        errorCodeMap.put("expired_token", ErrorCode.EXPIRED_TOKEN);
    }

    public AppException handleKeyCloakException(FeignException exception) {
        String responseBody = exception.contentUTF8();
        log.warn("Keycloak request failed. Status: {}, Body: {}", exception.status(), responseBody);
        log.warn("Response body length: {}, isEmpty: {}", responseBody != null ? responseBody.length() : 0, responseBody == null || responseBody.isEmpty());

        // If response body is empty and status is 401, assume invalid credentials
        if ((responseBody == null || responseBody.isEmpty()) && exception.status() == 401) {
            log.info("Empty response body with 401 status - treating as invalid credentials");
            return new AppException(ErrorCode.INVALID_GRANT);
        }

        try {
            // 1. Try parse Admin API error
            KeyCloakError kcError = tryParseAdminError(responseBody);
            if (kcError != null && kcError.getErrorMessage() != null) {
                log.debug("Parsed Admin API error: {}", kcError.getErrorMessage());
                ErrorCode code = errorCodeMap.get(kcError.getErrorMessage());
                if (code != null) {
                    log.info("Mapped Admin API error to: {}", code);
                    return new AppException(code);
                }
            }

            // 2. Try parse OAuth2 / Token API error
            String oauthError = tryParseOAuthError(responseBody);
            if (oauthError != null) {
                log.debug("Parsed OAuth2 error: {}", oauthError);
                ErrorCode code = errorCodeMap.get(oauthError);
                if (code != null) {
                    log.info("Mapped OAuth2 error '{}' to: {}", oauthError, code);
                    return new AppException(code);
                } else {
                    log.warn("OAuth2 error '{}' not found in errorCodeMap", oauthError);
                }
            } else {
                log.warn("Could not parse OAuth2 error from response body");
            }

        } catch (Exception e) {
            log.error("Unexpected error handling Keycloak exception", e);
        }

        log.warn("Returning UNCATEGORIZED_EXCEPTION - no matching error code found");
        return new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
    }

    private KeyCloakError tryParseAdminError(String body) {
        try {
            return objectMapper.readValue(body, KeyCloakError.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String tryParseOAuthError(String body) {
        try {
            log.debug("Attempting to parse OAuth error from body: {}", body);
            JsonNode node = objectMapper.readTree(body);
            if (node.has("error")) {
                String errorValue = node.get("error").asText();
                log.debug("Successfully extracted OAuth error field: {}", errorValue);
                return errorValue;
            } else {
                log.debug("Response body does not contain 'error' field");
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse OAuth error JSON: {}", e.getMessage());
        }
        return null;
    }
}
