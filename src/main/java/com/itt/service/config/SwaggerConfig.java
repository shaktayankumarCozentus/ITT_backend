package com.itt.service.config;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    
    private final Environment environment;
    
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;
    
    @Value("${swagger.servers.urls:}")
    private List<String> customServerUrls;

    private static final String BEARER_AUTH_COMPONENT_NAME = "Bearer Authentication";
    private static final String BEARER_AUTH_SCHEME = "Bearer";

    @Bean
    OpenAPI openAPI() {
        List<Server> servers = new ArrayList<>();
        
        // Add environment-specific servers
        String[] activeProfiles = environment.getActiveProfiles();
        
        if (activeProfiles.length == 0 || List.of(activeProfiles).contains("dev")) {
            // Development environment
            servers.add(new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("Development Server"));
        }
        
        if (List.of(activeProfiles).contains("test")) {
            // Test environment - add your test server URL
            servers.add(new Server()
                .url("https://test-api.yourdomain.com" + contextPath)
                .description("Test Environment"));
        }
        
        if (List.of(activeProfiles).contains("stage")) {
            // Staging environment - add your staging server URL
            servers.add(new Server()
                .url("https://stage-api.yourdomain.com" + contextPath)
                .description("Staging Environment"));
        }
        
        if (List.of(activeProfiles).contains("prod")) {
            // Production environment - add your production server URL
            servers.add(new Server()
                .url("https://api.yourdomain.com" + contextPath)
                .description("Production Environment"));
        }
        
        // Add any custom server URLs from configuration
        if (customServerUrls != null && !customServerUrls.isEmpty()) {
            for (String url : customServerUrls) {
                if (!url.trim().isEmpty()) {
                    servers.add(new Server().url(url.trim()).description("Custom Server"));
                }
            }
        }
        
        // Fallback server if no servers are configured
        if (servers.isEmpty()) {
            servers.add(new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("Default Server"));
        }

        final var info = new Info()
                .title("ITT Service API")
                .description("API for user management, role management, and authentication services. " +
                        "Provides endpoints for user administration, role management, company hierarchies, " +
                        "access control, and authentication services.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("ITT Development Team")
                        .email("dev-team@itt.com")
                        .url("https://itt.com"));

        return new OpenAPI()
                .servers(servers)
                .info(info)
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH_COMPONENT_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(BEARER_AUTH_SCHEME)))
                .addSecurityItem(new SecurityRequirement()
                        .addList(BEARER_AUTH_COMPONENT_NAME));
    }
}