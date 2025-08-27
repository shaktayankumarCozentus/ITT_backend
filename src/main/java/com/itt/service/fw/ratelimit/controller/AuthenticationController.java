package com.itt.service.fw.ratelimit.controller;

import com.itt.service.fw.audit.annotation.EventAuditLogger;
import com.itt.service.fw.ratelimit.configuration.PublicEndpoint;
import com.itt.service.fw.ratelimit.dto.request.UserCreationRequestDto;
import com.itt.service.fw.ratelimit.dto.request.UserLoginRequestDto;
import com.itt.service.fw.ratelimit.dto.response.ExceptionResponseDto;
import com.itt.service.fw.ratelimit.dto.response.TokenSuccessResponseDto;
import com.itt.service.fw.ratelimit.dto.response.UserResponseDto;
import com.itt.service.fw.ratelimit.service.UserService;
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
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "✅ STANDARDIZED: Authentication and user account management endpoints")
public class AuthenticationController {

	private final UserService userService;

	@PublicEndpoint
	@PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Register new user", description = "✅ STANDARDIZED: Creates a unique user record in the system corresponding to the provided information")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "201", description = "User record created successfully",
					content = @Content(schema = @Schema(implementation = Void.class))),
			@ApiResponse(responseCode = "409", description = "User account with provided email-id already exists",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
			@ApiResponse(responseCode = "404", description = "No plan exists in the system with provided-id",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request body",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))) })
	public ResponseEntity<HttpStatus> createUser(@Valid @RequestBody final UserCreationRequestDto userCreationRequest) {
		userService.create(userCreationRequest);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}


	@PublicEndpoint
	@GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Retrieve all users", description = "✅ STANDARDIZED: Retrieves the list of available users in the system")
	@ApiResponse(responseCode = "200", description = "Users retrieved successfully")
	public ResponseEntity<List<UserResponseDto>> retrieve() {
		return ResponseEntity.ok(userService.retrieve());
	}

	@PublicEndpoint
	@PostMapping(value = "/login")
	@Operation(summary = "User login", description = "✅ STANDARDIZED: Validates user login credentials and returns access-token on successful authentication")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Authentication successfull"),
			@ApiResponse(responseCode = "401", description = "Invalid credentials provided. Failed to authenticate user",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request body",
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))) })
	public ResponseEntity<TokenSuccessResponseDto> login(
			@Valid @RequestBody final UserLoginRequestDto userLoginRequest) {
		final var response = userService.login(userLoginRequest);
		return ResponseEntity.ok(response);
	}

}