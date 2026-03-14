package com.example.orderservice.service;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.ProcessOrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Retrieves an order by its unique ID.
     *
     * @param id the order ID
     * @return the order response
     */
    OrderResponse getOrder(UUID id);

    /**
     * Lists all orders.
     *
     * @return list of order responses
     */
    List<OrderResponse> listOrders(String status, int page, int size);

    /**
     * Processes an existing order and returns a lightweight response.
     *
     * @param orderNumber the order number (UUID)
     * @return the process order response
     */
    ProcessOrderResponse processOrder(UUID orderNumber);
}
