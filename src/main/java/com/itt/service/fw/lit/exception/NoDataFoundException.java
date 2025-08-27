package com.itt.service.fw.lit.exception;

import com.itt.service.fw.lit.enums.LitExceptionCode;

public class NoDataFoundException extends RuntimeException{
    public LitExceptionCode getMessageKey() {
        return LitExceptionCode.NO_DATA_FOUND;
    }
}
