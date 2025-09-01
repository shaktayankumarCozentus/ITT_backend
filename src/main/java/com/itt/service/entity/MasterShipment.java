package com.itt.service.entity;

import com.itt.service.enums.ModeOfTransport;
import com.itt.service.enums.ShipmentDeliveryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RD_master_shipment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MasterShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "shipment_id")
    private Integer shipmentId;

    @Column(name = "integrated_id")
    private Integer integratedId;

    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "container_count")
    private Integer containerCount;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "bol_number")
    private String bolNumber;

    @Column(name = "booking_number")
    private String bookingNumber;

    @Column(name = "geid")
    private Integer geid;

    @Column(name = "party_id")
    private Integer partyId;

    @Column(name = "booking_id")
    private Integer bookingId;

    @Column(name = "representative_id")
    private Integer representativeId;

    @Column(name = "address_id")
    private Integer addressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status")
    private ShipmentDeliveryStatus currentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_of_transport")
    private ModeOfTransport modeOfTransport;

    @Column(name = "place_of_delivery")
    private Integer placeOfDelivery;

    @Column(name = "place_of_receipt")
    private Integer placeOfReceipt;

    @Column(name = "port_of_departure")
    private Integer portOfDeparture;

    @Column(name = "port_of_arrival")
    private Integer portOfArrival;

    @Column(name = "is_hazardous")
    private Boolean isHazardous;

    @Column(name = "cargo_type")
    private String cargoType;
    
    @Column(name = "vessel_id")
    private Integer vesselId;
    
    @Column(name = "carrier_id")
    private Integer carrierId;
}
