# Small Delivery App — Project Plan

A plan for building a small delivery app in Java. The goal is a working MVP
(minimum viable product) you can build incrementally, learning as you go.

## 1. What the app does

A simple system where:

- **Customers** place an order (items + delivery address).
- **The system** tracks the order through its lifecycle:
  `PLACED → CONFIRMED → PICKED_UP → OUT_FOR_DELIVERY → DELIVERED` (or `CANCELLED`).
- **Couriers** are assigned to orders and update the status as they go.

That's it for the MVP. No payments, no real-time GPS, no push notifications —
those are v2 ideas (see section 8).

## 2. Recommended tech stack

| Layer      | Choice                          | Why |
|------------|---------------------------------|-----|
| Language   | Java 21 (LTS)                   | Current long-term-support release |
| Framework  | Spring Boot 3                   | Industry standard, huge community, easy REST APIs |
| Build tool | Maven                           | Simple, well documented |
| Database   | H2 (dev) → PostgreSQL (later)   | H2 is in-memory, zero setup; swap later |
| ORM        | Spring Data JPA                 | Removes boilerplate SQL for CRUD |
| Testing    | JUnit 5 + Spring Boot Test      | Comes with Spring Boot starter |
| API docs   | springdoc-openapi (Swagger UI)  | Free interactive API documentation |

Start the project at [start.spring.io](https://start.spring.io) with
dependencies: **Spring Web, Spring Data JPA, H2 Database, Validation**.

Simpler alternative: if Spring Boot feels like too much at first, build phase 1
as a plain console app (just Java classes + collections in memory), then port
it to Spring Boot in phase 2. The domain model below works for both.

## 3. Domain model (the core classes)

```
Customer            Order                 Courier
--------            -----                 -------
id                  id                    id
name                customer (ManyToOne)  name
email               courier  (ManyToOne)  phone
phone               items    (OneToMany)  status: AVAILABLE | BUSY
address             deliveryAddress
                    status (enum)
                    createdAt
                    deliveredAt

OrderItem
---------
id
order (ManyToOne)
productName
quantity
price
```

`OrderStatus` enum:
`PLACED, CONFIRMED, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED, CANCELLED`

Key business rules to enforce in the service layer:
- Status can only move forward (no `DELIVERED → PLACED`).
- An order can only be cancelled before `PICKED_UP`.
- A courier can only be assigned if their status is `AVAILABLE`.
- Assigning a courier sets them to `BUSY`; delivery/cancellation frees them.

## 4. REST API design (MVP endpoints)

```
Customers
  POST   /api/customers            create a customer
  GET    /api/customers/{id}       get a customer

Orders
  POST   /api/orders               place an order
  GET    /api/orders/{id}          get order details + status
  GET    /api/orders?status=...    list orders, filterable by status
  PATCH  /api/orders/{id}/status   advance the order status
  DELETE /api/orders/{id}          cancel (only while allowed)

Couriers
  POST   /api/couriers             register a courier
  GET    /api/couriers?status=AVAILABLE
  POST   /api/orders/{id}/assign   assign an available courier
```

Package structure:

```
com.example.delivery
├── controller/    REST endpoints
├── service/       business rules (status transitions, assignment)
├── repository/    Spring Data JPA interfaces
├── model/         entities + enums
└── dto/           request/response objects
```

## 5. Build phases

### Phase 1 — Core domain (1–2 weeks)
- Set up the project and Git branch workflow.
- Create the entities, enums, and repositories.
- Implement `OrderService` with the status-transition rules.
- Write unit tests for the transition rules (this is the heart of the app).

**Done when:** tests prove valid transitions pass and invalid ones throw.

### Phase 2 — REST API (1–2 weeks)
- Add controllers and DTOs for the endpoints in section 4.
- Add validation (`@NotNull`, `@Email`, etc.) and proper error responses
  (400 for bad input, 404 for missing resources, 409 for illegal transitions).
- Add Swagger UI and test every endpoint from the browser.

**Done when:** you can walk an order from PLACED to DELIVERED entirely via HTTP.

### Phase 3 — Courier assignment (1 week)
- Courier registration and availability.
- Assignment endpoint with the AVAILABLE/BUSY rules.
- Integration tests for the full flow: create customer → place order →
  assign courier → deliver.

**Done when:** the full happy path and the "no courier available" path both work.

### Phase 4 — Polish (1 week)
- Swap H2 for PostgreSQL (only config changes if JPA was used properly).
- Seed data on startup so the app is demo-ready.
- README with setup instructions and example `curl` commands.

## 6. Suggested Git workflow

Matches the Git skills this repo practices:
- One branch per phase or feature: `feature/domain-model`, `feature/rest-api`, ...
- Small, frequent commits with descriptive messages.
- Merge to `main` when a phase's "done when" criteria pass.

## 7. Things deliberately left out of the MVP

Skipping these keeps the project achievable:
- Authentication/login (add Spring Security later)
- Payments
- Real-time tracking / maps
- Notifications (email, SMS, push)
- A frontend UI — Swagger UI is your interface for the MVP

## 8. Ideas for v2

Once the MVP works: add Spring Security with roles (CUSTOMER, COURIER, ADMIN),
estimated delivery times, order history per customer, a simple web frontend
(Thymeleaf or a small React app), and Docker Compose for app + PostgreSQL.
