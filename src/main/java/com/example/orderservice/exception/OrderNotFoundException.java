package com.example.orderservice.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID id) {
        super("Order with id " + id + " not found");
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}

