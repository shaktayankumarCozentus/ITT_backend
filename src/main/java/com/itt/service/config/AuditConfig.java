package com.itt.service.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Configuration for JPA auditing functionality.
 * <p>
 * Enables automatic population of audit fields (createdBy, lastModifiedBy, etc.)
 * in JPA entities that extend auditing base classes or use auditing annotations.
 * <p>
 * The auditor provider attempts to resolve the current user from:
 * <ol>
 *   <li>Spring Security context (if available)</li>
 *   <li>MDC traceId (as fallback for system operations)</li>
 *   <li>Empty if neither is available</li>
 * </ol>
 * 
 * @see org.springframework.data.jpa.domain.support.AuditingEntityListener
 * @see org.springframework.data.annotation.CreatedBy
 * @see org.springframework.data.annotation.LastModifiedBy
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    /**
     * Provides the current auditor for JPA auditing operations.
     * <p>
     * Resolution strategy:
     * <ol>
     *   <li>Returns current authenticated user if Spring Security context exists</li>
     *   <li>Falls back to MDC traceId for system/background operations</li>
     *   <li>Returns empty Optional if neither is available</li>
     * </ol>
     * 
     * @return AuditorAware implementation for determining current auditor
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // TODO: Add Spring Security context check when security is implemented
            // SecurityContext context = SecurityContextHolder.getContext();
            // if (context != null && context.getAuthentication() != null) {
            //     return Optional.of(context.getAuthentication().getName());
            // }
            
            // Fallback to traceId for system operations
            return Optional.ofNullable(MDC.get("traceId"))
                          .filter(id -> !id.isEmpty());
        };
    }
}