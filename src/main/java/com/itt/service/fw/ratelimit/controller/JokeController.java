package com.itt.service.fw.ratelimit.controller;

import com.itt.service.fw.audit.annotation.EventAuditLogger;
import com.itt.service.fw.ratelimit.dto.response.ExceptionResponseDto;
import com.itt.service.fw.ratelimit.dto.response.JokeResponseDto;
import com.itt.service.fw.ratelimit.utility.JokeGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/jokes")
@Tag(name = "Joke Generator", description = "✅ STANDARDIZED: Public endpoint for generating random unfunny jokes")
public class JokeController {

	private final JokeGenerator jokeGenerator;

	@GetMapping(value = "/random", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Generate random joke", description = "✅ STANDARDIZED: Generates a random unfunny joke")
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "200",
					description = "Successfully generated random unfunny joke",
					headers = @Header(
							name = "X-Rate-Limit-Remaining",
							description = "The number of remaining API invocations available with the user after processing the request.",
							required = true,
							schema = @Schema(type = "integer")
					)
			),
			@ApiResponse(
					responseCode = "429",
					description = "API rate limit exhausted",
					headers = {
							@Header(
									name = "X-Rate-Limit-Retry-After-Seconds",
									description = "Wait period in seconds before the user can invoke the API endpoint.",
									required = true,
									schema = @Schema(type = "integer")
							),
							@Header(
									name = "X-Rate-Limit-Retry-After",
									description = "Human-readable formatted wait period (e.g., '1 H 2 M 3 S').",
									required = true,
									schema = @Schema(type = "string")
							)
					},
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))
			)
	})
	public ResponseEntity<JokeResponseDto> generate() {
		final var response = jokeGenerator.generate();
		return ResponseEntity.ok(response);
	}

}