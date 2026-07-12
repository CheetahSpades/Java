package com.example.delivery.controller;

import com.example.delivery.dto.CourierRequest;
import com.example.delivery.dto.CourierResponse;
import com.example.delivery.model.Courier;
import com.example.delivery.model.CourierStatus;
import com.example.delivery.service.CourierService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/couriers")
public class CourierController {

    private final CourierService courierService;

    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    @PostMapping
    public ResponseEntity<CourierResponse> register(@Valid @RequestBody CourierRequest request) {
        Courier courier = courierService.register(request.name(), request.phone());
        return ResponseEntity
                .created(URI.create("/api/couriers/" + courier.getId()))
                .body(CourierResponse.from(courier));
    }

    @GetMapping
    public List<CourierResponse> list(@RequestParam(required = false) CourierStatus status) {
        List<Courier> couriers = status == null ? courierService.findAll() : courierService.findByStatus(status);
        return couriers.stream().map(CourierResponse::from).toList();
    }
}
