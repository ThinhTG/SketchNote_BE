package com.sketchnotes.project_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    // General errors
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    NTERNAL_SERVER_ERROR(500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    // Authentication / Authorization
    UNAUTHENTICATED(401, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(401, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_KEY(1001, "Invalid key", HttpStatus.BAD_REQUEST),

    // User errors
    USER_NOT_EXISTED(400, "User does not exist", HttpStatus.BAD_REQUEST),
    CLIENT_NOT_FOUND(400, "Client not found", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(400, "Role not found", HttpStatus.BAD_REQUEST),
    USER_INACTIVE(400, "User is inactive", HttpStatus.BAD_REQUEST),

    // OAuth2 / Token API errors

    INVALID_TOKEN(3006, "Invalid token", HttpStatus.BAD_REQUEST),
    EXPIRED_TOKEN(3007, "Expired token", HttpStatus.UNAUTHORIZED),
    NOT_FOUND(404, "Not found", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(404, "User not found", HttpStatus.BAD_REQUEST),
    PROJECT_NOT_FOUND(404, "Project not found", HttpStatus.BAD_REQUEST),
    PAGE_NOT_FOUND(404, "Project not found", HttpStatus.BAD_REQUEST),
    FORBIDDEN_ACTION(403, "You do not have permission to perform this action", HttpStatus.FORBIDDEN),
    COLLAB_NOT_FOUND(403, "Collaboration not found", HttpStatus.FORBIDDEN),
    PROJECT_QUOTA_EXCEEDED(400, "Project quota exceeded. Please upgrade your subscription", HttpStatus.BAD_REQUEST),
    IMAGE_GENERATION_FAILED(500, "Image generation failed", HttpStatus.INTERNAL_SERVER_ERROR),
     IMAGE_REMOVAL_FAILED(500, "Image removal failed", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_EXTRACTION_FAILED(500, "Image extraction failed", HttpStatus.INTERNAL_SERVER_ERROR);


    private final int code;
    private final HttpStatusCode statusCode;
    private final String message;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
