package com.example.delivery.dto;

import com.example.delivery.model.Courier;
import com.example.delivery.model.CourierStatus;

public record CourierResponse(Long id, String name, String phone, CourierStatus status) {

    public static CourierResponse from(Courier courier) {
        return new CourierResponse(courier.getId(), courier.getName(), courier.getPhone(), courier.getStatus());
    }
}
