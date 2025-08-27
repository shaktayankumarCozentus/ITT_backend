package com.itt.service.config.openapi;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.DataTableRequest;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.master.MasterConfigDTO;
import com.itt.service.dto.peta_petd.PetaPetdBulkUpdateRequest;
import com.itt.service.dto.peta_petd.PetaPetdDTO;
import com.itt.service.dto.peta_petd.PetaPetdUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

/**
 * OpenAPI documentation configuration for PetaPetdManagementController endpoints.
 * <p>
 * This class provides OpenAPI/Swagger documentation for the following endpoints:
 * <ul>
 *   <li><b>POST /api/v1/peta-petd-management/list</b> - Retrieves a paginated list of PETA/PETD records.</li>
 *   <li><b>GET /api/v1/peta-petd-management/configs</b> - Fetches all available PETA/PETD configuration options.</li>
 *   <li><b>PUT /api/v1/peta-petd-management/bulk-update-tier</b> - Performs a bulk update of PETA/PETD tiers.</li>
 *   <li><b>PUT /api/v1/peta-petd-management/update</b> - Updates a single PETA/PETD record.</li>
 * </ul>
 * <p>
 * Each endpoint is secured and returns a standardized ApiResponse wrapper.
 * <p>
 * To extend documentation, add OpenAPI annotations to this class or directly to the controller methods.
 */
public interface PetaPetdManagementDocumentation {

    @Operation(
        summary = "Get paginated PETA/PETD records",
        description = "Retrieve a paginated list of PETA/PETD records with filtering and sorting options.",
        operationId = "getPetaPetdManagement"
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<PaginationResponse<PetaPetdDTO>>> getPetaPetdManagement(
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
        summary = "Get all PETA/PETD configs",
        description = "Fetch all available PETA/PETD configuration options.",
        operationId = "getAllPetaPetdConfigs"
    )
    @CommonApiResponses.StandardGetResponse
    ResponseEntity<ApiResponse<List<MasterConfigDTO>>> getAllPetaPetdConfigs();

    @Operation(
        summary = "Bulk update PETA/PETD tier",
        description = "Update the tier for multiple PETA/PETD records in bulk.",
        operationId = "bulkUpdatePetaPetd"
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<Void>> bulkUpdatePetaPetd(
        @Parameter(
            description = "Bulk update request for PETA/PETD tiers",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PetaPetdBulkUpdateRequest.class)
            )
        )
        @Valid @RequestBody PetaPetdBulkUpdateRequest request
    );

    @Operation(
        summary = "Update a PETA/PETD record",
        description = "Update details of a single PETA/PETD record.",
        operationId = "updatePetaPetd"
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<Void>> updatePetaPetd(
        @Parameter(
            description = "Update request for a PETA/PETD record",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PetaPetdUpdateRequest.class)
            )
        )
        @Valid @RequestBody PetaPetdUpdateRequest request
    );
}