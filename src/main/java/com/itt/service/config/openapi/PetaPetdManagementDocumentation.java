package com.itt.service.config.openapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.PaginationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

public interface PetaPetdManagementDocumentation {

    @Operation(
        summary = "Get paginated PETA/PETD records",
        description = "Retrieve a paginated list of PETA/PETD records with filtering and sorting options.",
        operationId = "getPetaPetdManagement",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Pagination and filtering criteria",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.itt.service.dto.DataTableRequest.class),
                examples = @ExampleObject(
                    name = "Peta/PETD List Request",
                    value = com.itt.service.config.openapi.ApiExamples.PetaPetdManagement.LIST_REQUEST
                )
            )
        )
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<PaginationResponse<com.itt.service.dto.peta_petd.PetaPetdDTO>>> getPetaPetdManagement(
        @Valid @RequestBody com.itt.service.dto.DataTableRequest request
    );

    @Operation(
        summary = "Get all PETA/PETD configs",
        description = "Fetch all available PETA/PETD configuration options.",
        operationId = "getAllPetaPetdConfigs"
    )
    @CommonApiResponses.StandardGetResponse
    ResponseEntity<ApiResponse<java.util.List<com.itt.service.dto.master.MasterConfigDTO>>> getAllPetaPetdConfigs();

    @Operation(
        summary = "Bulk update PETA/PETD calling flag",
        description = "Bulk enable/disable PETA calling across selected companies. Supports either explicit companyCodes list or selecting all via isAllSelected flag.",
        operationId = "bulkUpdatePetaPetd",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Bulk update request body",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.itt.service.dto.peta_petd.PetaPetdBulkUpdateRequest.class),
                examples = @ExampleObject(
                    name = "Bulk Update Peta/PETD Calling Request",
                    value = """
                        {
                          \"companyCodes\": [101, 102, 103],
                          \"petaCalling\": true,
                          \"isAllSelected\": false
                        }
                        """
                )
            )
        )
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<Void>> bulkUpdatePetaPetd(
        @Valid @RequestBody com.itt.service.dto.peta_petd.PetaPetdBulkUpdateRequest request
    );

    @Operation(
        summary = "Update a single PETA/PETD company setting",
        description = "Update PETA/PETD calling and frequency configuration for a single company.",
        operationId = "updatePetaPetd",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Single company update request body",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.itt.service.dto.peta_petd.PetaPetdUpdateRequest.class),
                examples = @ExampleObject(
                    name = "Update Peta/PETD Company Request",
                    value = """
                        {
                          \"companyCode\": 101,
                          \"petaCalling\": false,
                          \"oceanFrequencyType\": 2,
                          \"airFrequencyType\": 1,
                          \"railRoadFrequencyType\": 3
                        }
                        """
                )
            )
        )
    )
    @CommonApiResponses.UpdateResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<Void>> updatePetaPetd(
        @Valid @RequestBody com.itt.service.dto.peta_petd.PetaPetdUpdateRequest request
    );
}