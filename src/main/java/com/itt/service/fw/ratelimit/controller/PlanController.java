package com.itt.service.fw.ratelimit.controller;

import com.itt.service.fw.audit.annotation.EventAuditLogger;
import com.itt.service.fw.ratelimit.configuration.BypassRateLimit;
import com.itt.service.fw.ratelimit.configuration.PublicEndpoint;
import com.itt.service.fw.ratelimit.dto.request.UserPlanUpdationRequestDto;
import com.itt.service.fw.ratelimit.dto.response.ExceptionResponseDto;
import com.itt.service.fw.ratelimit.dto.response.PlanResponseDto;
import com.itt.service.fw.ratelimit.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/plans")
@Tag(name = "Plan Management", description = "✅ STANDARDIZED: Public endpoints for managing and retrieving available plan details")
public class PlanController {

	private final PlanService planService;

	@PublicEndpoint
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get all available plans", description = "✅ STANDARDIZED: Retrieves the list of available plans in the system")
	@ApiResponse(responseCode = "200", description = "Plans retrieved successfully")
	public ResponseEntity<List<PlanResponseDto>> retrieve() {
		return ResponseEntity.ok(planService.retrieve());
	}

	@BypassRateLimit
	@PutMapping
	@Operation(summary = "Update user plan", description = "✅ STANDARDIZED: Updates an existing plan of an authenticated user")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Plan updated successfully",
					content = @Content(schema = @Schema(implementation = Void.class))),
			@ApiResponse(responseCode = "404", description = "No plan exists in the system with provided-id",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
			@ApiResponse(responseCode = "429", description = "API rate limit exhausted",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request body",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))})
	public ResponseEntity<HttpStatus> update(@Valid @RequestBody final UserPlanUpdationRequestDto planUpdationRequest) {
		planService.update(planUpdationRequest);
		return ResponseEntity.ok().build();
	}

}