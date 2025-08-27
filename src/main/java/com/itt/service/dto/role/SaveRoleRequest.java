package com.itt.service.dto.role;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.itt.service.validator.SaveRoleRequestConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SaveRoleRequestConstraint
public class SaveRoleRequest {
    /**
     * If present → update, otherwise → create new
     */
    private Long id;

    private String name;

    private String description;

    @NotNull
    private Boolean isActive = true;

    private Boolean customLanding = false;
    private Long landingPageConfigId;

    @NotNull
    private Long roleTypeConfigId;

    @NotEmpty
    private List<Long> skinConfigIds;

    @NotEmpty
    private List<Long> privilegeIds;

    // Audit fields for tracking user operations
    private Integer createdById;
    private Integer updatedById;
}
