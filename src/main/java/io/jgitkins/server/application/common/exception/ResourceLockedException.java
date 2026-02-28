package io.jgitkins.server.application.common.exception;

import io.jgitkins.server.common.error.ErrorCode;
import io.jgitkins.server.common.exception.JgitkinsException;

public class ResourceLockedException extends JgitkinsException {

    public ResourceLockedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceLockedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ResourceLockedException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

}
