package com.itt.service.dto.user_management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user company information with enhanced search capabilities.
 * 
 * Represents company data for users in a paginated, searchable format.
 * Includes essential company identifiers and names for UI display and filtering.
 * 
 * @author System Generated
 * @since 2025-08-26
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User company information with search support")
public class UserCompanyDto {

    @Schema(description = "Unique company identifier", example = "123", required = true)
    private Integer companyId;

    @Schema(description = "Company display name", example = "ABC Corporation Ltd", required = true)
    private String companyName;
}