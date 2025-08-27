package com.itt.service.fw.ratelimit.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Jacksonized
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Schema(title = "User", accessMode = Schema.AccessMode.READ_ONLY)
public class UserResponseDto {

    @Schema(description = "Unique identifier of the user", example = "f72f7a28-936e-41f3-8e38-92f5e13a64d5")
    private UUID id;

    @Schema(description = "Email address of the user", example = "user@example.com")
    private String emailId;

    @Schema(description = "Timestamp when the user was created", example = "2024-07-01T10:45:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "Plan mapped to the user ", example = "FREE/BUSINESS/PROFESSIONAL")
    private String planName;
}
