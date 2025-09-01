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
@Table(name = "RD_master_carrier")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterCarrier {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "carrier_code")
	private String carrierCode;

	@Column(name = "carrier_name")
	private String carrierName;

	@Column(name = "carrier_alias")
	private String carrierAlias;
}
