package com.itt.service.dto.user_management;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to copy access from source to target user")
public class CopyAccessRequestDto {

    @Schema(description = "Target user ID (primary key)", example = "456")
    private Integer targetUserId;

    @Schema(description = "Source user ID (primary key)", example = "123")
    private Integer sourceUserId;

    @Schema(description = "Request type: preview or apply", example = "apply")
    private String requestType;
}
