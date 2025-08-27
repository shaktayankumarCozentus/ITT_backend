package com.itt.service.fw.ratelimit.configuration;

/**
 * ❌ DISABLED: RateLimitSecurityConfiguration
 * ==========================================
 * 
 * This configuration has been disabled to avoid conflicts with the main 
 * SecurityConfiguration.java tri-chain architecture which already handles:
 * 
 * 1. Public endpoints (/api/public/**, /api/auth/**, /api/health/**, /api/docs/**)
 * 2. External API endpoints (/api/ext/**) with rate limiting  
 * 3. Internal API endpoints (/api/v1/**) with JWT authentication
 * 4. Fallback chain (deny all unmatched)
 * 
 * The rate limiting functionality is properly integrated in SecurityConfiguration.java
 * via ExternalApiRateLimitFilter and standard RateLimitFilter.
 * 
 * @deprecated Use SecurityConfiguration.java instead
 * @see com.itt.service.config.security.SecurityConfiguration
 */

/*
// DISABLED TO AVOID SPRING SECURITY FILTER CHAIN CONFLICTS

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.itt.service.fw.ratelimit.filter.JwtAuthenticationFilter;
import com.itt.service.fw.ratelimit.filter.RateLimitFilter;
import com.itt.service.fw.ratelimit.utility.ApiEndpointSecurityInspector;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

// @Configuration  // DISABLED: Using SecurityConfiguration.java tri-chain architecture instead
@RequiredArgsConstructor
// @ConditionalOnProperty(value = "app.security.enabled", havingValue = "true", matchIfMissing = true)  // DISABLED
public class RateLimitSecurityConfiguration {

	private final RateLimitFilter rateLimitFilter;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ApiEndpointSecurityInspector apiEndpointSecurityInspector;

	@Value("${server.servlet.context-path:}")
	private String contextPath;

	@Bean
	@SneakyThrows
	public SecurityFilterChain rateLimitSecurityFilterChain(final HttpSecurity http) {
		http
				// Limit this chain to API v1 namespace so it doesn't consume 'any request'
				.securityMatcher("/api/v1/**").cors(c -> c.configurationSource(corsConfigurationSource()))
				.csrf(cs -> cs.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						// Adjust granular rules here if needed; currently allow all (rate limiting can
						// still apply)
						.anyRequest().permitAll());
		// Enable when auth/rate limit filters are ready
		// .addFilterBefore(jwtAuthenticationFilter,
		// UsernamePasswordAuthenticationFilter.class)
		// .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(List.of("*"));
		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(List.of("Authorization", "Origin", "Content-Type", "Accept"));
		cfg.setExposedHeaders(List.of("Content-Type", "X-Rate-Limit-Retry-After-Seconds", "X-Rate-Limit-Retry-After",
				"X-Rate-Limit-Remaining"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}
}

*/