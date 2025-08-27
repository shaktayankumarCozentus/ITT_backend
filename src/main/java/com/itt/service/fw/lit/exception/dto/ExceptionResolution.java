package com.itt.service.fw.lit.exception.dto;

import org.springframework.http.HttpStatus;

public record ExceptionResolution(String litCode, HttpStatus status) {}
