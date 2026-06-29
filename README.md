# E-Commerce Order Fulfillment Microservices

A microservices-based e-commerce platform built with Java 17 and Spring Boot 2.7.8, migrated from a monolithic architecture to independent deployable services. Improved scalability during seasonal shopping peaks and simplified service-level deployments.

**Duration:** June 2022 – August 2023

## Architecture

Six independent microservices communicating via Kafka (choreography-based Saga pattern):

| Service | Port | Database | Responsibility |
|---|---|---|---|
| api-gateway | 8080 | — | Spring Cloud Gateway — routes to all services |
| order-service | 8081 | order_db (MySQL) | Order lifecycle management |
| inventory-service | 8082 | inventory_db (MySQL) | Stock management and reservation |
| payment-service | 8083 | payment_db (MySQL) | Payment processing |
| shipping-service | 8084 | shipping_db (MySQL) | Shipment creation and tracking |
| notification-service | 8085 | notification_db (MySQL) | Email/SMS notifications |

## Event Flow (Kafka Choreography)

```
order-created        → inventory-service checks stock
inventory-reserved   → payment-service processes payment
inventory-insufficient → order-service marks FAILED
payment-completed    → order-service CONFIRMED + shipping-service creates shipment
payment-failed       → order-service FAILED + inventory-service releases stock
order-confirmed      → inventory-service marks reservation CONSUMED
shipment-created     → order-service PROCESSING
shipment-dispatched  → order-service SHIPPED
shipment-delivered   → order-service DELIVERED
```

All events are also consumed by **notification-service** for customer email alerts.

## Technologies

- **Java 17**, Spring Boot 2.7.8
- **Apache Kafka** — 9 event types for async choreography
- **Spring Cloud Gateway** 2021.0.5 — API gateway with route predicates
- **MySQL 8.0** — database per service pattern
- **Docker** & **Kubernetes** — containerized deployment
- **AWS** (ECS/EC2) — cloud hosting
- **Jenkins** — parallel CI/CD pipeline

## Setup

```bash
docker-compose up --build
```

Access:
- Frontend: http://localhost:80
- API Gateway: http://localhost:8080
- Order Service: http://localhost:8081
- Inventory Service: http://localhost:8082
- Payment Service: http://localhost:8083
- Shipping Service: http://localhost:8084
- Notification Service: http://localhost:8085

## Kubernetes Deployment

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/kafka.yaml
kubectl apply -f k8s/order-service.yaml
kubectl apply -f k8s/inventory-service.yaml
kubectl apply -f k8s/payment-service.yaml
kubectl apply -f k8s/shipping-service.yaml
kubectl apply -f k8s/notification-service.yaml
kubectl apply -f k8s/api-gateway.yaml
kubectl apply -f k8s/ingress.yaml
```

## API Endpoints

| Service | Endpoint | Method | Description |
|---|---|---|---|
| order | /api/orders | POST | Place new order |
| order | /api/orders/{id} | GET | Get order by ID |
| order | /api/orders?customerId= | GET | Get customer orders |
| order | /api/orders/{id}/cancel | PUT | Cancel order |
| inventory | /api/inventory/products | GET | List products |
| inventory | /api/inventory/products/{sku} | GET | Get product |
| inventory | /api/inventory/products/low-stock | GET | Low stock alert |
| inventory | /api/inventory/products/{sku}/stock | PUT | Adjust stock |
| payment | /api/payments | GET | All payments |
| payment | /api/payments/order/{orderId} | GET | Payment by order |
| shipping | /api/shipments | GET | All shipments |
| shipping | /api/shipments/order/{orderId} | GET | Shipment by order |
| shipping | /api/shipments/track/{tracking} | GET | Track package |
| shipping | /api/shipments/{id}/dispatch | PUT | Mark dispatched |
| shipping | /api/shipments/{id}/deliver | PUT | Mark delivered |
| notification | /api/notifications | GET | All notifications |
| notification | /api/notifications/order/{orderId} | GET | Order notifications |

## Pre-seeded Inventory

10 products across Electronics, Clothing, Footwear, Books, and Accessories — automatically loaded by inventory-service on startup.
