package com.example.delivery.dto;

import com.example.delivery.model.Order;
import com.example.delivery.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        String customerName,
        Long courierId,
        String courierName,
        String deliveryAddress,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal total,
        Instant createdAt,
        Instant deliveredAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getCourier() == null ? null : order.getCourier().getId(),
                order.getCourier() == null ? null : order.getCourier().getName(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.total(),
                order.getCreatedAt(),
                order.getDeliveredAt());
    }
}
