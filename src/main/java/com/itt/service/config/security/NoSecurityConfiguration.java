package com.itt.service.config.security;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import com.itt.service.dto.CurrentUserDto;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ✅ DEVELOPMENT: No Security Configuration
 * =========================================
 * 
 * This configuration is activated when security is disabled via:
 * app.security.enabled=false
 * 
 * 🎯 PURPOSE: - Provides a development-friendly environment with no
 * authentication - Automatically injects a mock user with all required roles -
 * Useful for local development, testing, and debugging
 * 
 * 🚨 SECURITY WARNING: - This configuration bypasses ALL security checks -
 * Should NEVER be used in production environments - All endpoints become
 * publicly accessible
 * 
 * 🔧 MOCK USER DETAILS: - Email: system@itt.com - Name: System - Roles: All
 * available roles (see ALL_ROLES set)
 * 
 * ⭐ DEVELOPER TIPS: - Enable this for rapid API testing without auth setup -
 * Disable when testing security features - Use SecurityConfiguration for
 * production deployments
 * 
 * @author Service Team
 * @version 2.1 - Enhanced Documentation
 * @since 2025.1
 */
@Configuration
@ConditionalOnProperty(value = "app.security.enabled", havingValue = "false")
@EnableMethodSecurity // <-- uncomment when you want @PreAuthorize enforced
public class NoSecurityConfiguration {

	// ═══════════════════════════════════════════════════════════════
	// 🎭 MOCK USER CONFIGURATION - All roles for unrestricted access
	// ═══════════════════════════════════════════════════════════════

	/**
	 * ✅ All available roles in the system. Mock user is granted ALL roles for
	 * development convenience.
	 */
	private static final Set<String> ALL_ROLES = Set.of("ROLE_HOME_PAGE_VIEW", "ROLE_ROLE_MANAGEMENT_VIEW","ROLE_ROLE_MANAGEMENT_EDIT",
			"ROLE_USER_MANAGEMENT_VIEW", "ROLE_USER_MANAGEMENT_EDIT", "ROLE_MASTER_DATA_VIEW",
			"ROLE_SUBSCRIPTION_MANAGEMENT_VIEW", "ROLE_SUBSCRIPTION_MANAGEMENT_EDIT", "ROLE_PETA_PETD_MANAGEMENT_VIEW",
			"ROLE_PETA_PETD_MANAGEMENT_EDIT", "ROLE_LIT_VIEW", "ROLE_LIT_EDIT");

	/**
	 * ✅ Mock user credentials for development.
	 */
	private static final String DEV_EMAIL = "system@itt.com";
	private static final String DEV_NAME = "System";

	/**
	 * ✅ MAIN SECURITY CHAIN: Disables all security constraints.
	 * 
	 * Configures Spring Security to: - Allow all requests without authentication -
	 * Disable CSRF protection - Disable frame options for H2 console access -
	 * Inject mock user into all requests
	 */
	@Bean
	SecurityFilterChain noSecurityFilterChain(HttpSecurity http, OncePerRequestFilter devUserInjectionFilter)
			throws Exception {
		return http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.headers(h -> h.frameOptions(f -> f.disable()))
				.addFilterBefore(devUserInjectionFilter, UsernamePasswordAuthenticationFilter.class).build();
	}

	/**
	 * ✅ MOCK USER INJECTION FILTER: Automatically authenticates all requests.
	 * 
	 * This filter runs for every request and: - Creates a mock authenticated user
	 * with all roles - Sets the SecurityContext for Spring Security - Provides
	 * currentUser attribute for request processing
	 */
	@Bean
	OncePerRequestFilter devUserInjectionFilter() {
		var authorities = ALL_ROLES.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableSet());

		return new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
					FilterChain filterChain) throws ServletException, IOException {

				// Create mock authenticated user if not already authenticated
				Authentication current = org.springframework.security.core.context.SecurityContextHolder.getContext()
						.getAuthentication();

				if (current == null || !current.isAuthenticated()) {
					var principal = new User(DEV_EMAIL, "N/A", authorities);
					var auth = new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
					org.springframework.security.core.context.SecurityContextHolder.getContext()
							.setAuthentication(auth);
				}

				// Provide currentUser attribute for controllers
				if (request.getAttribute("currentUser") == null) {
					request.setAttribute("currentUser", new CurrentUserDto(1, DEV_EMAIL, DEV_NAME));
				}

				filterChain.doFilter(request, response);
			}
		};
	}

	/**
	 * ✅ PASSWORD ENCODER: Required bean for security context.
	 * 
	 * Provides BCrypt password encoder when main SecurityConfiguration is not
	 * active (when security is disabled).
	 */
	@Bean
	@ConditionalOnMissingBean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}