package com.example.delivery.dto;

import com.example.delivery.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull OrderStatus status) {
}
