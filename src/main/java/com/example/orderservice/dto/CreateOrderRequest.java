package com.example.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;


public record CreateOrderRequest(
        @NotBlank String customerName,
        @NotBlank String customerEmail,
        @Valid @NotEmpty List<Item> items
) {
    public record Item(
            @NotBlank String productName,
            @Positive int quantity,
            @Positive BigDecimal unitPrice
    ) {}
}

