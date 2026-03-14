package com.example.orderservice.dto;

import com.example.orderservice.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderNumber,
        String customerName,
        String customerEmail,
        OrderStatus status,
        Instant createdAt,
        List<OrderItemResponse> items,
        BigDecimal totalAmount
) {
}

