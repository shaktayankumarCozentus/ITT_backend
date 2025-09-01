package com.itt.service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "geid_global_entities")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MasterGlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "global_entity_id")
    private Integer globalEntityId;

    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "address_id")
    private Integer addressId;

    @Column(name = "global_entity_name")
    private String globalEntityName;

    @Column(name = "normal_global_entity_name")
    private String normalGlobalEntityName;

    @Lob
    @Column(name = "global_entity_guid")
    private String globalEntityGuid;

    @Column(name = "alias_company_id")
    private Integer aliasCompanyId;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
}
