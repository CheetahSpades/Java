package com.example.delivery.service;

import com.example.delivery.exception.InvalidStatusTransitionException;
import com.example.delivery.exception.NotFoundException;
import com.example.delivery.model.Courier;
import com.example.delivery.model.CourierStatus;
import com.example.delivery.model.Customer;
import com.example.delivery.model.Order;
import com.example.delivery.model.OrderItem;
import com.example.delivery.model.OrderStatus;
import com.example.delivery.repository.OrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order placeOrder(Customer customer, String deliveryAddress, List<OrderItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one item");
        }
        Order order = new Order(customer, deliveryAddress);
        items.forEach(order::addItem);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order " + orderId + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Advances an order to the given status. The Order entity enforces which
     * moves are legal; this method additionally releases the courier when the
     * order reaches a terminal state.
     */
    public Order advanceStatus(Long orderId, OrderStatus next) {
        Order order = getOrder(orderId);
        order.changeStatus(next);
        if (next == OrderStatus.DELIVERED || next == OrderStatus.CANCELLED) {
            releaseCourier(order);
        }
        return orderRepository.save(order);
    }

    public Order cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (!order.getStatus().canTransitionTo(OrderStatus.CANCELLED)) {
            throw new InvalidStatusTransitionException(
                    "Order " + orderId + " can no longer be cancelled (status: " + order.getStatus() + ")");
        }
        return advanceStatus(orderId, OrderStatus.CANCELLED);
    }

    private void releaseCourier(Order order) {
        Courier courier = order.getCourier();
        if (courier != null) {
            courier.setStatus(CourierStatus.AVAILABLE);
        }
    }
}
