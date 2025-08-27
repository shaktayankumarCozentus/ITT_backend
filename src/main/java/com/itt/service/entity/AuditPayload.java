package com.itt.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_payloads")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditPayload {

    @Id
    @Column(name = "audit_id")
    private Long id;

    // The 'child' side. It maps its ID to the parent's ID.
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "audit_id")
    private UserEventAuditLog userEventAuditLog;

    // Using JSONB/JSON type is more efficient than TEXT if your DB supports it (e.g., PostgreSQL)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload", columnDefinition = "jsonb")
    private String requestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload", columnDefinition = "jsonb")
    private String responsePayload;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @Column(name = "user_roles", columnDefinition = "TEXT")
    private String userRoles;
}