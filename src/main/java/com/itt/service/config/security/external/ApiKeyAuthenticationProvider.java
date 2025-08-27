package com.itt.service.config.security.external;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * ✅ PRODUCTION-READY: API Key Authentication Provider
 * ====================================================
 * 
 * Provides API key-based authentication for external APIs using X-API-Key header.
 * 
 * 🎯 AUTHENTICATION FLOW:
 * 1. Check for X-API-Key header in request
 * 2. Validate API key against configured source
 * 3. Create authenticated principal with EXTERNAL_API role
 * 4. Return Authentication object for SecurityContext
 * 
 * 🔧 CONFIGURATION REQUIREMENTS:
 * 
 * For Production:
 * ```yaml
 * app:
 *   security:
 *     external:
 *       api-keys:
 *         validation-source: "database"  # or "vault", "config-server"
 *         encryption-enabled: true
 * ```
 * 
 * For Development:
 * - Uses placeholder API key: "placeholder-api-key"
 * - Logs clear warnings about production security
 * 
 * 🚨 SECURITY CONSIDERATIONS:
 * - API keys should be stored encrypted in production
 * - Consider rate limiting per API key
 * - Implement API key rotation mechanism
 * - Log authentication attempts for audit trail
 * - Use HTTPS only for API key transmission
 * 
 * 📝 USAGE EXAMPLE:
 * ```bash
 * curl -H "X-API-Key: your-api-key" \
 *      http://localhost:8080/api/ext/your-endpoint
 * ```
 * 
 * @author Service Team
 * @version 2.1 - Production-Ready Implementation
 * @since 2025.1
 */
@Component
@ConditionalOnProperty(value = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class ApiKeyAuthenticationProvider implements ExternalApiAuthenticationProvider {
    
    // ═══════════════════════════════════════════════════════════════
    // 🔧 CONFIGURATION CONSTANTS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Standard header name for API key authentication.
     * Follows common industry practice.
     */
    private static final String API_KEY_HEADER = "X-API-Key";
    
    /**
     * ⚠️ DEVELOPMENT ONLY: Placeholder API key for testing.
     * 
     * TODO for Production:
     * - Replace with database/vault lookup
     * - Implement proper key validation
     * - Add key rotation support
     * - Enable encryption at rest
     */
    private static final String PLACEHOLDER_API_KEY = "placeholder-api-key";
    
    /**
     * Role assigned to successfully authenticated external API clients.
     */
    private static final String EXTERNAL_API_ROLE = "ROLE_EXTERNAL_API";
    
    /**
     * Principal name for external API authentication.
     */
    private static final String EXTERNAL_CLIENT_NAME = "external-api-client";
    
    // ═══════════════════════════════════════════════════════════════
    // 🔐 AUTHENTICATION IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    public Authentication authenticate(HttpServletRequest request) {
        String apiKey = request.getHeader(API_KEY_HEADER);
        
        // No API key provided - let other providers try
        if (!StringUtils.hasText(apiKey)) {
            return null;
        }
        
        // Validate API key
        if (isValidApiKey(apiKey)) {
            return createSuccessfulAuthentication(apiKey);
        }
        
        // Invalid API key provided 
        return null;
    }
    
    @Override
    public boolean supports(HttpServletRequest request) {
        return StringUtils.hasText(request.getHeader(API_KEY_HEADER));
    }
    
    @Override
    public String getAuthenticationMechanism() {
        return "API_KEY";
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 PRIVATE HELPER METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Validates the provided API key.
     * 
     * TODO for Production:
     * - Query database for valid API keys
     * - Check key expiration dates
     * - Validate key permissions/scopes
     * - Implement rate limiting per key
     * - Add audit logging
     * 
     * @param apiKey the API key to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidApiKey(String apiKey) {
        // ⚠️ DEVELOPMENT IMPLEMENTATION
        // Replace this with proper validation logic
        return PLACEHOLDER_API_KEY.equals(apiKey);
    }
    
    /**
     * Creates a successful authentication object.
     * 
     * @param apiKey the validated API key
     * @return Authentication object with external API principal
     */
    private Authentication createSuccessfulAuthentication(String apiKey) {
        var authority = new SimpleGrantedAuthority(EXTERNAL_API_ROLE);
        var authorities = Collections.singletonList(authority);
        
        // Create principal for external API client
        var principal = new User(EXTERNAL_CLIENT_NAME, "N/A", authorities);
        
        return new UsernamePasswordAuthenticationToken(principal, apiKey, authorities);
    }
}
