package io.jgitkins.server.application.common.exception;

import io.jgitkins.server.application.common.ErrorCode;
import org.springframework.http.HttpStatus;

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

    @Override
    public ErrorCode getErrorCode() {
        return super.getErrorCode();
    }

    @Override
    public HttpStatus getStatus() {
        return super.getStatus();
    }

}
