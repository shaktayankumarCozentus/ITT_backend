package com.itt.service.dto.user_management;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Active role definition")
public class RoleDto {

    @Schema(description = "Unique role identifier", example = "role_101")
    private Integer roleId;

    @Schema(description = "Display name of the role", example = "Admin")
    private String roleName;
}
