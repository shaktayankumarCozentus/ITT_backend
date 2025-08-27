package com.itt.service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "master_company")
@Data
@NamedEntityGraph(
	    name = "MasterCompany.withSubscriptionAndOnboarded",
	    attributeNodes = {
	        @NamedAttributeNode("subscriptionTypeConfig"),
	        @NamedAttributeNode("onboardedBySource")
	    }
	)
public class MasterCompany {

	@Id
	private Integer id;

	@Column(name = "original_company_id")
	private Integer originalCompanyId;

	@Column(name = "parent_id")
	private Integer parentId;

	@Column(name = "alias_id")
	private Integer aliasId;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "normal_company_name")
	private String normalCompanyName;

	@Column(name = "start_date")
	private LocalDateTime startDate;

	@Column(name = "onboarded_by_source")
	private Integer onboardedBySourceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "onboarded_by_source", insertable = false, updatable = false)
	private MasterConfig onboardedBySource;

	@Column(name = "end_date")
	private LocalDateTime endDate;

	@Column(name = "created_on")
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

	@Column(name = "sets_enabled_date")
	private LocalDateTime setsEnabledDate;

	@Column(name = "lsp_enabled_date")
	private LocalDateTime lspEnabledDate;

	@Column(name = "bu_enabled_date")
	private LocalDateTime buEnabledDate;

	@Column(name = "company_exceptions_doc_only_enabled_date")
	private LocalDateTime companyExceptionsDocOnlyEnabledDate;

	@Column(name = "company_peta_subscribed_date")
	private LocalDateTime companyPetaSubscribedDate;

	@Column(name = "frequency")
	private String frequency;

	@Column(name = "enabled_for_data_pull_date")
	private LocalDateTime enabledForDataPullDate;

	@Column(name = "data_load_date")
	private LocalDateTime dataLoadDate;

	@Column(name = "etl_company_subscribed_carrier_activation_date")
	private LocalDateTime etlCompanySubscribedCarrierActivationDate;

	@Column(name = "psa_flag")
	private Integer psaFlag;

	@Column(name = "is_rm_parent_company")
	private String isRmParentCompany;

	@Column(name = "subscription_type_config_id")
	private Integer subscriptionTypeConfigId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subscription_type_config_id", insertable = false, updatable = false)
	private MasterConfig subscriptionTypeConfig;

}
