package com.itt.service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lit_upload_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LitUploadAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FILENAME", nullable = false, length = 255)
    private String filename;

    @Column(name = "STATUS", nullable = false)
    private int status; // 0 = FAILED, 1 = SUCCESS, extendable

    @Column(name = "RECORDS_PROCESSED")
    private int recordsProcessed;

    @Column(name = "RECORDS_INSERTED")
    private int recordsInserted;

    @Column(name = "RECORDS_UPDATED")
    private int recordsUpdated;

    @Column(name = "ERROR_COUNT")
    private int errorCount;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime timestamp;
}


