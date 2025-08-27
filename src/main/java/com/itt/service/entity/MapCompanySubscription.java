package com.itt.service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "map_company_subscription")
@NoArgsConstructor
@AllArgsConstructor
public class MapCompanySubscription {

	public MapCompanySubscription(Integer companyId, Integer featureToggleId, Integer isActive, Integer createdById,
			Integer updatedById) {
		this.companyId = companyId;
		this.featureToggleId = featureToggleId;
		this.isActive = isActive;
		this.createdById = createdById;
		this.updatedById = updatedById;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "company_id", nullable = true, columnDefinition = "INT DEFAULT 0")
	private Integer companyId;

	@Column(name = "feature_toggle_id", nullable = true, columnDefinition = "INT DEFAULT 0")
	private Integer featureToggleId;

	@Column(name = "is_active", nullable = true, columnDefinition = "INT DEFAULT 0")
	private Integer isActive;

	@Column(name = "created_on", nullable = false, insertable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdOn;

	@Column(name = "created_by_id")
	private Integer createdById;

	@Column(name = "updated_on")
	private LocalDateTime updatedOn;

	@Column(name = "updated_by_id")
	private Integer updatedById;

	// Optional: You can map relationships instead of using just IDs
	// Uncomment below if you want object mapping instead of foreign key ids

	/*
	 * @ManyToOne(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "company_id", referencedColumnName = "id", insertable =
	 * false, updatable = false) private MasterCompany company;
	 * 
	 * @ManyToOne(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "feature_toggle_id", referencedColumnName = "id",
	 * insertable = false, updatable = false) private MasterConfig featureToggle;
	 */
}
