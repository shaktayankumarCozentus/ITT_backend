package com.itt.service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.CompanyDTO;
import com.itt.service.dto.master.MasterConfigDTO;
import com.itt.service.service.MasterDataService;
import com.itt.service.util.ResponseBuilder;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/master")
@RequiredArgsConstructor
@Tag(name = "Master Data", description = "APIs for retrieving master data such as companies and configuration values")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MasterDataController {
	private final MasterDataService masterDataService;

	@GetMapping("/companies")
	public ResponseEntity<ApiResponse<List<CompanyDTO>>> getCompanies() {
		List<CompanyDTO> response = masterDataService.getParentCompanies();
		return ResponseBuilder.success(response);
	}

	@GetMapping("/config/key")
	public ResponseEntity<ApiResponse<List<MasterConfigDTO>>> getConfigByKey(@RequestParam String keyCode) {
		List<MasterConfigDTO> response = masterDataService.getConfigByKey(keyCode);
		return ResponseBuilder.success(response);
	}

	@GetMapping("/config/type")
	public ResponseEntity<ApiResponse<List<MasterConfigDTO>>> getConfigByType(@RequestParam String configType) {
		List<MasterConfigDTO> response = masterDataService.getConfigByType(configType);
		return ResponseBuilder.success(response);
	}
}
