package com.example.delivery.service;

import com.example.delivery.exception.CourierUnavailableException;
import com.example.delivery.exception.InvalidStatusTransitionException;
import com.example.delivery.exception.NotFoundException;
import com.example.delivery.model.Courier;
import com.example.delivery.model.CourierStatus;
import com.example.delivery.model.Order;
import com.example.delivery.model.OrderStatus;
import com.example.delivery.repository.CourierRepository;
import com.example.delivery.repository.OrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourierService {

    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;

    public CourierService(CourierRepository courierRepository, OrderRepository orderRepository) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
    }

    public Courier register(String name, String phone) {
        return courierRepository.save(new Courier(name, phone));
    }

    @Transactional(readOnly = true)
    public List<Courier> findAll() {
        return courierRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Courier> findByStatus(CourierStatus status) {
        return courierRepository.findByStatus(status);
    }

    /**
     * Assigns the first AVAILABLE courier to an order, or a specific courier
     * when {@code courierId} is given. The order must be active (not delivered
     * or cancelled) and must not already have a courier.
     */
    public Order assignToOrder(Long orderId, Long courierId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order " + orderId + " not found"));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "Order " + orderId + " is " + order.getStatus() + " and cannot be assigned a courier");
        }
        if (order.getCourier() != null) {
            throw new CourierUnavailableException(
                    "Order " + orderId + " already has courier " + order.getCourier().getId() + " assigned");
        }

        Courier courier = courierId == null ? firstAvailableCourier() : specificCourier(courierId);
        courier.setStatus(CourierStatus.BUSY);
        order.setCourier(courier);
        return orderRepository.save(order);
    }

    private Courier firstAvailableCourier() {
        return courierRepository.findByStatus(CourierStatus.AVAILABLE).stream()
                .findFirst()
                .orElseThrow(() -> new CourierUnavailableException("No available courier"));
    }

    private Courier specificCourier(Long courierId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new NotFoundException("Courier " + courierId + " not found"));
        if (!courier.isAvailable()) {
            throw new CourierUnavailableException("Courier " + courierId + " is not available");
        }
        return courier;
    }
}
