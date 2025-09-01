package com.itt.service.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.itt.service.dto.GlobalFilterDTO;
import com.itt.service.dto.home.KpiIndicatorDTO;
import com.itt.service.dto.home.LocationStatsDTO;
import com.itt.service.dto.home.ShipmentSummaryDTO;
import com.itt.service.entity.MasterShipment;
import com.itt.service.enums.ShipmentDeliveryStatus;

public interface MasterShipmentRepository extends JpaRepository<MasterShipment, Integer> {

	// Security filter
	String SECURITY_FILTER = """
			AND EXISTS (
			    SELECT 1
			    FROM MapUserGeid mug
			    WHERE mug.userId = :userId
			      AND mug.geid = shipment.geid
			)
			""";

	// Company filter
	String COMPANY_FILTER = """
			AND (:#{#companies} IS NULL OR shipment.companyId IN :companies)
			""";

	// Shipment property filters
	String SHIPMENT_PROPERTY_FILTERS = """
			AND (:#{#filter.hazardousMaterial} IS NULL OR shipment.isHazardous = :#{#filter.hazardousMaterial})
			AND (:#{#filter.modeOfTransport} IS NULL OR shipment.modeOfTransport IN :#{#filter.modeOfTransport})
			AND (:#{#filter.placesOfReceipt} IS NULL OR shipment.placeOfReceipt IN :#{#filter.placesOfReceipt})
			AND (:#{#filter.placesOfDelivery} IS NULL OR shipment.placeOfDelivery IN :#{#filter.placesOfDelivery})
			AND (:#{#filter.cargoTypes} IS NULL OR shipment.cargoType IN :#{#filter.cargoTypes})
			""";

	// Related entity filters
	String RELATED_ENTITY_FILTERS = """
			AND (:#{#filter.carriers} IS NULL OR shipment.carrierId IN :#{#filter.carriers})
			AND (:#{#filter.vessels} IS NULL OR shipment.vesselId IN :#{#filter.vessels})
			""";

	// Origin port filters
	String ORIGIN_PORT_FILTERS = """
			AND (:#{#filter.originRegions} IS NULL OR originPort.regionId IN :#{#filter.originRegions})
			AND (:#{#filter.originCountries} IS NULL OR originPort.countryId IN :#{#filter.originCountries})
			AND (:#{#filter.originPorts} IS NULL OR originPort.id IN :#{#filter.originPorts})
			""";

	// Destination port filters
	String DESTINATION_PORT_FILTERS = """
			AND (:#{#filter.destinationRegions} IS NULL OR destinationPort.regionId IN :#{#filter.destinationRegions})
			AND (:#{#filter.destinationCountries} IS NULL OR destinationPort.countryId IN :#{#filter.destinationCountries})
			AND (:#{#filter.destinationPorts} IS NULL OR destinationPort.id IN :#{#filter.destinationPorts})
			""";

	// Combined common filters
	String BASE_FILTERS = SECURITY_FILTER + COMPANY_FILTER + SHIPMENT_PROPERTY_FILTERS + RELATED_ENTITY_FILTERS;
	String STANDARD_FILTERS = BASE_FILTERS + ORIGIN_PORT_FILTERS + DESTINATION_PORT_FILTERS;

	// Transport mode statistics
	String TRANSPORT_MODE_STATISTICS = """
			COUNT(shipment),
			SUM(CASE WHEN shipment.modeOfTransport = 'OCEAN' THEN 1 ELSE 0 END),
			SUM(CASE WHEN shipment.modeOfTransport = 'AIR' THEN 1 ELSE 0 END),
			SUM(CASE WHEN shipment.modeOfTransport = 'RAIL' THEN 1 ELSE 0 END),
			SUM(CASE WHEN shipment.modeOfTransport = 'ROAD' THEN 1 ELSE 0 END),
			""";

	// Country queries
	@Query("""
			SELECT new com.itt.service.dto.home.LocationStatsDTO(
			country.id, country.countryCode, country.countryName, country.latitude, country.longitude,
			""" + TRANSPORT_MODE_STATISTICS + """
			NULL
			)
			FROM MasterShipment shipment
			JOIN MasterPort originPort ON shipment.portOfDeparture = originPort.id
			JOIN MasterCountry country ON originPort.countryId = country.id
			JOIN MasterPort destinationPort ON shipment.portOfArrival = destinationPort.id
			WHERE 1=1
			""" + STANDARD_FILTERS + """
			GROUP BY country.id
			ORDER BY COUNT(shipment) DESC
			""")
	List<LocationStatsDTO> findCountryStatsForOrigin(@Param("userId") Integer userId,
			@Param("filter") GlobalFilterDTO filter, @Param("companies") List<Integer> companies);

	@Query("""
			SELECT new com.itt.service.dto.home.LocationStatsDTO(
			country.id, country.countryCode, country.countryName, country.latitude, country.longitude,
			""" + TRANSPORT_MODE_STATISTICS + """
			NULL
			)
			FROM MasterShipment shipment
			JOIN MasterPort destinationPort ON shipment.portOfArrival = destinationPort.id
			JOIN MasterCountry country ON destinationPort.countryId = country.id
			JOIN MasterPort originPort ON shipment.portOfDeparture = originPort.id
			WHERE 1=1
			""" + STANDARD_FILTERS + """
			GROUP BY country.id
			ORDER BY COUNT(shipment) DESC
			""")
	List<LocationStatsDTO> findCountryStatsForDestination(@Param("userId") Integer userId,
			@Param("filter") GlobalFilterDTO filter, @Param("companies") List<Integer> companies);

	// City queries
	@Query("""
			SELECT new com.itt.service.dto.home.LocationStatsDTO(
			city.id, '', city.cityName, city.latitude, city.longitude,
			""" + TRANSPORT_MODE_STATISTICS
			+ """
					NULL
					)
					FROM MasterShipment shipment
					JOIN MasterPort originPort ON shipment.portOfDeparture = originPort.id AND originPort.countryId = (:#{#locationId})
					JOIN MasterCity city ON originPort.cityId = city.id
					JOIN MasterPort destinationPort ON shipment.portOfArrival = destinationPort.id
					WHERE 1=1
					"""
			+ STANDARD_FILTERS + """
					GROUP BY city.id
					ORDER BY COUNT(shipment) DESC
					""")
	List<LocationStatsDTO> findCityStatsForOriginByCountry(@Param("userId") Integer userId,
			@Param("filter") GlobalFilterDTO filter, @Param("companies") List<Integer> companies,
			@Param("locationId") Integer locationId);

	@Query("""
			SELECT new com.itt.service.dto.home.LocationStatsDTO(
			city.id, '', city.cityName, city.latitude, city.longitude,
			""" + TRANSPORT_MODE_STATISTICS
			+ """
					NULL
					)
					FROM MasterShipment shipment
					JOIN MasterPort destinationPort ON shipment.portOfArrival = destinationPort.id AND destinationPort.countryId = (:#{#locationId})
					JOIN MasterCity city ON destinationPort.cityId = city.id
					JOIN MasterPort originPort ON shipment.portOfDeparture = originPort.id
					WHERE 1=1
					"""
			+ STANDARD_FILTERS + """
					GROUP BY city.id
					ORDER BY COUNT(shipment) DESC
					""")
	List<LocationStatsDTO> findCityStatsForDestinationByCountry(@Param("userId") Integer userId,
			@Param("filter") GlobalFilterDTO filter, @Param("companies") List<Integer> companies,
			@Param("locationId") Integer locationId);

	// Port queries
	@Query("""
			SELECT new com.itt.service.dto.home.LocationStatsDTO(
			originPort.id, originPort.portCode, originPort.portName, originPort.portLatitude, originPort.portLongitude,
			""" + TRANSPORT_MODE_STATISTICS
			+ """
					originPort.portModeType
					)
					FROM MasterShipment shipment
					JOIN MasterPort originPort ON shipment.portOfDeparture = originPort.id AND originPort.cityId = (:#{#locationId})
					JOIN MasterPort destinationPort ON shipment.portOfArrival = destinationPort.id
					WHERE 1=1
					"""
			+ STANDARD_FILTERS + """
					GROUP BY originPort.id
					ORDER BY COUNT(shipment) DESC
					""")
	List<LocationStatsDTO> findPortStatsForOriginByCity(@Param("userId") Integer userId,
			@Param("filter") GlobalFilterDTO filter, @Param("companies") List<Integer> companies,
			@Param("locationId") Integer locationId);

	@Query("""
			SELECT new com.itt.service.dto.home.LocationStatsDTO(
			destinationPort.id, destinationPort.portCode, destinationPort.portName, destinationPort.portLatitude, destinationPort.portLongitude,
			"""
			+ TRANSPORT_MODE_STATISTICS
			+ """
					destinationPort.portModeType
					)
					FROM MasterShipment shipment
					JOIN MasterPort destinationPort ON shipment.portOfArrival = destinationPort.id AND destinationPort.cityId = (:#{#locationId})
					JOIN MasterPort originPort ON shipment.portOfDeparture = originPort.id
					WHERE 1=1
					"""
			+ STANDARD_FILTERS + """
					GROUP BY destinationPort.id
					ORDER BY COUNT(shipment) DESC
					""")
	List<LocationStatsDTO> findPortStatsForDestinationByCity(@Param("userId") Integer userId,
			@Param("filter") GlobalFilterDTO filter, @Param("companies") List<Integer> companies,
			@Param("locationId") Integer locationId);

	// KPI query - uses individual components for custom filter placement
	@Query("""
			SELECT new com.itt.service.dto.home.KpiIndicatorDTO(
			SUM(CASE WHEN shipment.modeOfTransport = 'OCEAN' THEN 1 ELSE 0 END),
			SUM(CASE WHEN shipment.modeOfTransport = 'AIR' THEN 1 ELSE 0 END),
			SUM(CASE WHEN shipment.modeOfTransport = 'RAIL' THEN 1 ELSE 0 END),
			SUM(CASE WHEN shipment.modeOfTransport = 'ROAD' THEN 1 ELSE 0 END)
			)
			FROM MasterShipment shipment
			JOIN MasterPort destinationPort ON shipment.portOfArrival = destinationPort.id
			JOIN MasterPort originPort ON shipment.portOfDeparture = originPort.id
			WHERE 1=1
			AND (shipment.currentStatus = :status)
			""" + STANDARD_FILTERS)
	KpiIndicatorDTO findKpiCountsByFilters(@Param("userId") Integer userId,
			@Param("status") ShipmentDeliveryStatus status, @Param("filter") GlobalFilterDTO filter,
			@Param("companies") List<Integer> companies);

	// Shipment queries
	@Query("""
			SELECT new com.itt.service.dto.home.ShipmentSummaryDTO(
			shipment.id, shipment.referenceNumber, carrier.carrierName, originPort.portName, destinationPort.portName
			)
			FROM MasterShipment shipment
			JOIN MasterPort originPort ON shipment.portOfDeparture = originPort.id AND originPort.id = :portId
			JOIN MasterPort destinationPort ON shipment.portOfArrival = destinationPort.id
			JOIN MasterCarrier carrier ON shipment.carrierId = carrier.id
			WHERE 1=1
			""" + STANDARD_FILTERS)
	Page<ShipmentSummaryDTO> findShipmentsByOriginPort(@Param("userId") Integer userId, @Param("portId") Integer portId,
			@Param("filter") GlobalFilterDTO filter, @Param("companies") List<Integer> companies, Pageable pageable);

	@Query("""
			SELECT new com.itt.service.dto.home.ShipmentSummaryDTO(
			shipment.id, shipment.referenceNumber, carrier.carrierName, originPort.portName, destinationPort.portName
			)
			FROM MasterShipment shipment
			JOIN MasterPort destinationPort ON shipment.portOfArrival = destinationPort.id AND destinationPort.id = :portId
			JOIN MasterPort originPort ON shipment.portOfDeparture = originPort.id
			JOIN MasterCarrier carrier ON shipment.carrierId = carrier.id
			WHERE 1=1
			"""
			+ STANDARD_FILTERS)
	Page<ShipmentSummaryDTO> findShipmentsByDestinationPort(@Param("userId") Integer userId,
			@Param("portId") Integer portId, @Param("filter") GlobalFilterDTO filter,
			@Param("companies") List<Integer> companies, Pageable pageable);
}