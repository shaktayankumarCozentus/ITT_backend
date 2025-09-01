package com.itt.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RD_master_vessel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterVessel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "call_sign")
    private String callSign;

    @Column(name = "imo_number")
    private String imoNumber;

    @Column(name = "mmsi")
    private Integer mmsi;

    @Column(name = "name")
    private String name;
}
