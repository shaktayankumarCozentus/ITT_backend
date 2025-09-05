package com.itt.service.fw.ratelimit.filter;

import com.itt.service.fw.ratelimit.configuration.BypassRateLimit;
import com.itt.service.fw.ratelimit.service.RateLimitingService;
import com.itt.service.fw.ratelimit.util.RateLimitResponseHelper;
import com.itt.service.fw.ratelimit.utility.ApiEndpointSecurityInspector;
import com.itt.service.fw.ratelimit.utility.AuthenticatedUserIdProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static com.itt.service.config.security.Auth0TokenProcessor.CURRENT_USER_ATTRIBUTE;

/**
 * RateLimitFilter is a custom filter registered with the spring security filter
 * chain and works in conjunction with the security configuration, as defined in
 * SecurityConfiguration.java. As per established configuration, this filter is executed
 * after evaluation of JwtAuthenticationFilter.
 *
 * This filter is responsible for enforcing rate limit on secured
 * API endpoint(s) corresponding to the user's current plan. It intercepts
 * incoming HTTP  requests and evaluates whether the user has exhausted the
 * limit enforced.
 * If the limit is exceeded, an error response indicating that the
 * request limit linked to the user's current plan has been exhausted
 * is returned back to the client.
 *
 * This filter is only executed when a secure API endpoint in invoked, and is skipped
 * if the incoming request is destined to a non-secured public API endpoint.
 *
 * Additionally, the rate limit enforcement can be bypassed for specific private
 * API endpoints by annotating the corresponding controller methods with
 * {@link BypassRateLimit} annotation.
 *
 * @see BypassRateLimit
 * @see RateLimitingService
 * @see ApiEndpointSecurityInspector
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private final RateLimitingService rateLimitingService;
	private final RateLimitResponseHelper rateLimitResponseHelper;
	private final RequestMappingHandlerMapping requestHandlerMapping;
	private final AuthenticatedUserIdProvider authenticatedUserIdProvider;
	private final ApiEndpointSecurityInspector apiEndpointSecurityInspector;

	public RateLimitFilter(RateLimitingService rateLimitingService,
						   RateLimitResponseHelper rateLimitResponseHelper,
						   @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping requestHandlerMapping,
						   AuthenticatedUserIdProvider authenticatedUserIdProvider,
						   ApiEndpointSecurityInspector apiEndpointSecurityInspector) {
		this.rateLimitingService = rateLimitingService;
		this.rateLimitResponseHelper = rateLimitResponseHelper;
		this.requestHandlerMapping = requestHandlerMapping;
		this.authenticatedUserIdProvider = authenticatedUserIdProvider;
		this.apiEndpointSecurityInspector = apiEndpointSecurityInspector;
	}

	@Override
	@SneakyThrows
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		final var unsecuredApiBeingInvoked = apiEndpointSecurityInspector.isUnsecureRequest(request);

		if (Boolean.FALSE.equals(unsecuredApiBeingInvoked) && authenticatedUserIdProvider.isAvailable()) {
//		if (Boolean.FALSE) {
			final var isRequestBypassed = isBypassed(request);

			if (Boolean.FALSE.equals(isRequestBypassed)) {
				final var userId = authenticatedUserIdProvider.getCurrentUserId();
				final var bucket = rateLimitingService.getBucket(userId);
				final var consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);
				final var isConsumptionPassed = consumptionProbe.isConsumed();

				if (Boolean.FALSE.equals(isConsumptionPassed)) {
					// Use shared utility for rate limit error response
					rateLimitResponseHelper.setRateLimitErrorDetails(response, consumptionProbe,
							"API request limit linked to your current plan has been exhausted.");
					return;
				}

				// Use shared utility for success headers
				final var remainingTokens = consumptionProbe.getRemainingTokens();
				rateLimitResponseHelper.addRateLimitHeaders(response, remainingTokens, 1000);
			}
		}
		filterChain.doFilter(request, response);
	}

	/**
	 * Checks if the controller method corresponding to current request is annotated
	 * with {@link BypassRateLimit} annotation, indicating that rate limit
	 * enforcement should be bypassed.
	 *
	 * @param request HttpServletRequest representing the incoming HTTP request
	 * @return {@code true} if the request is to be bypassed, {@code false} otherwise
	 */
	@SneakyThrows
	private boolean isBypassed(HttpServletRequest request) {
		var handlerChain = requestHandlerMapping.getHandler(request);
		if (handlerChain != null && handlerChain.getHandler() instanceof HandlerMethod handlerMethod) {
			return handlerMethod.getMethod().isAnnotationPresent(BypassRateLimit.class);
		}
		return Boolean.FALSE;
	}

}