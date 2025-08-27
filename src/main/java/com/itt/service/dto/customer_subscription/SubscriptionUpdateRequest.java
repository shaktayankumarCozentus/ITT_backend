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
public class SubscriptionUpdateRequest {
    @NotNull
    private Integer companyCode;
    @NotNull
    private Integer subscriptionTierType;
    @NotEmpty
    private List<@NotNull Integer> featureIds;
}
