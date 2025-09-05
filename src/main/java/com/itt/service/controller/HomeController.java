package com.itt.service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itt.service.dto.ApiResponse;
import com.itt.service.dto.CurrentUserDto;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.home.KpiIndicatorRequestDTO;
import com.itt.service.dto.home.LocationStatsSummaryDTO;
import com.itt.service.dto.home.MapViewRequestDTO;
import com.itt.service.dto.home.PortShipmentsInfoRequestDTO;
import com.itt.service.dto.home.ShipmentSummaryDTO;
import com.itt.service.dto.home.TransportModeKpiDTO;
import com.itt.service.service.HomeService;
import com.itt.service.util.ResponseBuilder;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "Home Page", description = "APIs for home page and general information endpoints")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HomeController {
	private final HomeService homeService;

	@PostMapping("/kpi")
	@PreAuthorize("hasRole('ROLE_HOME_PAGE_VIEW')")
	public ResponseEntity<ApiResponse<TransportModeKpiDTO>> getKpiIndicator(@RequestBody KpiIndicatorRequestDTO request,
			@RequestAttribute(required = true) CurrentUserDto currentUser) {
		TransportModeKpiDTO response = homeService.getKpiIndicator(request, currentUser).toModeCountList();
		return ResponseBuilder.success(response);
	}

	@PostMapping("/map-data")
	@PreAuthorize("hasRole('ROLE_HOME_PAGE_VIEW')")
	public ResponseEntity<ApiResponse<LocationStatsSummaryDTO>> getMapData(@RequestBody MapViewRequestDTO request,
			@RequestAttribute(required = true) CurrentUserDto currentUser) {
		LocationStatsSummaryDTO response = homeService.getMapData(request, currentUser);
		return ResponseBuilder.success(response);
	}

	@PostMapping("/port-shipment-list")
	@PreAuthorize("hasRole('ROLE_HOME_PAGE_VIEW')")
	public ResponseEntity<ApiResponse<PaginationResponse<ShipmentSummaryDTO>>> getShipmentsInfoByPort(
			@RequestBody PortShipmentsInfoRequestDTO request,
			@RequestAttribute(required = true) CurrentUserDto currentUser) {
		PaginationResponse<ShipmentSummaryDTO> response = homeService.getShipmentsInfoByPort(request, currentUser);
		return ResponseBuilder.success(response);
	}
}