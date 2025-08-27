package com.itt.service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base Data Transfer Object containing common audit fields.
 * <p>
 * This abstract base class provides standard audit tracking fields that should be
 * included in most DTOs representing persistent entities. It supports:
 * <ul>
 *   <li><strong>Creation tracking:</strong> who created the record and when</li>
 *   <li><strong>Modification tracking:</strong> who last updated the record and when</li>
 *   <li><strong>Audit trail:</strong> maintains history for compliance and debugging</li>
 * </ul>
 * <p>
 * Usage pattern:
 * <pre>
 * {@code
 * public class UserDTO extends BaseDTO {
 *     private String username;
 *     private String email;
 *     // ... other fields
 * }
 * }
 * </pre>
 * <p>
 * Fields are automatically populated by:
 * <ul>
 *   <li>JPA auditing (if entity extends auditable base)</li>
 *   <li>Service layer mapping from entity audit fields</li>
 *   <li>Manual setting in business logic when required</li>
 * </ul>
 * 
 * @see com.itt.service.config.AuditConfig
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseDTO {
    
    /**
     * Identifier of the user who created this record.
     * <p>
     * Typically contains:
     * <ul>
     *   <li>Username from authentication context</li>
     *   <li>System identifier for automated processes</li>
     *   <li>Service account name for batch operations</li>
     * </ul>
     */
    private String createdBy;
    
    /**
     * Timestamp when this record was created.
     * <p>
     * Set automatically during entity creation and should not be modified
     * after initial persistence.
     */
    private LocalDateTime createdOn;
    
    /**
     * Identifier of the user who last updated this record.
     * <p>
     * Updated on every modification to track the most recent change author.
     * May be the same as createdBy if no updates have occurred.
     */
    private String updatedBy;
    
    /**
     * Timestamp when this record was last updated.
     * <p>
     * Updated automatically on every modification. May be null if no
     * updates have occurred since creation.
     */
    private LocalDateTime updatedOn;
}
