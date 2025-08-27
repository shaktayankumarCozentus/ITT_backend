package com.itt.service.fw.lit.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LitExceptionCode {

    // Generic Errors
    UNKNOWN("LIT.UPLOAD.ERROR.UNKNOWN"),
    NO_DATA_FOUND("LIT.ERR.RESOLVE.NO_DATA_FOUND"),
    INTERNAL_SERVER("LIT.ERR.INTERNAL"),
    JSON_PARSING("LIT.ERR.JSON.PARSE"),

    // Validation-related
    VALIDATION_ERROR("LIT.UPLOAD.ERROR.VALIDATION"),
    EMPTY_LINE("LIT.UPLOAD.ERROR.EMPTY.LINE"),
    INCOMPLETE_FIELDS("LIT.UPLOAD.ERROR.INCOMPLETE.FIELDS"),

    // Business-specific
    DUPLICATE_RESOURCE("LIT.ERR.RESOURCE.DUPLICATE"),
    RESOURCE_NOT_FOUND("LIT.ERR.RESOURCE.NOT_FOUND"),
    REST_SERVICE_ERROR("LIT.ERR.REST_SERVICE"),

    // Framework-specific
    BAD_REQUEST("LIT.ERR.BAD_REQUEST");

    private final String code;
}
