package com.itt.service.config.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.itt.service.exception.CustomException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * ✅ PRODUCTION-READY: Auth0 JWT Authentication Filter
 * ===================================================
 * 
 * Custom servlet filter for Auth0 JWT token processing in the Service's
 * multi-chain security architecture. This filter specifically handles: 1. Auth0
 * JWT token validation and processing 2. Spring Security Authentication object
 * creation 3. SecurityContext population for authenticated users 4. Graceful
 * error handling with global exception integration
 * 
 * 🔧 ARCHITECTURE POSITION: This filter operates within the security filter
 * chain configured in SecurityConfiguration. It's specifically designed for the
 * JWT authentication chain that processes requests with Auth0 JWT tokens in the
 * Authorization header.
 * 
 * 🎯 AUTHENTICATION FLOW: 1. Receive HTTP request with potential Auth0 JWT
 * token 2. Delegate token processing to Auth0TokenProcessor 3. Set Spring
 * Security Authentication in SecurityContext 4. Handle authentication errors
 * gracefully 5. Continue filter chain regardless of authentication result 6.
 * Let SecurityConfiguration handle authorization decisions
 * 
 * 🔧 ERROR HANDLING STRATEGY: - CustomException: Log with error code and
 * continue filter chain - Generic Exception: Log error and clear
 * SecurityContext - No authentication: Continue without setting authentication
 * - SecurityConfiguration handles final authorization decisions
 * 
 * 📊 AUDIT LOGGING: - Successful authentications: DEBUG level with user
 * identification - Authentication failures: WARN level with error codes -
 * Unexpected errors: ERROR level with full stack traces - No authentication:
 * DEBUG level to avoid log noise
 * 
 * 🚨 SECURITY CONSIDERATIONS: - Filter does NOT block requests on
 * authentication failure - SecurityContext is cleared on authentication errors
 * - Global exception handling ensures consistent error responses - All
 * sensitive data handled by Auth0TokenProcessor
 * 
 * 📝 INTEGRATION EXAMPLE: ```java // In SecurityConfiguration.java
 * 
 * @Bean public SecurityFilterChain jwtFilterChain() { return http
 *       .addFilterBefore(auth0JwtAuthFilter,
 *       UsernamePasswordAuthenticationFilter.class) .build(); } ```
 * 
 * @author Service Team
 * @version 2.1 - Production-Ready Implementation with Enhanced Error Handling
 * @since 2025.1 (Modernized with SLF4J logging, global exception handling)
 */
@Component
@ConditionalOnProperty(value = "app.security.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class Auth0JwtAuthFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(Auth0JwtAuthFilter.class);

	// ═══════════════════════════════════════════════════════════════
	// 🔧 DEPENDENCIES & CONFIGURATION
	// ═══════════════════════════════════════════════════════════════

	/**
	 * Auth0 token processor for JWT validation and authentication creation. Handles
	 * all Auth0-specific token processing logic.
	 */
	private final Auth0TokenProcessor auth0TokenProcessor;

	// ═══════════════════════════════════════════════════════════════
	// 🔐 FILTER IMPLEMENTATION
	// ═══════════════════════════════════════════════════════════════

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String requestPath = request.getRequestURI();
		String requestMethod = request.getMethod();

		try {
			log.debug("Processing Auth0 JWT authentication for request: {} {}", requestMethod, requestPath);

			// Attempt authentication through Auth0TokenProcessor
			Authentication authentication = auth0TokenProcessor.authenticate(request);

			if (authentication != null) {
				// Set authentication in SecurityContext
				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.debug("Auth0 authentication successful - User: {}, Path: {}", authentication.getName(),
						requestPath);
			} else {
				log.debug("🔓 No Auth0 authentication provided for request: {} {}", requestMethod, requestPath);
			}

		} catch (CustomException ex) {
			// Handle known authentication errors with proper logging
			log.warn("Auth0 authentication failed - Error: {} ({}), Message: {}, Path: {} {}", ex.getErrorCode().name(),
					ex.getErrorCode().getCode(), ex.getUserMessage(), requestMethod, requestPath);

			// Clear security context for failed authentication
			SecurityContextHolder.clearContext();

			// Continue with filter chain - SecurityConfiguration handles authorization

		} catch (Exception ex) {
			// Handle unexpected errors
			log.error("Unexpected Auth0 authentication error for request: {} {} - Error: {}", requestMethod,
					requestPath, ex.getMessage(), ex);

			// Clear security context for unexpected errors
			SecurityContextHolder.clearContext();
		}

		// Always continue filter chain regardless of authentication result
		// SecurityConfiguration will handle authorization decisions
		filterChain.doFilter(request, response);
	}
}