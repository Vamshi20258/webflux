package com.example.ride_pricing.exception;

import com.example.ride_pricing.common.ApiResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Mono<ApiResponse<Object>> handleRuntime(RuntimeException ex) {
        return Mono.just(ApiResponse.failed(400, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ApiResponse<Object>> handleException(Exception ex) {
        return Mono.just(ApiResponse.error(500, "Internal Server Error"));
    }
}