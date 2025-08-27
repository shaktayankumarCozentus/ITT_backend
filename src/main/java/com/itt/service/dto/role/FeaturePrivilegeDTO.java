package com.itt.service.dto.role;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeaturePrivilegeDTO {
    private Long categoryId;
    private String categoryName;
    private String categoryKey;
    private List<FeatureDTO> features;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureDTO {
        private Long featureId;
        private String featureName;
        private String featureKey;
        private List<PrivilegeDTO> privileges;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrivilegeDTO {
        private Long privilegeId;
        private String privilegeName;
        private String privilegeKey;
        private Long mapCatFeatPrivId;
        private Boolean isSelected;
    }
}