package com.itt.service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itt.service.config.openapi.CustomerSubscriptionDocumentation;
import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.CompanyRequestDTO;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.customer_subscription.CompanyDTO;
import com.itt.service.dto.customer_subscription.CustomerSubscriptionDTO;
import com.itt.service.dto.customer_subscription.SubscriptionBulkUpdateRequest;
import com.itt.service.dto.customer_subscription.SubscriptionCopyRequest;
import com.itt.service.dto.customer_subscription.SubscriptionUpdateRequest;
import com.itt.service.dto.master.MasterConfigDTO;
import com.itt.service.service.CustomerSubscriptionService;
import com.itt.service.util.ResponseBuilder;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/customer-subscriptions")
@RequiredArgsConstructor
@Tag(name = "Customer Subscription", description = "APIs for managing customer subscriptions, features, and subscription tiers")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CustomerSubscriptionController implements CustomerSubscriptionDocumentation {
	private final CustomerSubscriptionService customerSubscriptionService;

	@PostMapping("/list")
	@PreAuthorize("hasRole('ROLE_SUBSCRIPTION_MANAGEMENT_VIEW')")
	public ResponseEntity<ApiResponse<PaginationResponse<CustomerSubscriptionDTO>>> getCustomerSubscriptions(
			@Valid @RequestBody DataTableRequest request) {
		PaginationResponse<CustomerSubscriptionDTO> response = customerSubscriptionService.findAllWithFeatures(request);
		return ResponseBuilder.dynamicResponse(response);
	}

	@GetMapping("/configs")
	@PreAuthorize("hasRole('ROLE_SUBSCRIPTION_MANAGEMENT_VIEW')")
	public ResponseEntity<ApiResponse<List<MasterConfigDTO>>> getAllSubscriptionConfigs() {
		return ResponseBuilder.success(customerSubscriptionService.getAllFeaturesSubscriptionTypeFromMasterConfig());
	}

	@PutMapping("/bulk-update-tier")
	@PreAuthorize("hasRole('ROLE_SUBSCRIPTION_MANAGEMENT_EDIT')")
	public ResponseEntity<ApiResponse<Void>> bulkUpdateSubscriptionTier(
			@Valid @RequestBody SubscriptionBulkUpdateRequest request) {
		String responseMessage = customerSubscriptionService.bulkUpdateSubscriptionTier(request);
		return ResponseBuilder.success(responseMessage);
	}

	@PutMapping("/update")
	@PreAuthorize("hasRole('ROLE_SUBSCRIPTION_MANAGEMENT_EDIT')")
	public ResponseEntity<ApiResponse<Void>> updateCustomerSubscription(
			@Valid @RequestBody SubscriptionUpdateRequest request) {
		String responseMessage = customerSubscriptionService.updateCustomerSubscription(request);
		return ResponseBuilder.success(responseMessage);
	}

	@PostMapping("/copy")
	@PreAuthorize("hasRole('ROLE_SUBSCRIPTION_MANAGEMENT_EDIT')")
	public ResponseEntity<ApiResponse<Void>> copyCustomerSubscription(
			@Valid @RequestBody SubscriptionCopyRequest request) {
		String responseMessage = customerSubscriptionService.copyCustomerSubscription(request);
		return ResponseBuilder.success(responseMessage);
	}
	
	@PostMapping("/companies")
	@PreAuthorize("hasRole('ROLE_SUBSCRIPTION_MANAGEMENT_VIEW')")
	public ResponseEntity<ApiResponse<List<CompanyDTO>>> getCompanyList(
			@Valid @RequestBody CompanyRequestDTO request) {
		List<CompanyDTO> response = customerSubscriptionService.getCompanyList(request);
		return ResponseBuilder.dynamicResponse(response);
	}
}
