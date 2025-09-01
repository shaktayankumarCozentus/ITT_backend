package com.itt.service.dto.home;

import java.util.List;

import com.itt.service.dto.GlobalFilterDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapViewRequestDTO {
	private GlobalFilterDTO globalFilter;
	private List<Integer> companyCodes;
	private Level level;
	private LocationType locationType;
	private Integer locationId;

	public enum LocationType {
		ORIGIN, DESTINATION
	}

	public enum Level {
		COUNTRY, CITY, PORT;
	}
}
