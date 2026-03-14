package com.example.orderservice.controller;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.ErrorResponse;
import com.example.orderservice.dto.OrderResponse;

import com.example.orderservice.dto.ProcessOrderResponse;
import com.example.orderservice.service.OrderServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Operations related to order management")
public class OrderController {

    private final OrderServiceImpl service;

    public OrderController(OrderServiceImpl service) {
        this.service = service;
    }

    @PostMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    @Operation(
            summary = "Create a new order",
            description = "Creates a new order and returns the created order details.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Order created successfully",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(ref = "ErrorResponse"))
                    )
            }
    )
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderResponse response = service.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(
            value = "/{id}",
            produces = "application/json"
    )
    @Operation(
            summary = "Get an order by ID",
            description = "Retrieves a single order using its unique ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order found",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found",
                            content = @Content(schema = @Schema(ref = "ErrorResponse"))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(ref = "ErrorResponse"))
                    )
            }
    )
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(service.getOrder(id));
    }

    @GetMapping(
            produces = "application/json"
    )
    @Operation(
            summary = "List all orders",
            description = "Returns a list of all orders.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of orders",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(ref = "ErrorResponse"))
                    )
            }
    )
    public ResponseEntity<List<OrderResponse>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size

    ) {
        return ResponseEntity.ok(service.listOrders(status, page, size));
    }

    @PostMapping(
            value = "/{orderNumber}/process",
            produces = "application/json"
    )
    @Operation(
            summary = "Process an order",
            description = "Processes an existing order and returns the updated order details.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order processed successfully",
                            content = @Content(schema = @Schema(implementation = OrderResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found",
                            content = @Content(schema = @Schema(ref = "ErrorResponse"))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Order cannot be processed due to business rules",
                            content = @Content(schema = @Schema(ref = "ErrorResponse"))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(schema = @Schema(ref = "ErrorResponse"))
                    )
            }
    )
    public ResponseEntity<ProcessOrderResponse> processOrder(
            @PathVariable UUID orderNumber) {
        return ResponseEntity.ok(service.processOrder(orderNumber));
    }
}