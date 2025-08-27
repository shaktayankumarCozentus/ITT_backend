package com.itt.service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP filter that populates MDC (Mapped Diagnostic Context) with a unique trace ID
 * for each incoming request.
 * <p>
 * This filter:
 * <ul>
 *   <li>Generates a unique UUID-based trace ID for each request</li>
 *   <li>Stores the trace ID in SLF4J MDC for logging correlation</li>
 *   <li>Adds the trace ID to the HTTP response header for client correlation</li>
 *   <li>Ensures proper cleanup of MDC after request processing</li>
 * </ul>
 * <p>
 * The trace ID enables:
 * <ul>
 *   <li>Correlating all log entries for a single request</li>
 *   <li>Distributed tracing across microservices</li>
 *   <li>Client-side request correlation via response headers</li>
 *   <li>Audit trail tracking for security and compliance</li>
 * </ul>
 * 
 * @see org.slf4j.MDC
 * @see com.itt.service.aspect.LoggingAspect
 */
@Component
public class MdcFilter extends OncePerRequestFilter {
    
    /** MDC key for storing the trace ID */
    private static final String TRACE_ID = "traceId";
    
    /** HTTP response header name for trace ID */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * Processes each HTTP request to set up and clean up trace ID context.
     * <p>
     * Execution flow:
     * <ol>
     *   <li>Generate unique trace ID (UUID-based)</li>
     *   <li>Store in MDC for logging correlation</li>
     *   <li>Add to response header for client visibility</li>
     *   <li>Process the request through filter chain</li>
     *   <li>Clean up MDC to prevent memory leaks</li>
     * </ol>
     * 
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain for continued processing
     * @throws ServletException if request processing fails
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Generate unique trace ID for this request
            String traceId = UUID.randomUUID().toString();
            
            // Store in MDC for logging correlation
            MDC.put(TRACE_ID, traceId);
            
            // Add to response header for client correlation
            response.setHeader(TRACE_ID_HEADER, traceId);
            
            // Continue with request processing
            filterChain.doFilter(request, response);
            
        } finally {
            // Always clean up MDC to prevent memory leaks
            MDC.remove(TRACE_ID);
        }
    }
}
