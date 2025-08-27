package com.itt.service.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a security role in the Service system.
 * 
 * <p>This entity defines the roles that can be assigned to users for access control
 * and permission management. Roles determine what features and data users can access
 * within the portal system.</p>
 * 
 * <h3>Role Management Features:</h3>
 * <ul>
 *   <li><strong>Role Types:</strong> Configurable role categories via master_config references</li>
 *   <li><strong>Landing Pages:</strong> Customizable default landing pages per role</li>
 *   <li><strong>Audit Trail:</strong> Complete creation and modification tracking</li>
 *   <li><strong>Status Management:</strong> Active/inactive state control</li>
 * </ul>
 * 
 * <h3>Database Schema:</h3>
 * <ul>
 *   <li><strong>Table:</strong> master_role</li>
 *   <li><strong>Primary Key:</strong> id (auto-generated)</li>
 *   <li><strong>Unique Constraints:</strong> name (enforced at application level)</li>
 *   <li><strong>Foreign Keys:</strong> role_type_config_id, landing_page_config_id</li>
 * </ul>
 * 
 * <h3>Configuration Integration:</h3>
 * <p>The role entity leverages the master_config system for flexible configuration:</p>
 * <ul>
 *   <li><strong>Role Types:</strong> Links to master_config entries defining role categories</li>
 *   <li><strong>Landing Pages:</strong> References master_config for default navigation targets</li>
 * </ul>
 * 
 * <h3>Audit Information:</h3>
 * <p>Comprehensive audit trail including:</p>
 * <ul>
 *   <li>Creation timestamp and user</li>
 *   <li>Last modification timestamp and user</li>
 *   <li>Active status for soft deletion patterns</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Create a new admin role
 * Role adminRole = Role.builder()
 *     .name("ADMIN")
 *     .description("System Administrator Role")
 *     .roleTypeConfig(adminTypeConfig)
 *     .landingPageConfig(dashboardPageConfig)
 *     .isActive(1)
 *     .createdById(1)
 *     .createdOn(LocalDateTime.now())
 *     .build();
 * 
 * // Create a user role
 * Role userRole = Role.builder()
 *     .name("USER")
 *     .description("Standard User Role")
 *     .roleTypeConfig(userTypeConfig)
 *     .landingPageConfig(homePageConfig)
 *     .isActive(1)
 *     .createdById(2)
 *     .createdOn(LocalDateTime.now())
 *     .build();
 * }</pre>
 * 
 * @author Service Team
 * @version 1.0
 * @since 1.0
 * @see MasterConfig
 * @see MapRoleCategoryFeaturePrivilegeSkin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "master_role", 
       uniqueConstraints = @UniqueConstraint(name = "uk_role_name", columnNames = "name"))
public class Role {

    /**
     * Unique identifier for the role.
     * Auto-generated primary key using database identity column.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * The unique name of the role.
     * 
     * <p>Role names should follow naming conventions:</p>
     * <ul>
     *   <li>Use uppercase with underscores (e.g., "SYSTEM_ADMIN")</li>
     *   <li>Be descriptive and business-meaningful</li>
     *   <li>Avoid technical jargon where possible</li>
     *   <li>Maximum length of 50 characters</li>
     * </ul>
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * Optional human-readable description of the role.
     * 
     * <p>Provides additional context about the role's purpose, responsibilities,
     * and scope. This description is often displayed in administrative interfaces
     * to help with role management and assignment decisions.</p>
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * Configuration reference defining the type/category of this role.
     * 
     * <p>Links to a master_config entry that categorizes roles by type
     * (e.g., "SYSTEM_ROLE", "BUSINESS_ROLE", "GUEST_ROLE"). This enables
     * flexible role categorization and type-specific behavior.</p>
     * 
     * <p>Uses lazy loading to optimize query performance when role type
     * information is not immediately needed.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_type_config_id")
    private MasterConfig roleTypeConfig;

    /**
     * Configuration reference for the default landing page after login.
     * 
     * <p>Links to a master_config entry that defines where users with this
     * role should be redirected after successful authentication. This enables
     * role-based navigation and personalized user experiences.</p>
     * 
     * <p>Uses lazy loading as landing page configuration is typically
     * only needed during the authentication flow.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landing_page_config_id")
    private MasterConfig landingPageConfig;

    /**
     * Timestamp when this role was created.
     * 
     * <p>Used for audit trails, reporting, and data lifecycle management.
     * Should be set automatically when the entity is first persisted.</p>
     */
    @Column(name = "created_on")
    private LocalDateTime createdOn;

    /**
     * Identifier of the user who created this role.
     * 
     * <p>Stores the user ID of the person who created this role.
     * Essential for audit trails and accountability in role management.</p>
     */
    @Column(name = "created_by_id", nullable = false)
    private Integer createdById;

    /**
     * Reference to the user who created this role.
     * 
     * <p>Provides direct access to the creator's details for audit trails
     * and user accountability. Uses lazy loading for performance optimization.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", insertable = false, updatable = false)
    private MasterUser createdByUser;

    /**
     * Timestamp when this role was last modified.
     * 
     * <p>Updated automatically whenever the role entity is modified.
     * Used for tracking changes and cache invalidation strategies.</p>
     */
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    /**
     * Identifier of the user who last modified this role.
     * 
     * <p>Tracks the most recent modifier for audit purposes. May be null
     * if the role has never been updated since creation.</p>
     */
    @Column(name = "updated_by_id")
    private Integer updatedById;

    /**
     * Reference to the user who last modified this role.
     * 
     * <p>Provides direct access to the modifier's details for audit trails.
     * Uses lazy loading for performance optimization. May be null if the role
     * has never been updated since creation.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id", insertable = false, updatable = false)
    private MasterUser updatedByUser;

    /**
     * Active status flag for soft deletion and status management.
     * 
     * <p>Uses integer values for database compatibility:</p>
     * <ul>
     *   <li><strong>0:</strong> Inactive/Disabled - Role cannot be assigned to users</li>
     *   <li><strong>1:</strong> Active - Role is available for assignment</li>
     * </ul>
     * 
     * <p>Inactive roles are typically retained for audit purposes and
     * historical data integrity rather than being physically deleted.</p>
     */
    @Column(name = "is_active", nullable = false)
    private Integer isActive;
    
    /**
     * Convenience method to check if the role is currently active.
     * 
     * @return true if the role is active (is_active = 1), false otherwise
     */
    public boolean isActive() {
        return Integer.valueOf(1).equals(this.isActive);
    }
    
    /**
     * Convenience method to activate the role.
     * Sets the is_active flag to 1.
     */
    public void activate() {
        this.isActive = 1;
    }
    
    /**
     * Convenience method to deactivate the role.
     * Sets the is_active flag to 0.
     */
    public void deactivate() {
        this.isActive = 0;
    }
}