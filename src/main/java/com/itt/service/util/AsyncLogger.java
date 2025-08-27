package com.itt.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Asynchronous logging utility to offload log operations from main application threads.
 * <p>
 * This component provides async logging methods that:
 * <ul>
 *   <li>Execute logging operations on background threads</li>
 *   <li>Prevent log I/O from blocking application logic</li>
 *   <li>Improve application responsiveness under load</li>
 *   <li>Use the configured ThreadPoolTaskExecutor for execution</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>
 * {@code
 * @Component
 * public class SomeService {
 *     private final AsyncLogger asyncLogger;
 *     
 *     public void someMethod() {
 *         asyncLogger.info("Processing started for user: {}", userId);
 *         // ... business logic
 *         asyncLogger.info("Processing completed successfully");
 *     }
 * }
 * }
 * </pre>
 * 
 * @see com.itt.service.config.AsyncConfig
 * @see com.itt.service.aspect.LoggingAspect
 */
@Component
public class AsyncLogger {
    
    /** SLF4J logger instance for actual log output */
    private static final Logger LOG = LoggerFactory.getLogger(AsyncLogger.class);

    /**
     * Logs an informational message asynchronously.
     * <p>
     * This method queues the log operation for background execution,
     * allowing the calling thread to continue immediately.
     * 
     * @param message the log message with optional placeholders
     * @param args    arguments to substitute into message placeholders
     */
    @Async
    public void info(String message, Object... args) {
        LOG.info(message, args);
    }

    /**
     * Logs an error message asynchronously.
     * <p>
     * This method queues the log operation for background execution,
     * allowing the calling thread to continue immediately.
     * 
     * @param message the log message with optional placeholders
     * @param args    arguments to substitute into message placeholders
     */
    @Async
    public void error(String message, Object... args) {
        LOG.error(message, args);
    }
    
    /**
     * Logs a warning message asynchronously.
     * <p>
     * This method queues the log operation for background execution,
     * allowing the calling thread to continue immediately.
     * 
     * @param message the log message with optional placeholders
     * @param args    arguments to substitute into message placeholders
     */
    @Async
    public void warn(String message, Object... args) {
        LOG.warn(message, args);
    }
    
    /**
     * Logs a debug message asynchronously.
     * <p>
     * This method queues the log operation for background execution,
     * allowing the calling thread to continue immediately.
     * 
     * @param message the log message with optional placeholders
     * @param args    arguments to substitute into message placeholders
     */
    @Async
    public void debug(String message, Object... args) {
        LOG.debug(message, args);
    }
}
