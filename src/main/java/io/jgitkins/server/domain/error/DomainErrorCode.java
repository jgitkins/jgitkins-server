package io.jgitkins.server.domain.error;

import io.jgitkins.server.common.error.ErrorCode;

public enum DomainErrorCode implements ErrorCode {
    DOM_RULE_VIOLATION("DOM_RULE_VIOLATION", "Domain rule violation"),
    DOM_RUNNER_ALREADY_ACTIVE("DOM_RUNNER_ALREADY_ACTIVE", "Runner already active"),
    DOM_RUNNER_TOKEN_INVALID("DOM_RUNNER_TOKEN_INVALID", "Runner token is invalid"),
    DOM_USERNAME_ALREADY_SET("DOM_USERNAME_ALREADY_SET", "Username already set");

    private final String code;
    private final String defaultMessage;

    DomainErrorCode(String code, String defaultMessage) {
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

