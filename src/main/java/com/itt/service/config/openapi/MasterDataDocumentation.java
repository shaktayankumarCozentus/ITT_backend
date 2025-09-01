package com.itt.service.config.openapi;


import org.springframework.http.ResponseEntity;

import com.itt.service.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;


public interface MasterDataDocumentation {

    @Operation(
        summary = "Get all parent companies",
        description = "Retrieve a list of all parent companies available in the system.",
        operationId = "getCompanies"
    )
    @CommonApiResponses.StandardGetResponse
    ResponseEntity<ApiResponse<java.util.List<com.itt.service.dto.CompanyDTO>>> getCompanies();

    @Operation(
        summary = "Get configuration by key",
        description = "Retrieve master configuration values by a specific key code.",
        operationId = "getConfigByKey",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(
                name = "keyCode",
                description = "Key code to filter configuration values",
                required = true,
                example = "SOME_KEY",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Key Code Example",
                    value = "SOME_KEY"
                )
            )
        }
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<java.util.List<com.itt.service.dto.master.MasterConfigDTO>>> getConfigByKey(
        String keyCode
    );

    @Operation(
        summary = "Get configuration by type",
        description = "Retrieve master configuration values by configuration type.",
        operationId = "getConfigByType",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(
                name = "configType",
                description = "Configuration type to filter values",
                required = true,
                example = "TYPE_A",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "Config Type Example",
                    value = "TYPE_A"
                )
            )
        }
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<java.util.List<com.itt.service.dto.master.MasterConfigDTO>>> getConfigByType(
        String configType
    );
}
