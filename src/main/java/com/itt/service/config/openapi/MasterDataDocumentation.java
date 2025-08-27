package com.itt.service.config.openapi;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.CompanyDTO;
import com.itt.service.dto.master.MasterConfigDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
    name = "Master Data",
    description = "APIs for retrieving master data such as companies and configuration values"
)
public interface MasterDataDocumentation {

    @Operation(
        summary = "Get all parent companies",
        description = "Retrieve a list of all parent companies available in the system.",
        operationId = "getCompanies"
    )
    @CommonApiResponses.StandardGetResponse
    ResponseEntity<ApiResponse<List<CompanyDTO>>> getCompanies();

    @Operation(
        summary = "Get configuration by key",
        description = "Retrieve master configuration values by a specific key code.",
        operationId = "getConfigByKey"
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<List<MasterConfigDTO>>> getConfigByKey(
        @Parameter(
            description = "Key code to filter configuration values",
            required = true,
            example = "SOME_KEY"
        )
        @RequestParam String keyCode
    );

    @Operation(
        summary = "Get configuration by type",
        description = "Retrieve master configuration values by configuration type.",
        operationId = "getConfigByType"
    )
    @CommonApiResponses.StandardGetResponse
    @CommonApiResponses.ValidationResponse
    ResponseEntity<ApiResponse<List<MasterConfigDTO>>> getConfigByType(
        @Parameter(
            description = "Configuration type to filter values",
            required = true,
            example = "TYPE_A"
        )
        @RequestParam String configType
    );
}
