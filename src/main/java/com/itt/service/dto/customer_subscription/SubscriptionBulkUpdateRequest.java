package com.itt.service.dto.customer_subscription;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionBulkUpdateRequest {

	@NotEmpty(message = "Company codes list cannot be empty")
	private List<@NotNull(message = "Company code cannot be null") Integer> companyCodes;

	@NotNull(message = "Subscription tier type must be specified")
	private Integer subscriptionTierType;

	private Boolean isAllSelected;
}