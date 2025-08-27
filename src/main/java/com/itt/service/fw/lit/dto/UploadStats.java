package com.itt.service.fw.lit.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UploadStats {
    public int totalProcessed = 0;
    public int inserted = 0;
    public int updated = 0;
    public int errorCount = 0;

    public UploadStats sanitizeForError() {
        return errorCount > 0
                ? new UploadStats(totalProcessed, 0, 0, errorCount)
                : this;
    }
}
