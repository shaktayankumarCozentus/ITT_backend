package com.itt.service.fw.lit.service.impl;

import com.itt.service.entity.LitUploadAudit;
import com.itt.service.fw.lit.dto.UploadStats;
import com.itt.service.fw.lit.enums.UploadStatus;
import com.itt.service.fw.logger.api.annotation.Loggable;
import com.itt.service.repository.LitUploadAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Loggable
public class LitAuditService {

    private final LitUploadAuditRepository auditRepo;

    public LitAuditService(LitUploadAuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAudit(String filename, UploadStatus status, UploadStats stats) {
        // If there are errors, override inserted/updated counts
        var safeStats= stats.sanitizeForError();

        var audit = LitUploadAudit.builder()
                .filename(filename)
                .status(status.getCode())
                .recordsProcessed(safeStats.totalProcessed)
                .recordsInserted(safeStats.inserted)
                .recordsUpdated(safeStats.updated)
                .errorCount(safeStats.errorCount)
                .timestamp(LocalDateTime.now())
                .build();
        auditRepo.save(audit);
    }


}

