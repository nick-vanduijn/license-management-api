# License Management API 

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Monitoring & Observability](#monitoring--observability)
- [Security](#security)
- [Troubleshooting](#troubleshooting)

## Overview

A **production-ready, multi-tenant licensing API** built with **Spring Boot 3.5.4**, featuring strong authentication, digital license signing, rate limiting, and observability.

Designed for SaaS and enterprise environments where security, scalability, and tenant isolation are critical.

## Features

### Core

* **Multi-tenant Architecture** → Tenant isolation at database + application layers
* **License Management** → Create, manage, and digitally sign licenses
* **JWT Authentication** → Secure API access with bearer tokens
* **Digital Signatures** → Ed25519 signing ensures license integrity
* **Audit Logging** → Full audit trail for compliance

### Production Enhancements

* **Rate Limiting** → Configurable with burst support
* **Security Headers** → HSTS, CSP, XSS & CSRF protections
* **Monitoring** → Prometheus metrics + Spring Boot health checks
* **Structured Logging** → JSON logs with correlation IDs
* **Database Migrations** → Managed by Flyway
* **Connection Pooling** → HikariCP with tuned settings
* **Graceful Shutdown** → Safe lifecycle management

## Quick Start

### Prerequisites

* Java **17+**
* Docker & Docker Compose
* PostgreSQL **16+**
* Redis **7+**

### 1. Clone & Build

```bash
git clone <repository-url>
cd license-management-api
```

### 2. Configure Environment

```bash
cp .env.production .env
# Edit .env with production values
```

### 3. Generate Signing Keys

```bash
# Generate Ed25519 key pair
openssl genpkey -algorithm Ed25519 -out private.pem
openssl pkey -in private.pem -pubout -out public.pem

# Convert keys to base64
export LICENSE_SIGNING_PRIVATE_KEY=$(base64 -w 0 private.pem)
export LICENSE_SIGNING_PUBLIC_KEY=$(base64 -w 0 public.pem)
```

### 4. Deploy

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### 5. Verify

```bash
curl http://localhost:8080/actuator/health
```

## Configuration

### Environment Variables

| Variable                      | Description                | Required | Default |
| ----------------------------- | -------------------------- | -------- | ------- |
| `DATABASE_PASSWORD`           | PostgreSQL password        | ✅        | -       |
| `REDIS_PASSWORD`              | Redis password             | ✅        | -       |
| `JWT_SECRET`                  | 256-bit JWT secret         | ✅        | -       |
| `LICENSE_SIGNING_PRIVATE_KEY` | Base64 Ed25519 private key | ✅        | -       |
| `LICENSE_SIGNING_PUBLIC_KEY`  | Base64 Ed25519 public key  | ✅        | -       |
| `CORS_ALLOWED_ORIGINS`        | Allowed origins            | ✅        | -       |
| `RATE_LIMIT_RPM`              | Requests/minute            | ❌        | 100     |
| `DATABASE_POOL_SIZE`          | Max DB connections         | ❌        | 50      |

### Profiles

* `development` → Local dev with debug logs
* `test` → In-memory H2 DB
* `production` → Optimized for deployment

## API Documentation

### Authentication

All requests require a **JWT token** and tenant header:

```bash
curl -H "Authorization: Bearer <jwt>" \
     -H "X-Tenant-ID: <tenant-id>" \
     http://localhost:8080/api/v1/licenses
```

### Endpoints

#### Organizations

* `POST /api/v1/organizations` → Create
* `GET /api/v1/organizations` → List
* `GET /api/v1/organizations/{id}` → Get
* `PUT /api/v1/organizations/{id}` → Update
* `DELETE /api/v1/organizations/{id}` → Delete

#### Licenses

* `POST /api/v1/licenses` → Create
* `GET /api/v1/licenses` → List
* `GET /api/v1/licenses/{id}` → Get
* `PUT /api/v1/licenses/{id}` → Update
* `POST /api/v1/licenses/{id}/suspend` → Suspend
* `POST /api/v1/licenses/{id}/reactivate` → Reactivate
* `POST /api/v1/licenses/{id}/revoke` → Revoke
* `GET /api/v1/licenses/{id}/token` → Signed license token

### OpenAPI

* Swagger UI → `http://localhost:8080/swagger-ui.html`
* Spec → `http://localhost:8080/v3/api-docs`

## Monitoring & Observability

### Health

```bash
curl http://localhost:8080/actuator/health
```

### Metrics

```bash
curl http://localhost:8080/actuator/prometheus
```

### Monitoring Stack

* **Prometheus** → `http://localhost:9090`
* **Grafana** → `http://localhost:3000` (credentials from env)
* **Logs** → `/var/log/license-management-api/`

## Security

### API Security

* JWT with strong, rotating secrets
* Input validation & SQL injection protection
* XSS & CSRF protection headers
* Strict CORS policy

### Headers

* HSTS (1 year, preload)
* CSP (Content Security Policy)
* X-Frame-Options: `DENY`
* X-Content-Type-Options: `nosniff`
* Referrer Policy: `strict-origin-when-cross-origin`

### Rate Limiting

* Per-tenant & per-IP enforcement
* Automatic `429 Too Many Requests` with retry headers

## Troubleshooting

**Database Connection**

```bash
docker exec -it license-postgres \
  psql -U license_user -d license_management -c "SELECT 1;"
```

**Logs**

```bash
docker logs license-api --tail 100 -f
docker logs license-api 2>&1 | grep ERROR
```bash
docker exec -it license-postgres \
  psql -U license_user -d license_management -c "SELECT 1;"
```

**Logs**

```bash
docker logs license-api --tail 100 -f
docker logs license-api 2>&1 | grep ERROR
```

**Rate Limiting**

```bash
curl -I http://localhost:8080/api/v1/licenses
```

