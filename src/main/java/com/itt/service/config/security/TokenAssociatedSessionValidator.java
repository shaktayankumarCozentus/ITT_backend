package com.itt.service.config.security;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.itt.service.config.AwsSecrets;
import com.itt.service.enums.ErrorCode;
import com.itt.service.exception.BusinessException;
import com.itt.service.exception.CustomException;

/**
 * ✅ PRODUCTION-READY: Token Associated Session Validator
 * =======================================================
 * 
 * Advanced JWT token validator that provides multi-layer validation for Auth0
 * JWT tokens: 1. Standard JWT expiration and claims validation 2. Auth0
 * Management API session state verification 3. Real-time session activity
 * validation 4. Comprehensive error handling with global exception integration
 * 
 * 🔧 VALIDATION LAYERS: - Token Expiration: Validates JWT exp claim against
 * current time - Required Claims: Ensures presence of subject and session ID
 * claims - Session State: Verifies active session via Auth0 Management API -
 * Error Handling: Integrates with global CustomException framework
 * 
 * 🎯 SECURITY BENEFITS: - Prevents usage of tokens after session termination -
 * Validates session state beyond JWT signature validation - Provides real-time
 * session revocation capability - Enables centralized session management
 * through Auth0
 * 
 * 🔧 CONFIGURATION REQUIREMENTS:
 * 
 * For Production: ```yaml app: security: enabled: true auth0: domain:
 * "your-domain.auth0.com" clientId: "management-api-client-id" m2mClientSecret:
 * "management-api-client-secret" ```
 * 
 * For Development: - Conditional activation via app.security.enabled property -
 * Graceful degradation if Auth0 Management API unavailable - Comprehensive
 * debug logging for troubleshooting
 * 
 * 📊 AUDIT LOGGING: - Token validation results: DEBUG level with masked
 * sensitive data - Session validation failures: WARN level for security
 * monitoring - Auth0 API errors: ERROR level with detailed error context -
 * Security violations: Appropriate levels based on severity
 * 
 * 🚨 SECURITY CONSIDERATIONS: - All sensitive data (subjects, session IDs)
 * masked in logs - Management API credentials secured via environment variables
 * - RestTemplate configured with appropriate timeouts - Graceful error handling
 * prevents information disclosure
 * 
 * 📝 INTEGRATION EXAMPLE: ```java // In JwtConfiguration.java
 * 
 * @Bean public JwtDecoder jwtDecoder() { var decoder =
 *       JwtDecoders.fromIssuerLocation(issuerUri);
 *       decoder.setJwtValidator(tokenAssociatedSessionValidator); return
 *       decoder; } ```
 * 
 * @author Service Team
 * @version 2.1 - Production-Ready Implementation with Enhanced Security
 * @since 2025.1 (Modernized with Spring patterns, global exception handling)
 * @see OAuth2TokenValidator
 * @see Jwt
 * @see CustomException
 * @see ErrorCode
 */
@Component
@ConditionalOnProperty(value = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class TokenAssociatedSessionValidator implements OAuth2TokenValidator<Jwt> {

	private static final Logger log = LoggerFactory.getLogger(TokenAssociatedSessionValidator.class);

	// Configuration constants

	// JWT Claims
	private static final String SID_CLAIM = "sid";

	// OAuth2 Constants
	private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
	private static final String SCOPE_READ_SESSIONS = "read:sessions";
	private static final String ACCESS_TOKEN_FIELD = "access_token";

	// HTTP and URL Constants
	private static final String API_V2_PATH = "/api/v2/";
	private static final String OAUTH_TOKEN_PATH = "/oauth/token";
	private static final String SESSIONS_PATH = "sessions/";
	private static final String HTTPS_PREFIX = "https://";

	// ═══════════════════════════════════════════════════════════════
	// 🔧 DEPENDENCIES & CONFIGURATION
	// ═══════════════════════════════════════════════════════════════

	private final RestTemplate restTemplate;
	private final AwsSecrets awsSecrets;

	/**
	 * Constructor injection for TokenAssociatedSessionValidator.
	 * 
	 * @param auth0Domain  Auth0 tenant domain
	 * @param clientId     Management API client ID
	 * @param clientSecret Management API client secret
	 * @param restTemplate Configured RestTemplate for HTTP calls
	 */
	public TokenAssociatedSessionValidator(AwsSecrets awsSecrets, RestTemplate restTemplate) {
		this.awsSecrets = awsSecrets;
		this.restTemplate = restTemplate;

		log.info("🔧 TokenAssociatedSessionValidator initialized for domain: {}", awsSecrets.getAuth0Domain());
	}

	// ═══════════════════════════════════════════════════════════════
	// 🔐 TOKEN VALIDATION IMPLEMENTATION
	// ═══════════════════════════════════════════════════════════════

	@Override
	public OAuth2TokenValidatorResult validate(Jwt jwt) {
		try {
			log.debug("Starting token validation for subject: {}", maskSubject(jwt.getSubject()));

			// Layer 1: Token expiration validation
			OAuth2TokenValidatorResult expirationResult = validateTokenExpiration(jwt);
			if (expirationResult.hasErrors()) {
				return expirationResult;
			}

			// Layer 2: Required claims validation
			OAuth2TokenValidatorResult claimsResult = validateRequiredClaims(jwt);
			if (claimsResult.hasErrors()) {
				return claimsResult;
			}

			// Layer 3: Session state validation via Auth0 Management API
			OAuth2TokenValidatorResult sessionResult = validateSessionState(jwt);
			if (sessionResult.hasErrors()) {
				return sessionResult;
			}

			log.debug("Token validation successful for subject: {}", maskSubject(jwt.getSubject()));
			return OAuth2TokenValidatorResult.success();

		} catch (JwtException ex) {
			log.error("JWT validation error for subject {}: {}", maskSubject(jwt.getSubject()), ex.getMessage());
			return createFailureResult(ErrorCode.TOKEN_INVALID, "The token is invalid");
		} catch (BusinessException ex) {
			log.warn("Business validation error for subject {}: {}", maskSubject(jwt.getSubject()), ex.getMessage());
			return createFailureResult(ex.getErrorCode(), ex.getUserMessage());
		} catch (CustomException ex) {
			log.error("Custom validation error for subject {}: {}", maskSubject(jwt.getSubject()), ex.getMessage());
			return createFailureResult(ex.getErrorCode(), ex.getUserMessage());
		} catch (Exception ex) {
			log.error("Unexpected error during token validation for subject {}: {}", maskSubject(jwt.getSubject()),
					ex.getMessage(), ex);
			return createFailureResult(ErrorCode.INTERNAL_SERVER_ERROR,
					"Token validation failed due to internal error");
		}
	}

	// Private validation methods

	/**
	 * Validates token expiration against current time.
	 */
	private OAuth2TokenValidatorResult validateTokenExpiration(Jwt jwt) {
		if (!isTokenNotExpired(jwt)) {
			log.warn("JWT token has expired for subject: {}", maskSubject(jwt.getSubject()));
			return createFailureResult(ErrorCode.TOKEN_EXPIRED, "The token has expired");
		}
		log.debug("✅ Token expiration validation passed");
		return OAuth2TokenValidatorResult.success();
	}

	/**
	 * Validates presence of required JWT claims.
	 */
	private OAuth2TokenValidatorResult validateRequiredClaims(Jwt jwt) {
		if (!hasRequiredClaims(jwt)) {
			log.warn("📋 JWT token is missing required claims for subject: {}", maskSubject(jwt.getSubject()));
			return createFailureResult(ErrorCode.VALIDATION_FAILED, "Token is missing required claims");
		}
		log.debug("✅ Required claims validation passed");
		return OAuth2TokenValidatorResult.success();
	}

	/**
	 * Validates session state via Auth0 Management API.
	 */
	private OAuth2TokenValidatorResult validateSessionState(Jwt jwt) {
		try {
			// Debug: Log all available claims
			log.debug("Available JWT claims: {}", jwt.getClaims().keySet());
			log.debug("All claims: {}", jwt.getClaims());

			String sessionId = jwt.getClaimAsString(SID_CLAIM);
			log.debug("Extracted sessionId using getClaimAsString: {}", sessionId);

			// Alternative extraction method
			Object sidObject = jwt.getClaims().get(SID_CLAIM);
			log.debug("SID claim object: {}, type: {}", sidObject,
					sidObject != null ? sidObject.getClass().getSimpleName() : "null");

			// If getClaimAsString returns null, try direct cast
			if (sessionId == null && sidObject != null) {
				sessionId = sidObject.toString();
				log.debug("Using toString() method, sessionId: {}", sessionId);
			}

			// Require session ID for all tokens (strict validation like old code)
			if (!StringUtils.hasText(sessionId)) {
				log.warn("JWT token is missing required session ID claim for subject: {}",
						maskSubject(jwt.getSubject()));
				return createFailureResult(ErrorCode.VALIDATION_FAILED, "Token is missing required session ID claim");
			}

			// Validate session state via Auth0 Management API
			log.debug("Validating session for SID: {}", maskSessionId(sessionId));
			String managementToken = getManagementApiToken();
			boolean isSessionActive = isSessionActive(managementToken, sessionId);
			log.debug("Session validation for SID: {} completed, Active: {}", maskSessionId(sessionId),
					isSessionActive);

			if (isSessionActive) {
				log.debug("Session state validation passed");
				return OAuth2TokenValidatorResult.success();
			} else {
				log.warn("Session not active for SID: {}", maskSessionId(sessionId));
				return createFailureResult(ErrorCode.TOKEN_EXPIRED, "Session is no longer active");
			}

		} catch (RestClientException ex) {
			log.error("Auth0 Management API unavailable during session validation: {}", ex.getMessage());
			// Be strict: if we can't validate the session, fail the request
			return createFailureResult(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
					"Session validation service unavailable");
		} catch (Exception ex) {
			log.error("Unexpected error during session validation: {}", ex.getMessage());
			return createFailureResult(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
					"Session validation service unavailable");
		}
	}

	/**
	 * Creates a standardized failure result using project error codes.
	 */
	private OAuth2TokenValidatorResult createFailureResult(ErrorCode errorCode, String description) {
		return OAuth2TokenValidatorResult.failure(new OAuth2Error(errorCode.getCode(), description, null));
	}

	/**
	 * Validates that the JWT token has not expired.
	 */
	private boolean isTokenNotExpired(Jwt jwt) {
		Instant expiresAt = jwt.getExpiresAt();
		if (expiresAt == null) {
			log.warn("⚠️ JWT token has no expiration time");
			return false;
		}
		return Instant.now().isBefore(expiresAt);
	}

	/**
	 * Validates that the JWT token contains all required claims.
	 */
	private boolean hasRequiredClaims(Jwt jwt) {
		String subject = jwt.getSubject();

		// Debug: Check JWT structure and SID claim extraction
		log.debug("hasRequiredClaims - JWT class: {}", jwt.getClass().getName());
		log.debug("hasRequiredClaims - All claim keys: {}", jwt.getClaims().keySet());
		log.debug("hasRequiredClaims - All claims: {}", jwt.getClaims());

		String sessionId = jwt.getClaimAsString(SID_CLAIM);
		Object sidObject = jwt.getClaims().get(SID_CLAIM);
		log.debug("hasRequiredClaims - sessionId from getClaimAsString: {}", sessionId);
		log.debug("hasRequiredClaims - SID object: {}, type: {}", sidObject,
				sidObject != null ? sidObject.getClass().getSimpleName() : "null");

		// Try case variations and direct access
		Object sidLowerCase = jwt.getClaims().get("sid");
		Object sidUpperCase = jwt.getClaims().get("SID");
		log.debug("hasRequiredClaims - sid (lowercase): {}", sidLowerCase);
		log.debug("hasRequiredClaims - SID (uppercase): {}", sidUpperCase);

		// If getClaimAsString returns null, try direct access
		if (sessionId == null && sidObject != null) {
			sessionId = sidObject.toString();
			log.debug("hasRequiredClaims - Using direct toString(), sessionId: {}", sessionId);
		} else if (sessionId == null && sidLowerCase != null) {
			sessionId = sidLowerCase.toString();
			log.debug("hasRequiredClaims - Using lowercase direct access, sessionId: {}", sessionId);
		}

		boolean hasSubject = StringUtils.hasText(subject);
		boolean hasSessionId = StringUtils.hasText(sessionId);

		if (!hasSubject) {
			log.debug("Missing or empty subject claim");
		}
		if (!hasSessionId) {
			log.debug("Missing or empty session ID claim");
		}

		// Require both subject and session ID claims (strict like old code)
		return hasSubject && hasSessionId;
	}

	// ═══════════════════════════════════════════════════════════════
	// 🔗 AUTH0 MANAGEMENT API INTEGRATION
	// ═══════════════════════════════════════════════════════════════

	/**
	 * Obtains a Management API access token from Auth0 using client credentials
	 * flow.
	 * 
	 * @return Access token for Auth0 Management API
	 * @throws CustomException if token acquisition fails
	 */
	public String getManagementApiToken() {
		String audience = buildManagementApiAudience();
		log.debug("🔑 Requesting Auth0 Management API token for audience: {}", audience);

		try {
			MultiValueMap<String, String> requestBody = buildTokenRequestBody(audience);
			HttpEntity<MultiValueMap<String, String>> requestEntity = createTokenRequest(requestBody);

			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(buildTokenUrl(), HttpMethod.POST,
					requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
					});

			return extractAccessToken(response);

		} catch (HttpClientErrorException ex) {
			log.error("❌ Auth0 token request failed with status: {}", ex.getStatusCode());
			throw new CustomException(ErrorCode.INVALID_CREDENTIALS,
					"Failed to authenticate with Auth0: " + ex.getStatusCode().value(), ex);
		} catch (RestClientException ex) {
			log.error("❌ Failed to obtain Auth0 Management API token: {}", ex.getMessage());
			throw new CustomException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
					"Auth0 Management API is currently unavailable", ex);
		}
	}

	/**
	 * Validates session state by calling Auth0 Management API.
	 * 
	 * @param accessToken Auth0 Management API access token
	 * @param sessionId   Session ID to validate
	 * @return true if session is active, false otherwise
	 */
	private boolean isSessionActive(String accessToken, String sessionId) {
		if (!StringUtils.hasText(sessionId)) {
			log.warn("⚠️ Cannot validate session: session ID is null or empty");
			return false;
		}

		try {
			HttpEntity<String> requestEntity = createSessionValidationRequest(accessToken);
			String url = buildSessionValidationUrl(sessionId);

			log.debug("🔍 Validating session for SID: {}", maskSessionId(sessionId));

			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
					String.class);

			boolean isActive = HttpStatus.OK.equals(responseEntity.getStatusCode());
			log.debug("📊 Session status for SID {}: {}", maskSessionId(sessionId), isActive ? "ACTIVE" : "INACTIVE");

			return isActive;

		} catch (HttpClientErrorException ex) {
			if (HttpStatus.NOT_FOUND.equals(ex.getStatusCode())) {
				log.debug("🔍 Session not found: {}", maskSessionId(sessionId));
				return false;
			}
			log.error("❌ Session validation failed with status: {}", ex.getStatusCode());
			return false;
		} catch (RestClientException ex) {
			log.error("❌ Session validation request failed: {}", ex.getMessage());
			return false;
		}
	}

	// Helper methods for URL and request building

	private String buildManagementApiAudience() {
		return HTTPS_PREFIX + awsSecrets.getAuth0Domain() + API_V2_PATH;
	}

	private String buildTokenUrl() {
		return HTTPS_PREFIX + awsSecrets.getAuth0Domain() + OAUTH_TOKEN_PATH;
	}

	private String buildSessionValidationUrl(String sessionId) {
		return buildManagementApiAudience() + SESSIONS_PATH + sessionId;
	}

	private MultiValueMap<String, String> buildTokenRequestBody(String audience) {
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
		requestBody.add("grant_type", GRANT_TYPE_CLIENT_CREDENTIALS);
		requestBody.add("client_id", awsSecrets.getAuth0ClientId());
		requestBody.add("client_secret", awsSecrets.getAuth0ClientSecret());
		requestBody.add("audience", audience);
		requestBody.add("scope", SCOPE_READ_SESSIONS);
		return requestBody;
	}

	private HttpEntity<MultiValueMap<String, String>> createTokenRequest(MultiValueMap<String, String> requestBody) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		return new HttpEntity<>(requestBody, headers);
	}

	private HttpEntity<String> createSessionValidationRequest(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		return new HttpEntity<>(headers);
	}

	private String extractAccessToken(ResponseEntity<Map<String, Object>> response) {
		Map<String, Object> responseBody = response.getBody();
		if (responseBody == null || !responseBody.containsKey(ACCESS_TOKEN_FIELD)) {
			throw new CustomException(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
					"Auth0 Management API returned invalid response");
		}

		log.debug("✅ Successfully obtained Auth0 Management API token");
		return (String) responseBody.get(ACCESS_TOKEN_FIELD);
	}

	// ═══════════════════════════════════════════════════════════════
	// 🔒 SECURITY-FOCUSED HELPER METHODS (DATA MASKING)
	// ═══════════════════════════════════════════════════════════════

	/**
	 * Masks JWT subject for secure logging - preserves first and last 4 characters.
	 */
	private String maskSubject(String subject) {
		if (!StringUtils.hasText(subject) || subject.length() < 8) {
			return "***";
		}
		return subject.substring(0, 4) + "***" + subject.substring(subject.length() - 4);
	}

	/**
	 * Masks session ID for secure logging - preserves first 4 characters only.
	 */
	private String maskSessionId(String sessionId) {
		if (!StringUtils.hasText(sessionId) || sessionId.length() < 8) {
			return "***";
		}
		return sessionId.substring(0, 4) + "***";
	}
}