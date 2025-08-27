package com.itt.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous method execution.
 * <p>
 * Provides a custom ThreadPoolTaskExecutor for @Async methods with optimized
 * settings for logging and other background tasks. The executor is tuned for:
 * <ul>
 *   <li>Core pool size: 10 threads for immediate availability</li>
 *   <li>Max pool size: 50 threads for burst capacity</li>
 *   <li>Queue capacity: 200 tasks for buffering during high load</li>
 *   <li>Named threads: "AsyncLogger-" prefix for easy identification</li>
 * </ul>
 * 
 * @see com.itt.service.util.AsyncLogger
 * @see com.itt.service.aspect.LoggingAspect
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Creates a custom ThreadPoolTaskExecutor for asynchronous method execution.
     * <p>
     * This executor is optimized for logging operations but can be used by any
     * @Async annotated method in the application.
     * 
     * @return configured ThreadPoolTaskExecutor instance
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool always available for immediate task execution
        executor.setCorePoolSize(10);
        
        // Maximum pool size for handling burst loads
        executor.setMaxPoolSize(50);
        
        // Queue capacity for task buffering during high load
        executor.setQueueCapacity(200);
        
        // Thread naming for easy identification in logs and monitoring
        executor.setThreadNamePrefix("AsyncLogger-");
        
        // Initialize the executor
        executor.initialize();
        
        return executor;
    }
}
