package com.itt.service.fw.ratelimit.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Schema(title = "PlanUpdationRequest", accessMode = Schema.AccessMode.WRITE_ONLY)
public class UserPlanUpdationRequestDto {

	@NotNull
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "plan to be attached with user record")
	private UUID planId;

}