package com.itt.service.dto.role;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder      // ← add builder
public class RoleWithPrivilegesDto {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;

    // ← new field
    private ConfigDto roleType;

    private LandingPageDto landingPage;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;

    private List<FeaturePrivilegeDTO> privilegeHierarchy;
    private List<ConfigDto> skinConfigs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LandingPageDto {
        private Long id;
        private String key;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigDto {
        private Long id;
        private String key;
        private String name;
    }
}