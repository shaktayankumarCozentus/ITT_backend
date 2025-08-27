package com.itt.service.fw.lit.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorList implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private String errorCode;
    private String fieldName;
    private String fieldLength;
    private String errorMessage;
}
