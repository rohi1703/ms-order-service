package com.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp,
        List<String> details,
        String correlationId
) {
    public ErrorResponse(int status, String error, String message, String correlationId) {
        this(status, error, message, Instant.now(), null, null);
    }

    public ErrorResponse(int status, String error, String message, List<String> details,String correlationId) {
        this(status, error, message, Instant.now(), details, null);
    }
}

