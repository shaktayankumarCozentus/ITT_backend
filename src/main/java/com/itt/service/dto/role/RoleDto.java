package com.itt.service.dto.role;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class RoleDto {
    private Long id;
    private String name;
    private String description;
    private boolean isActive;
    private Long roleTypeConfigId;
    private Long landingPageConfigId;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;

    public RoleDto(Long id, String name, String description, boolean isActive,
                   Long roleTypeConfigId, Long landingPageConfigId,
                   LocalDateTime createdOn, String createdBy,
                   LocalDateTime updatedOn, String updatedBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.roleTypeConfigId = roleTypeConfigId;
        this.landingPageConfigId = landingPageConfigId;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.updatedOn = updatedOn;
        this.updatedBy = updatedBy;
    }
}
