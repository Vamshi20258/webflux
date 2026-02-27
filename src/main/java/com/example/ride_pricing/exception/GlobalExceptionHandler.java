package com.example.ride_pricing.exception;

import com.example.ride_pricing.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleRuntime(RuntimeException ex) {
        // We wrap the ApiResponse in a ResponseEntity to set the actual HTTP Header status to 400
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.failed(400, ex.getMessage()))
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleException(Exception ex) {
        // We wrap the ApiResponse in a ResponseEntity to set the actual HTTP Header status to 500
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(500, "Internal Server Error: " + ex.getMessage()))
        );
    }
}