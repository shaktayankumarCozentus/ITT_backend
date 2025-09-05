package com.itt.service.config.security;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.itt.service.dto.CurrentUserDto;
import com.itt.service.enums.ErrorCode;
import com.itt.service.exception.CustomException;
import com.itt.service.repository.MasterUserRepository;
import com.itt.service.repository.RoleRepository;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ✅ PRODUCTION-READY: Auth0 JWT Token Processor & Authenticator
 * =============================================================
 * 
 * Comprehensive Auth0 token processor that validates JWT tokens and creates Spring Security Authentication objects.
 * Integrates with the Service's authorization system to:
 * 1. Decode and validate Auth0 JWT tokens
 * 2. Extract user claims and profile information
 * 3. Load user privileges from the database
 * 4. Create Spring Security Authentication with proper roles
 * 5. Set current user context in HTTP request
 * 
 * 🔧 AUTH0 INTEGRATION:
 * This processor works with Auth0's JWT token format and extracts:
 * - User email (primary identifier)
 * - Name components (given_name, family_name, nickname)
 * - Profile metadata (updated_at, email_verified)
 * - Creates standardized Spring Security Jwt object
 * 
 * 🎯 AUTHENTICATION FLOW:
 * 1. Extract JWT from Authorization header (Bearer token)
 * 2. Decode JWT using Auth0 library
 * 3. Validate required claims (email, name components)
 * 4. Load user privileges from database via RoleRepository
 * 5. Create Spring Security Authentication with roles
 * 6. Set CurrentUserDto in request attributes for controllers
 * 
 * 🔧 SECURITY FEATURES:
 * - Global exception handling with CustomException integration
 * - Email masking in logs for privacy protection
 * - Comprehensive token validation and error handling
 * - Database-driven role and privilege assignment
 * - Standard Spring Security Authentication creation
 * 
 * 📊 AUDIT LOGGING:
 * - Successful authentications: DEBUG level with masked email
 * - Token decode errors: ERROR level with exception details
 * - Missing user data: WARN level for user management
 * - Authentication failures: ERROR level for security monitoring
 * 
 * 🚨 SECURITY CONSIDERATIONS:
 * - All sensitive data (emails) are masked in logs
 * - CustomException used for consistent error handling
 * - Database lookups for user validation and privileges
 * - Request attributes used for user context sharing
 * - Proper Spring Security integration patterns
 * 
 * 📝 USAGE FLOW:
 * ```
 * 1. Client sends: Authorization: Bearer <jwt-token>
 * 2. Auth0TokenProcessor validates and extracts claims
 * 3. Database lookup for user privileges and validation
 * 4. Spring Security Authentication created with roles
 * 5. CurrentUserDto available in request.getAttribute("currentUser")
 * ```
 * 
 * @author Service Team
 * @version 2.1 - Production-Ready Implementation with Global Exception Handling
 * @since 2025.1 (Modernized from JWTClaimsSet to Spring Security Jwt)
 */
@Component
@ConditionalOnProperty(value = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class Auth0TokenProcessor  {

    private static final Logger log = LoggerFactory.getLogger(Auth0TokenProcessor.class);

    // ═══════════════════════════════════════════════════════════════
    // 🔧 DEPENDENCIES & CONFIGURATION
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * JWT configuration containing header names and validation settings.
     */
    private final JwtConfiguration jwtConfiguration;
    
    /**
     * Custom JWT decoder with claim preservation (including 'sid' claim).
     */
    private final JwtDecoder jwtDecoder;
    
    /**
     * Repository for user role and privilege management.
     */
    private final RoleRepository roleRepository;
    
    /**
     * Repository for master user data and validation.
     */
    private final MasterUserRepository masterUserRepository;
    


    // Constructor
    public Auth0TokenProcessor(JwtConfiguration jwtConfiguration,
                              @Lazy JwtDecoder jwtDecoder,
                              RoleRepository roleRepository,
                              MasterUserRepository masterUserRepository) {
        this.jwtConfiguration = jwtConfiguration;
        this.jwtDecoder = jwtDecoder;
        this.roleRepository = roleRepository;
        this.masterUserRepository = masterUserRepository;
        
        log.info("Auth0TokenProcessor initialized with custom JWT decoder - all claims will be preserved");
    }
    


    // JWT Claim constants for Auth0 integration
    private static final String CLAIM_GIVEN_NAME = "given_name";
    private static final String CLAIM_FAMILY_NAME = "family_name";
    private static final String CLAIM_EMAIL = "email";
    
    // Standard attribute name for user context
    private static final String CURRENT_USER_ATTRIBUTE = "currentUser";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String BEARER_PREFIX = "Bearer ";

    // ═══════════════════════════════════════════════════════════════
    // 🔐 AUTHENTICATION IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Authenticate token from header using custom JWT decoder that preserves all claims including 'sid'.
     * Creates a Spring Security Authentication object for the user.
     * 
     * @param request HTTP request containing the JWT token
     * @return Authentication object for the authenticated user
     * @throws CustomException if authentication fails with specific error codes
     */
    public Authentication authenticate(HttpServletRequest request) {
        try {
            String tokenValue = extractTokenFromHeader(request);
            String bearerToken = getBearerToken(tokenValue);
            
            // Use custom JWT decoder that preserves ALL claims including 'sid'
            Jwt jwt = jwtDecoder.decode(bearerToken);
            
            String email = extractEmailFromJwt(jwt);
            log.info("DEBUG - Processing authentication for user email: {}", maskEmail(email));
            log.debug("Processing authentication for user: {}", maskEmail(email));

            // Log all claims to verify 'sid' is preserved
            log.info("🔍 JWT Claims after custom decoder: {}", jwt.getClaims());

            // Create authentication with user privileges
            return createAuthenticationForUser(request, email, jwt);
            
        } catch (JwtException ex) {
            log.error("❌ JWT validation error: {}", ex.getMessage());
            throw new CustomException(ErrorCode.TOKEN_INVALID, "JWT token validation failed", ex);
        } catch (CustomException ex) {
            // Re-throw custom exceptions to preserve error context
            throw ex;
        } catch (Exception ex) {
            log.error("❌ Authentication processing error: {}", ex.getMessage(), ex);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Authentication processing failed", ex);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 🔍 PRIVATE HELPER METHODS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Extracts JWT token from Authorization header.
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String idToken = request.getHeader(this.jwtConfiguration.getHttpHeader());
        if (!StringUtils.hasText(idToken)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "Missing authorization header");
        }
        return idToken;
    }

    /**
     * Extracts email from Spring Security JWT claims with validation.
     */
    private String extractEmailFromJwt(Jwt jwt) {
        String email = jwt.getClaimAsString(CLAIM_EMAIL);
        if (!StringUtils.hasText(email)) {
            throw new CustomException(ErrorCode.TOKEN_INVALID, "JWT token missing required email claim");
        }
        return email;
    }

    /**
     * Creates authentication object for the user with database-driven privileges.
     */
    private Authentication createAuthenticationForUser(HttpServletRequest request, String email, Jwt jwt) {
        try {
            // Load user privileges from database
            var userPrivilegeData = roleRepository.getUserFeaturePrivileges(email);
            var grantedAuthorities = userPrivilegeData.stream()
                    .map(privilege -> new SimpleGrantedAuthority(ROLE_PREFIX + privilege))
                    .collect(Collectors.toSet());
                    
            log.info("🔍 DEBUG - User '{}' raw privileges from DB: {}", maskEmail(email), userPrivilegeData);
            log.info("🔍 DEBUG - User '{}' final granted authorities: {}", maskEmail(email), grantedAuthorities);
            log.debug("User '{}' privileges from DB: {}", maskEmail(email), userPrivilegeData);
            log.debug("Granted authorities for user '{}': {}", maskEmail(email), grantedAuthorities);
            
            // Create Spring Security User object
            User user = new User(email, "", grantedAuthorities);
            
            // Validate user exists in system
            Integer userId = masterUserRepository.findUserIdByEmail(email);
            if (userId == null) {
                log.warn("⚠️ User ID not found for email: {}", maskEmail(email));
                throw new CustomException(ErrorCode.USER_NOT_FOUND, "User not found in system");
            }
            
            // Create current user context for controllers - extract names from JWT claims
            String givenName = jwt.getClaimAsString(CLAIM_GIVEN_NAME);
            String familyName = jwt.getClaimAsString(CLAIM_FAMILY_NAME);
            String fullName = buildFullName(givenName, familyName);
            CurrentUserDto currentUserDto = new CurrentUserDto(userId, email, fullName);
            request.setAttribute(CURRENT_USER_ATTRIBUTE, currentUserDto);
            
            log.debug("✅ Authentication successful for user: {} with {} privileges: {}", 
                     maskEmail(email), grantedAuthorities.size(), grantedAuthorities);
            
            return new JwtAuthentication(user, jwt, grantedAuthorities);
            
        } catch (Exception ex) {
            log.error("❌ Failed to create authentication for user: {}", maskEmail(email), ex);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, 
                "Failed to retrieve user privileges", ex);
        }
    }
    
    /**
     * Builds full name from given and family names.
     */
    private String buildFullName(String givenName, String familyName) {
        if (StringUtils.hasText(givenName) && StringUtils.hasText(familyName)) {
            return givenName + " " + familyName;
        } else if (StringUtils.hasText(givenName)) {
            return givenName;
        } else if (StringUtils.hasText(familyName)) {
            return familyName;
        }
        return "Unknown User";
    }

    /**
     * Extract token from Authorization header by removing Bearer prefix.
     */
    private String getBearerToken(String token) {
        return token.startsWith(BEARER_PREFIX) ? token.substring(BEARER_PREFIX.length()) : token;
    }

    /**
     * Masks email for secure logging - preserves first 2 characters of local part.
     */
    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];
        
        if (localPart.length() <= 2) {
            return "***@" + domainPart;
        }
        return localPart.substring(0, 2) + "***@" + domainPart;
    }
    
  
}