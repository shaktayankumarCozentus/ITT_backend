package com.itt.service.fw.ratelimit.dto.request;

import com.itt.service.fw.ratelimit.enums.TimeUnit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlanCreateRequestDto(

        @NotBlank(message = "Plan name must not be blank")
        String name,

        @NotNull(message = "Requests allowed must not be null")
        @Min(value = 1, message = "Requests allowed must be at least 1")
        Integer requestsAllowed,

        @NotNull(message = "Time unit must be provided")
        TimeUnit timeUnit
) {}

