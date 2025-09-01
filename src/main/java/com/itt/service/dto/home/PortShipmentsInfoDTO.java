package com.itt.service.dto.home;

import java.util.List;

import com.itt.service.dto.PaginationResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortShipmentsInfoDTO {
	private String portName;
	private String portCode;
	private String cityName;
	private Long totalShipmentCount;
	private PaginationResponse<List<ShipmentSummaryDTO>> portShipments;
}
