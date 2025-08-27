package com.itt.service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "user_audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Use UUID for correlation_id for better cross-service tracing
    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @CreationTimestamp
    @Column(name = "event_timestamp", nullable = false, updatable = false)
    private Instant eventTimestamp;

    // Renamed for clarity and consistency
    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "endpoint", length = 255)
    private String endpoint;

    @Column(name = "status_code")
    private int statusCode;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    // The 'parent' side of the relationship. Saving this entity will also save the payload.
    @OneToOne(mappedBy = "userEventAuditLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private AuditPayload payload;

    // Helper method to establish the bidirectional link
    public void setPayload(AuditPayload payload) {
        if (payload == null) {
            if (this.payload != null) {
                this.payload.setUserEventAuditLog(null);
            }
        } else {
            payload.setUserEventAuditLog(this);
        }
        this.payload = payload;
    }
}