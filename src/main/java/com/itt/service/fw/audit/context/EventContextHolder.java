package com.itt.service.fw.audit.context;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Provides utility methods to extract user-specific context from the current request.
 */
public final class EventContextHolder {

    private EventContextHolder() {
        // utility class, prevent instantiation
    }

    /**
     * Returns the current authenticated user's username.
     * If the user is not authenticated, returns "anonymous".
     */
    public static String getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    if (principal instanceof UserDetails userDetails) {
                        return userDetails.getUsername();
                    } else {
                        return principal.toString();
                    }
                })
                .orElse("anonymous");
    }

    /**
     * Returns the current user's roles (comma-separated) if available.
     */
    public static String getCurrentUserRoles() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(auth -> auth.getAuthorities().stream()
                        .map(grantedAuthority -> grantedAuthority.getAuthority())
                        .reduce((a, b) -> a + "," + b)
                        .orElse("none"))
                .orElse("anonymous");
    }
}

