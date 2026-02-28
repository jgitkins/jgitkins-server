package io.jgitkins.server.application.common.error;

import io.jgitkins.server.common.error.ErrorCode;

public enum ApplicationErrorCode implements ErrorCode {
    APP_BAD_REQUEST("APP_BAD_REQUEST", "Bad request"),
    APP_UNAUTHORIZED("APP_UNAUTHORIZED", "Unauthorized"),
    APP_FORBIDDEN("APP_FORBIDDEN", "Forbidden"),
    APP_NOT_FOUND("APP_NOT_FOUND", "Resource not found"),
    APP_CONFLICT("APP_CONFLICT", "Conflict"),
    APP_UNPROCESSABLE("APP_UNPROCESSABLE", "Unprocessable request"),
    APP_INTERNAL_SERVER_ERROR("APP_INTERNAL_SERVER_ERROR", "Internal server error");

    private final String code;
    private final String defaultMessage;

    ApplicationErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }
}
