package com.itt.service.entity;

import java.time.LocalDateTime;

import com.itt.service.enums.ModeOfTransport;

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
@Table(name = "RD_master_port")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterPort {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "port_code")
	private String portCode;

	@Column(name = "port_name")
	private String portName;

	@Enumerated(EnumType.STRING)
	@Column(name = "port_mode_type")
	private ModeOfTransport portModeType;

	@Column(name = "port_region_alias")
	private String portRegionAlias;

	@Column(name = "port_country_code")
	private String portCountryCode;

	@Column(name = "port_state")
	private String portState;

	@Column(name = "port_city")
	private String portCity;

	@Column(name = "port_latitude")
	private Double portLatitude;

	@Column(name = "port_longitude")
	private Double portLongitude;

	@Column(name = "region_id")
	private Integer regionId;

	@Column(name = "country_id")
	private Integer countryId;

	@Column(name = "city_id")
	private Integer cityId;

	@Column(name = "created_on", nullable = false)
	private LocalDateTime createdOn;
}
