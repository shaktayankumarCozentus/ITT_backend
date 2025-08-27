package com.itt.service.dto.user_management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User count result for active/inactive role")
public class UserCountResponseDto {

    @Schema(description = "Total number of users", example = "42")
    private Long count;
}