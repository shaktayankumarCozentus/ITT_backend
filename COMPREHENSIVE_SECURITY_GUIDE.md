# Service - Security Guide

## Quick Navigation
- [🔧 How to Add New Endpoints](#how-to-add-new-endpoints)
- [🧪 Testing Authentication](#testing-authentication)  
- [⚙️ Configuration Setup](#configuration-setup)
- [🚨 Common Issues](#common-issues)

---

## Security Overview

We use a **3-chain security system** that automatically routes requests:

```
1. PUBLIC ENDPOINTS    → No auth needed (health, docs, login)
2. EXTERNAL APIs       → API key auth (/api/ext/**)  
3. INTERNAL APIs       → JWT auth (/api/v1/**)
```

## File Structure (Actual)

```
src/main/java/com/itt/service/config/
├── security/
│   ├── SecurityConfiguration.java               # Main security config
│   ├── Auth0JwtAuthFilter.java                 # JWT processing
│   ├── TokenAssociatedSessionValidator.java    # Session validation
│   ├── external/
│   │   ├── ExternalApiAuthenticationFilter.java
│   │   ├── ExternalApiAuthenticationProvider.java
│   │   └── ApiKeyAuthenticationProvider.java
│   └── NoSecurityConfiguration.java            # Disabled security
├── CacheConfig.java                             # Rate limiting cache
└── AwsSecrets.java                             # AWS secrets
```

---

## 🔧 How to Add New Endpoints

### Add Public Endpoint (No Auth)
1. Open `SecurityConfiguration.java`
2. Add to `PUBLIC_ENDPOINTS` array:
   ```java
   private static final String[] PUBLIC_ENDPOINTS = {
       "/api/public/**",
       "/api/auth/**", 
       "/api/health/**",
       "/api/docs/**",
       "/api/your-new-endpoint/**"  // ← Add here
   };
   ```

### Add Internal API Endpoint (JWT Required)
1. Create controller with `/api/v1/**` path
2. JWT auth is automatic - no config needed!
   ```java
   @RestController
   @RequestMapping("/api/v1/users")
   public class UserController {
       // Automatically requires JWT
   }
   ```

### Add External API Endpoint (API Key Required)  
1. Create controller with `/api/ext/**` path
2. API key auth is automatic
   ```java
   @RestController
   @RequestMapping("/api/ext/partners")
   public class PartnerController {
       // Automatically requires X-API-Key header
   }
   ```

---

## 🧪 Testing Authentication

### Quick Test Commands
```bash
# 1. Test Public Endpoint (should work)
curl http://localhost:8080/api/health

# 2. Test Internal API without JWT (should get 401)
curl http://localhost:8080/api/v1/users

# 3. Test Internal API with JWT (should work)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/v1/users

# 4. Test External API without key (should get 401)
curl http://localhost:8080/api/ext/partners

# 5. Test External API with key (should work)
curl -H "X-API-Key: your-api-key" \
     http://localhost:8080/api/ext/partners
```

### Get JWT Token for Testing
```bash
# Get token from Auth0
curl -X POST https://YOUR_DOMAIN.auth0.com/oauth/token \
  -H "Content-Type: application/json" \
  -d '{
    "client_id": "YOUR_CLIENT_ID",
    "client_secret": "YOUR_CLIENT_SECRET",
    "audience": "YOUR_API_AUDIENCE",
    "grant_type": "client_credentials"
  }'
```

---

## ⚙️ Configuration Setup

### Required Environment Variables
```bash
# Auth0 Settings
export AUTH0_CLIENT_ID=your_client_id
export AUTH0_M2M_CLIENT_SECRET=your_secret
export AUTH0_DOMAIN=your-domain.auth0.com

# Security Toggle
export SECURITY_ENABLED=true
```

### Application Properties
```yaml
# Basic Security
app:
  security:
    enabled: true

# Auth0 JWT  
auth0:
  clientId: ${AUTH0_CLIENT_ID}
  m2mClientSecret: ${AUTH0_M2M_CLIENT_SECRET}
  domain: ${AUTH0_DOMAIN}

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://${AUTH0_DOMAIN}/
```

### Disable Security (Development Only)
```yaml
app:
  security:
    enabled: false  # Disables all security
```

---

## 🚨 Common Issues

### "401 Unauthorized" on Internal APIs
**Problem:** JWT token rejected

**Solutions:**
1. Check if token is expired
2. Verify Auth0 domain in config
3. Check issuer-uri is accessible
4. Ensure token format: `Authorization: Bearer <token>`

### "403 Forbidden" on All Requests  
**Problem:** Security config issue

**Solutions:**
1. Check `app.security.enabled=true`
2. Verify endpoint path matches PUBLIC_ENDPOINTS
3. Check if multiple security configs are active

### "No bean of type PasswordEncoder"
**Problem:** Missing password encoder

**Solution:** Already fixed in SecurityConfiguration.java:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### External API Always Returns 401
**Problem:** API key validation failing

**Solutions:**
1. Check header format: `X-API-Key: your-key`
2. Verify ApiKeyAuthenticationProvider is active
3. Check if rate limiting is blocking requests

---

## Method-Level Security (Optional)

Use annotations for fine-grained control:
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/api/v1/admin/delete-user")
public ResponseEntity<?> deleteUser() {
    // Only admins can access
}

@Secured("ROLE_USER") 
@GetMapping("/api/v1/profile")
public ResponseEntity<?> getProfile() {
    // Users and admins can access
}
```

---

## Debug Mode

Enable detailed security logging:
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    com.itt.service.config.security: DEBUG
```

---

## Key Security Classes

| Class | Purpose |
|-------|---------|
| `SecurityConfiguration.java` | Main security setup with 3 chains |
| `Auth0JwtAuthFilter.java` | JWT token processing |
| `ExternalApiAuthenticationFilter.java` | API key validation |
| `TokenAssociatedSessionValidator.java` | Session validation |
| `NoSecurityConfiguration.java` | Disables security when needed |

---

## Support

**Team:** Service Development Team  
**Version:** 2.0 (Tri-Chain Architecture)  
**Spring Boot:** 3.5.0

For issues:
1. Check this guide first
2. Review the troubleshooting section  
3. Enable debug logging
4. Contact the development team

---

*Keep this guide updated as security implementation evolves.*
