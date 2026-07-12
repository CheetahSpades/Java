package com.example.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.example.delivery.exception.InvalidStatusTransitionException;
import com.example.delivery.exception.NotFoundException;
import com.example.delivery.model.Courier;
import com.example.delivery.model.CourierStatus;
import com.example.delivery.model.Customer;
import com.example.delivery.model.Order;
import com.example.delivery.model.OrderItem;
import com.example.delivery.model.OrderStatus;
import com.example.delivery.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;
    private Customer customer;
    private Order order;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository);
        customer = new Customer("Ada Lovelace", "ada@example.com", "555-0100", "1 Analytical Way");
        order = new Order(customer, "1 Analytical Way");
        order.addItem(new OrderItem("Pizza Margherita", 2, new BigDecimal("9.50")));
    }

    private void repositoryReturnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        // lenient: tests where the transition is rejected never reach save()
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void placingAnOrderStartsItAsPlaced() {
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order placed = orderService.placeOrder(customer, "1 Analytical Way",
                List.of(new OrderItem("Pizza Margherita", 2, new BigDecimal("9.50"))));

        assertEquals(OrderStatus.PLACED, placed.getStatus());
        assertEquals(new BigDecimal("19.00"), placed.total());
    }

    @Test
    void placingAnEmptyOrderIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder(customer, "1 Analytical Way", List.of()));
    }

    @Test
    void orderCanWalkTheFullHappyPath() {
        repositoryReturnsOrder();

        orderService.advanceStatus(1L, OrderStatus.CONFIRMED);
        orderService.advanceStatus(1L, OrderStatus.PICKED_UP);
        orderService.advanceStatus(1L, OrderStatus.OUT_FOR_DELIVERY);
        Order delivered = orderService.advanceStatus(1L, OrderStatus.DELIVERED);

        assertEquals(OrderStatus.DELIVERED, delivered.getStatus());
        assertNotNull(delivered.getDeliveredAt());
    }

    @Test
    void skippingAStepIsRejected() {
        repositoryReturnsOrder();

        assertThrows(InvalidStatusTransitionException.class,
                () -> orderService.advanceStatus(1L, OrderStatus.OUT_FOR_DELIVERY));
        assertEquals(OrderStatus.PLACED, order.getStatus());
    }

    @Test
    void movingBackwardsIsRejected() {
        repositoryReturnsOrder();
        orderService.advanceStatus(1L, OrderStatus.CONFIRMED);

        assertThrows(InvalidStatusTransitionException.class,
                () -> orderService.advanceStatus(1L, OrderStatus.PLACED));
    }

    @Test
    void orderCanBeCancelledBeforePickup() {
        repositoryReturnsOrder();
        orderService.advanceStatus(1L, OrderStatus.CONFIRMED);

        Order cancelled = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertNull(cancelled.getDeliveredAt());
    }

    @Test
    void orderCannotBeCancelledAfterPickup() {
        repositoryReturnsOrder();
        orderService.advanceStatus(1L, OrderStatus.CONFIRMED);
        orderService.advanceStatus(1L, OrderStatus.PICKED_UP);

        assertThrows(InvalidStatusTransitionException.class, () -> orderService.cancelOrder(1L));
        assertEquals(OrderStatus.PICKED_UP, order.getStatus());
    }

    @Test
    void deliveryFreesTheAssignedCourier() {
        repositoryReturnsOrder();
        Courier courier = new Courier("Grace Hopper", "555-0200");
        courier.setStatus(CourierStatus.BUSY);
        order.setCourier(courier);

        orderService.advanceStatus(1L, OrderStatus.CONFIRMED);
        orderService.advanceStatus(1L, OrderStatus.PICKED_UP);
        orderService.advanceStatus(1L, OrderStatus.OUT_FOR_DELIVERY);
        orderService.advanceStatus(1L, OrderStatus.DELIVERED);

        assertEquals(CourierStatus.AVAILABLE, courier.getStatus());
    }

    @Test
    void cancellationFreesTheAssignedCourier() {
        repositoryReturnsOrder();
        Courier courier = new Courier("Grace Hopper", "555-0200");
        courier.setStatus(CourierStatus.BUSY);
        order.setCourier(courier);

        orderService.cancelOrder(1L);

        assertEquals(CourierStatus.AVAILABLE, courier.getStatus());
    }

    @Test
    void unknownOrderIdRaisesNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.advanceStatus(99L, OrderStatus.CONFIRMED));
    }
}
