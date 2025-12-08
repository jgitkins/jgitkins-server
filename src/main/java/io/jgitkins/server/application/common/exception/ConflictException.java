package io.jgitkins.server.application.common.exception;

import io.jgitkins.server.application.common.ErrorCode;

public class ConflictException extends ApplicationException {

    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ConflictException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
