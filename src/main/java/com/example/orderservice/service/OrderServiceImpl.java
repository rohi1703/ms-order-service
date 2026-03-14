package com.example.orderservice.service;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.OrderItem;
import com.example.orderservice.domain.OrderStatus;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.ProcessOrderResponse;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("creating order for customer={}", request.customerName());
        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setStatus(OrderStatus.PENDING);

        for (CreateOrderRequest.Item itemReq : request.items()) {
            OrderItem item = new OrderItem();
            item.setProductName(itemReq.productName());
            item.setQuantity(itemReq.quantity());
            item.setUnitPrice(itemReq.unitPrice());
            order.getItems().add(item);
        }

        Order savedOrder = orderRepository.save(order);
        return OrderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
        return OrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders;

        if (status != null && !status.isBlank()) {
            orders = orderRepository.findByStatus(OrderStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.stream()
                .map(OrderMapper::toResponse)
                .toList();

    }

    @Transactional
    public ProcessOrderResponse processOrder(UUID orderNumber) {
        log.info("processing order number={}", orderNumber);
        Order order = orderRepository.findById(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
        return OrderMapper.toProcessOrderResponse(process(order));
    }

    private Order process(Order order) {
        // a failed order shouldnt be processed
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be processed");
        }
        //ideally below Order status transition should be based on the business rules.
        order.setStatus(OrderStatus.PROCESSING);

        //ideally below Order status transition should be based on the business rules.
        //order.setStatus(OrderStatus.COMPLETED);
        return orderRepository.save(order);
    }
}
