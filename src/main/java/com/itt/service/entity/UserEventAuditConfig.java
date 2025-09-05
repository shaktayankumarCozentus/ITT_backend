package com.itt.service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_event_audit_config")
@Getter
@Setter
public class UserEventAuditConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // matches DDL
    private Long id;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod; // GET/POST/ANY

    @Column(name = "match_type", nullable = false, length = 10)
    private String matchType;  // EXACT/ANT/REGEX

    @Column(name = "path_pattern", nullable = false, length = 255)
    private String pathPattern;

    @Column(name = "log_request", nullable = false)
    private boolean logRequest;

    @Column(name = "log_response", nullable = false)
    private boolean logResponse;

    @Column(name = "log_error", nullable = false)
    private boolean logError;

    @Column(name = "only_on_error", nullable = false)
    private boolean onlyOnError;

    @Column(name = "mask_fields", length = 512)
    private String maskFields; // comma separated

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
