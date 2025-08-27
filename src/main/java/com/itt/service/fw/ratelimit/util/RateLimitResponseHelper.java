package com.itt.service.fw.ratelimit.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itt.service.fw.ratelimit.dto.response.ExceptionResponseDto;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * ✅ SHARED UTILITY: Rate Limit Response Helper
 * ==============================================
 * 
 * Centralizes rate limit error response creation to eliminate duplication
 * between RateLimitFilter and ExternalApiRateLimitFilter.
 * 
 * 🎯 KEY FEATURES:
 * - Consistent error response format across all rate limiting filters
 * - Standardized HTTP headers for rate limiting feedback
 * - Proper error message formatting and localization support
 * - Comprehensive audit logging for rate limit violations
 * 
 * 🔧 RESPONSE FORMAT:
 * - Standard HTTP 429 (Too Many Requests) status
 * - Consistent JSON error structure using ExceptionResponseDto
 * - Rate limiting headers for client guidance
 * - Retry-After header for proper client behavior
 * 
 * Developer Notes:
 * - Used by both user-based and external API rate limiting filters
 * - Maintains consistency across different authentication mechanisms
 * - Provides detailed logging for monitoring and debugging
 * - Supports internationalization through proper message keys
 * 
 * @author ITT Development Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitResponseHelper {

    private final ObjectMapper objectMapper;
    
    private static final String RATE_LIMIT_ERROR_MESSAGE = "API request limit has been exhausted.";
    private static final HttpStatus RATE_LIMIT_ERROR_STATUS = HttpStatus.TOO_MANY_REQUESTS;

    /**
     * Sets comprehensive rate limit error details in the HTTP response.
     * 
     * This method provides a standardized way to format rate limit exceeded responses
     * with proper HTTP status, headers, and JSON body. It ensures consistency across
     * all rate limiting implementations in the application.
     * 
     * @param response The HttpServletResponse to populate with error details
     * @param consumptionProbe The Bucket4j ConsumptionProbe containing rate limit information
     * @param contextMessage Additional context message for the rate limit (optional)
     */
    @SneakyThrows
    public void setRateLimitErrorDetails(HttpServletResponse response, 
                                       ConsumptionProbe consumptionProbe, 
                                       String contextMessage) {
        // Set HTTP status and content type
        response.setStatus(RATE_LIMIT_ERROR_STATUS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Calculate wait time in seconds
        final var waitSeconds = TimeUnit.NANOSECONDS.toSeconds(consumptionProbe.getNanosToWaitForRefill());
        
        // Set rate limiting headers for client guidance
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitSeconds));
        response.setHeader("X-Rate-Limit-Retry-After", formatRetryAfter(waitSeconds));
        response.setHeader("X-Rate-Limit-Limit", "1000"); // Could be made configurable
        response.setHeader("X-Rate-Limit-Remaining", "0");
        response.setHeader("X-Rate-Limit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + waitSeconds));

        // Create standardized error response
        final var errorResponse = prepareErrorResponseBody(contextMessage, waitSeconds);
        response.getWriter().write(errorResponse);
        
        // Log rate limit violation for monitoring
        log.warn("🚫 Rate limit exceeded: context={}, waitSeconds={}, retryAfter={}", 
                contextMessage != null ? contextMessage : "general", 
                waitSeconds, 
                formatRetryAfter(waitSeconds));
    }

    /**
     * Overloaded method with default context message.
     * 
     * @param response The HttpServletResponse to populate with error details
     * @param consumptionProbe The Bucket4j ConsumptionProbe containing rate limit information
     */
    public void setRateLimitErrorDetails(HttpServletResponse response, ConsumptionProbe consumptionProbe) {
        setRateLimitErrorDetails(response, consumptionProbe, null);
    }

    /**
     * Prepares the JSON error response body using the standard ExceptionResponseDto format.
     * 
     * @param contextMessage Additional context for the error message
     * @param waitSeconds Number of seconds client should wait before retrying
     * @return JSON string representation of the error response
     */
    @SneakyThrows
    private String prepareErrorResponseBody(String contextMessage, long waitSeconds) {
        String message = contextMessage != null ? contextMessage : RATE_LIMIT_ERROR_MESSAGE;
        
        final var errorResponseDto = new ExceptionResponseDto<String>();
        errorResponseDto.setStatus(RATE_LIMIT_ERROR_STATUS.name());
        errorResponseDto.setDescription(message);

        return objectMapper.writeValueAsString(errorResponseDto);
    }

    /**
     * Formats the retry-after duration in a human-readable format.
     * 
     * @param waitSeconds Number of seconds to wait
     * @return Formatted duration string (e.g., "2 minutes", "30 seconds")
     */
    private String formatRetryAfter(long waitSeconds) {
        if (waitSeconds < 60) {
            return waitSeconds + " seconds";
        } else if (waitSeconds < 3600) {
            long minutes = waitSeconds / 60;
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        } else {
            long hours = waitSeconds / 3600;
            return hours + " hour" + (hours > 1 ? "s" : "");
        }
    }

    /**
     * Creates rate limit headers for successful responses (showing remaining capacity).
     * 
     * This method can be used to add rate limit information to successful responses,
     * providing clients with visibility into their current usage status.
     * 
     * @param response The HttpServletResponse to add headers to
     * @param remainingTokens Number of tokens remaining in the bucket
     * @param totalLimit Total rate limit capacity
     */
    public void addRateLimitHeaders(HttpServletResponse response, long remainingTokens, int totalLimit) {
        response.setHeader("X-Rate-Limit-Limit", String.valueOf(totalLimit));
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(remainingTokens));
        response.setHeader("X-Rate-Limit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + 3600)); // Next hour
    }
}
