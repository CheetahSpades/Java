# Delivery App

Small delivery app built with Java 21 and Spring Boot 3. See
[`../DELIVERY_APP_PLAN.md`](../DELIVERY_APP_PLAN.md) for the full project plan.

## Status

- [x] **Phase 1 — Core domain:** entities, status lifecycle, `OrderService`, unit tests
- [x] **Phase 2 — REST API:** controllers, DTOs, validation, error handling, Swagger UI
- [x] **Phase 3 — Courier assignment:** registration, AVAILABLE/BUSY rules, integration tests
- [x] **Phase 4 — Polish:** demo seed data, PostgreSQL profile, this README

## Quick start

```bash
mvn test                # run all tests (unit + integration)
mvn spring-boot:run     # start on http://localhost:8080 with demo data
```

Useful pages once running:

- **Swagger UI** (interactive API docs): http://localhost:8080/swagger-ui.html
- **H2 console** (browse the dev database): http://localhost:8080/h2-console
  (JDBC URL `jdbc:h2:mem:deliverydb`, user `sa`, empty password)

Demo data is seeded on startup (2 customers, 2 couriers, 2 orders).
Disable it with `--app.seed-data=false`.

## API walkthrough with curl

Walk an order through its whole life:

```bash
BASE=http://localhost:8080

# 1. create a customer
curl -s -X POST $BASE/api/customers -H 'Content-Type: application/json' -d '{
  "name": "Ada Lovelace", "email": "ada@example.com",
  "phone": "555-0100", "address": "1 Analytical Way"
}'

# 2. place an order (customer id 3 if you kept the seed data)
curl -s -X POST $BASE/api/orders -H 'Content-Type: application/json' -d '{
  "customerId": 3,
  "items": [{"productName": "Pizza Margherita", "quantity": 2, "price": 9.50}]
}'

# 3. confirm it (replace 3 with the order id from step 2)
curl -s -X PATCH $BASE/api/orders/3/status -H 'Content-Type: application/json' \
  -d '{"status": "CONFIRMED"}'

# 4. assign the first available courier
curl -s -X POST $BASE/api/orders/3/assign

# 5. drive it to delivery
curl -s -X PATCH $BASE/api/orders/3/status -H 'Content-Type: application/json' -d '{"status": "PICKED_UP"}'
curl -s -X PATCH $BASE/api/orders/3/status -H 'Content-Type: application/json' -d '{"status": "OUT_FOR_DELIVERY"}'
curl -s -X PATCH $BASE/api/orders/3/status -H 'Content-Type: application/json' -d '{"status": "DELIVERED"}'

# other useful calls
curl -s $BASE/api/orders                          # list all orders
curl -s "$BASE/api/orders?status=PLACED"          # filter by status
curl -s "$BASE/api/couriers?status=AVAILABLE"     # who can take an order
curl -s -X DELETE $BASE/api/orders/2              # cancel (only before pickup)
```

Errors come back as JSON: `400` for invalid input (with per-field messages),
`404` for unknown resources, `409` when a business rule is violated
(illegal status transition, courier not available).

## Running against PostgreSQL

The app uses in-memory H2 by default. To use PostgreSQL instead:

```bash
docker compose up -d                                    # start postgres
mvn spring-boot:run -Dspring-boot.run.profiles=postgres # run with the postgres profile
```

Data now survives restarts. Stop the database with `docker compose down`.

## Layout

```
src/main/java/com/example/delivery
├── controller/   REST endpoints (customers, orders, couriers)
├── dto/          request/response records with validation
├── service/      business rules live here
├── repository/   Spring Data JPA interfaces
├── model/        Customer, Courier, Order, OrderItem + status enums
├── exception/    domain exceptions + GlobalExceptionHandler
└── config/       DataSeeder (demo data)
```

The order lifecycle is enforced in `OrderStatus.canTransitionTo` and
`Order.changeStatus`:

```
PLACED -> CONFIRMED -> PICKED_UP -> OUT_FOR_DELIVERY -> DELIVERED
   \          \
    `----------`--> CANCELLED   (only before pickup)
```

Rules: status only moves forward; cancellation only before pickup; a courier
must be AVAILABLE to be assigned and becomes BUSY until the order is
delivered or cancelled.
