package com.itt.service.dto.home;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransportModeKpiDTO {
	private Long totalCount;
	private List<ModeCountDTO> transportModeData;
}
