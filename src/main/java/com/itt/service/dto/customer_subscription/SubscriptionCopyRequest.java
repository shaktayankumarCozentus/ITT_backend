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
public class SubscriptionCopyRequest {
    
    @NotNull(message = "Source company ID must be specified")
    private Integer sourceCompanyId;
    
    @NotEmpty(message = "Target company IDs list cannot be empty")
    private List<@NotNull(message = "Target company ID cannot be null") Integer> targetCompanyIds;
}