package com.itt.service.dto.user_management;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Summary comparing access between two users")
public class AccessSummaryResponseDto {

    @Schema(description = "User ID (primary key)", example = "456")
    private Integer userId;

    @Schema(description = "List of company codes assigned", example = "[\"comp_001\", \"comp_005\"]")
    private List<Integer> assignedCompanies;

    @Schema(description = "Assigned role ID", example = "role_123")
    private Integer roleId;

    @Schema(description = "Assigned role ID", example = "role_123")
    private String roleName;
}
