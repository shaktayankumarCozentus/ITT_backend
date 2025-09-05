package com.itt.service.fw.ratelimit.utility;

import com.itt.service.enums.ErrorCode;
import com.itt.service.exception.CustomException;
import com.itt.service.repository.MasterUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class dedicated to provide authenticated user's ID as stored in the
 * DataSource in UUID format which uniquely identifies the user in the system.
 * This is fetched from the principal in security context, where it is stored in
 * by the {@link com.itt.service.config.security.Auth0JwtAuthFilter} during HTTP
 * request evaluation through the filter chain.
 *
 * @see com.itt.service.config.security.Auth0JwtAuthFilter
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticatedUserIdProvider {

	private final MasterUserRepository masterUserRepository;

	/**
	 * Retrieves ID corresponding to the authenticated user from the security
	 * context.
	 *
	 * @return Unique ID (Integer formatted) corresponding to the authenticated user.
	 * @throws IllegalStateException if the method is invoked when a request was
	 *                               destined to a public API endpoint and did not pass
	 *                               the JwtAuthenticationFilter
	 */

	public Integer getCurrentUserId() {
		// Extract username from SecurityContext
		String userName = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
				.map(Authentication::getPrincipal)
				.filter(User.class::isInstance)
				.map(User.class::cast)
				.map(User::getUsername)
				.orElseThrow(() -> new IllegalStateException("⚠️ No authenticated user found"));

		// Fetch userId from DB using repository
		Integer userId = masterUserRepository.findUserIdByEmail(userName);
		if (userId == null) {
			log.warn("⚠️ User ID not found for email: {}", userName);
			throw new CustomException(ErrorCode.USER_NOT_FOUND, "User not found in system");
		}

		return userId;
	}

	/**
	 * Checks whether the security context is populated with a valid authentication
	 * object.
	 *
	 * @return {@code true} if an authentication context is available, {@code false}
	 * otherwise.
	 */
	public boolean isAvailable() {
		final var authentication = SecurityContextHolder.getContext().getAuthentication();
		return Optional.ofNullable(authentication).isPresent();
	}

}