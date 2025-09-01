package com.itt.service.dto;

import java.util.List;

import com.itt.service.enums.ModeOfTransport;
import com.itt.service.enums.ShipmentTimelinessStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalFilterDTO {
	 // Single-select fields
    private ShipmentTimelinessStatus shipmentStatus; // Ontime, Early, Late, Undefined
    private Boolean hazardousMaterial; // Y, N
    // Multi-select fields
    private List<ModeOfTransport> modeOfTransport; // Air, Ocean, Rail, Road
    private List<Integer> customerServiceRepresentatives;
    private List<Integer> lspNames;
    private List<Integer> originRegions;
    private List<Integer> originCountries;
    private List<Integer> originPorts;
    private List<Integer> placesOfReceipt;
    private List<Integer> destinationRegions;
    private List<Integer> destinationCountries;
    private List<Integer> destinationPorts;
    private List<Integer> placesOfDelivery;
    private List<Integer> carriers; // For Ocean or Air
    private List<Integer> vessels; // For Ocean
    private List<String> cargoTypes; // FCL, LCL, Air, FTL, LTL
}
