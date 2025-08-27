package com.itt.service.dto.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkinGroupDto {
    private RoleTypeDto roleType;       // Enhanced to include full role type details
    private List<SkinDto> skins;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleTypeDto {
        private Long id;
        private String key;
        private String name;
    }
}
