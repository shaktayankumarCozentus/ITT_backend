package com.itt.service.dto.customer_subscription;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for bulk subscription tier updates.
 * <p>
 * This DTO encapsulates the data required to update subscription tiers for
 * multiple companies in a single operation. It supports:
 * <ul>
 *   <li><strong>Bulk processing:</strong> updates multiple companies efficiently</li>
 *   <li><strong>Atomic operations:</strong> all updates succeed or fail together</li>
 *   <li><strong>Validation:</strong> ensures required fields are provided</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>
 * {@code
 * SubscriptionBulkUpdateRequest request = new SubscriptionBulkUpdateRequest();
 * request.setCompanyCodes(Arrays.asList(1001, 1002, 1003));
 * request.setSubscriptionTierType(2); // Premium tier
 * 
 * // Process via REST endpoint
 * POST /api/customer-subscriptions/bulk-update-tier
 * }
 * </pre>
 * 
 * @see com.itt.service.controller.CustomerSubscriptionController
 * @see com.itt.service.service.CustomerSubscriptionService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionBulkUpdateRequest {
    
    /**
     * List of company codes to update.
     * <p>
     * Company codes must:
     * <ul>
     *   <li>Be non-empty list (at least one company)</li>
     *   <li>Contain valid, existing company identifiers</li>
     *   <li>All be non-null values</li>
     * </ul>
     * 
     * @validation NotEmpty - list must contain at least one element
     * @validation NotNull - individual elements cannot be null
     */
    @NotEmpty(message = "Company codes list cannot be empty")
    private List<@NotNull(message = "Company code cannot be null") Integer> companyCodes;
    
    /**
     * The new subscription tier type to apply to all specified companies.
     * <p>
     * Subscription tier types typically represent:
     * <ul>
     *   <li>1 = Basic/Free tier</li>
     *   <li>2 = Premium tier</li>
     *   <li>3 = Enterprise tier</li>
     * </ul>
     * <p>
     * Must reference a valid subscription tier defined in the system.
     * 
     * @validation NotNull - subscription tier must be specified
     */
    @NotNull(message = "Subscription tier type must be specified")
    private Integer subscriptionTierType;
    
    /**
	 * Flag indicating if the update should apply to all companies in the system.
	 * <p>
	 * If set to true, the {@code companyCodes} list is ignored and the update
	 * applies to all companies. If false, only the specified companies are updated.
	 * <p>
	 * Default is false.
	 */
    private Boolean isAllSelected;
}