package com.itt.service.fw.ratelimit.utility;

import com.itt.service.fw.ratelimit.configuration.OpenApiConfigurationProperties;
import com.itt.service.fw.ratelimit.configuration.PublicEndpoint;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/**
 * Utility class responsible for evaluating the accessibility of API endpoints
 * based on their security configuration. It works in conjunction with the
 * mappings of controller methods annotated with {@link PublicEndpoint}.
 *
 * @see PublicEndpoint
 * @see OpenApiConfigurationProperties
 */
@Component
@EnableConfigurationProperties(OpenApiConfigurationProperties.class)
public class ApiEndpointSecurityInspector {

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping requestHandlerMapping;

    @Autowired
    private OpenApiConfigurationProperties openApiConfigurationProperties;

    private static final List<String> SWAGGER_V3_PATHS = List.of("/swagger-ui/**", "/v3/api-docs/**",
            "/api/docs/swagger-ui/**", "/api/docs/v3/api-docs/**",
            "/api/docs/api-docs/**", "/api/docs/api-docs");

    @Getter
    private List<String> publicGetEndpoints = new ArrayList<>();
    @Getter
    private List<String> publicPostEndpoints = new ArrayList<>();

    /**
     * Initializes the class by gathering public endpoints for various HTTP methods.
     * It identifies designated public endpoints within the application's mappings
     * and adds them to separate lists based on their associated HTTP methods.
     * If OpenAPI is enabled, Swagger endpoints are also considered as public.
     */
    @PostConstruct
    public void init() {
        final var handlerMethods = requestHandlerMapping.getHandlerMethods();
        handlerMethods.forEach((requestInfo, handlerMethod) -> {
            if (handlerMethod.hasMethodAnnotation(PublicEndpoint.class)) {
                final var httpMethod = requestInfo.getMethodsCondition().getMethods().iterator().next().asHttpMethod();
                final var apiPaths = requestInfo.getPathPatternsCondition().getPatternValues();

                if (httpMethod.equals(GET)) {
                    publicGetEndpoints.addAll(apiPaths);
                } else if (httpMethod.equals(POST)) {
                    publicPostEndpoints.addAll(apiPaths);
                }
            }
        });

        final var openApiEnabled = openApiConfigurationProperties.getOpenApi().isEnabled();
        if (Boolean.TRUE.equals(openApiEnabled)) {
            publicGetEndpoints.addAll(SWAGGER_V3_PATHS);
        }
    }

    /**
     * Checks if the provided HTTP request is directed towards an unsecured API endpoint.
     *
     * @param request The HTTP request to inspect.
     * @return {@code true} if the request is to an unsecured API endpoint, {@code false} otherwise.
     */
    public boolean isUnsecureRequest(@NonNull final HttpServletRequest request) {
        final var requestHttpMethod = HttpMethod.valueOf(request.getMethod());
        var unsecuredApiPaths = getUnsecuredApiPaths(requestHttpMethod);
        unsecuredApiPaths = Optional.ofNullable(unsecuredApiPaths).orElseGet(ArrayList::new);

        return unsecuredApiPaths.stream().anyMatch(apiPath -> new AntPathMatcher().match(apiPath, request.getRequestURI()));
    }

    /**
     * Retrieves the list of unsecured API paths based on the provided HTTP method.
     *
     * @param httpMethod The HTTP method for which unsecured paths are to be retrieved.
     * @return A list of unsecured API paths for the specified HTTP method.
     */
    private List<String> getUnsecuredApiPaths(@NonNull final HttpMethod httpMethod) {
        switch (httpMethod) {
            case GET:
                return publicGetEndpoints;
            case POST:
                return publicPostEndpoints;
            default:
                return Collections.emptyList();
        }
    }
}
