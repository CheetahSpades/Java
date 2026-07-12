package com.example.delivery.model;

import java.util.Set;

/**
 * Lifecycle of an order. Status may only move forward:
 *
 *   PLACED -> CONFIRMED -> PICKED_UP -> OUT_FOR_DELIVERY -> DELIVERED
 *
 * An order may be CANCELLED, but only before it has been picked up.
 * DELIVERED and CANCELLED are terminal states.
 */
public enum OrderStatus {
    PLACED,
    CONFIRMED,
    PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    private Set<OrderStatus> allowedNextStatuses() {
        return switch (this) {
            case PLACED -> Set.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> Set.of(PICKED_UP, CANCELLED);
            case PICKED_UP -> Set.of(OUT_FOR_DELIVERY);
            case OUT_FOR_DELIVERY -> Set.of(DELIVERED);
            case DELIVERED, CANCELLED -> Set.of();
        };
    }

    public boolean canTransitionTo(OrderStatus next) {
        return allowedNextStatuses().contains(next);
    }
}
