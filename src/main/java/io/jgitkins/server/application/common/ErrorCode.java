package io.jgitkins.server.application.common;

public enum ErrorCode {



    /***
     * Branches
     */

    BAD_REQUEST("BAD_REQUEST", "BAD_REQUEST")

    // 400
    , RUNNER_INVALID_TOKEN("RUNNER_INVALID_TOKEN", "Runner token is invalid")

    // 401/403
    , UNAUTHORIZED("UNAUTHORIZED", "Unauthorized")
    , FORBIDDEN("FORBIDDEN", "Forbidden")

    // 404
    , ORGANIZE_NOT_FOUND("ORGANIZE_NOT_FOUND", "Organize Not Found")
    , REPOSITORY_NOT_FOUND("REPOSITORY_NOT_FOUND", "Source Repository Not Found")
    , BRANCH_NOT_FOUND("BRANCH_NOT_FOUND", "Branch Not Found")
    , COMMIT_NOT_FOUND("COMMIT_NOT_FOUND", "Commit Not Found")
    , SOURCE_BRANCH_NOT_FOUND("SOURCE_BRANCH_NOT_FOUND", "Source Branch Not Found")
    , COMMIT_TREE_NOT_FOUND("COMMIT_TREE_NOT_FOUND", "Commit Tree Not Found")
    , RUNNER_NOT_FOUND("RUNNER_NOT_FOUND", "Runner Not Found")

    // 409
    , REPOSITORY_ALREADY_EXISTS("REPOSITORY_ALREADY_EXISTS", "Repository Already Exists")
    , BRANCH_ALREADY_EXISTS("BRANCH_ALREADY_EXISTS", "Branch Already Exists")
    , RUNNER_ALREADY_ACTIVE("RUNNER_ALREADY_ACTIVE", "Runner Already Active")
    , ORGANIZE_ALREADY_EXISTS("ORGANIZE_ALREADY_EXISTS", "Organize Already Exists")

    // 422
    , REPOSITORY_DOES_NOT_INITIALIZED("REPOSITORY_DOES_NOT_INITIALIZED", "Repository Does Not Initialized")

    // 500
    , REPOSITORY_CREATE_FAILED("REPOSITORY_CREATE_FAILED", "Repository Create Failed")
    , BRANCH_CREATE_FAILED("BRANCH_CREATE_FAILED", "Branch Create Failed")
    , BRANCH_DELETE_FAILED("BRANCH_DELETE_FAILED", "Branch Delete Failed")
    , BRANCH_LOAD_FAILED("BRANCH_LOAD_FAILED", "Branch Load Failed")
    , HEAD_POINT_FAILED("HEAD_POINT_FAILED", "Head Point Failed")
    , COMMIT_LOAD_FAILED("COMMIT_LOAD_FAILED", "Commit Load Failed")
    , COMMIT_CREATE_FAILED("COMMIT_CREATE_FAILED", "Commit Create Found")
    , COMMIT_TREE_LOAD_FAILED("COMMIT_TREE_LOAD_FAILED", "Commit Tree Load Failed")
    , FILE_LOAD_FAILED("FILE_LOAD_FAILED", "File Load Failed")
    , RUNNER_DELETE_FAILED("RUNNER_DELETE_FAILED", "Runner Delete Failed")
    , RUNNER_ACTIVATION_FAILED("RUNNER_ACTIVATION_FAILED", "Runner Activation Failed")
    , REPOSITORY_DELETE_FAILED("REPOSITORY_DELETE_FAILED", "Repository Delete Failed")

    , INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Server Error Occur")
    ;

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
