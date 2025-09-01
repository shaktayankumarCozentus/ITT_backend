package com.itt.service.dto.home;

import com.itt.service.enums.ShipmentTimelinessStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentSummaryDTO {
	public ShipmentSummaryDTO(Integer shipmentId, String shipmentNumber, String carrierName, String portOfLoading,
			String portOfDischarge) {
		this.shipmentId = shipmentId;
		this.shipmentNumber = shipmentNumber;
		this.carrierName = carrierName;
		this.portOfLoading = portOfLoading;
		this.portOfDischarge = portOfDischarge;
		this.shipmentStatus = determineShipmentStatus(shipmentId);
	}

	private Integer shipmentId;
	private String shipmentNumber;
	private ShipmentTimelinessStatus shipmentStatus;
	private String carrierName;
	private String portOfLoading;
	private String portOfDischarge;

	private ShipmentTimelinessStatus determineShipmentStatus(Integer shipmentId) {
		if (shipmentId == null) {
			return ShipmentTimelinessStatus.ONTIME;
		}
		if (shipmentId % 7 == 0) {
			return ShipmentTimelinessStatus.UNDEFINED;
		} else if (shipmentId % 5 == 0) {
			return ShipmentTimelinessStatus.LATE;
		} else if (shipmentId % 3 == 0) {
			return ShipmentTimelinessStatus.EARLY;
		} else {
			return ShipmentTimelinessStatus.ONTIME;
		}
	}
}
