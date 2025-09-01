package com.itt.service.dto.home;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationStatsSummaryDTO {

	public LocationStatsSummaryDTO(List<LocationStatsDTO> locationStatsList) {
		this.locationStatsList = locationStatsList;
		this.totalShipmentCount = locationStatsList.stream().mapToLong(LocationStatsDTO::getTotalShipmentCount).sum();

	}

	private List<LocationStatsDTO> locationStatsList;

	private Long totalShipmentCount;
}