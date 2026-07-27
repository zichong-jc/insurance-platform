
package com.example.insurance.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private Integer code;
    private String message;
    private T data;
    private String timestamp;

    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(200)
                .message("success")
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> Result<T> success(String message) {
        return Result.<T>builder()
                .code(200)
                .message(message)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> Result<T> created(T data) {
        return Result.<T>builder()
                .code(201)
                .message("created")
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }

    public static <T> Result<T> notFound(String message) {
        return error(404, message);
    }

    public static <T> Result<T> serverError(String message) {
        return error(500, message);
    }

    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }

    public static <T> Result<T> forbidden(String message) {
        return error(403, message);
    }

    public static <T> Result<T> conflict(String message) {
        return error(409, message);
    }
}