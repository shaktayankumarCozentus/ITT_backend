package com.itt.service.dto.home;

import com.itt.service.enums.ModeOfTransport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationStatsDTO {

	private Integer locationId;
	private String locationCode;
	private String locationName;
	private Double latitude;
	private Double longitude;
	private Long totalShipmentCount;
	private Long oceanShipmentCount;
	private Long airShipmentCount;
	private Long railShipmentCount;
	private Long roadShipmentCount;
	private ModeOfTransport modeOfTransport;
}
