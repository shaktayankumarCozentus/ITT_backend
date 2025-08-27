package com.itt.service.config.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Custom authentication token that wraps Spring Security's Jwt object.
 * Modernized implementation using Spring Security OAuth2 JWT instead of
 * deprecated Nimbus library.
 * 
 * @since 2025.1 (Modernized from Nimbus JWTClaimsSet to Spring Security Jwt)
 */
public class JwtAuthentication extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;

	private final Object principal;
	private final Jwt jwt;

	/**
	 * Creates a new JwtAuthentication instance.
	 * 
	 * @param principal the principal (usually the user)
	 * @param jwt the JWT token
	 * @param authorities the granted authorities
	 */
	public JwtAuthentication(Object principal, Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.principal = principal;
		this.jwt = jwt;
		super.setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return this.principal;
	}

	/**
	 * Returns all claims from the JWT token.
	 * 
	 * @return a map containing all JWT claims
	 */
	public Map<String, Object> getClaims() {
		return this.jwt.getClaims();
	}
}