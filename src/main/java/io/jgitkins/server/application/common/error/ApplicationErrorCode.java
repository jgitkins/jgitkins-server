package io.jgitkins.server.application.common.error;

import io.jgitkins.server.common.error.ErrorCode;

public enum ApplicationErrorCode implements ErrorCode {
    BAD_REQUEST("BAD_REQUEST", "Bad request"),
    RUNNER_INVALID_TOKEN("RUNNER_INVALID_TOKEN", "Runner token is invalid"),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized"),
    FORBIDDEN("FORBIDDEN", "Forbidden"),
    NOT_FOUND("NOT_FOUND", "Resource not found"),
    ORGANIZE_NOT_FOUND("ORGANIZE_NOT_FOUND", "Organize Not Found"),
    REPOSITORY_NOT_FOUND("REPOSITORY_NOT_FOUND", "Source Repository Not Found"),
    BRANCH_NOT_FOUND("BRANCH_NOT_FOUND", "Branch Not Found"),
    COMMIT_NOT_FOUND("COMMIT_NOT_FOUND", "Commit Not Found"),
    SOURCE_BRANCH_NOT_FOUND("SOURCE_BRANCH_NOT_FOUND", "Source Branch Not Found"),
    COMMIT_TREE_NOT_FOUND("COMMIT_TREE_NOT_FOUND", "Commit Tree Not Found"),
    RUNNER_NOT_FOUND("RUNNER_NOT_FOUND", "Runner Not Found"),
    CONFLICT("CONFLICT", "Conflict"),
    REPOSITORY_ALREADY_EXISTS("REPOSITORY_ALREADY_EXISTS", "Repository Already Exists"),
    BRANCH_ALREADY_EXISTS("BRANCH_ALREADY_EXISTS", "Branch Already Exists"),
    RUNNER_ALREADY_ACTIVE("RUNNER_ALREADY_ACTIVE", "Runner Already Active"),
    ORGANIZE_ALREADY_EXISTS("ORGANIZE_ALREADY_EXISTS", "Organize Already Exists"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "Username Already Exists"),
    UNPROCESSABLE("UNPROCESSABLE", "Unprocessable request"),
    REPOSITORY_DOES_NOT_INITIALIZED("REPOSITORY_DOES_NOT_INITIALIZED", "Repository Does Not Initialized"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal server error");

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
