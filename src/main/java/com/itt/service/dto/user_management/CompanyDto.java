package com.itt.service.dto.user_management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Company info used in tree structure")
public class CompanyDto {

    @Schema(description = "Unique company code", example = "comp_001")
    private String companyCode;

    @Schema(description = "Company display name", example = "ABC Corp")
    private String companyName;

    @Schema(description = "Subscription type of the company", example = "PAID")
    private String subscriptionType;

    @Schema(description = "Onboarding date", example = "2023-09-10T10:00:00")
    private LocalDateTime onboardedOn;

    @Schema(description = "Child companies under current node")
    private List<CompanyDto> children;
}
