package com.example.orderservice.dto;

import java.time.Instant;
import java.util.UUID;

public record ProcessOrderResponse(
        UUID orderNumber,
        String status,
        Instant processedAt
) {}
