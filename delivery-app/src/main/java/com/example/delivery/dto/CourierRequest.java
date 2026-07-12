package com.example.delivery.dto;

import jakarta.validation.constraints.NotBlank;

public record CourierRequest(@NotBlank String name, String phone) {
}
