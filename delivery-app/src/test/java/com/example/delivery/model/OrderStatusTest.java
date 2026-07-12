package com.example.delivery.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void statusMovesForwardAlongTheHappyPath() {
        assertTrue(OrderStatus.PLACED.canTransitionTo(OrderStatus.CONFIRMED));
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PICKED_UP));
        assertTrue(OrderStatus.PICKED_UP.canTransitionTo(OrderStatus.OUT_FOR_DELIVERY));
        assertTrue(OrderStatus.OUT_FOR_DELIVERY.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void statusCannotMoveBackwards() {
        assertFalse(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PLACED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PLACED));
        assertFalse(OrderStatus.OUT_FOR_DELIVERY.canTransitionTo(OrderStatus.PICKED_UP));
    }

    @Test
    void statusCannotSkipSteps() {
        assertFalse(OrderStatus.PLACED.canTransitionTo(OrderStatus.PICKED_UP));
        assertFalse(OrderStatus.PLACED.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void cancellationIsOnlyAllowedBeforePickup() {
        assertTrue(OrderStatus.PLACED.canTransitionTo(OrderStatus.CANCELLED));
        assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.PICKED_UP.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.OUT_FOR_DELIVERY.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void terminalStatesAllowNoFurtherMoves() {
        for (OrderStatus next : OrderStatus.values()) {
            assertFalse(OrderStatus.DELIVERED.canTransitionTo(next));
            assertFalse(OrderStatus.CANCELLED.canTransitionTo(next));
        }
    }
}
