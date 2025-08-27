package com.itt.service.config.security.external;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * ✅ PRODUCTION-READY: External API Authentication Filter
 * ======================================================
 * 
 * Orchestrates authentication for external API endpoints (/api/ext/**) by:
 * 1. Processing authentication attempts through registered providers
 * 2. Managing SecurityContext for successful authentications
 * 3. Providing detailed audit logging for security monitoring
 * 4. Handling authentication failures with proper HTTP responses
 * 
 * 🔧 ARCHITECTURE PATTERN:
 * This filter implements the Chain of Responsibility pattern, allowing
 * multiple authentication mechanisms to be tried in sequence until one
 * succeeds or all fail.
 * 
 * 🎯 AUTHENTICATION FLOW:
 * 1. Check if request is for external API (/api/ext/**)
 * 2. Try each registered authentication provider in order
 * 3. Set SecurityContext for first successful authentication
 * 4. Return 401 Unauthorized if no valid credentials provided
 * 5. Continue filter chain only for authenticated requests
 * 
 * 🔧 PROVIDER REGISTRATION:
 * Providers are auto-discovered via Spring's @Component scanning:
 * - ApiKeyAuthenticationProvider (X-API-Key header)
 * - HmacAuthenticationProvider (HMAC signatures)
 * - OAuth2ClientCredentialsProvider (OAuth2 client credentials)
 * - MtlsAuthenticationProvider (Mutual TLS certificates)
 * 
 * 📊 AUDIT LOGGING:
 * - Successful authentications: DEBUG level with provider details
 * - Failed attempts: DEBUG level with request details (excluding sensitive data)
 * - Authentication errors: WARN level for security monitoring
 * 
 * 🚨 SECURITY CONSIDERATIONS:
 * - Filter blocks unauthenticated requests to /api/ext/** paths
 * - Returns 401 Unauthorized with JSON error response
 * - Authentication state is cleared between requests
 * - Sensitive data (keys, tokens) never logged
 * 
 * 📝 INTEGRATION EXAMPLE:
 * ```java
 * // Custom authentication provider
 * @Component
 * public class CustomAuthProvider implements ExternalApiAuthenticationProvider {
 *     @Override
 *     public Authentication authenticate(HttpServletRequest request) {
 *         // Your authentication logic
 *         return createAuthentication();
 *     }
 * }
 * ```
 * 
 * @author Service Team
 * @version 2.1 - Production-Ready Implementation
 * @since 2025.1
 */
@Component
@ConditionalOnProperty(value = "app.security.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ExternalApiAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(ExternalApiAuthenticationFilter.class);
    
    // ═══════════════════════════════════════════════════════════════
    // 🔧 DEPENDENCIES & CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Auto-discovered authentication providers.
     * Spring automatically injects all beans implementing ExternalApiAuthenticationProvider.
     */
    private final List<ExternalApiAuthenticationProvider> authenticationProviders;
    
    // ═══════════════════════════════════════════════════════════════
    // 🔐 FILTER IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════════
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Only process requests to external API endpoints
        if (!requestPath.startsWith("/api/ext/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        log.debug("🔍 Processing external API authentication for request: {} {}", 
            request.getMethod(), requestPath);
        
        Authentication authentication = attemptAuthentication(request);
        
        if (authentication != null) {
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("✅ External API authentication successful for user: {} via mechanism: {}", 
                authentication.getName(), getAuthenticationMechanism(authentication));
            
            // Continue filter chain for authenticated requests
            filterChain.doFilter(request, response);
        } else {
            // No authentication provided or authentication failed
            log.debug("🚫 External API authentication failed for request: {} {}", 
                request.getMethod(), requestPath);
            
            // Return structured error response
            sendUnauthorizedResponse(response);
        }
    }
    
    // ═══════════════════════════════════════════════════════════════
    // 🔍 PRIVATE HELPER METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Attempts authentication using all registered providers.
     * 
     * Uses Chain of Responsibility pattern - tries each provider until
     * one succeeds or all fail.
     * 
     * @param request the HTTP request to authenticate
     * @return Authentication object if successful, null if no provider can authenticate
     */
    private Authentication attemptAuthentication(HttpServletRequest request) {
        for (ExternalApiAuthenticationProvider provider : authenticationProviders) {
            try {
                if (provider.supports(request)) {
                    log.debug("🔑 Attempting authentication with provider: {}", 
                        provider.getAuthenticationMechanism());
                    
                    Authentication authentication = provider.authenticate(request);
                    if (authentication != null) {
                        log.debug("✅ Authentication successful with provider: {}", 
                            provider.getAuthenticationMechanism());
                        return authentication;
                    }
                }
            } catch (Exception e) {
                log.warn("⚠️ Authentication provider {} failed for request to {}: {}", 
                           provider.getAuthenticationMechanism(), 
                           request.getRequestURI(), 
                           e.getMessage());
                // Continue trying other providers
            }
        }
        
        return null;
    }
    
    /**
     * Extracts the authentication mechanism from the Authentication object.
     * 
     * @param authentication the Authentication object
     * @return the authentication mechanism name, or "UNKNOWN" if not determinable
     */
    private String getAuthenticationMechanism(Authentication authentication) {
        // Try to determine mechanism from authentication details
        if (authentication.getDetails() instanceof String details) {
            return details;
        }
        
        // Fallback to generic description
        return authentication.getClass().getSimpleName();
    }
    
    /**
     * Sends a structured 401 Unauthorized response for failed authentication.
     * 
     * @param response the HTTP response object
     * @throws IOException if writing the response fails
     */
    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String errorResponse = """
            {
                "error": "Unauthorized",
                "message": "Valid authentication credentials required for external API access",
                "timestamp": "%s",
                "status": 401
            }
            """.formatted(java.time.Instant.now().toString());
        
        response.getWriter().write(errorResponse);
    }
}
