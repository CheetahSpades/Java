package com.example.delivery.dto;

import com.example.delivery.model.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(String productName, int quantity, BigDecimal price, BigDecimal lineTotal) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getProductName(), item.getQuantity(), item.getPrice(), item.lineTotal());
    }
}
