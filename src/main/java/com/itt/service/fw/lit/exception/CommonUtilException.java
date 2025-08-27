package com.itt.service.fw.lit.exception;

import com.itt.service.fw.lit.enums.LitExceptionCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Setter
@Getter
public class CommonUtilException extends BaseLitException {
    @Serial
    private static final long serialVersionUID = -1096705945921813339L;
    @Override
    public LitExceptionCode getMessageKey() {
        return LitExceptionCode.BAD_REQUEST;
    }
}