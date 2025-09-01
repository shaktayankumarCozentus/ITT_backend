package com.itt.service.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SwaggerConfig {

	private final Environment environment;

	@Value("${server.port:8080}")
	private int serverPort;

	@Value("${server.servlet.context-path:}")
	private String contextPath;

	@Value("${swagger.servers.urls:}")
	private List<String> customServerUrls;

	@Value("${swagger.api.title:ITT Service API}")
	private String apiTitle;

	@Value("${swagger.api.description:Track and Trace (ITT) integration APIs}")
	private String apiDescription;

	@Value("${swagger.api.version:1.0.0}")
	private String apiVersion;

	@Value("${swagger.contact.name:ITT Development Team}")
	private String contactName;

	@Value("${swagger.contact.email:dev-team@itt.com}")
	private String contactEmail;

	@Value("${swagger.contact.url:https://itt.bdpsmart.com/help}")
	private String contactUrl;

	private static final String BEARER_AUTH_COMPONENT_NAME = "Bearer Authentication";
	private static final String API_KEY_AUTH_COMPONENT_NAME = "API Key Authentication";
	private static final String BEARER_AUTH_SCHEME = "Bearer";

	// Environment to domain mapping
	private static final String DEV_DOMAIN = "https://dev.itt.bdpsmart.com";
	private static final String TEST_DOMAIN = "https://test.itt.bdpsmart.com";
	private static final String STAGE_DOMAIN = "https://stage.itt.bdpsmart.com";
	private static final String PROD_DOMAIN = "https://itt.bdpsmart.com";

	@Bean
	OpenAPI openAPI() {
		List<Server> servers = configureServers();
		Info apiInfo = configureApiInfo();
		Components components = configureComponents();
		List<SecurityRequirement> securityRequirements = configureSecurityRequirements();
		List<Tag> tags = configureTags();

		log.info("Configuring OpenAPI with {} servers for profiles: {}", servers.size(),
				Arrays.toString(environment.getActiveProfiles()));

		return new OpenAPI().servers(servers).info(apiInfo).components(components).security(securityRequirements)
				.tags(tags).externalDocs(new ExternalDocumentation().description("ITT Service Documentation")
						.url("https://docs.itt.bdpsmart.com"));
	}

	private List<Server> configureServers() {
		List<Server> servers = new ArrayList<>();
		String[] activeProfiles = environment.getActiveProfiles();
		List<String> profileList = Arrays.asList(activeProfiles);

		// ALWAYS add localhost for local debugging regardless of environment
		servers.add(createServer("http://localhost:" + serverPort + contextPath, "Local"));

		// Add environment-specific servers based on active profiles (matching
		// application.yml)
		if (activeProfiles.length == 0 || profileList.contains("dev")) {
			servers.add(createServer(DEV_DOMAIN + contextPath, "Development"));
			log.info("Added servers for dev profile: localhost:{} and {}", serverPort, DEV_DOMAIN);
		}

		if (profileList.contains("test")) {
			servers.add(createServer(TEST_DOMAIN + contextPath, "Test"));
			log.info("Added servers for test profile: localhost:{} and {}", serverPort, TEST_DOMAIN);
		}

		if (profileList.contains("stage")) {
			servers.add(createServer(STAGE_DOMAIN + contextPath, "Staging"));
			log.info("Added servers for stage profile: localhost:{} and {}", serverPort, STAGE_DOMAIN);
		}

		if (profileList.contains("prod")) {
			servers.add(createServer(PROD_DOMAIN + contextPath, "Production"));
			log.info("Added servers for prod profile: localhost:{} and {}", serverPort, PROD_DOMAIN);
		}

		// Add custom server URLs from configuration
		addCustomServers(servers);

		// Note: No fallback needed since localhost is always added
		log.info("Total servers configured: {}", servers.size());

		return servers;
	}

	private Server createServer(String url, String description) {
		return new Server().url(url).description(description);
	}

	private void addCustomServers(List<Server> servers) {
		if (customServerUrls != null && !customServerUrls.isEmpty()) {
			customServerUrls.stream().filter(StringUtils::hasText).forEach(url -> {
				servers.add(createServer(url.trim(), "Custom Server - " + url.trim()));
				log.info("Added custom server: {}", url.trim());
			});
		}
	}

	private Info configureApiInfo() {
		return new Info().title(apiTitle)
				.description(apiDescription + ". All APIs for integrated Track and Trace (ITT) system.")
				.version(apiVersion).contact(new Contact().name(contactName).email(contactEmail).url(contactUrl))
				.license(new License().name("ITT Internal License").url("https://itt.bdpsmart.com/license"));
	}

	private Components configureComponents() {
		return new Components()
				.addSecuritySchemes(BEARER_AUTH_COMPONENT_NAME,
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme(BEARER_AUTH_SCHEME)
								.bearerFormat("JWT").description("JWT Bearer token authentication"))
				.addSecuritySchemes(API_KEY_AUTH_COMPONENT_NAME,
						new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)
								.name("X-API-Key")
								.description("API Key authentication for service-to-service communication"));
	}

	private List<SecurityRequirement> configureSecurityRequirements() {
		return List.of(new SecurityRequirement().addList(BEARER_AUTH_COMPONENT_NAME),
				new SecurityRequirement().addList(API_KEY_AUTH_COMPONENT_NAME));
	}

	private List<Tag> configureTags() {
		return List.of(new Tag().name("Authentication").description("Authentication and authorization endpoints"),
				new Tag().name("User Management").description("User administration and profile management"),
				new Tag().name("Role Management").description("Role and permission management"),
				new Tag().name("Company Management").description("Company hierarchy and organization management"),
				new Tag().name("System").description("System health and monitoring endpoints"));
	}
}