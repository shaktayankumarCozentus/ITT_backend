package com.itt.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.itt.service.dto.CurrentUserDto;
import com.itt.service.dto.PaginationResponse;
import com.itt.service.dto.home.KpiIndicatorDTO;
import com.itt.service.dto.home.KpiIndicatorRequestDTO;
import com.itt.service.dto.home.LocationStatsDTO;
import com.itt.service.dto.home.LocationStatsSummaryDTO;
import com.itt.service.dto.home.MapViewRequestDTO;
import com.itt.service.dto.home.PortShipmentsInfoRequestDTO;
import com.itt.service.dto.home.ShipmentSummaryDTO;
import com.itt.service.enums.ShipmentDeliveryStatus;
import com.itt.service.enums.ShipmentTimelinessStatus;
import com.itt.service.repository.MasterShipmentRepository;
import com.itt.service.service.impl.HomeServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("HomeService - Business Logic Tests")
class HomeServiceImplTest {

	@Mock
	private MasterShipmentRepository shipmentRepository;

	@InjectMocks
	private HomeServiceImpl homeService;

	@Nested
	@DisplayName("Get Map Data Tests")
	class GetMapDataTests {

		@Test
		@DisplayName("Should return country stats for origin location type")
		void shouldReturnCountryStatsForOriginLocationType() {
			// Given
			MapViewRequestDTO request = createMapViewRequest(MapViewRequestDTO.Level.COUNTRY,
					MapViewRequestDTO.LocationType.ORIGIN, null);
			CurrentUserDto currentUser = createCurrentUser();
			List<LocationStatsDTO> mockStats = createMockLocationStats();

			when(shipmentRepository.findCountryStatsForOrigin(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()))).thenReturn(mockStats);

			// When
			LocationStatsSummaryDTO result = homeService.getMapData(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getLocationStatsList()).hasSize(2);
			assertThat(result.getLocationStatsList().get(0).getLocationName()).isEqualTo("USA");
			assertThat(result.getLocationStatsList().get(0).getTotalShipmentCount()).isEqualTo(100);

			verify(shipmentRepository).findCountryStatsForOrigin(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()));
		}

		@Test
		@DisplayName("Should return country stats for destination location type")
		void shouldReturnCountryStatsForDestinationLocationType() {
			// Given
			MapViewRequestDTO request = createMapViewRequest(MapViewRequestDTO.Level.COUNTRY,
					MapViewRequestDTO.LocationType.DESTINATION, null);
			CurrentUserDto currentUser = createCurrentUser();
			List<LocationStatsDTO> mockStats = createMockLocationStats();

			when(shipmentRepository.findCountryStatsForDestination(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()))).thenReturn(mockStats);

			// When
			LocationStatsSummaryDTO result = homeService.getMapData(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getLocationStatsList()).hasSize(2);

			verify(shipmentRepository).findCountryStatsForDestination(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()));
		}

		@Test
		@DisplayName("Should return city stats for origin location type")
		void shouldReturnCityStatsForOriginLocationType() {
			// Given
			Integer countryId = 1;
			MapViewRequestDTO request = createMapViewRequest(MapViewRequestDTO.Level.CITY,
					MapViewRequestDTO.LocationType.ORIGIN, countryId);
			CurrentUserDto currentUser = createCurrentUser();
			List<LocationStatsDTO> mockStats = createMockLocationStats();

			when(shipmentRepository.findCityStatsForOriginByCountry(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()), eq(countryId))).thenReturn(mockStats);

			// When
			LocationStatsSummaryDTO result = homeService.getMapData(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getLocationStatsList()).hasSize(2);

			verify(shipmentRepository).findCityStatsForOriginByCountry(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()), eq(countryId));
		}

		@Test
		@DisplayName("Should return city stats for destination location type")
		void shouldReturnCityStatsForDestinationLocationType() {
			// Given
			Integer countryId = 1;
			MapViewRequestDTO request = createMapViewRequest(MapViewRequestDTO.Level.CITY,
					MapViewRequestDTO.LocationType.DESTINATION, countryId);
			CurrentUserDto currentUser = createCurrentUser();
			List<LocationStatsDTO> mockStats = createMockLocationStats();

			when(shipmentRepository.findCityStatsForDestinationByCountry(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()), eq(countryId))).thenReturn(mockStats);

			// When
			LocationStatsSummaryDTO result = homeService.getMapData(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getLocationStatsList()).hasSize(2);

			verify(shipmentRepository).findCityStatsForDestinationByCountry(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()), eq(countryId));
		}

		@Test
		@DisplayName("Should return port stats for origin location type")
		void shouldReturnPortStatsForOriginLocationType() {
			// Given
			Integer cityId = 1;
			MapViewRequestDTO request = createMapViewRequest(MapViewRequestDTO.Level.PORT,
					MapViewRequestDTO.LocationType.ORIGIN, cityId);
			CurrentUserDto currentUser = createCurrentUser();
			List<LocationStatsDTO> mockStats = createMockLocationStats();

			when(shipmentRepository.findPortStatsForOriginByCity(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()), eq(cityId))).thenReturn(mockStats);

			// When
			LocationStatsSummaryDTO result = homeService.getMapData(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getLocationStatsList()).hasSize(2);

			verify(shipmentRepository).findPortStatsForOriginByCity(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()), eq(cityId));
		}

		@Test
		@DisplayName("Should return port stats for destination location type")
		void shouldReturnPortStatsForDestinationLocationType() {
			// Given
			Integer cityId = 1;
			MapViewRequestDTO request = createMapViewRequest(MapViewRequestDTO.Level.PORT,
					MapViewRequestDTO.LocationType.DESTINATION, cityId);
			CurrentUserDto currentUser = createCurrentUser();
			List<LocationStatsDTO> mockStats = createMockLocationStats();

			when(shipmentRepository.findPortStatsForDestinationByCity(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()), eq(cityId))).thenReturn(mockStats);

			// When
			LocationStatsSummaryDTO result = homeService.getMapData(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getLocationStatsList()).hasSize(2);

			verify(shipmentRepository).findPortStatsForDestinationByCity(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()), eq(cityId));
		}

		@Test
		@DisplayName("Should handle empty location stats")
		void shouldHandleEmptyLocationStats() {
			// Given
			MapViewRequestDTO request = createMapViewRequest(MapViewRequestDTO.Level.COUNTRY,
					MapViewRequestDTO.LocationType.ORIGIN, null);
			CurrentUserDto currentUser = createCurrentUser();

			when(shipmentRepository.findCountryStatsForOrigin(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()))).thenReturn(Collections.emptyList());

			// When
			LocationStatsSummaryDTO result = homeService.getMapData(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getLocationStatsList()).isEmpty();

			verify(shipmentRepository).findCountryStatsForOrigin(eq(currentUser.getUserId()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()));
		}
	}

	@Nested
	@DisplayName("Get KPI Indicator Tests")
	class GetKpiIndicatorTests {

		@Test
		@DisplayName("Should return KPI indicators with correct data")
		void shouldReturnKpiIndicatorsWithCorrectData() {
			// Given
			KpiIndicatorRequestDTO request = createKpiIndicatorRequest();
			CurrentUserDto currentUser = createCurrentUser();
			KpiIndicatorDTO mockKpi = createMockKpiIndicator();

			when(shipmentRepository.findKpiCountsByFilters(eq(currentUser.getUserId()), eq(request.getShipmentStatus()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()))).thenReturn(mockKpi);

			// When
			KpiIndicatorDTO result = homeService.getKpiIndicator(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getTotalCount()).isEqualTo(2000);

			verify(shipmentRepository).findKpiCountsByFilters(eq(currentUser.getUserId()),
					eq(request.getShipmentStatus()), eq(request.getGlobalFilter()), eq(request.getCompanyCodes()));
		}

		@Test
		@DisplayName("Should handle null KPI data")
		void shouldHandleNullKpiData() {
			// Given
			KpiIndicatorRequestDTO request = createKpiIndicatorRequest();
			CurrentUserDto currentUser = createCurrentUser();

			when(shipmentRepository.findKpiCountsByFilters(eq(currentUser.getUserId()), eq(request.getShipmentStatus()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()))).thenReturn(null);

			// When
			KpiIndicatorDTO result = homeService.getKpiIndicator(request, currentUser);

			// Then
			assertThat(result).isNull();

			verify(shipmentRepository).findKpiCountsByFilters(eq(currentUser.getUserId()),
					eq(request.getShipmentStatus()), eq(request.getGlobalFilter()), eq(request.getCompanyCodes()));
		}

		@Test
		@DisplayName("Should handle empty filters")
		void shouldHandleEmptyFilters() {
			// Given
			KpiIndicatorRequestDTO request = new KpiIndicatorRequestDTO();
			CurrentUserDto currentUser = createCurrentUser();
			KpiIndicatorDTO mockKpi = createMockKpiIndicator();

			when(shipmentRepository.findKpiCountsByFilters(eq(currentUser.getUserId()), eq(request.getShipmentStatus()),
					eq(request.getGlobalFilter()), eq(request.getCompanyCodes()))).thenReturn(mockKpi);

			// When
			KpiIndicatorDTO result = homeService.getKpiIndicator(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getTotalCount()).isEqualTo(2000);
			assertThat(result.getOceanCount()).isEqualTo(1000);
			assertThat(result.getAirCount()).isEqualTo(800);
			assertThat(result.getRailCount()).isEqualTo(150);
			assertThat(result.getRoadCount()).isEqualTo(50);

			verify(shipmentRepository).findKpiCountsByFilters(eq(currentUser.getUserId()),
					eq(request.getShipmentStatus()), eq(request.getGlobalFilter()), eq(request.getCompanyCodes()));
		}
	}

	@Nested
	@DisplayName("Get Shipments Info By Port Tests")
	class GetShipmentsInfoByPortTests {

		@Test
		@DisplayName("Should return paginated shipments for origin port")
		void shouldReturnPaginatedShipmentsForOriginPort() {
			// Given
			PortShipmentsInfoRequestDTO request = createPortShipmentsRequest(MapViewRequestDTO.LocationType.ORIGIN, 1);
			CurrentUserDto currentUser = createCurrentUser();
			List<ShipmentSummaryDTO> mockShipments = createMockShipmentSummaries();
			Page<ShipmentSummaryDTO> mockPage = new PageImpl<>(mockShipments, PageRequest.of(0, 10),
					mockShipments.size());

			when(shipmentRepository.findShipmentsByOriginPort(eq(currentUser.getUserId()), eq(1),
					eq(request.getGlobalFilterRequest().getGlobalFilter()),
					eq(request.getGlobalFilterRequest().getCompanyCodes()), any(Pageable.class))).thenReturn(mockPage);

			// When
			PaginationResponse<ShipmentSummaryDTO> result = homeService.getShipmentsInfoByPort(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getTotalElements()).isEqualTo(2);
			assertThat(result.getContent().get(0).getShipmentId()).isEqualTo(1);

			verify(shipmentRepository).findShipmentsByOriginPort(eq(currentUser.getUserId()), eq(1),
					eq(request.getGlobalFilterRequest().getGlobalFilter()),
					eq(request.getGlobalFilterRequest().getCompanyCodes()), any(Pageable.class));
		}

		@Test
		@DisplayName("Should return paginated shipments for destination port")
		void shouldReturnPaginatedShipmentsForDestinationPort() {
			// Given
			PortShipmentsInfoRequestDTO request = createPortShipmentsRequest(MapViewRequestDTO.LocationType.DESTINATION,
					2);
			CurrentUserDto currentUser = createCurrentUser();
			List<ShipmentSummaryDTO> mockShipments = createMockShipmentSummaries();
			Page<ShipmentSummaryDTO> mockPage = new PageImpl<>(mockShipments, PageRequest.of(0, 10),
					mockShipments.size());

			when(shipmentRepository.findShipmentsByDestinationPort(eq(currentUser.getUserId()), eq(2),
					eq(request.getGlobalFilterRequest().getGlobalFilter()),
					eq(request.getGlobalFilterRequest().getCompanyCodes()), any(Pageable.class))).thenReturn(mockPage);

			// When
			PaginationResponse<ShipmentSummaryDTO> result = homeService.getShipmentsInfoByPort(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getContent()).hasSize(2);
			assertThat(result.getTotalElements()).isEqualTo(2);

			verify(shipmentRepository).findShipmentsByDestinationPort(eq(currentUser.getUserId()), eq(2),
					eq(request.getGlobalFilterRequest().getGlobalFilter()),
					eq(request.getGlobalFilterRequest().getCompanyCodes()), any(Pageable.class));
		}

		@Test
		@DisplayName("Should handle empty shipment page")
		void shouldHandleEmptyShipmentPage() {
			// Given
			PortShipmentsInfoRequestDTO request = createPortShipmentsRequest(MapViewRequestDTO.LocationType.ORIGIN, 1);
			CurrentUserDto currentUser = createCurrentUser();
			Page<ShipmentSummaryDTO> emptyPage = Page.empty();

			when(shipmentRepository.findShipmentsByOriginPort(eq(currentUser.getUserId()), eq(1),
					eq(request.getGlobalFilterRequest().getGlobalFilter()),
					eq(request.getGlobalFilterRequest().getCompanyCodes()), any(Pageable.class))).thenReturn(emptyPage);

			// When
			PaginationResponse<ShipmentSummaryDTO> result = homeService.getShipmentsInfoByPort(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getContent()).isEmpty();
			assertThat(result.getTotalElements()).isEqualTo(0);

			verify(shipmentRepository).findShipmentsByOriginPort(eq(currentUser.getUserId()), eq(1),
					eq(request.getGlobalFilterRequest().getGlobalFilter()),
					eq(request.getGlobalFilterRequest().getCompanyCodes()), any(Pageable.class));
		}

		@Test
		@DisplayName("Should handle pagination parameters correctly")
		void shouldHandlePaginationParametersCorrectly() {
			// Given
			PortShipmentsInfoRequestDTO request = createPortShipmentsRequest(MapViewRequestDTO.LocationType.ORIGIN, 1);
			request.getDataTableRequest().getPagination().setPage(1);
			request.getDataTableRequest().getPagination().setSize(20);

			CurrentUserDto currentUser = createCurrentUser();
			List<ShipmentSummaryDTO> mockShipments = createMockShipmentSummaries();
			Page<ShipmentSummaryDTO> mockPage = new PageImpl<>(mockShipments, PageRequest.of(1, 20), 100);

			when(shipmentRepository.findShipmentsByOriginPort(eq(currentUser.getUserId()), eq(1),
					eq(request.getGlobalFilterRequest().getGlobalFilter()),
					eq(request.getGlobalFilterRequest().getCompanyCodes()), any(Pageable.class))).thenReturn(mockPage);

			// When
			PaginationResponse<ShipmentSummaryDTO> result = homeService.getShipmentsInfoByPort(request, currentUser);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getPage()).isEqualTo(1);
			assertThat(result.getSize()).isEqualTo(20);
			assertThat(result.getTotalElements()).isEqualTo(100);

			verify(shipmentRepository).findShipmentsByOriginPort(eq(currentUser.getUserId()), eq(1),
					eq(request.getGlobalFilterRequest().getGlobalFilter()),
					eq(request.getGlobalFilterRequest().getCompanyCodes()),
					argThat(pageable -> pageable.getPageNumber() == 1 && pageable.getPageSize() == 20));
		}
	}

	// ==================== Helper Methods ====================

	private CurrentUserDto createCurrentUser() {
		return new CurrentUserDto(1, "system@itt.com", "System");
	}

	private MapViewRequestDTO createMapViewRequest(MapViewRequestDTO.Level level,
			MapViewRequestDTO.LocationType locationType, Integer locationId) {
		MapViewRequestDTO request = new MapViewRequestDTO();
		request.setLevel(level);
		request.setLocationType(locationType);
		request.setLocationId(locationId);
		request.setGlobalFilter(null);
		request.setCompanyCodes(Arrays.asList(1, 2));
		return request;
	}

	private List<LocationStatsDTO> createMockLocationStats() {
		LocationStatsDTO stats1 = new LocationStatsDTO();
		stats1.setLocationId(1);
		stats1.setLocationName("USA");
		stats1.setTotalShipmentCount(100L);
		stats1.setLatitude(40.7128);
		stats1.setLongitude(-74.0060);

		LocationStatsDTO stats2 = new LocationStatsDTO();
		stats2.setLocationId(2);
		stats2.setLocationName("Canada");
		stats2.setTotalShipmentCount(75L);
		stats2.setLatitude(45.4215);
		stats2.setLongitude(-75.6972);

		return Arrays.asList(stats1, stats2);
	}

	private KpiIndicatorRequestDTO createKpiIndicatorRequest() {
		KpiIndicatorRequestDTO request = new KpiIndicatorRequestDTO();
		request.setShipmentStatus(ShipmentDeliveryStatus.IN_TRANSIT);
		request.setGlobalFilter(null);
		request.setCompanyCodes(Arrays.asList(1, 2));
		return request;
	}

	private KpiIndicatorDTO createMockKpiIndicator() {
		return new KpiIndicatorDTO(1000L, 800L, 150L, 50L);
	}

	private PortShipmentsInfoRequestDTO createPortShipmentsRequest(MapViewRequestDTO.LocationType locationType,
			Integer locationId) {
		PortShipmentsInfoRequestDTO request = new PortShipmentsInfoRequestDTO();

		// Create nested DataTableRequest
		com.itt.service.dto.DataTableRequest dataTableRequest = new com.itt.service.dto.DataTableRequest();
		com.itt.service.dto.DataTableRequest.Pagination pagination = new com.itt.service.dto.DataTableRequest.Pagination();
		pagination.setPage(0);
		pagination.setSize(10);
		dataTableRequest.setPagination(pagination);
		request.setDataTableRequest(dataTableRequest);

		// Create GlobalFilterRequest
		MapViewRequestDTO globalFilterRequest = new MapViewRequestDTO();
		globalFilterRequest.setLocationType(locationType);
		globalFilterRequest.setLocationId(locationId);
		globalFilterRequest.setGlobalFilter(null);
		globalFilterRequest.setCompanyCodes(Arrays.asList(1, 2));
		request.setGlobalFilterRequest(globalFilterRequest);

		return request;
	}

	private List<ShipmentSummaryDTO> createMockShipmentSummaries() {
		ShipmentSummaryDTO shipment1 = new ShipmentSummaryDTO();
		shipment1.setShipmentId(1);
		shipment1.setShipmentStatus(ShipmentTimelinessStatus.ONTIME);
		shipment1.setPortOfLoading("New York");
		shipment1.setPortOfDischarge("London");

		ShipmentSummaryDTO shipment2 = new ShipmentSummaryDTO();
		shipment2.setShipmentId(2);
		shipment2.setShipmentStatus(ShipmentTimelinessStatus.LATE);
		shipment2.setPortOfLoading("Los Angeles");
		shipment2.setPortOfDischarge("Tokyo");

		return Arrays.asList(shipment1, shipment2);
	}
}
