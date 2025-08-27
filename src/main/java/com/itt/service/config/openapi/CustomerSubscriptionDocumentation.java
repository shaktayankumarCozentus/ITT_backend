package com.itt.service.config.openapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.customer_subscription.CustomerSubscriptionDTO;
import com.itt.service.dto.customer_subscription.SubscriptionBulkUpdateRequest;
import com.itt.service.dto.customer_subscription.SubscriptionCopyRequest;
import com.itt.service.dto.customer_subscription.SubscriptionUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

public interface CustomerSubscriptionDocumentation {

    @Operation(
        summary = "Get paginated customer subscriptions",
        description = "Retrieve a paginated list of customer subscriptions with filtering and sorting options.",
        operationId = "getCustomerSubscriptions"
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<PaginationResponse<CustomerSubscriptionDTO>>> getCustomerSubscriptions(
        @Parameter(
            description = "Pagination and filtering criteria",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DataTableRequest.class)
            )
        )
        @Valid @RequestBody DataTableRequest request
    );

    @Operation(
        summary = "Bulk update subscription tier",
        description = "Update the subscription tier for multiple customer subscriptions in bulk.",
        operationId = "bulkUpdateSubscriptionTier"
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<Void>> bulkUpdateSubscriptionTier(
        @Parameter(
            description = "Bulk update request for subscription tiers",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubscriptionBulkUpdateRequest.class)
            )
        )
        @Valid @RequestBody SubscriptionBulkUpdateRequest request
    );

    @Operation(
        summary = "Update a customer subscription",
        description = "Update details of a single customer subscription.",
        operationId = "updateCustomerSubscription"
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<Void>> updateCustomerSubscription(
        @Parameter(
            description = "Update request for a customer subscription",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubscriptionUpdateRequest.class)
            )
        )
        @Valid @RequestBody SubscriptionUpdateRequest request
    );

    @Operation(
        summary = "Copy a customer subscription",
        description = "Copy an existing customer subscription to create a new one.",
        operationId = "copyCustomerSubscription"
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<Void>> copyCustomerSubscription(
        @Parameter(
            description = "Copy request for a customer subscription",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubscriptionCopyRequest.class)
            )
        )
        @Valid @RequestBody SubscriptionCopyRequest request
    );
}
