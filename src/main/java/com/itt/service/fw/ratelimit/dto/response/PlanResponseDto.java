package com.itt.service.fw.ratelimit.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.itt.service.fw.ratelimit.enums.TimeUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Getter
@Builder
@Jacksonized
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Schema(title = "Plan", accessMode = Schema.AccessMode.READ_ONLY)
public class PlanResponseDto {

	@Schema(description = "Unique identifier of the plan", example = "a38e2b9a-ff7f-42a0-8b1c-4ff6d2b9a0cc")
	private UUID id;

	@Schema(description = "Name of the plan", example = "PROFESSIONAL")
	private String name;

	@Schema(description = "Number of requests allowed for the specified time unit", example = "100")
	private Integer requestsAllowed;

	@Schema(description = "Unit of time associated with the rate limit", example = "HOUR", allowableValues = {"SECOND", "MINUTE", "HOUR"})
	private TimeUnit timeUnit;
}
