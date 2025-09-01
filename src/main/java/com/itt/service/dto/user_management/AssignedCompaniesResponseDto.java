package com.itt.service.dto.user_management;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Grouped assigned companies for a user")
public class AssignedCompaniesResponseDto {

    @Schema(description = "Companies flagged as PSA (psa_flag == 1)")
    private List<AssignedCompanyDto> psaCompanies;

    @Schema(description = "Companies NOT flagged as PSA (psa_flag == 0 or NULL)")
    private List<AssignedCompanyDto> nonPsaCompanies;
}