package io.jgitkins.server.infrastructure.common.error;

import io.jgitkins.server.common.error.ErrorCode;

public enum InfrastructureErrorCode implements ErrorCode {
    INF_INTERNAL_ERROR("INF_INTERNAL_ERROR", "Infrastructure internal error"),
    INF_PERSISTENCE_OPERATION_FAILED("INF_PERSISTENCE_OPERATION_FAILED", "Persistence operation failed"),
    INF_JGIT_OPERATION_FAILED("INF_JGIT_OPERATION_FAILED", "JGit operation failed"),
    INF_FILESYSTEM_ACCESS_FAILED("INF_FILESYSTEM_ACCESS_FAILED", "Filesystem access failed");

    private final String code;
    private final String defaultMessage;

    InfrastructureErrorCode(String code, String defaultMessage) {
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

