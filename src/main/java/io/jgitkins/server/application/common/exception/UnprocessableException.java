package io.jgitkins.server.application.common.exception;

import io.jgitkins.server.common.error.ErrorCode;
import io.jgitkins.server.common.exception.JgitkinsException;

public class UnprocessableException extends JgitkinsException {

    public UnprocessableException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnprocessableException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public UnprocessableException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
