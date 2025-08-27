package com.itt.service.config.security;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.itt.service.config.AwsSecrets;
import com.itt.service.config.security.external.ExternalApiAuthenticationFilter;

/**
 * Security Configuration for Service
 * 
 * Tri-chain security architecture: 1. PUBLIC ENDPOINTS - No authentication
 * required 2. EXTERNAL API - External authentication (API keys, HMAC, mTLS,
 * etc.) 3. INTERNAL API - JWT-based authentication 4. FALLBACK - Deny all
 * unmatched requests
 * 
 * @author Service Team
 * @version 2.0
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(value = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfiguration {

	private static final Logger log = LoggerFactory.getLogger(SecurityConfiguration.class);

	// ===================================================================================
	// CONFIGURATION CONSTANTS
	// ===================================================================================

	/**
	 * Public endpoints that require no authentication.
	 */
	private static final String[] PUBLIC_ENDPOINTS = { "/api/public/**", "/api/auth/**", "/api/health/**",
			"/api/docs/**" };

	/**
	 * External API endpoints that require external authentication.
	 */
	private static final String EXTERNAL_API_PATTERN = "/api/ext/**";

	/**
	 * Internal API endpoints that require JWT authentication.
	 */
	private static final String INTERNAL_API_PATTERN = "/api/v1/**";

	// ===================================================================================
	// DEPENDENCY INJECTION
	// ===================================================================================

	private final Auth0JwtAuthFilter auth0JwtAuthFilter;
	private final ExternalApiAuthenticationFilter externalApiAuthenticationFilter;
	private final com.itt.service.fw.ratelimit.filter.ExternalApiRateLimitFilter externalApiRateLimitFilter;
	private final AwsSecrets awsSecrets;

	public SecurityConfiguration(Auth0JwtAuthFilter auth0JwtAuthFilter,
			ExternalApiAuthenticationFilter externalApiAuthenticationFilter,
			com.itt.service.fw.ratelimit.filter.ExternalApiRateLimitFilter externalApiRateLimitFilter,
			AwsSecrets awsSecrets) {
		this.auth0JwtAuthFilter = auth0JwtAuthFilter;
		this.externalApiAuthenticationFilter = externalApiAuthenticationFilter;
		this.externalApiRateLimitFilter = externalApiRateLimitFilter;
		this.awsSecrets = awsSecrets;
	}

	// ===================================================================================
	// SECURITY FILTER CHAINS
	// ===================================================================================

	/**
	 * Chain 1: Public endpoints - no authentication required
	 */
	@Bean
	@Order(1)
	SecurityFilterChain publicEndpointsFilterChain(HttpSecurity http) throws Exception {
		return http.securityMatcher(PUBLIC_ENDPOINTS).cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).build();
	}

	/**
	 * Chain 2: External API endpoints - external authentication required
	 */
	@Bean
	@Order(2)
	SecurityFilterChain externalApiFilterChain(HttpSecurity http) throws Exception {
		return http.securityMatcher(EXTERNAL_API_PATTERN)
				.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				// Rate limiting filter executes before authentication
				.addFilterBefore(externalApiRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
				// Authentication filter executes after rate limiting
				.addFilterAfter(externalApiAuthenticationFilter, externalApiRateLimitFilter.getClass()).build();
	}

	/**
	 * Chain 3: Internal API endpoints - JWT authentication required
	 */
	@Bean
	@Order(3)
	SecurityFilterChain internalApiFilterChain(HttpSecurity http) throws Exception {
		return http.securityMatcher(INTERNAL_API_PATTERN)
				.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				.addFilterBefore(auth0JwtAuthFilter, UsernamePasswordAuthenticationFilter.class).build();
	}

	/**
	 * Chain 4: Fallback - deny all unmatched requests
	 */
	@Bean
	@Order(4)
	SecurityFilterChain fallbackFilterChain(HttpSecurity http) throws Exception {
		return http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth.anyRequest().denyAll()).build();
	}

	// ===================================================================================
	// CORS & JWT CONFIGURATION
	// ===================================================================================

	/**
	 * Simple CORS Configuration - production-ready
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		var configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of("*"));
		configuration.setAllowedMethods(List.of("*"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(false); // Secure for production

		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	/**
	 * JWT Decoder Configuration
	 */
	@Bean
	JwtDecoder jwtDecoder(RestTemplate restTemplate) {
		// Create JWT decoder from OIDC issuer but configure it to preserve all claims
		NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders
				.fromOidcIssuerLocation(awsSecrets.getOauth2JwtIssuerUri());

		// Override the claim set converter to preserve ALL claims including custom ones
		// like 'sid'
		// This ensures that non-standard claims are not filtered out by the OIDC claim
		// processor
		jwtDecoder.setClaimSetConverter(claims -> {
			// Log all original claims for debugging (using INFO to ensure visibility)
			log.info("🔍 JWT Decoder - Original claims from token: {}", claims);

			// Convert claims and handle timestamp conversion from Date to Instant using
			// modern Java features
			var convertedClaims = new HashMap<>(claims);

			// Convert Date objects to Instant for timestamp claims using enhanced iteration
			convertedClaims.entrySet().forEach(entry -> {
				if (entry.getValue() instanceof java.util.Date date) {
					// Use pattern matching for instanceof (Java 16+)
					entry.setValue(date.toInstant());
					log.debug("🔧 JWT Decoder - Converted timestamp claim '{}' from Date to Instant", entry.getKey());
				}
			});

			log.info("✅ JWT Decoder - Converted claims: {}", convertedClaims);

			return convertedClaims;
		});

		// Configure standard issuer validation
		OAuth2TokenValidator<Jwt> withIssuer = JwtValidators
				.createDefaultWithIssuer(awsSecrets.getOauth2JwtIssuerUri());

		// Add custom token session validation
		OAuth2TokenValidator<Jwt> tokenAssociatedSessionValidator = new TokenAssociatedSessionValidator(awsSecrets,
				restTemplate);

		// Combine validators
		OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(withIssuer,
				tokenAssociatedSessionValidator);

		jwtDecoder.setJwtValidator(combinedValidator);
		return jwtDecoder;
	}

	/**
	 * REST Template Bean
	 */
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * Password Encoder Bean Used for encoding passwords in user authentication
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}