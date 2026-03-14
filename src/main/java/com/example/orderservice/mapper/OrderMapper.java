package com.example.orderservice.mapper;

import com.example.orderservice.domain.Order;
import com.example.orderservice.dto.OrderItemResponse;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.ProcessOrderResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() {}

    public static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProductName(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        BigDecimal total = itemResponses.stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponse(
                order.getId(),
                order.getCustomerEmail(),
                order.getCustomerName(),
                order.getStatus(),
                order.getCreatedAt(),
                itemResponses,
                total
        );

    }

    public static ProcessOrderResponse toProcessOrderResponse(Order order) {
        return new ProcessOrderResponse(
                order.getId(),
                order.getStatus().name(),
                Instant.now()
        );
    }

}
