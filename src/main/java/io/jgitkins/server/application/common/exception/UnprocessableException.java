package io.jgitkins.server.application.common.exception;

import io.jgitkins.server.application.common.ErrorCode;

public class UnprocessableException extends ApplicationException {

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
