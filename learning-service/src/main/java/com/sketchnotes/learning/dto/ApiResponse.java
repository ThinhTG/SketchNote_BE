package com.sketchnotes.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private Object errors;

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data, null);
    }

    public static <T> ApiResponse<T> error(int status, String message, Object errors) {
        return new ApiResponse<>(status, message, null, errors);
    }
}
