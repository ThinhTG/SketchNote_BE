package com.sketchnotes.payment_service.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private HttpStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String error;
    private String path;

    public static <T> APIResponse<T> success(T data) {
        return APIResponse.<T>builder()
                .success(true)
                .data(data)
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> APIResponse<T> success(T data, String message) {
        return APIResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> APIResponse<T> error(String message, HttpStatus status) {
        return APIResponse.<T>builder()
                .success(false)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> APIResponse<T> error(String message, String error, HttpStatus status, String path) {
        return APIResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .status(status)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}