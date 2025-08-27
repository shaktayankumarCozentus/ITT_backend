package com.itt.service.fw.lit.exception;

import com.itt.service.fw.lit.enums.LitExceptionCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class DuplicateResourceFoundException extends BaseLitException {
    @Serial
    private static final long serialVersionUID = -1096705945921813339L;
    @Override
    public LitExceptionCode getMessageKey() {
        return LitExceptionCode.DUPLICATE_RESOURCE;
    }
}
