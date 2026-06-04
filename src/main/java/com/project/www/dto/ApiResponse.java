package com.project.www.dto;

public class ApiResponse<T> {
    private String message;
    private T data;

    public ApiResponse(String m, T d) {
        this.message = m;
        this.data = d;
    }

    public static <T> ApiResponse<T> ok(String m, T d) {
        return new ApiResponse<>(m, d);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("Success", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null);
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
