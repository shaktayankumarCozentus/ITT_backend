package com.itt.service.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.itt.service.dto.CurrentUserDto;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.home.KpiIndicatorDTO;
import com.itt.service.dto.home.KpiIndicatorRequestDTO;
import com.itt.service.dto.home.LocationStatsDTO;
import com.itt.service.dto.home.LocationStatsSummaryDTO;
import com.itt.service.dto.home.MapViewRequestDTO;
import com.itt.service.dto.home.PortShipmentsInfoRequestDTO;
import com.itt.service.dto.home.ShipmentSummaryDTO;
import com.itt.service.repository.MasterShipmentRepository;
import com.itt.service.service.HomeService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class HomeServiceImpl implements HomeService {

	private final MasterShipmentRepository shipmentRepository;

	@Override
	public LocationStatsSummaryDTO getMapData(MapViewRequestDTO request, CurrentUserDto currentUser) {
		switch (request.getLevel()) {
		case COUNTRY:
			return fetchCountries(request, currentUser);
		case CITY:
			return fetchCitiesByCountry(request, currentUser);
		case PORT:
			return fetchPortsByCity(request, currentUser);
		default:
			break;
		}
		return new LocationStatsSummaryDTO();
	}

	private LocationStatsSummaryDTO fetchCountries(MapViewRequestDTO request, CurrentUserDto currentUser) {
		List<LocationStatsDTO> list = new ArrayList<>();
		switch (request.getLocationType()) {
		case ORIGIN:
			list = shipmentRepository.findCountryStatsForOrigin(currentUser.getUserId(), request.getGlobalFilter(),
					request.getCompanyCodes());
			break;
		case DESTINATION:
			list = shipmentRepository.findCountryStatsForDestination(currentUser.getUserId(), request.getGlobalFilter(),
					request.getCompanyCodes());
			break;
		}
		return new LocationStatsSummaryDTO(list);
	}

	private LocationStatsSummaryDTO fetchCitiesByCountry(MapViewRequestDTO request, CurrentUserDto currentUser) {
		List<LocationStatsDTO> list = new ArrayList<>();
		switch (request.getLocationType()) {
		case ORIGIN:
			list = shipmentRepository.findCityStatsForOriginByCountry(currentUser.getUserId(),
					request.getGlobalFilter(), request.getCompanyCodes(), request.getLocationId());
			break;
		case DESTINATION:
			list = shipmentRepository.findCityStatsForDestinationByCountry(currentUser.getUserId(),
					request.getGlobalFilter(), request.getCompanyCodes(), request.getLocationId());
			break;
		}
		return new LocationStatsSummaryDTO(list);
	}

	private LocationStatsSummaryDTO fetchPortsByCity(MapViewRequestDTO request, CurrentUserDto currentUser) {
		List<LocationStatsDTO> list = new ArrayList<>();
		switch (request.getLocationType()) {
		case ORIGIN:
			list = shipmentRepository.findPortStatsForOriginByCity(currentUser.getUserId(), request.getGlobalFilter(),
					request.getCompanyCodes(), request.getLocationId());
			break;
		case DESTINATION:
			list = shipmentRepository.findPortStatsForDestinationByCity(currentUser.getUserId(),
					request.getGlobalFilter(), request.getCompanyCodes(), request.getLocationId());
			break;
		}
		return new LocationStatsSummaryDTO(list);
	}

	@Override
	public KpiIndicatorDTO getKpiIndicator(KpiIndicatorRequestDTO request, CurrentUserDto currentUser) {
		KpiIndicatorDTO kpi = shipmentRepository.findKpiCountsByFilters(currentUser.getUserId(),
				request.getShipmentStatus(), request.getGlobalFilter(), request.getCompanyCodes());
		return kpi;
	}

	@Override
	public PaginationResponse<ShipmentSummaryDTO> getShipmentsInfoByPort(PortShipmentsInfoRequestDTO request,
			CurrentUserDto currentUser) {
		Pageable pageRequest = PageRequest.of(request.getDataTableRequest().getPagination().getPage(),
				request.getDataTableRequest().getPagination().getSize());
		Page<ShipmentSummaryDTO> page = Page.empty();
		switch (request.getGlobalFilterRequest().getLocationType()) {
		case ORIGIN:
			page = shipmentRepository.findShipmentsByOriginPort(currentUser.getUserId(),
					request.getGlobalFilterRequest().getLocationId(),
					request.getGlobalFilterRequest().getGlobalFilter(),
					request.getGlobalFilterRequest().getCompanyCodes(), pageRequest);
			break;
		case DESTINATION:
			page = shipmentRepository.findShipmentsByDestinationPort(currentUser.getUserId(),
					request.getGlobalFilterRequest().getLocationId(),
					request.getGlobalFilterRequest().getGlobalFilter(),
					request.getGlobalFilterRequest().getCompanyCodes(), pageRequest);
			break;
		}
		PaginationResponse<ShipmentSummaryDTO> response = new PaginationResponse<ShipmentSummaryDTO>(page);
		return response;
	}
}
