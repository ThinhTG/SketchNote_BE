package com.sketchnotes.payment_service.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

//    public AppException handleKeyCloakException(FeignException exception) {
//        String responseBody = exception.contentUTF8();
//        log.warn("Keycloak request failed. Status: {}, Body: {}", exception.status(), responseBody);
//
//        try {
//            // 1. Try parse Admin API error
//            KeyCloakError kcError = tryParseAdminError(responseBody);
//            if (kcError != null && kcError.getErrorMessage() != null) {
//                ErrorCode code = errorCodeMap.get(kcError.getErrorMessage());
//                if (code != null) {
//                    return new AppException(code);
//                }
//            }
//
//            // 2. Try parse OAuth2 / Token API error
//            String oauthError = tryParseOAuthError(responseBody);
//            if (oauthError != null) {
//                ErrorCode code = errorCodeMap.get(oauthError);
//                if (code != null) {
//                    return new AppException(code);
//                }
//            }
//
//        } catch (Exception e) {
//            log.error("Unexpected error handling Keycloak exception", e);
//        }
//
//        return new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
//    }
//
//    private KeyCloakError tryParseAdminError(String body) {
//        try {
//            return objectMapper.readValue(body, KeyCloakError.class);
//        } catch (JsonProcessingException e) {
//            return null;
//        }
//    }

    private String tryParseOAuthError(String body) {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("error")) {
                return node.get("error").asText();
            }
        } catch (JsonProcessingException ignored) {
        }
        return null;
    }
}
