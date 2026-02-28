package io.jgitkins.server.application.common.exception;

import io.jgitkins.server.common.error.ErrorCode;
import io.jgitkins.server.common.exception.JgitkinsException;

public class ResourceNotFoundException extends JgitkinsException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
