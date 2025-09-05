package com.itt.service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@Entity
@ToString
@Table(name = "user_plan_mappings")
public class UserPlanMapping {

	@Id
	@Setter(AccessLevel.NONE)
	@Column(name = "id", nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "user_id", nullable = true)
	private Integer userId;

	@Setter(AccessLevel.NONE)
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
	private MasterUser user;

	@Column(name = "plan_id", nullable = true)
	private Integer planId;

	@Setter(AccessLevel.NONE)
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "plan_id", nullable = true, insertable = false, updatable = false)
	private Plan plan;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@Setter(AccessLevel.NONE)
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Setter(AccessLevel.NONE)
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		this.isActive = Boolean.TRUE;
		this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
		this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
	}

}