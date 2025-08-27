package com.itt.service.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "map_role_category_feature_privilege_skin")
public class MapRoleCategoryFeaturePrivilegeSkin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_cat_feat_priv_id", nullable = false)
    private MapCategoryFeaturePrivilege mapCategoryFeaturePrivilege;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_config_id")
    private MasterConfig skinConfig;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "created_by_id", nullable = false)
    private Integer createdById;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @Column(name = "updated_by_id")
    private Integer updatedById;
}