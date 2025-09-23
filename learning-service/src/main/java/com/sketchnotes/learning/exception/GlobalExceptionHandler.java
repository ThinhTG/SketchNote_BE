package com.sketchnotes.learning.exception;

import com.sketchnotes.learning.dto.ApiResponse;
import jakarta.ws.rs.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle lỗi chung chung
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception ex) {
        return ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage()
        );
    }

    // Handle lỗi không tìm thấy (ví dụ bạn có custom NotFoundException)
    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<?> handleNotFound(NotFoundException ex) {
        return ApiResponse.error(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null
        );
    }

    // Handle lỗi validate @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage()
                ));
        return ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors
        );
    }
}

