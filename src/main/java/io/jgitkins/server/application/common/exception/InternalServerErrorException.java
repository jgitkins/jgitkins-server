package io.jgitkins.server.application.common.exception;

import io.jgitkins.server.application.common.ErrorCode;
import org.springframework.http.HttpStatus;

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

    @Override
    public ErrorCode getErrorCode() {
        return super.getErrorCode();
    }

    @Override
    public HttpStatus getStatus() {
        return super.getStatus();
    }

}
