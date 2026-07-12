package com.example.delivery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PlaceOrderRequest(
        @NotNull Long customerId,
        String deliveryAddress,
        @NotEmpty @Valid List<OrderItemRequest> items) {
}
