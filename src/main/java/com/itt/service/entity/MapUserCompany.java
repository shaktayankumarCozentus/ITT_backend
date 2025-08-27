package com.itt.service.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity mapping for the map_user_company join table, including audit columns.
 */
@Entity
@Table(name = "map_user_company")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapUserCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** FK to master_user.id */
    @Column(name = "user_id")
    private Integer userId;

    /** FK to master_company.id */
    @Column(name = "company_id")
    private Integer companyId;

    /** When the assignment was created */
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    /** Who created the assignment (user_id) */
    @Column(name = "created_by_id", nullable = false)
    private Integer createdById;

    /** When the assignment was last updated */
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    /** Who last updated the assignment (user_id) */
    @Column(name = "updated_by_id")
    private Integer updatedById;

    // ----------------------------------------------------
    // Optional navigation properties
    // ----------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_map_user_company"),
            insertable = false,
            updatable = false
    )
    private MasterUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "company_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_map_company"),
            insertable = false,
            updatable = false
    )
    private MasterCompany company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "created_by_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_map_user_comp_create_user"),
            insertable = false,
            updatable = false
    )
    private MasterUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "updated_by_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_map_user_comp_update_user"),
            insertable = false,
            updatable = false
    )
    private MasterUser updatedBy;
}
