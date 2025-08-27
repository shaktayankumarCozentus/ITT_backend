package com.itt.service.config;

import com.itt.service.fw.audit.config.EventAuditLoggerInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final EventAuditLoggerInterceptor auditLoggerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the EventAuditLoggerInterceptor for all paths
        registry.addInterceptor(auditLoggerInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ensure proper handling of Swagger UI static resources
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
