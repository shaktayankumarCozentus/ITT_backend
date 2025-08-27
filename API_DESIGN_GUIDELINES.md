# 📘 API Design Guidelines for Spring Boot Projects

These are the comprehensive best practices your team should follow when designing RESTful APIs for consistency, scalability, and maintainability.

---

## 📌 1. General Structure & URL Design

* **Base URL Pattern:**
  ```
  /api/v{version}/{resource}/{resourceId}/{sub-resource}/{subResourceId}
  ```

* **Resource Naming Rules:**
  - Use **plural nouns** for resources: `/users`, `/shipments`, `/orders`
  - Use **kebab-case** for multi-word endpoints: `/customer-subscriptions`, `/shipping-addresses`
  - Avoid verbs in URLs: ❌ `/createUser` ✅ `POST /users`
  - Use **lowercase** letters consistently

* **Examples:**
  ```
  ✅ GET  /api/v1/users/123/orders/456/items
  ✅ POST /api/v1/customer-subscriptions
  ✅ GET  /api/v1/shipping-addresses?userId=123
  
  ❌ GET  /api/v1/getUser/123
  ❌ POST /api/v1/CreateShipment
  ❌ GET  /api/v1/user_orders
  ```

---

## 📁 2. API Versioning Strategy

* **Always version APIs explicitly** to ensure backward compatibility:
  ```
  /api/v1/users
  /api/v2/users  (breaking changes)
  ```

* **Versioning Options:**
  - **URL Path** (Recommended): `/api/v1/users`
  - **Header**: `Accept: application/vnd.api+json;version=1`
  - **Query Parameter**: `/api/users?version=1`

* **Version Management:**
  ```
  v1.0 → Initial release
  v1.1 → Backward-compatible changes (new optional fields)
  v2.0 → Breaking changes (removed/changed fields)
  ```

* **Deprecation Strategy:**
  ```json
  // Response headers for deprecated versions
  {
    "Deprecation": "true",
    "Sunset": "2025-12-31T23:59:59Z",
    "Link": "</api/v2/users>; rel=\"successor-version\""
  }
  ```

---

## 📂 3. HTTP Methods & Status Codes

### Standard CRUD Operations

| Action            | Endpoint                | Method | Status Code | Description |
|-------------------|-------------------------|--------|-------------|-------------|
| List resources    | `GET /users`           | GET    | 200         | Get all users |
| Get by ID         | `GET /users/{id}`      | GET    | 200, 404    | Get specific user |
| Create            | `POST /users`          | POST   | 201, 400    | Create new user |
| Full Update       | `PUT /users/{id}`      | PUT    | 200, 404    | Replace entire user |
| Partial Update    | `PATCH /users/{id}`    | PATCH  | 200, 404    | Update specific fields |
| Delete            | `DELETE /users/{id}`   | DELETE | 204, 404    | Remove user |

### Advanced Operations

| Action            | Endpoint                     | Method | Status Code |
|-------------------|------------------------------|--------|-------------|
| Search            | `GET /users/search`          | GET    | 200         |
| Bulk Create       | `POST /users/bulk`           | POST   | 201, 207    |
| Bulk Update       | `PATCH /users/bulk`          | PATCH  | 200, 207    |
| Bulk Delete       | `DELETE /users/bulk`         | DELETE | 204         |
| Archive           | `POST /users/{id}/archive`   | POST   | 200         |
| Restore           | `POST /users/{id}/restore`   | POST   | 200         |

### Status Code Guidelines

**Success Codes:**
- `200 OK` - Successful GET, PUT, PATCH
- `201 Created` - Successful POST with resource creation
- `204 No Content` - Successful DELETE or action with no response body
- `207 Multi-Status` - Bulk operations with mixed results

**Error Codes:**
- `400 Bad Request` - Invalid request syntax or validation errors
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Access denied
- `404 Not Found` - Resource doesn't exist
- `409 Conflict` - Resource conflict (duplicate, constraint violation)
- `422 Unprocessable Entity` - Valid syntax but semantic errors
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Server error

---

## 🔍 4. Querying & Pagination (Simplified)

- Support basic filtering: `?status=active&role=admin`
- Support sorting: `?sort=field,asc|desc`
- Use zero-based pagination: `?page=0&size=20`

---

## 📦 5. Sub-resources & Relationships (Simplified)

- Use nested paths for strong ownership: `/users/{id}/orders`
- Use top-level for independent resources: `/products`

---

## 🔐 6. Auth & Security (Simplified)

- Standard endpoints: `/auth/login`, `/auth/logout`, `/auth/refresh`
- Use `Bearer` tokens in `Authorization` header
- Include `X-Request-ID` for tracing

---

## 📁 7. File Operations

### File Upload Endpoints

```http
# Single file upload
POST /api/v1/documents/upload
Content-Type: multipart/form-data

# Multiple files upload
POST /api/v1/documents/bulk-upload
Content-Type: multipart/form-data

# Profile picture upload
POST /api/v1/users/{userId}/avatar
Content-Type: multipart/form-data

# Document attachment to resource
POST /api/v1/orders/{orderId}/attachments
Content-Type: multipart/form-data
```

### File Download Endpoints

```http
# Direct file download
GET /api/v1/documents/{documentId}/download
GET /api/v1/users/{userId}/avatar

# Secure file access with temporary URLs
GET /api/v1/documents/{documentId}/secure-url
GET /api/v1/reports/{reportId}/download?token=temp-access-token

# File streaming for large files
GET /api/v1/videos/{videoId}/stream
Range: bytes=200-1023
```

### File Upload Request Example

```http
POST /api/v1/documents/upload
Content-Type: multipart/form-data
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Form data:
file: [binary file data]
metadata: {
  "title": "Project Proposal",
  "category": "BUSINESS_DOCUMENT",
  "tags": ["proposal", "2025", "client-a"],
  "isPublic": false
}
```

### File Upload Response

```json
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "documentId": "doc_123456789",
    "fileName": "project-proposal.pdf",
    "fileSize": 2048576,
    "mimeType": "application/pdf",
    "checksum": "sha256:abc123...",
    "downloadUrl": "/api/v1/documents/doc_123456789/download",
    "thumbnailUrl": "/api/v1/documents/doc_123456789/thumbnail",
    "uploadedAt": "2025-06-26T13:30:00.123Z"
  },
  "timestamp": "2025-06-26T13:30:00.123Z"
}
```

### File Validation Rules

```json
{
  "allowedTypes": ["image/jpeg", "image/png", "application/pdf", "text/csv"],
  "maxFileSize": "10MB",
  "maxFiles": 5,
  "virusScanRequired": true,
  "allowedExtensions": [".jpg", ".png", ".pdf", ".csv"]
}
```

---

## 📊 8. Reports & Analytics

### Report Generation Endpoints

```http
# Standard reports with query parameters
GET /api/v1/reports/sales-summary?from=2025-01-01&to=2025-12-31&region=US
GET /api/v1/reports/user-activity?userId=123&period=last-30-days
GET /api/v1/reports/shipment-analytics?status=delivered&groupBy=month

# Complex report generation with POST
POST /api/v1/reports/custom-analytics
POST /api/v1/reports/export/csv
POST /api/v1/reports/export/pdf
```

### Report Request Examples

**Simple Report:**
```http
GET /api/v1/reports/revenue-dashboard?from=2025-01-01&to=2025-06-30&currency=USD&groupBy=month
```

**Complex Report Generation:**
```json
{
  "reportType": "CUSTOM_ANALYTICS",
  "dateRange": {
    "from": "2025-01-01",
    "to": "2025-12-31"
  },
  "filters": {
    "regions": ["US", "EU", "ASIA"],
    "productCategories": ["ELECTRONICS", "CLOTHING"],
    "customerTypes": ["PREMIUM", "ENTERPRISE"]
  },
  "metrics": ["revenue", "orderCount", "averageOrderValue"],
  "groupBy": ["month", "region"],
  "format": "JSON",
  "includeCharts": true,
  "emailReport": {
    "enabled": true,
    "recipients": ["admin@company.com"],
    "schedule": "WEEKLY"
  }
}
```

### Report Response Format

```json
{
  "success": true,
  "message": "Sales report generated successfully",
  "data": {
    "reportId": "rpt_123456789",
    "reportType": "SALES_SUMMARY",
    "generatedAt": "2025-06-26T13:30:00.123Z",
    "period": {
      "from": "2025-01-01",
      "to": "2025-06-30"
    },
    "summary": {
      "totalRevenue": 1250000.50,
      "totalOrders": 3420,
      "averageOrderValue": 365.50,
      "topSellingProduct": "Laptop Pro 15\""
    },
    "chartData": [
      {
        "month": "2025-01",
        "revenue": 180000.00,
        "orders": 520
      }
    ],
    "downloadLinks": {
      "pdf": "/api/v1/reports/rpt_123456789/download/pdf",
      "csv": "/api/v1/reports/rpt_123456789/download/csv",
      "excel": "/api/v1/reports/rpt_123456789/download/xlsx"
    }
  },
  "timestamp": "2025-06-26T13:30:00.123Z"
}
```

### Async Report Generation

```http
# Start async report generation
POST /api/v1/reports/large-dataset/generate

# Check report status
GET /api/v1/reports/jobs/{jobId}/status

# Download completed report
GET /api/v1/reports/jobs/{jobId}/download
```

---

## 🔄 9. Batch Operations

### Batch Endpoints

```http
# Bulk resource creation
POST /api/v1/users/bulk-create
POST /api/v1/orders/bulk-create
POST /api/v1/products/bulk-import

# Bulk resource updates
PATCH /api/v1/users/bulk-update
PATCH /api/v1/orders/bulk-status-update

# Bulk resource deletion
DELETE /api/v1/users/bulk-delete
POST /api/v1/orders/bulk-archive    # Soft delete alternative
```

### Batch Request Examples

**Bulk User Creation:**
```json
{
  "users": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "role": "USER"
    },
    {
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane.smith@example.com",
      "role": "ADMIN"
    }
  ],
  "validateOnly": false,
  "skipDuplicates": true,
  "sendWelcomeEmail": true
}
```

**Bulk Status Update:**
```json
{
  "orderIds": [123, 456, 789],
  "status": "SHIPPED",
  "updateReason": "Bulk shipment processing",
  "notifyCustomers": true,
  "trackingNumbers": {
    "123": "TRACK123",
    "456": "TRACK456",
    "789": "TRACK789"
  }
}
```

### Batch Response Format (207 Multi-Status)

```json
{
  "success": true,
  "message": "Bulk operation completed with mixed results",
  "data": {
    "totalRequested": 3,
    "successful": 2,
    "failed": 1,
    "results": [
      {
        "index": 0,
        "status": "SUCCESS",
        "statusCode": 201,
        "data": {
          "userId": 123,
          "email": "john.doe@example.com"
        }
      },
      {
        "index": 1,
        "status": "SUCCESS",
        "statusCode": 201,
        "data": {
          "userId": 124,
          "email": "jane.smith@example.com"
        }
      },
      {
        "index": 2,
        "status": "FAILED",
        "statusCode": 400,
        "error": {
          "code": "DUPLICATE_EMAIL",
          "message": "Email already exists",
          "field": "email"
        }
      }
    ]
  },
  "timestamp": "2025-06-26T13:30:00.123Z"
}
```

### Batch Processing Guidelines

* **Use HTTP 207 Multi-Status** for partial success scenarios
* **Validate before processing** - offer `validateOnly: true` option
* **Provide detailed results** for each item in the batch
* **Set reasonable limits** (e.g., max 1000 items per batch)
* **Support partial processing** - don't fail entire batch for single item errors
* **Use async processing** for large batches with job tracking

---

## 📡 10. Webhooks & Event-Driven APIs

### Webhook Endpoints

```http
# Webhook subscription management
GET    /api/v1/webhooks                     # List webhooks
POST   /api/v1/webhooks                     # Create webhook
GET    /api/v1/webhooks/{webhookId}         # Get webhook details
PUT    /api/v1/webhooks/{webhookId}         # Update webhook
DELETE /api/v1/webhooks/{webhookId}         # Delete webhook

# Webhook testing and validation
POST   /api/v1/webhooks/{webhookId}/test    # Test webhook
GET    /api/v1/webhooks/{webhookId}/logs    # Webhook delivery logs
POST   /api/v1/webhooks/{webhookId}/retry   # Retry failed delivery
```

### Webhook Registration Example

```json
{
  "url": "https://client-app.com/webhooks/orders",
  "events": ["order.created", "order.updated", "order.cancelled"],
  "secret": "webhook_secret_key",
  "isActive": true,
  "retryPolicy": {
    "maxAttempts": 3,
    "backoffStrategy": "EXPONENTIAL"
  },
  "headers": {
    "X-Custom-Header": "custom-value"
  }
}
```

### Webhook Payload Format

```json
{
  "eventId": "evt_123456789",
  "eventType": "order.created",
  "timestamp": "2025-06-26T13:30:00.123Z",
  "apiVersion": "v1",
  "data": {
    "orderId": 12345,
    "customerId": 67890,
    "status": "PENDING",
    "totalAmount": 299.99,
    "items": [
      {
        "productId": 101,
        "quantity": 2,
        "price": 149.99
      }
    ]
  },
  "previousData": null    # For update events, includes previous state
}
```

### Webhook Security

**Headers sent with webhook:**
```http
X-Webhook-Signature: sha256=abc123...
X-Webhook-ID: webhook_123
X-Webhook-Timestamp: 1640995200
X-Webhook-Event: order.created
Content-Type: application/json
User-Agent: MyApp-Webhooks/1.0
```

**Expected Response from Client:**
- **2xx status code** to acknowledge successful receipt
- **Non-2xx status code** will trigger retry mechanism
- Response should be fast (< 5 seconds timeout)

### Event Types Examples

```
# User events
user.created
user.updated
user.deleted
user.password_reset

# Order events
order.created
order.updated
order.cancelled
order.shipped
order.delivered

# Payment events
payment.succeeded
payment.failed
payment.refunded

# System events
system.maintenance_scheduled
system.backup_completed
```

---

## ❤️ 11. Health Checks & System APIs

### Health Check Endpoints

```http
# Basic health check
GET /health                              # Simple alive check
GET /api/v1/health                       # API health check
GET /api/v1/health/detailed              # Detailed health status

# Component-specific health checks
GET /api/v1/health/database              # Database connectivity
GET /api/v1/health/redis                 # Cache connectivity
GET /api/v1/health/external-services     # Third-party dependencies
```

### System Information APIs

```http
GET /api/v1/info                         # Application info
GET /api/v1/metrics                      # Application metrics
GET /api/v1/version                      # Version information
GET /actuator/env                        # Environment properties (Spring Boot)
GET /actuator/configprops                # Configuration properties
```

### Health Check Response Examples

**Simple Health Check:**
```json
{
  "status": "UP",
  "timestamp": "2025-06-26T13:30:00.123Z"
}
```

**Detailed Health Check:**
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "connectionPool": {
          "active": 5,
          "max": 20,
          "idle": 15
        },
        "responseTime": "12ms"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "responseTime": "2ms",
        "memoryUsage": "45%"
      }
    },
    "externalApi": {
      "status": "DOWN",
      "details": {
        "url": "https://api.external-service.com",
        "error": "Connection timeout",
        "lastSuccess": "2025-06-26T12:30:00.123Z"
      }
    }
  },
  "timestamp": "2025-06-26T13:30:00.123Z"
}
```

### Metrics Response Example

```json
{
  "application": {
    "name": "portal-user-service",
    "version": "1.2.3",
    "uptime": "5d 12h 30m",
    "startTime": "2025-06-21T01:00:00.000Z"
  },
  "system": {
    "cpuUsage": "15.5%",
    "memoryUsage": "512MB / 2GB",
    "diskSpace": "45GB / 100GB"
  },
  "api": {
    "totalRequests": 125430,
    "requestsPerMinute": 45,
    "averageResponseTime": "185ms",
    "errorRate": "0.05%"
  },
  "timestamp": "2025-06-26T13:30:00.123Z"
}
```

---

## 🧪 12. Response Format Standards

### Success Response Structure

```json
{
  "success": true,
  "message": "Optional descriptive message",
  "data": { /* Response payload */ },
  "metadata": {
    "requestId": "req_123456789",
    "processingTime": "125ms",
    "apiVersion": "v1"
  },
  "timestamp": "2025-06-26T13:13:03.642Z"
}
```

### Error Response Structure

```json
{
  "success": false,
  "errorCode": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "details": "One or more fields contain invalid values",
  "validationErrors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "Must be a valid email address",
      "code": "INVALID_EMAIL_FORMAT"
    },
    {
      "field": "age",
      "rejectedValue": -5,
      "message": "Must be greater than 0",
      "code": "INVALID_RANGE"
    }
  ],
  "metadata": {
    "path": "/api/v1/users",
    "method": "POST",
    "requestId": "req_123456789",
    "traceId": "trace_987654321"
  },
  "timestamp": "2025-06-26T13:13:03.642Z"
}
```

### Different Error Scenarios

**Resource Not Found (404):**
```json
{
  "success": false,
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "User with ID 123 not found",
  "metadata": {
    "path": "/api/v1/users/123",
    "method": "GET",
    "requestId": "req_123456789"
  },
  "timestamp": "2025-06-26T13:13:03.642Z"
}
```

**Authentication Error (401):**
```json
{
  "success": false,
  "errorCode": "AUTHENTICATION_REQUIRED",
  "message": "Access token is missing or invalid",
  "details": "Please provide a valid Bearer token in the Authorization header",
  "metadata": {
    "path": "/api/v1/users",
    "method": "GET",
    "requestId": "req_123456789"
  },
  "timestamp": "2025-06-26T13:13:03.642Z"
}
```

**Authorization Error (403):**
```json
{
  "success": false,
  "errorCode": "ACCESS_DENIED",
  "message": "Insufficient permissions to access this resource",
  "details": "Required role: ADMIN, Current role: USER",
  "metadata": {
    "path": "/api/v1/admin/users",
    "method": "GET",
    "requestId": "req_123456789",
    "userId": 456
  },
  "timestamp": "2025-06-26T13:13:03.642Z"
}
```

**Rate Limit Error (429):**
```json
{
  "success": false,
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "message": "Too many requests",
  "details": "Rate limit of 100 requests per minute exceeded",
  "metadata": {
    "path": "/api/v1/users",
    "method": "GET",
    "requestId": "req_123456789",
    "retryAfter": 60,
    "limit": 100,
    "remaining": 0,
    "resetTime": "2025-06-26T13:14:00.000Z"
  },
  "timestamp": "2025-06-26T13:13:03.642Z"
}
```

**Server Error (500):**
```json
{
  "success": false,
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred",
  "details": "Please try again later or contact support if the problem persists",
  "metadata": {
    "path": "/api/v1/users",
    "method": "POST",
    "requestId": "req_123456789",
    "traceId": "trace_987654321",
    "supportReference": "ERR-2025-0626-001"
  },
  "timestamp": "2025-06-26T13:13:03.642Z"
}
```

### Response Headers

**Standard Headers:**
```http
# Security headers
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block

# API-specific headers
X-API-Version: v1
X-Request-ID: req_123456789
X-Rate-Limit-Remaining: 95
X-Rate-Limit-Reset: 1640995200

# Caching headers
Cache-Control: no-cache, no-store, must-revalidate
ETag: "abc123"
Last-Modified: Wed, 26 Jun 2025 13:00:00 GMT
```

---

## ⚡ 13. Caching & Performance

### Caching Headers

```http
# Static resources (long-term caching)
Cache-Control: public, max-age=31536000, immutable
ETag: "abc123def456"

# Dynamic content (short-term caching)
Cache-Control: private, max-age=300, must-revalidate
Last-Modified: Wed, 26 Jun 2025 13:00:00 GMT

# No caching for sensitive data
Cache-Control: no-cache, no-store, must-revalidate
Pragma: no-cache
```

### ETags for Conditional Requests

**Response with ETag:**
```http
HTTP/1.1 200 OK
ETag: "abc123def456"
Content-Type: application/json

{
  "success": true,
  "data": { "userId": 123, "name": "John Doe" }
}
```

**Conditional Request:**
```http
GET /api/v1/users/123
If-None-Match: "abc123def456"
```

**Not Modified Response:**
```http
HTTP/1.1 304 Not Modified
ETag: "abc123def456"
```

### Performance Optimization

**Response Compression:**
```http
Accept-Encoding: gzip, deflate, br
Content-Encoding: gzip
```

**Field Selection (Sparse Fieldsets):**
```http
GET /api/v1/users?fields=id,firstName,lastName,email
GET /api/v1/orders?include=items,customer&exclude=internalNotes
```

**Async Processing for Heavy Operations:**
```http
# Start async operation
POST /api/v1/reports/generate
{
  "reportType": "ANNUAL_SUMMARY",
  "year": 2025
}

# Response
{
  "success": true,
  "data": {
    "jobId": "job_123456789",
    "status": "PROCESSING",
    "estimatedCompletion": "2025-06-26T13:35:00.000Z"
  }
}

# Check status
GET /api/v1/jobs/job_123456789/status
```

---

## 🚦 14. Rate Limiting & Throttling

### Rate Limit Headers

```http
X-RateLimit-Limit: 1000           # Requests allowed per window
X-RateLimit-Remaining: 999        # Requests remaining in window
X-RateLimit-Reset: 1640995200     # UTC timestamp when limit resets
X-RateLimit-Window: 3600          # Window size in seconds
```

### Rate Limiting Strategies

**Per User/API Key:**
```
100 requests per minute per user
1000 requests per hour per API key
```

**Per Endpoint:**
```
POST /users: 10 requests per minute
GET /users: 100 requests per minute
POST /reports/generate: 5 requests per hour
```

**Burst vs Sustained:**
```
Burst: 50 requests in 10 seconds
Sustained: 1000 requests per hour
```

### Rate Limit Exceeded Response

```json
{
  "success": false,
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "message": "Rate limit exceeded",
  "metadata": {
    "limit": 100,
    "remaining": 0,
    "retryAfter": 60,
    "resetTime": "2025-06-26T13:14:00.000Z"
  },
  "timestamp": "2025-06-26T13:13:03.642Z"
}
```

---

## 🌐 15. Internationalization & Localization

### Language Support

**Headers:**
```http
Accept-Language: en-US,en;q=0.9,es;q=0.8
Content-Language: en-US
```

**Query Parameters:**
```http
GET /api/v1/products?lang=es-ES&currency=EUR
GET /api/v1/users/123?locale=fr-FR
```

### Localized Response Example

```json
{
  "success": true,
  "message": "Usuario creado exitosamente",
  "data": {
    "userId": 123,
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan.perez@ejemplo.com",
    "locale": "es-ES",
    "timezone": "Europe/Madrid"
  },
  "timestamp": "2025-06-26T13:13:03.642Z"
}
```

### Multi-Currency Support

```json
{
  "price": {
    "amount": 29.99,
    "currency": "USD",
    "localizedPrice": {
      "amount": 25.45,
      "currency": "EUR",
      "formatted": "25,45 €"
    },
    "exchangeRate": 0.85,
    "exchangeRateDate": "2025-06-26"
  }
}
```

---

## 🏢 16. Multi-tenancy Support

### Tenant Identification

**Header-based:**
```http
X-Tenant-ID: company-abc
X-Organization-ID: org_123456
```

**URL-based:**
```http
GET /api/v1/tenants/company-abc/users
GET /api/v1/organizations/org_123456/orders
```

**Subdomain-based:**
```http
https://company-abc.api.yourdomain.com/v1/users
https://org-123456.api.yourdomain.com/v1/orders
```

### Tenant-aware Response

```json
{
  "success": true,
  "data": {
    "userId": 123,
    "tenantId": "company-abc",
    "tenantName": "ABC Corporation",
    "permissions": ["READ_USERS", "WRITE_ORDERS"],
    "limits": {
      "maxUsers": 1000,
      "usedUsers": 456,
      "storageQuota": "100GB"
    }
  }
}
```

---

## ✅ 17. API Design Best Practices

### URL Naming Conventions

```
✅ CORRECT Examples:
GET  /api/v1/users                    # List users
GET  /api/v1/users/123                # Get specific user
POST /api/v1/users                    # Create user
PUT  /api/v1/users/123                # Update entire user
PATCH /api/v1/users/123               # Partial user update
DELETE /api/v1/users/123              # Delete user
GET  /api/v1/users/123/orders         # User's orders
POST /api/v1/orders/batch-create      # Batch operation
GET  /api/v1/customer-subscriptions   # Kebab-case for multi-word
POST /api/v1/users/123/reset-password # Action on specific resource

❌ INCORRECT Examples:
GET  /api/v1/getUsers                 # Don't use verbs
POST /api/v1/createUser               # Don't use verbs
GET  /api/v1/user/123                 # Use plural nouns
PUT  /api/v1/updateUser/123          # Don't repeat HTTP verb
DELETE /api/v1/deleteUser/123        # Don't repeat HTTP verb
GET  /api/v1/user_orders             # Avoid snake_case
POST /api/v1/batchCreateShipments    # Use kebab-case
GET  /api/v1/Users/123               # Use lowercase
```

### Request/Response Examples

**Create User Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "USER",
  "preferences": {
    "notifications": true,
    "theme": "dark"
  }
}
```

**Update User Request (PATCH):**
```json
{
  "firstName": "Jonathan",
  "preferences": {
    "theme": "light"
  }
}
```

**Bulk Operation Request:**
```json
{
  "operation": "UPDATE_STATUS",
  "filters": {
    "status": "PENDING",
    "createdBefore": "2025-06-01"
  },
  "updates": {
    "status": "CANCELLED",
    "reason": "Bulk cancellation"
  }
}
```

---

## 🔒 18. Security Best Practices

### Input Validation

```json
{
  "validationRules": {
    "email": {
      "required": true,
      "format": "email",
      "maxLength": 255
    },
    "password": {
      "required": true,
      "minLength": 8,
      "pattern": "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]",
      "description": "Must contain uppercase, lowercase, number, and special character"
    },
    "age": {
      "type": "integer",
      "minimum": 18,
      "maximum": 120
    }
  }
}
```

### Security Headers

```http
# Security headers that should be included
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
Referrer-Policy: strict-origin-when-cross-origin
```

### API Key Management

```http
# API Key in header (preferred)
X-API-Key: your-secret-api-key

# Or in query parameter (less secure)
GET /api/v1/users?api_key=your-secret-api-key
```

---

## 📊 19. Monitoring & Observability

### Request/Response Logging

```json
{
  "requestId": "req_123456789",
  "timestamp": "2025-06-26T13:30:00.123Z",
  "method": "POST",
  "path": "/api/v1/users",
  "statusCode": 201,
  "responseTime": "125ms",
  "userAgent": "MyApp/1.0",
  "clientIP": "192.168.1.100",
  "userId": 456,
  "requestSize": "1.2KB",
  "responseSize": "0.8KB"
}
```

### Error Tracking

```json
{
  "errorId": "err_987654321",
  "timestamp": "2025-06-26T13:30:00.123Z",
  "level": "ERROR",
  "message": "Database connection failed",
  "stackTrace": "...",
  "context": {
    "requestId": "req_123456789",
    "userId": 456,
    "endpoint": "/api/v1/users",
    "database": "postgresql",
    "connectionPool": "primary"
  }
}
```

### Metrics to Track

```
API Metrics:
- Request count per endpoint
- Response times (p50, p95, p99)
- Error rates by status code
- Throughput (requests per second)

Business Metrics:
- Active users
- Feature usage
- Conversion rates
- Revenue metrics

System Metrics:
- CPU/Memory usage
- Database performance
- Cache hit rates
- Queue depths
```

---

## 📘 20. Development Tools & Documentation

### API Documentation Tools

**OpenAPI/Swagger:**
```yaml
openapi: 3.0.3
info:
  title: Service API
  description: Comprehensive API for user management
  version: 1.0.0
  contact:
    name: API Support
    email: api-support@company.com
paths:
  /api/v1/users:
    get:
      summary: List users
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            default: 20
      responses:
        '200':
          description: Users retrieved successfully
```

**Postman Collection:**
```json
{
  "info": {
    "name": "Service API",
    "description": "Complete API collection for testing",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "auth": {
    "type": "bearer",
    "bearer": [
      {
        "key": "token",
        "value": "{{access_token}}",
        "type": "string"
      }
    ]
  },
  "variable": [
    {
      "key": "base_url",
      "value": "https://api.yourapp.com",
      "type": "string"
    }
  ]
}
```

### Testing Tools & Frameworks

**Unit Testing with Spring Boot:**
```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Test
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "firstName": "John",
                        "lastName": "Doe",
                        "email": "john.doe@example.com"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").exists());
    }
}
```

**API Performance Testing:**
```javascript
// k6 performance test
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 0 },
  ],
};

export default function() {
  let response = http.get('https://api.yourapp.com/api/v1/users');
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
}
```

### Development Environment Setup

**Docker Compose for Local Development:**
```yaml
version: '3.8'
services:
  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DATABASE_URL=jdbc:postgresql://db:5432/userservice
    depends_on:
      - db
      - redis
  
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: userservice
      POSTGRES_USER: dev
      POSTGRES_PASSWORD: devpass
    ports:
      - "5432:5432"
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

### API Versioning in Spring Boot

```java
@RestController
@RequestMapping("/api/v1/users")
@Api(tags = "User Management")
public class UserController {
    
    @GetMapping
    @ApiOperation(value = "List users", response = ApiResponse.class)
    public ResponseEntity<ApiResponse<PagedResult<User>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Implementation
    }
    
    @PostMapping
    @ApiOperation(value = "Create user", response = ApiResponse.class)
    public ResponseEntity<ApiResponse<User>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        // Implementation
    }
}
```

---

## 🎨 DTO Patterns

To standardize API responses and simplify implementation across endpoints, use the following reusable DTOs:

### 1. ApiResponse<T>
```java
// src/main/java/com/itt/service/dto/ApiResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private List<ValidationError> validationErrors;
    private String path;
    private String traceId;
    private LocalDateTime timestamp;

    // Static builders for success and error responses
    public static <T> ApiResponse<T> success(T data) { /*...*/ }
    public static <T> ApiResponse<T> error(ErrorCode code, String msg) { /*...*/ }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private Object rejectedValue;
        private String message;
        private String code;
    }
}
```

### 2. PaginationResponse<T>
```java
// src/main/java/com/itt/service/dto/PaginationResponse.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private List<T> content;

    public PaginationResponse(Page<T> pageResult) {
        this.page = pageResult.getNumber();
        this.size = pageResult.getSize();
        this.totalElements = pageResult.getTotalElements();
        this.totalPages = pageResult.getTotalPages();
        this.last = pageResult.isLast();
        this.content = pageResult.getContent();
    }
}
```

### 3. DataTableRequest
```java
// src/main/java/com/itt/service/dto/DataTableRequest.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataTableRequest {
    @Valid
    private Pagination pagination = new Pagination();
    @Valid
    private SearchFilter searchFilter;
    @Valid
    private List<Column> columns = new ArrayList<>();
    private transient SortFieldValidator sortFieldValidator;

    public Pageable toPageable() { /* builds page, size and optional sort orders */ }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination { /* page, size with @Min/@Max */ }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchFilter { /* searchText, columns */ }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Column { /* columnName, filter, sort */ }
}
```

These patterns make it simple to return consistent payloads, handle pagination, sorting and filtering in a uniform way.

*Last updated: June 26, 2025*
*Version: 2.1*
