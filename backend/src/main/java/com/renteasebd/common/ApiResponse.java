package com.renteasebd.common;

public record ApiResponse<T>(boolean success, T data, ApiError error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message, String field) {
        return new ApiResponse<>(false, null, new ApiError(code, message, field));
    }
}
