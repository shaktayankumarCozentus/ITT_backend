package com.itt.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LitMessageId implements Serializable {

    @Column(name = "LIT_CODE", nullable = false)
    @Pattern(regexp = "^[^\u0000]*$", message = "Invalid character in input")
    private String litCode;

    @Column(name = "LANGUAGE_CODE", nullable = false)
    @Pattern(regexp = "^[^\u0000]*$", message = "Invalid character in input")
    private String languageCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LitMessageId that)) return false;
        return Objects.equals(litCode, that.litCode) && Objects.equals(languageCode, that.languageCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(litCode, languageCode);
    }
}
