package com.project.www.integrations.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;

    public static <T> ApiResponseDto<T> success(String message, T data) {
        return ApiResponseDto.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> ApiResponseDto<T> success(T data) {
        return ApiResponseDto.<T>builder().success(true).data(data).build();
    }

    public static <T> ApiResponseDto<T> failure(String message, String error) {
        return ApiResponseDto.<T>builder().success(false).message(message).error(error).build();
    }

    public static <T> ApiResponseDto<T> failure(String message) {
        return ApiResponseDto.<T>builder().success(false).message(message).build();
    }
}
