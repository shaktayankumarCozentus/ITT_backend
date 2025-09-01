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
        operationId = "getCustomerSubscriptions",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Pagination and filtering criteria",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Customer Subscription List Request Example",
                    value = com.itt.service.config.openapi.ApiExamples.CustomerSubscription.LIST_REQUEST
                )
            )
        )
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<PaginationResponse<CustomerSubscriptionDTO>>> getCustomerSubscriptions(
        @Valid @RequestBody DataTableRequest request
    );

    @Operation(
        summary = "Get all subscription configs",
        description = "Retrieve all available subscription configuration options.",
        operationId = "getAllSubscriptionConfigs"
    )
    @CommonApiResponses.StandardGetResponse
    ResponseEntity<ApiResponse<java.util.List<com.itt.service.dto.master.MasterConfigDTO>>> getAllSubscriptionConfigs();

    @Operation(
        summary = "Bulk update subscription tier",
        description = "Update the subscription tier for multiple customer subscriptions in bulk.",
        operationId = "bulkUpdateSubscriptionTier",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Bulk update request for subscription tiers",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Bulk Update Subscription Request Example",
                    value = com.itt.service.config.openapi.ApiExamples.CustomerSubscription.BULK_UPDATE_REQUEST
                )
            )
        )
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<Void>> bulkUpdateSubscriptionTier(
        @Valid @RequestBody SubscriptionBulkUpdateRequest request
    );

    @Operation(
        summary = "Update a customer subscription",
        description = "Update details of a single customer subscription.",
        operationId = "updateCustomerSubscription",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Update request for a customer subscription",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Update Customer Subscription Request Example",
                    value = "{\"id\":1,\"tier\":\"PREMIUM\"}"
                )
            )
        )
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<Void>> updateCustomerSubscription(
        @Valid @RequestBody SubscriptionUpdateRequest request
    );

    @Operation(
        summary = "Copy a customer subscription",
        description = "Copy an existing customer subscription to create a new one.",
        operationId = "copyCustomerSubscription",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Copy request for a customer subscription",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Copy Customer Subscription Request Example",
                    value = "{\"sourceId\":1,\"targetCustomer\":\"Acme Corp\"}"
                )
            )
        )
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<Void>> copyCustomerSubscription(
        @Valid @RequestBody SubscriptionCopyRequest request
    );

    @Operation(
        summary = "Get company list",
        description = "Retrieve a list of companies for customer subscriptions.",
        operationId = "getCompanyList",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Company request DTO",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Company List Request Example",
                    value = "{\"filter\":\"ACTIVE\"}"
                )
            )
        )
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<java.util.List<com.itt.service.dto.customer_subscription.CompanyDTO>>> getCompanyList(
        @Valid @RequestBody com.itt.service.dto.CompanyRequestDTO request
    );
}
