package com.itt.service.dto.peta_petd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetaPetdUpdateRequest {
	private Integer companyCode;
	private Boolean petaCalling;
	private Integer oceanFrequencyType;
	private Integer airFrequencyType;
	private Integer railRoadFrequencyType;
}
