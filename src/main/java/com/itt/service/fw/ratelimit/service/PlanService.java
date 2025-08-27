package com.itt.service.fw.ratelimit.service;

import com.itt.service.fw.ratelimit.dto.request.UserPlanUpdationRequestDto;
import com.itt.service.fw.ratelimit.dto.response.PlanResponseDto;
import com.itt.service.entity.UserPlanMapping;
import com.itt.service.exception.InvalidPlanException;
import com.itt.service.repository.PlanRepository;
import com.itt.service.repository.UserPlanMappingRepository;
import com.itt.service.fw.ratelimit.utility.AuthenticatedUserIdProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

	private final PlanRepository planRepository;
	private final RateLimitingService rateLimitingService;
	private final UserPlanMappingRepository userPlanMappingRepository;
	private final AuthenticatedUserIdProvider authenticatedUserIdProvider;

	/**
	 * Updates the subscription plan for a user and deactivates their current plan
	 * in the system. The rate-limit corresponding to the previous plan is cleared
	 * on successful plan updation.
	 * 
	 * If the provided plan-id to update matches the user's current plan-id, then no
	 * changes in the datasource is performed and method execution is halted.
	 *
	 * @param userPlanUpdationRequestDto containing user's new plan details.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 * @throws InvalidPlanException if no plan exists with provided-id.
	 */
	public void update(@NonNull final UserPlanUpdationRequestDto userPlanUpdationRequestDto) {
		final var planId = userPlanUpdationRequestDto.getPlanId();
		final var isPlanIdValid = planRepository.existsById(planId);
		if (Boolean.FALSE.equals(isPlanIdValid)) {
			throw new InvalidPlanException("No plan exists in the system with provided-id");
		}

		final var userId = authenticatedUserIdProvider.getUserId();
		final var isExistingUserPlan = userPlanMappingRepository.isActivePlan(userId, planId);
		if (Boolean.TRUE.equals(isExistingUserPlan)) {
			return;
		}

		userPlanMappingRepository.deactivateCurrentPlan(userId);

		final var newPlan = new UserPlanMapping();
		newPlan.setUserId(userId);
		newPlan.setPlanId(planId);
		userPlanMappingRepository.save(newPlan);

		rateLimitingService.reset(userId);
	}

	/**
	 * Retrieves all available subscription plans.
	 *
	 * @return List of PlanResponseDto containing details of each available plan.
	 */
	public List<PlanResponseDto> retrieve() {
		return planRepository.findAll()
				.stream()
				.map(plan -> PlanResponseDto.builder()
						.id(plan.getId())
						.name(plan.getName())
						.requestsAllowed(plan.getRequestsAllowed())
						.timeUnit(plan.getTimeUnit())
						.build())
				.toList();
	}

}