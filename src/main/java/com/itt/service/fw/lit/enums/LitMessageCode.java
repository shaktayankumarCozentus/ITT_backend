package com.itt.service.fw.lit.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LitMessageCode {

    // === Upload Errors ===
    EMPTY_LINE("E001", "LIT.UPLOAD.ERROR.EMPTY.LINE"),
    INCOMPLETE_FIELDS("E002", "LIT.UPLOAD.ERROR.INCOMPLETE.FIELDS"),
    UNKNOWN("E999", "LIT.UPLOAD.ERROR.UNKNOWN"),
    VALIDATION("E003", "LIT.UPLOAD.ERROR.VALIDATION"),

    // === No Data ===
    NO_DATA_FOUND("E004", "LIT.ERR.RESOLVE.NO_DATA_FOUND"),

    // === Labels ===
    TRACKING_SEARCH("L001", "LIT.LBL.TRACKING.SEARCH"),
    DASHBOARD_HEADER("L002", "LIT.LBL.DASHBOARD.HEADER"),
    FORM_SHIPMENT_ID("L003", "LIT.LBL.FORM.SHIPMENT_ID"),
    FORM_CONTAINER_TYPE("L004", "LIT.LBL.FORM.CONTAINER_TYPE"),

    // === Info Messages ===
    SHIPMENT_CREATED("I001", "LIT.INFO.SHIPMENT.CREATED"),

    // === Application Errors ===
    TRACKING_INVALID_ID("T001", "LIT.ERR.TRACKING.INVALID_ID"),
    RESOURCE_NOT_FOUND("T002", "LIT.ERR.RESOURCE.NOT_FOUND"),
    RESOURCE_DUPLICATE("T003", "LIT.ERR.RESOURCE.DUPLICATE"),
    JSON_PARSE("T004", "LIT.ERR.JSON.PARSE"),
    INTERNAL_ERROR("T005", "LIT.ERR.INTERNAL"),
    REST_SERVICE_ERROR("T006", "LIT.ERR.REST_SERVICE"),

    // === Warnings ===
    API_LIMIT_REACHED("W001", "LIT.WARN.API.LIMIT_REACHED");

    private final String code;
    private final String messageKey;
}
