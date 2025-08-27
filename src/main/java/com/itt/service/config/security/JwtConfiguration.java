package com.itt.service.config.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * ✅ SIMPLIFIED: JWT Authentication Configuration
 * ===============================================
 * 
 * Provides configuration constants for JWT authentication headers.
 * 
 * 🎯 DEVELOPER USAGE: - Standard "Authorization" header for JWT tokens - Used
 * by Auth0TokenProcessor for consistent header handling
 * 
 * ⭐ NOTE: Simplified from complex configuration to constant values for better
 * maintainability and developer understanding.
 * 
 * @author Service Team
 * @version 2.1 - Simplified Configuration
 * @since 2025.1
 */
@Component
@ConditionalOnProperty(value = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class JwtConfiguration {

	/**
	 * Standard HTTP header for JWT token transmission. Uses the RFC 6750 Bearer
	 * Token specification.
	 */
	public static final String JWT_HEADER = "Authorization";

	/**
	 * Gets the HTTP header name used for JWT token transmission.
	 * 
	 * @return the HTTP header name ("Authorization")
	 */
	public String getHttpHeader() {
		return JWT_HEADER;
	}
}
