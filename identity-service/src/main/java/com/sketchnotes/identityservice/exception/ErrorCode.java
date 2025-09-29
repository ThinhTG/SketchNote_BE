package com.sketchnotes.identityservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // General errors
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    // Authentication / Authorization
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_KEY(1001, "Invalid key", HttpStatus.BAD_REQUEST),

    // User errors
    INVALID_EMAIL(1003, "Email must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    EMAIL_IS_MISSING(1010, "Please enter e mail", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1009, "Username already exists, please choose another one", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1008, "Email already exists, please choose another one", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1011, "User does not exist", HttpStatus.BAD_REQUEST),
    CLIENT_NOT_FOUND(2001, "Client not found", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(2002, "Role not found", HttpStatus.BAD_REQUEST),
    USER_INACTIVE(1012, "User is inactive", HttpStatus.BAD_REQUEST),

    // OAuth2 / Token API errors
    INVALID_GRANT(3001, "Invalid grant", HttpStatus.BAD_REQUEST),
    INVALID_CLIENT(3002, "Invalid client", HttpStatus.BAD_REQUEST),
    INVALID_SCOPE(3003, "Invalid scope", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_CLIENT(3004, "Unauthorized client", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(3005, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(3006, "Invalid token", HttpStatus.BAD_REQUEST),
    EXPIRED_TOKEN(3007, "Expired token", HttpStatus.UNAUTHORIZED),
    NOT_FOUND(404, "Not found", HttpStatus.BAD_REQUEST);
    private final int code;
    private final HttpStatusCode statusCode;
    private final String message;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
