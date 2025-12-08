package io.jgitkins.server.application.common.exception;

import io.jgitkins.server.application.common.ErrorCode;

public class InternalServerErrorException extends ApplicationException {

    public InternalServerErrorException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InternalServerErrorException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public InternalServerErrorException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
