package com.itt.service.dto.home;

import java.util.List;

import com.itt.service.dto.GlobalFilterDTO;
import com.itt.service.enums.ShipmentDeliveryStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiIndicatorRequestDTO {
	private ShipmentDeliveryStatus shipmentStatus;
	private GlobalFilterDTO globalFilter;
	private List<Integer> companyCodes;
}