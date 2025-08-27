package com.itt.service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.itt.service.fw.lit.constant.AppConstant.NULL_BYTE;

@Entity
@Table(name = "lit_message")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LitMessage {
    @EmbeddedId
    private LitMessageId id;

    @Column(name = "MESSAGE", nullable = false)
    private String message;

    @PrePersist
    @PreUpdate
    private void sanitizeFields() {
        if (id != null) {
            id.setLitCode(clean(id.getLitCode()));
            id.setLanguageCode(clean(id.getLanguageCode()));
        }
    }

    private String clean(String input) {
        return input != null ? input.replace(NULL_BYTE, "") : null;
    }
}
