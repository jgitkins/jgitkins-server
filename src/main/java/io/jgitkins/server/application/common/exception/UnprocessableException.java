package io.jgitkins.server.application.common.exception;

import io.jgitkins.server.application.common.ErrorCode;
import org.springframework.http.HttpStatus;

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

    @Override
    public ErrorCode getErrorCode() {
        return super.getErrorCode();
    }

    @Override
    public HttpStatus getStatus() {
        return super.getStatus();
    }

}
