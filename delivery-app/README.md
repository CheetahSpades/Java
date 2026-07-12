# Delivery App

Small delivery app built with Java 21 and Spring Boot 3. See
[`../DELIVERY_APP_PLAN.md`](../DELIVERY_APP_PLAN.md) for the full project plan.

## Status

- [x] **Phase 1 — Core domain:** entities, status lifecycle, `OrderService`, unit tests
- [ ] **Phase 2 — REST API:** controllers, DTOs, validation, Swagger UI
- [ ] **Phase 3 — Courier assignment**
- [ ] **Phase 4 — Polish:** PostgreSQL, seed data

## Running

```bash
# run the tests (the heart of phase 1 — the status-transition rules)
mvn test

# start the app (no endpoints yet; H2 console at http://localhost:8080/h2-console)
mvn spring-boot:run
```

## Layout

```
src/main/java/com/example/delivery
├── model/        Customer, Courier, Order, OrderItem + status enums
├── repository/   Spring Data JPA interfaces
├── service/      OrderService — business rules live here
└── exception/    InvalidStatusTransitionException, NotFoundException
```

The order lifecycle is enforced in `OrderStatus.canTransitionTo` and
`Order.changeStatus`:

```
PLACED -> CONFIRMED -> PICKED_UP -> OUT_FOR_DELIVERY -> DELIVERED
   \          \
    `----------`--> CANCELLED   (only before pickup)
```
