package com.itt.service.dto.customer_subscription;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for copying subscription features between companies.
 * <p>
 * This DTO enables copying subscription configurations and features from one
 * source company to multiple target companies. It supports:
 * <ul>
 *   <li><strong>Configuration replication:</strong> copies subscription setup from source</li>
 *   <li><strong>Selective feature copying:</strong> specify which features to copy</li>
 *   <li><strong>Bulk target updates:</strong> apply to multiple companies at once</li>
 *   <li><strong>Validation:</strong> ensures all required identifiers are provided</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>
 * {@code
 * SubscriptionCopyRequest request = new SubscriptionCopyRequest();
 * request.setSourceCompanyId(1001);                    // Copy from company 1001
 * request.setTargetCompanyIds(Arrays.asList(1002, 1003)); // Copy to companies 1002, 1003
 * request.setFeatureIds(Arrays.asList(101, 102, 103));     // Copy specific features
 * 
 * // Process via REST endpoint
 * POST /api/customer-subscriptions/copy
 * }
 * </pre>
 * 
 * @see com.itt.service.controller.CustomerSubscriptionController
 * @see com.itt.service.service.CustomerSubscriptionService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCopyRequest {
    
    /**
     * Company ID to copy subscription configuration from.
     * <p>
     * This company must:
     * <ul>
     *   <li>Exist in the system</li>
     *   <li>Have a valid subscription configuration</li>
     *   <li>Have the features specified in featureIds</li>
     * </ul>
     * 
     * @validation NotNull - source company must be specified
     */
    @NotNull(message = "Source company ID must be specified")
    private Integer sourceCompanyId;
    
    /**
     * List of company IDs to copy subscription configuration to.
     * <p>
     * Target companies must:
     * <ul>
     *   <li>Be a non-empty list (at least one target)</li>
     *   <li>Contain valid, existing company identifiers</li>
     *   <li>All be non-null values</li>
     *   <li>Be different from the source company</li>
     * </ul>
     * 
     * @validation NotEmpty - must specify at least one target company
     * @validation NotNull - individual company IDs cannot be null
     */
    @NotEmpty(message = "Target company IDs list cannot be empty")
    private List<@NotNull(message = "Target company ID cannot be null") Integer> targetCompanyIds;
}