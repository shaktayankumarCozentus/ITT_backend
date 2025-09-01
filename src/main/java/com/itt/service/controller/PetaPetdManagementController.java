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

import com.itt.service.config.openapi.PetaPetdManagementDocumentation;
import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.master.MasterConfigDTO;
import com.itt.service.dto.peta_petd.PetaPetdBulkUpdateRequest;
import com.itt.service.dto.peta_petd.PetaPetdDTO;
import com.itt.service.dto.peta_petd.PetaPetdUpdateRequest;
import com.itt.service.service.PetaPetdManagementService;
import com.itt.service.util.ResponseBuilder;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/peta-petd-management")
@Slf4j
@Tag(name = "Peta/PETD Management", description = "APIs for managing Peta/PETD settings for companies")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class PetaPetdManagementController implements PetaPetdManagementDocumentation {

	private final PetaPetdManagementService petaPetdManagementService;

	@PostMapping("/list")
	@PreAuthorize("hasRole('ROLE_PETA_PETD_MANAGEMENT_VIEW')")
	public ResponseEntity<ApiResponse<PaginationResponse<PetaPetdDTO>>> getPetaPetdManagement(
			@Valid @RequestBody DataTableRequest request) {
		PaginationResponse<PetaPetdDTO> response = petaPetdManagementService.findAll(request);
		return ResponseBuilder.dynamicResponse(response);
	}

	@GetMapping("/configs")
	@PreAuthorize("hasRole('ROLE_PETA_PETD_MANAGEMENT_VIEW')")
	public ResponseEntity<ApiResponse<List<MasterConfigDTO>>> getAllPetaPetdConfigs() {
		List<MasterConfigDTO> configs = petaPetdManagementService.getAllPetaPetdConfigs();
		return ResponseBuilder.success(configs);
	}

	@PutMapping("/bulk-update")
	@PreAuthorize("hasRole('ROLE_PETA_PETD_MANAGEMENT_EDIT')")
	public ResponseEntity<ApiResponse<Void>> bulkUpdatePetaPetd(@Valid @RequestBody PetaPetdBulkUpdateRequest request) {
		String responseMessage = petaPetdManagementService.bulkUpdatePetaPetd(request);
		return ResponseBuilder.success(responseMessage);
	}

	@PutMapping("/update")
	@PreAuthorize("hasRole('ROLE_PETA_PETD_MANAGEMENT_EDIT')")
	public ResponseEntity<ApiResponse<Void>> updatePetaPetd(@Valid @RequestBody PetaPetdUpdateRequest request) {
		String responseMessage = petaPetdManagementService.updatePetaPetd(request);
		return ResponseBuilder.success(responseMessage);
	}
}