package com.itt.service.fw.ratelimit.service;

import com.itt.service.repository.UserPlanMappingRepository;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimitingService {

	private final ProxyManager<Integer> proxyManager;
	private final UserPlanMappingRepository userPlanMappingRepository;

	/**
	 * Retrieves the stored rate-limiting bucket for the specified user. If no
	 * bucket is found for the user, a new one is created and stored in the
	 * provisioned cache based on the user's current subscription plan.
	 *
	 * @param userId unique identifier of the user.
	 * @return The rate-limiting {@link Bucket} associated with the user.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 */
	public Bucket getBucket(final Integer userId) {
		return proxyManager.builder().build(userId, () -> createBucketConfiguration(userId));
	}

	/**
	 * Resets the rate limiting for the specified user-id.
	 *
	 * @param userId unique identifier of the user.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 */
	public void reset(@NonNull final Integer userId) {
		proxyManager.removeProxy(userId);
	}

	/**
	 * Constructs an instance of {@link BucketConfiguration} corresponding to the
	 * user's active plan which enforce the allowed rate-limit of API invocation.
	 *
	 * @param userId The unique identifier of the user.
	 * @return The bucket configuration for rate limiting based on the user's active plan.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 */
	private BucketConfiguration createBucketConfiguration(@NonNull final Integer userId) {
		final var userPlanMapping = userPlanMappingRepository.getActivePlan(userId);
		final var requestsAllowed = userPlanMapping.getPlan().getRequestsAllowed();
		var timeUnit = userPlanMapping.getPlan().getTimeUnit();
		Duration duration = switch (timeUnit) {
			case HOUR -> Duration.ofHours(1);
			case MINUTE -> Duration.ofMinutes(1);
			case SECOND -> Duration.ofSeconds(1);
			case null  -> Duration.ofHours(1); // fallback for null or unexpected enum value
		};

		Duration.ofHours(1);
		return BucketConfiguration.builder()
				.addLimit(limit -> limit.capacity(requestsAllowed).refillIntervally(requestsAllowed, duration))
				.build();
	}

}