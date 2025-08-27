package com.itt.service.fw.lit.exception;

import com.itt.service.fw.lit.enums.LitExceptionCode;

public abstract class BaseLitException extends RuntimeException {
    public abstract LitExceptionCode getMessageKey();
}

