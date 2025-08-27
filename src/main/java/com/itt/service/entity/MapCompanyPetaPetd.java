package com.itt.service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "map_company_peta_petd")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapCompanyPetaPetd {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false)
	private MasterCompany company;
	
	@Column(name = "company_id", insertable = false, updatable = false)
	private Integer companyId;

	@Column(name = "peta_petd_enabled_flag", nullable = false)
	private Boolean petaPetdEnabledFlag = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ocean_frequency_id", insertable = false, updatable = false)
	private MasterConfig oceanFrequency;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "air_frequency_id", insertable = false, updatable = false)
	private MasterConfig airFrequency;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rail_road_frequency_id", insertable = false, updatable = false)
	private MasterConfig railRoadFrequency;

	@Column(name = "created_on", nullable = false, updatable = false)
	private LocalDateTime createdOn;

	@Column(name = "created_by_id")
	private Integer createdById;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_id", insertable = false, updatable = false)
	private MasterUser createdBy;

	@Column(name = "updated_on")
	private LocalDateTime updatedOn;

	@Column(name = "updated_by_id")
	private Integer updatedById;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by_id", insertable = false, updatable = false)
	private MasterUser updatedBy;
}