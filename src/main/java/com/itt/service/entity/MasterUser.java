package com.itt.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user in the system.
 */
@Entity
@Table(name = "master_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "is_bdp_employee")
    private Boolean isBdpEmployee;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "created_by_id")
    private Integer createdById;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @Column(name = "updated_by_id")
    private Integer updatedById;

    @Column(name = "role_id")
    private Integer roleId;

    /**
     * Reference to the user who created this user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", insertable = false, updatable = false)
    private MasterUser createdByUser;

    /**
     * Reference to the user who last modified this user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id", insertable = false, updatable = false)
    private MasterUser updatedByUser;

    /**
     * Companies associated with this user.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "map_user_company",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "company_id")
    )
    @Builder.Default
    private Set<MasterCompany> companies = new HashSet<>();

    /**
     * The role assigned to this user.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role assignedRole;

    /**
     * Convenience method to check if the user is currently active.
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    /**
     * Convenience method to check if the user is a BDP employee.
     */
    public boolean isBdpEmployee() {
        return Boolean.TRUE.equals(this.isBdpEmployee);
    }
}
