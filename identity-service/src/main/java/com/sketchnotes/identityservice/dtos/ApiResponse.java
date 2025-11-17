package com.sketchnotes.identityservice.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default
    private int code = 200;
    private String message;
    private T result;

    public static <T> ApiResponse<T> success(T result, String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message, T result) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> success2(T data) {
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> success2(String message, T data) {
        return new ApiResponse<>(0, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(-1, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
