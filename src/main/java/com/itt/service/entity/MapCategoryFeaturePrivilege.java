package com.itt.service.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapping table for feature‐category → feature → privilege relationships.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "map_category_feature_privilege")
public class MapCategoryFeaturePrivilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_category_id", nullable = false)
    private MasterConfig featureCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    private MasterConfig feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "privilege_id", nullable = false)
    private MasterConfig privilege;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "created_by_id", nullable = false)
    private Integer createdById;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @Column(name = "updated_by_id")
    private Integer updatedById;
}