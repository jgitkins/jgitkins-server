package io.jgitkins.server.presentation.common.error;

import io.jgitkins.server.common.error.ErrorCode;

public enum PresentationErrorCode implements ErrorCode {
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized"),
    FORBIDDEN("FORBIDDEN", "Forbidden"),
    NOT_FOUND("NOT_FOUND", "Not found"),
    VALIDATION_FAILED("VALIDATION_FAILED", "Request validation failed"),
    MALFORMED_JSON("MALFORMED_JSON", "Malformed request body"),
    TYPE_MISMATCH("TYPE_MISMATCH", "Request parameter type mismatch"),
    BAD_REQUEST("BAD_REQUEST", "Bad request");

    private final String code;
    private final String defaultMessage;

    PresentationErrorCode(String code, String defaultMessage) {
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
