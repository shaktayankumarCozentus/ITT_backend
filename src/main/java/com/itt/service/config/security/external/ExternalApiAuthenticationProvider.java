package com.itt.service.config.security.external;

import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ✅ EXTENSIBLE: External API Authentication Provider Interface
 * =============================================================
 * 
 * Defines the contract for external API authentication mechanisms.
 * Supports multiple authentication strategies in a unified way.
 * 
 * 🎯 SUPPORTED MECHANISMS:
 * - API Key authentication (X-API-Key header)
 * - HMAC signature validation  
 * - OAuth Client Credentials flow
 * - Mutual TLS (mTLS) certificate authentication
 * 
 * 🚀 DEVELOPER USAGE:
 * 
 * 1. Implement this interface for new auth mechanisms:
 *    ```java
 *    @Component
 *    public class HmacAuthProvider implements ExternalApiAuthenticationProvider {
 *        @Override
 *        public Authentication authenticate(HttpServletRequest request) {
 *            // Your HMAC validation logic
 *        }
 *        
 *        @Override 
 *        public boolean supports(HttpServletRequest request) {
 *            // Check for HMAC headers
 *        }
 *        
 *        @Override
 *        public String getAuthenticationMechanism() {
 *            return "HMAC_SHA256";
 *        }
 *    }
 *    ```
 * 
 * 2. Spring auto-registers your provider in ExternalApiAuthenticationFilter
 * 3. No SecurityConfiguration changes needed!
 * 
 * ⭐ ARCHITECTURE BENEFITS:
 * - Zero-configuration addition of new auth mechanisms
 * - Automatic provider discovery via Spring dependency injection
 * - Clean separation of authentication concerns
 * - Easy testing and mocking of individual providers
 * 
 * @author Service Team
 * @version 2.1 - Enhanced Documentation & Examples
 * @since 2025.1
 */
public interface ExternalApiAuthenticationProvider {
    
    /**
     * Authenticates an external API request based on the provided credentials.
     * 
     * Implementation Guidelines:
     * - Return Authentication object for successful authentication
     * - Return null for missing credentials (let other providers try)
     * - Throw exceptions only for invalid/malformed credentials
     * - Populate SecurityContext with appropriate authorities
     * 
     * @param request the HTTP request containing authentication credentials
     * @return Authentication object if successful, null if no credentials found
     * @throws RuntimeException for invalid/malformed credentials
     */
    Authentication authenticate(HttpServletRequest request);
    
    /**
     * Checks if this provider can handle the given request.
     * 
     * Implementation Guidelines:
     * - Check for required headers, certificates, or other indicators
     * - Return true only if you can definitely process this request
     * - Should be fast - this is called for every external API request
     * 
     * @param request the HTTP request to evaluate
     * @return true if this provider can authenticate the request, false otherwise
     */
    boolean supports(HttpServletRequest request);
    
    /**
     * Returns the authentication mechanism type this provider handles.
     * 
     * Standard Mechanism Names:
     * - "API_KEY" - API key authentication
     * - "HMAC_SHA256" - HMAC signature validation
     * - "OAUTH_CLIENT_CREDENTIALS" - OAuth 2.0 client credentials
     * - "MTLS" - Mutual TLS certificate authentication
     * 
     * @return the authentication mechanism name for logging and debugging
     */
    String getAuthenticationMechanism();
}
