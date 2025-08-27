package com.itt.service.dto.user_management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload to assign role and companies to user")
public class AccessAssignmentRequestDto {

    @Schema(description = "Role ID to assign", example = "role_123")
    private Integer roleId;

    @Schema(description = "Whether the user is a BDP employee", example = "false")
    private Boolean isBdpEmployee;

    @Schema(description = "List of companies to assign")
    private List<AssignedCompanyDto> assignedCompanies;
}
