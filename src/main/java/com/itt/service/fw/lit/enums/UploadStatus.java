package com.itt.service.fw.lit.enums;

import lombok.Getter;

@Getter
public enum UploadStatus {
    FAILED(0), SUCCESS(1);

    private final int code;

    UploadStatus(int code) {
        this.code = code;
    }
}
