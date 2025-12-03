package com.sketchnotes.identityservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode{

    // General errors
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    // Authentication / Authorization
    UNAUTHENTICATED(401, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(401, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_KEY(1001, "Invalid key", HttpStatus.BAD_REQUEST),

    // User errors
    INVALID_EMAIL(400, "Email must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(400, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH(400, "Passwords do not match", HttpStatus.BAD_REQUEST),
    EMAIL_IS_MISSING(400, "Please enter e mail", HttpStatus.BAD_REQUEST),
    USER_EXISTED(400, "Username already exists, please choose another one", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(400, "Email already exists, please choose another one", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(400, "User does not exist", HttpStatus.BAD_REQUEST),
    CLIENT_NOT_FOUND(400, "Client not found", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(400, "Role not found", HttpStatus.BAD_REQUEST),
    USER_INACTIVE(400, "User is inactive", HttpStatus.BAD_REQUEST),

    // OAuth2 / Token API errors
    INVALID_GRANT(400, "Invalid email or password", HttpStatus.BAD_REQUEST),
    INVALID_CLIENT(500, "Invalid client", HttpStatus.BAD_REQUEST),
    INVALID_SCOPE(500, "Invalid scope", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_CLIENT(3004, "Unauthorized client", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(3005, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(400, "Invalid token", HttpStatus.BAD_REQUEST),
    EXPIRED_TOKEN(400, "Expired token", HttpStatus.UNAUTHORIZED),
    NOT_FOUND(404, "Not found", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(404, "User not found", HttpStatus.BAD_REQUEST),
    BLOG_NOT_FOUND(404, "Blog not found", HttpStatus.BAD_REQUEST),
    CONTENT_NOT_FOUND(404, "Content not found", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_VERIFIED(400, "Email not verified", HttpStatus.BAD_REQUEST),
    
    // Subscription errors
    SUBSCRIPTION_PLAN_NOT_FOUND(404, "Subscription plan not found", HttpStatus.NOT_FOUND),
    SUBSCRIPTION_NOT_FOUND(404, "Subscription not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE(400, "Insufficient wallet balance", HttpStatus.BAD_REQUEST),
    PROJECT_QUOTA_EXCEEDED(400, "Project quota exceeded. Please upgrade your subscription", HttpStatus.BAD_REQUEST),
    
    // Credit errors
    INSUFFICIENT_CREDITS(400, "Insufficient AI credits", HttpStatus.BAD_REQUEST),
    INVALID_CREDIT_AMOUNT(400, "Invalid credit amount", HttpStatus.BAD_REQUEST),
    CREDIT_TRANSACTION_FAILED(500, "Credit transaction failed", HttpStatus.INTERNAL_SERVER_ERROR),
    MINIMUM_PURCHASE_NOT_MET(400, "Minimum purchase is 100 credits", HttpStatus.BAD_REQUEST),
    CREDIT_PACKAGE_NOT_FOUND(404, "Credit package not found", HttpStatus.NOT_FOUND),
    CREDIT_PACKAGE_NOT_ACTIVE(400, "Credit package is not active", HttpStatus.BAD_REQUEST),
    INVALID_PRICE(400, "Discounted price cannot be greater than original price", HttpStatus.BAD_REQUEST),
    WALLET_NOT_FOUND(404, "Wallet not found. Please create a wallet first", HttpStatus.NOT_FOUND),
    
    // Notification errors
    NOTIFICATION_NOT_FOUND(404, "Notification not found", HttpStatus.NOT_FOUND);

    private final int code;
    private final HttpStatusCode statusCode;
    private final String message;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
