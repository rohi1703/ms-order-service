# Order Service



A RESTful backend service for managing customer orders with full lifecycle state management.

Built with Spring Boot 3.3, Java 21, H2, and Gradle.



---



## Table of Contents



- [Quick Start](#quick-start)

- [API Endpoints](#api-endpoints)

- [Architecture](#architecture)

- [Domain Model](#domain-model)

- [Order Lifecycle (State Machine)](#order-lifecycle-state-machine)

- [Validation](#validation)

- [Error Handling](#error-handling)

- [Assumptions](#assumptions)

- [Trade-offs](#trade-offs)

- [What I Would Improve for Production](#what-i-would-improve-for-production)

- [Running Tests](#running-tests)



---



## Quick Start



### Prerequisites



- Java 21 (JDK) — [Download from Adoptium](https://adoptium.net/)

- Gradle 8+ — only needed once to generate the wrapper JAR



### One-Time Setup (Generate Gradle Wrapper)



The wrapper scripts are included, but the `gradle-wrapper.jar` binary must be generated once:



```bash

# Install Gradle (if not already installed), then run:

cd ms-order-service

gradle wrapper

```



This creates `gradle/wrapper/gradle-wrapper.jar`. After this, you only need Java — Gradle is managed by the wrapper.



### Build & Run



```bash

# Build the project (downloads dependencies on first run)

./gradlew build

 

# Run the application

./gradlew bootRun

```



On Windows, use `gradlew.bat` instead of `./gradlew`.



The service starts on http://localhost:8081.



### Useful URLs

| http://localhost:8081/ms-order-service/swagger-ui | Interactive API docs (Swagger UI) |

| http://localhost:8081/ms-order-service/api-docs | OpenAPI 3.0 spec (JSON) |

| http://localhost:8081/ms-order-service/h2-console | H2 database console (JDBC URL: `jdbc:h2:mem:orderdb`) |



---



## API Endpoints

| Method | Endpoint | Description | Status Codes |

| `POST` | `/api/orders` | Create a new order | `201`, `400`, `500`|

| `GET` | `/api/orders/{id}` | Retrieve an order by ID | `200`, `404`, `500` |

| `GET` | `/api/orders?status=PENDING&page=0&size=10` | List orders (paginated, optional status filter) | `200` |

| `POST` | `/api/orders/{id}/process` | Transition: PENDING → PROCESSING -> COMPLETED and FAILED| `200`, `409`, `404`, `500` |



### Example: Create an Order



```bash

curl --location 'http://localhost:8081/ms-order-service/api/orders' \
--header 'Content-Type: application/json' \
--header 'X-Correlation-ID: 013aa04a-9ca1-4eab-b2fb-fc20f4cc95f3' \
--data-raw '{
  "customerName": "cust-123",
  "customerEmail": "rohit@gmail.com",
  "items": [
    {
      "productName": "SKU-001",
      "quantity": 1,
      "unitPrice": 10.50
    },
    {
      "productName": "SKU-002",
      "quantity": 2,
      "unitPrice": 25.00
    }
  ]
}'
```



### Example: Process an Order



```bash

curl --location --request POST 'http://localhost:8081/ms-order-service/api/orders/da8f9f19-8dfc-4552-a105-eb8e611b8650/process' \
--header 'X-Correlation-ID: 9c741eb2-d45a-4860-8b7e-fe4bce2f321f'

 

---

### Example: fetch an Order
curl --location 'http://localhost:8081/ms-order-service/api/orders/bf481db-a4d6-442c-8fc5-12f1efde0bf0' \
--header 'X-Correlation-ID: 0d7d668a-8e90-4098-8dec-61efd627f211'


### Example: list Orders

curl --location 'http://localhost:8081/ms-order-service/api/orders?status=PENDING&page=0&size=10' \
--header 'X-Correlation-ID: 58d34679-e4ca-45a2-ab3d-8c91d8d9f62c' 



## Architecture

 

The service follows a layered architecture with clear separation of concerns:

 

```

  Controller - REST API, validation, HTTP concerns

  Service  - Business logic, transaction boundaries

  Domain - Entities, state machine, business rules
 
  Repository - Data access (Spring Data JPA)

  H2 (mem) - In-memory relational database

```

## Domain Model
```

Order (1) ── (*) OrderItem
 
id: UUID (generated)
customerName: String
customerEmail: String
status: OrderStatus (PENDING | PROCESSING | COMPLETED | FAILED)
totalAmount: BigDecimal (derived from items)
createdAt: LocalDateTime
updatedAt: LocalDateTime



OrderItem
id: Long (generated)
productName: String
quantity: int
unitPrice: BigDecimal
subtotal: BigDecimal (unitPrice × quantity)

```

 

## Order Lifecycle (State Machine)

```

PENDING ───► PROCESSING ───► COMPLETED

    │               │

    └───► FAILED ◄──┘

```

- PENDING — Initial state when an order is created.

- PROCESSING — Order is being fulfilled.

- COMPLETED — Order successfully fulfilled (terminal).

- FAILED — Order failed or was cancelled (terminal).-- under consideration

 

Invalid transitions return 409 Conflict with a descriptive error message.

---

 

## Validation

| Field | Rule |

| `customerName` | Required, non-blank |

| `customerEmail` | Required, non-blank |

| `items` | At least one item required |

| `items[].productName` | Required, non-blank |

| `items[].quantity` | Minimum 1 |

| `items[].unitPrice` | Required, minimum 0.01 |


Validation errors return 400 Bad Request with field-level details:


json

{
    "status": 400,
    "error": "Validation Failed",
    "message": "Request body contains invalid fields",
    "timestamp": "2026-03-14T11:43:05.147406700Z",
    "details": [
        "items[0].quantity: must be greater than 0"
    ],
    "correlationId": 07ccde6d-1767-47d2-bf67-1df77b3063e8
}

```



---



## Error Handling

All errors follow a consistent `ErrorResponse` structure:

| HTTP Status | Scenario |

| `400` | Validation failure, malformed request |

| `404` | Order not found |

| `409` | Invalid state transition |

| `500` | Unexpected server error |


---

## Assumptions

A few things I kept in mind (or intentionally left out) while building this:


- No external integrations. No inventory checks, payment gateway, or notifications. The state transitions are triggered manually through the API — there's nothing async happening behind the scenes.

- Orders can't be modified after creation. Once you POST an order, the items and customer details are locked in. Only the status changes. I didn't build an "edit order" flow but it would be a must for live application.

- H2 in-memory database. Data doesn't survive a restart. I picked H2 for zero-setup portability — you can clone and run without installing Postgres.

- Hibernate manages the schema via `ddl-auto: create-drop`. No Flyway or Liquibase. This obviously wouldn't fly in production, but it keeps things simple for a demo.

- No auth. There's no security layer — any caller can hit any endpoint. In a real setup I'd add Spring Security with JWT or OAuth2.

- Customer info lives on the order itself. I just store `customerName` and `customerEmail` as fields on `Order` rather than referencing a separate `Customer` entity. Good enough here, but in production you'd probably want a proper customer service or at least a foreign key.

- Timestamps use `LocalDateTime`, which is timezone-unaware. Fine for a single-region app, but would need to be `Instant` or `OffsetDateTime` (UTC) in a multi-region setup.

- Single currency assumed. There's no `currency` field — all monetary values are implicitly in the same currency.

- No cancellation flow. The only terminal states are `COMPLETED` and `FAILED` — there's no `CANCELLED` status. A real system would almost certainly need one.

- Order creation/processing isn't idempotent. If a client retries a failed POST, they'll get a duplicate order. No idempotency key support.

- No limit on the number of items per order. Someone could technically send a request with 10,000 line items. Should probably cap that.

- Product names are just free text. No SKU validation or product catalog lookup — any non-blank string works. May require a seed entry

---



## Trade-offs

These are the design decisions where I consciously picked one approach over another.


H2 vs. a real database — H2 means zero setup and fast tests, but it hides differences in SQL dialects. For example, UUID handling and index behavior are different in other options such as Postgres. I'd swap this out before going to production.

Synchronous processing — Everything happens in the request-response cycle. Simpler to build and test, but means there's no decoupling — if this service is down, callers are stuck. An event-driven approach (Kafka, etc.) would be more resilient.

`ddl-auto` vs. migration tools — No Flyway/Liquibase means the schema is recreated on every restart. Simple for development, but in production you need versioned migrations or you lose data.

Hand-written mapper vs. MapStruct — I wrote `OrderMapper` by hand to avoid extra dependencies. It's easy to follow, but it becomes tedious to maintain as the number of DTOs grows. MapStruct would generate this at compile time.

Records for DTOs — Java records are great — immutable, concise, validation works on them. But you can't use them for JPA entities (need mutable fields, no-arg constructor), so there's a bit of a style mismatch between the DTO and domain layers.

UUIDs for order IDs — Globally unique and safe for distributed systems, but they're larger than `BIGINT` and cause more index fragmentation. I used sequential `IDENTITY` for `OrderItem` though — felt like a reasonable split since item IDs are only used internally.

Offset pagination — Spring Data makes this trivial, but `OFFSET`-based queries slow down at high page numbers. Cursor-based pagination would be better at scale.

Denormalized `totalAmount` — I store the total on the order rather than computing it from items on every read. Since orders are immutable after creation, the recalculation risk is basically zero here.

Catch-all exception handler — The `GlobalExceptionHandler` catches `Exception.class` and returns a generic 500 JSON response. Consistent for clients, but it swallows stack traces. In production, I'd definitely add `log.error(...)` there so errors aren't invisible.
---



## What I Would Improve for Production

If I were taking this to production, here's what I'd tackle, roughly in order of priority.

### Things that need to happen first

- Swap H2 for PostgreSQL (or MySQL or Nosql). The in-memory DB loses everything on restart and behaves differently from real databases in subtle ways.

- Add Flyway or Liquibase for database migrations. `ddl-auto: create-drop` is a non-starter in production — it literally drops all your tables.

- Async order processing — use a message broker for real fulfillment logic.

- Add authentication and authorization. Right now anyone can do anything. I'd add Spring Security with JWT at minimum, and role-based access for state transitions (e.g., only admins can mark orders as failed).

- Fix the catch-all exception handler. It currently swallows the stack trace completely. A single `log.error("Unexpected error", ex)` would save hours of debugging.

- Add Spring Boot Actuator for health checks, readiness/liveness probes. Essential if you're running in Kubernetes or behind a load balancer.

- Add indexes. The `findByStatus` query will do a full table scan without an index on the `status` column.

- fetching passwords, secrets from secret store or vault — The H2 console should definitely not be enabled in production.

- Dockerize it. There's no `Dockerfile` or `docker-compose.yml` yet.

- add more unit and integration tests

- Order modification — let users edit items on `PENDING` orders before processing starts.

- Rate limiting — something like Bucket4j or an API gateway in front.

- Metrics with Micrometer + Prometheus — order creation rates, transition latency, error rates.

- performance testing to validate behavior under pressure, especially around optimistic lock contention.

- CircuitBreaker around downstream calls




### Things I'd want shortly after

- Idempotency keys on order creation — accept a client-generated key and enforce uniqueness so retries don't create duplicates.

- Retry logic for optimistic/pessimistic lock failures and DB restart/unavailable — something like `@Retryable` from Spring Retry so clients don't always have to handle 409s themselves.

- A `CANCELLED` status. Real orders get cancelled. The state machine should support `PENDING → CANCELLED` at minimum.

- Cap the number of items per order — add `@Size(max = 100)` or similar on the items list. Right now it's unbounded.

- Distributed tracing (OpenTelemetry or Micrometer Tracing) for debugging across services.

- CORS config — any browser-based frontend would be blocked without it.

- API versioning (`/api/v1/orders`) so I can evolve the API without breaking existing clients.

- Graceful shutdown (`server.shutdown=graceful`) so in-flight requests finish before the app stops.


### Things I'd get to eventually

- Audit trail — an `order_events` table (or even event sourcing) to track every state transition with who/when/why.

- Multi-currency support — add a `currencyCode` (ISO 4217) field alongside monetary amounts.

- Cursor-based pagination for large datasets — offset pagination gets slow at high page numbers.

---



## Running Tests



```bash

# Run all tests

./gradlew test

 

# Run with verbose output

./gradlew test --info

```



### Test Coverage



| Test Class | Type | What It Covers |

|------------|------|----------------|

-- this is yet to be done


---



## Project Structure



```

src/main/java/com/nab/orderservice/

├── OrderServiceApplication.java        # Spring Boot entry point

├── config/

│   └── OpenApiConfig.java              # Swagger/OpenAPI metadata

|   └── CorrelationIdFilter.java        # to make coorelationid available throughout application layers

├── controller/

│   └── OrderController.java            # REST endpoints

├── domain/

│   ├── Order.java                      # Order aggregate root

│   ├── OrderItem.java                  # Line item entity

│   └── OrderStatus.java               # Lifecycle enum

├── dto/

│   ├── CreateOrderRequest.java         # Inbound DTO (validated)

│   ├── ProcessOrderResponse.java       # Outbound processed order DTO

│   ├── OrderResponse.java             # Outbound DTO

│   ├── OrderItemResponse.java         # Outbound item DTO

│   └── ErrorResponse.java            # Standardized error envelope

├── exception/

│   ├── GlobalExceptionHandler.java    # Centralized error handling

│   └── OrderNotFoundException.java

├── mapper/

│   └── OrderMapper.java              # Entity ↔ DTO conversion

├── repository/

│   └── OrderRepository.java          # Spring Data JPA interface

└── service/

    └── OrderServiceImpl.java         # Business logic + transactions