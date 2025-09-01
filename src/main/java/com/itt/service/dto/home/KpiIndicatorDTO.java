package com.itt.service.dto.home;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KpiIndicatorDTO {

	public KpiIndicatorDTO(Long oceanCount, Long airCount, Long railCount, Long roadCount) {
		this.oceanCount = oceanCount != null ? oceanCount : 0;
		this.airCount = airCount != null ? airCount : 0;
		this.railCount = railCount != null ? railCount : 0;
		this.roadCount = roadCount != null ? roadCount : 0;
		this.totalCount = (oceanCount != null ? oceanCount : 0) + (airCount != null ? airCount : 0)
				+ (railCount != null ? railCount : 0) + (roadCount != null ? roadCount : 0);
	}

	private Long totalCount; // Total count of the KPI indicator
	private Long oceanCount; // Count for Ocean mode
	private Long airCount; // Count for Air mode
	private Long railCount; // Count for Rail mode
	private Long roadCount; // Count for Road mode
}
