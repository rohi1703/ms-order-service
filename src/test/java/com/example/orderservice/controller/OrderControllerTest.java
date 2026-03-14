package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.service.OrderServiceImpl;
import com.example.orderservice.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.example.orderservice.domain.OrderStatus.COMPLETED;
import static com.example.orderservice.domain.OrderStatus.PENDING;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @InjectMocks
    private OrderController controller;

    @Mock
    private OrderServiceImpl service;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // SUCCESS: Valid create order request
    @Test
    void createOrder_validRequest_returnsOk() throws Exception {
        OrderResponse response = new OrderResponse(
                UUID.randomUUID(),
                "Rohit",
                "Rohit.manchanda17@gmail.com",
                PENDING,
                Instant.now(),
                List.of(),
                BigDecimal.TEN
        );

        when(service.createOrder(any())).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Rohit",
                                  "customerEmail": "rohit@gmail.com",
                                  "items": [
                                    { "productName": "SKU-1", "quantity": 1, "unitPrice": 10.0 }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated());
    }


    // VALIDATION: Missing customerId
    @Test
    void createOrder_missingCustomerId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "",
                                  "items": [
                                    { "sku": "SKU-1", "quantity": 1, "unitPrice": 10.0 }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("customerName: must not be blank"));
    }


    // VALIDATION: Empty items list
    @Test
    void createOrder_emptyItems_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Rohit",
                                  "customerEmail": "rohit@gmail.com",
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details[0]").value("items: must not be empty"));
    }


    // VALIDATION: Invalid nested item fields
    //@Test
    void createOrder_invalidItemFields_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Rohit",
                                  "customerEmail": "rohit@gmail.com",
                                  "items": [
                                    { "sku": "", "quantity": 0, "unitPrice": -5 }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details.length()").value(3));
    }

    // 404: Order not found
    //@Test
    void getOrder_notFound_returns404() throws Exception {
        UUID orderNumber = UUID.randomUUID();
        when(service.getOrder(orderNumber)).thenThrow(new OrderNotFoundException(orderNumber));

        mockMvc.perform(get("/orders/"+orderNumber))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Order with id 99 not found"));
    }


    // SUCCESS: Get order
    //@Test
    void getOrder_success_returnsOrder() throws Exception {
        UUID orderNumber = UUID.randomUUID();
        OrderResponse response = new OrderResponse(
                UUID.randomUUID(),
                "Rohit",
                "Rohit@gmail.com",
                COMPLETED,
                Instant.now(),
                List.of(),
                BigDecimal.valueOf(50)
        );

        when(service.getOrder(orderNumber)).thenReturn(response);

        mockMvc.perform(get("/orders/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.customerId").value("cust-999"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}