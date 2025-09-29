package com.sketchnotes.identityservice.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.sketchnotes.identityservice.dto.identity.KeyCloakError;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ErrorNormalizer {
    private final ObjectMapper objectMapper;
    private final Map<String, ErrorCode> errorCodeMap;

    public ErrorNormalizer() {
        objectMapper = new ObjectMapper();
        errorCodeMap = new HashMap<>();

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
        //errorCodeMap.put("invalid_token", ErrorCode.INVALID_TOKEN);
        errorCodeMap.put("expired_token", ErrorCode.EXPIRED_TOKEN);
    }

    public AppException handleKeyCloakException(FeignException exception) {
        try {
            log.warn("Cannot complete request", exception);

            // Try deserialize as Admin API error
            try {
                var response = objectMapper.readValue(exception.contentUTF8(), KeyCloakError.class);
                if (Objects.nonNull(response.getErrorMessage())
                        && Objects.nonNull(errorCodeMap.get(response.getErrorMessage()))) {
                    return new AppException(errorCodeMap.get(response.getErrorMessage()));
                }
            } catch (JsonProcessingException ignored) {
            }

            // Try deserialize as OAuth2 token error
            try {
                var response = objectMapper.readTree(exception.contentUTF8());
                if (response.has("error") && errorCodeMap.containsKey(response.get("error").asText())) {
                    return new AppException(errorCodeMap.get(response.get("error").asText()));
                }
            } catch (JsonProcessingException ignored) {
            }

        } catch (Exception e) {
            log.error("Unexpected error handling Keycloak exception", e);
        }

        return new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
    }
}
