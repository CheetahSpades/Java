package com.example.delivery.controller;

import com.example.delivery.dto.OrderResponse;
import com.example.delivery.dto.PlaceOrderRequest;
import com.example.delivery.dto.StatusUpdateRequest;
import com.example.delivery.model.Customer;
import com.example.delivery.model.Order;
import com.example.delivery.model.OrderItem;
import com.example.delivery.model.OrderStatus;
import com.example.delivery.service.CourierService;
import com.example.delivery.service.CustomerService;
import com.example.delivery.service.OrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final CourierService courierService;

    public OrderController(OrderService orderService, CustomerService customerService,
            CourierService courierService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.courierService = courierService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody PlaceOrderRequest request) {
        Customer customer = customerService.getCustomer(request.customerId());
        String address = request.deliveryAddress() == null || request.deliveryAddress().isBlank()
                ? customer.getAddress()
                : request.deliveryAddress();
        List<OrderItem> items = request.items().stream()
                .map(item -> new OrderItem(item.productName(), item.quantity(), item.price()))
                .toList();
        Order order = orderService.placeOrder(customer, address, items);
        return ResponseEntity
                .created(URI.create("/api/orders/" + order.getId()))
                .body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return OrderResponse.from(orderService.getOrder(id));
    }

    @GetMapping
    public List<OrderResponse> list(@RequestParam(required = false) OrderStatus status) {
        List<Order> orders = status == null ? orderService.findAll() : orderService.findByStatus(status);
        return orders.stream().map(OrderResponse::from).toList();
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return OrderResponse.from(orderService.advanceStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public OrderResponse cancel(@PathVariable Long id) {
        return OrderResponse.from(orderService.cancelOrder(id));
    }

    /**
     * Assigns a courier to the order: the first AVAILABLE one, or a specific
     * courier when {@code courierId} is given.
     */
    @PostMapping("/{id}/assign")
    public OrderResponse assign(@PathVariable Long id, @RequestParam(required = false) Long courierId) {
        return OrderResponse.from(courierService.assignToOrder(id, courierId));
    }
}
