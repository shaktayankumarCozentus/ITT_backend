package com.itt.service.fw.ratelimit.example;

import com.itt.service.fw.ratelimit.service.ExternalApiRateLimitingService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * ✅ EXAMPLE: External API Rate Limiting Usage Examples
 * ======================================================
 * 
 * This class demonstrates how to integrate the External API Rate Limiting Service
 * into your controllers and services. It shows best practices for handling rate
 * limits, error responses, and monitoring.
 * 
 * 🎯 USAGE PATTERNS:
 * - API Key-based rate limiting
 * - Custom rate limit responses
 * - Rate limit monitoring and metrics
 * - Integration with existing authentication
 * 
 * 🔧 IMPLEMENTATION APPROACHES:
 * 1. Controller-level rate limiting (shown here)
 * 2. Filter-based rate limiting (recommended for global application)
 * 3. Service-level rate limiting for specific operations
 * 4. Aspect-oriented programming (AOP) for declarative rate limiting
 * 
 * Developer Notes:
 * - Always extract API keys securely
 * - Provide meaningful error messages
 * - Include rate limit information in response headers
 * - Log rate limit violations for monitoring
 * - Consider implementing grace periods for legitimate users
 * 
 * @author ITT Development Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/api/external")
@RequiredArgsConstructor
public class ExternalApiRateLimitingExampleController {
    
    private final ExternalApiRateLimitingService rateLimitingService;
    
    /**
     * Example 1: Basic API Key Rate Limiting
     * 
     * This example shows the simplest integration pattern where rate limiting
     * is applied at the controller method level.
     */
    @GetMapping("/basic-example")
    public ResponseEntity<?> basicRateLimitingExample(
            @RequestHeader("X-API-Key") String apiKey,
            HttpServletRequest request) {
        
        log.info("🔑 Processing request for API key: {}", maskApiKey(apiKey));
        
        // Check rate limiting
        Bucket bucket = rateLimitingService.getExternalApiBucket(apiKey);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (!probe.isConsumed()) {
            log.warn("⚠️ Rate limit exceeded for API key: {}", maskApiKey(apiKey));
            
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-RateLimit-Limit", "1000") // from configuration
                    .header("X-RateLimit-Remaining", "0")
                    .header("X-RateLimit-Reset", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000))
                    .body(Map.of(
                        "error", "Rate limit exceeded",
                        "message", "Too many requests. Please try again later.",
                        "retryAfter", probe.getNanosToWaitForRefill() / 1_000_000_000
                    ));
        }
        
        // Add rate limit information to successful responses
        return ResponseEntity.ok()
                .header("X-RateLimit-Limit", "1000")
                .header("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()))
                .body(Map.of(
                    "message", "Request processed successfully",
                    "data", "Your API response data here",
                    "timestamp", System.currentTimeMillis()
                ));
    }
    
    /**
     * Example 2: Advanced Rate Limiting with Custom Configuration
     * 
     * This example shows how to implement rate limiting with custom logic,
     * different limits for different API tiers, and comprehensive monitoring.
     */
    @PostMapping("/advanced-example")
    public ResponseEntity<?> advancedRateLimitingExample(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestHeader(value = "X-API-Tier", defaultValue = "standard") String apiTier,
            @RequestBody Map<String, Object> requestData) {
        
        log.info("🔑 Processing {} tier request for API key: {}", apiTier, maskApiKey(apiKey));
        
        // Check if rate limiting is enabled for this API
        if (!rateLimitingService.isRateLimitingEnabled()) {
            log.debug("Rate limiting is disabled, processing request");
            return processRequest(requestData);
        }
        
        // Get bucket and check rate limit
        Bucket bucket = rateLimitingService.getExternalApiBucket(apiKey);
        
        // Calculate tokens to consume based on request complexity
        int tokensToConsume = calculateTokensRequired(requestData, apiTier);
        
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(tokensToConsume);
        
        if (!probe.isConsumed()) {
            // Log for monitoring and alerting
            log.warn("⚠️ Rate limit exceeded for API key: {} (tier: {}, tokens attempted: {})", 
                    maskApiKey(apiKey), apiTier, tokensToConsume);
            
            // Return detailed rate limit information
            return buildRateLimitExceededResponse(probe, tokensToConsume);
        }
        
        // Log successful request for monitoring
        log.info("✅ Request processed successfully for API key: {} (tokens consumed: {}, remaining: {})", 
                maskApiKey(apiKey), tokensToConsume, probe.getRemainingTokens());
        
        // Process the actual request
        ResponseEntity<?> response = processRequest(requestData);
        
        // Add rate limit headers to response
        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .header("X-RateLimit-Limit", "1000") // This should come from configuration
                .header("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()))
                .header("X-RateLimit-Tokens-Consumed", String.valueOf(tokensToConsume))
                .body(response.getBody());
    }
    
    /**
     * Example 3: Rate Limiting with Grace Period
     * 
     * This example shows how to implement a grace period for legitimate users
     * who occasionally exceed rate limits.
     */
    @GetMapping("/grace-period-example")
    public ResponseEntity<?> gracePeriodRateLimitingExample(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestParam(value = "priority", defaultValue = "normal") String priority) {
        
        Bucket bucket = rateLimitingService.getExternalApiBucket(apiKey);
        
        // First, try normal consumption
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (!probe.isConsumed()) {
            // For high-priority requests, try to allow one more request
            if ("high".equals(priority)) {
                log.info("🎯 Attempting grace period for high-priority request from API key: {}", maskApiKey(apiKey));
                
                // You could implement custom logic here, such as:
                // - Allowing one request per hour even when rate limited
                // - Checking if the user has premium status
                // - Implementing a separate grace period bucket
                
                // For this example, we'll just log and still deny the request
                log.warn("⚠️ High-priority request still rate limited for API key: {}", maskApiKey(apiKey));
            }
            
            return buildRateLimitExceededResponse(probe, 1);
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "Request processed with priority: " + priority,
            "remainingTokens", probe.getRemainingTokens()
        ));
    }
    
    /**
     * Example 4: Administrative Endpoint for Rate Limit Management
     * 
     * This shows how to provide administrative functions for managing rate limits.
     */
    @PostMapping("/admin/reset-rate-limit")
    public ResponseEntity<?> resetRateLimitExample(
            @RequestHeader("X-Admin-Key") String adminKey,
            @RequestParam("apiKey") String apiKey) {
        
        // Validate admin permissions (implement your own logic)
        if (!isValidAdminKey(adminKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid admin credentials"));
        }
        
        try {
            rateLimitingService.resetExternalApiRateLimit(apiKey);
            log.info("🔄 Admin reset rate limit for API key: {}", maskApiKey(apiKey));
            
            return ResponseEntity.ok(Map.of(
                "message", "Rate limit reset successfully",
                "apiKey", maskApiKey(apiKey),
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("❌ Failed to reset rate limit for API key: {}", maskApiKey(apiKey), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reset rate limit"));
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🛠️ HELPER METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Calculates the number of tokens required for a request based on complexity and API tier.
     */
    private int calculateTokensRequired(Map<String, Object> requestData, String apiTier) {
        int baseTokens = 1;
        
        // Example: More complex requests require more tokens
        if (requestData.size() > 10) {
            baseTokens += 2;
        }
        
        // Example: Different API tiers have different token requirements
        return switch (apiTier.toLowerCase()) {
            case "premium" -> baseTokens; // Premium users get standard cost
            case "standard" -> baseTokens;
            case "basic" -> baseTokens * 2; // Basic users pay more tokens
            default -> baseTokens;
        };
    }
    
    /**
     * Builds a standardized rate limit exceeded response.
     */
    private ResponseEntity<?> buildRateLimitExceededResponse(ConsumptionProbe probe, int tokensAttempted) {
        long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Limit", "1000")
                .header("X-RateLimit-Remaining", "0")
                .header("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + retryAfterSeconds))
                .header("Retry-After", String.valueOf(retryAfterSeconds))
                .body(Map.of(
                    "error", "Rate limit exceeded",
                    "message", "Too many requests. Please try again later.",
                    "tokensAttempted", tokensAttempted,
                    "retryAfterSeconds", retryAfterSeconds,
                    "timestamp", System.currentTimeMillis()
                ));
    }
    
    /**
     * Simulates actual request processing.
     */
    private ResponseEntity<?> processRequest(Map<String, Object> requestData) {
        // Your actual business logic here
        return ResponseEntity.ok(Map.of(
            "result", "Request processed successfully",
            "processedData", requestData.size() + " fields processed",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * Validates admin credentials (implement your own logic).
     */
    private boolean isValidAdminKey(String adminKey) {
        // Implement your admin key validation logic
        return "admin-secret-key".equals(adminKey); // DON'T DO THIS IN PRODUCTION!
    }
    
    /**
     * Masks API key for secure logging.
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
    }
}
