package io.jgitkins.server.application.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {



    /***
     * Branches
     */

    REPOSITORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Source Repository Not Found")
    , REPOSITORY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Repository Create Failed")
    , REPOSITORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "Repository Already Exists")

    , SOURCE_BRANCH_NOT_FOUND(HttpStatus.NOT_FOUND, "Source Branch Not Found")
    , BRANCH_ALREADY_EXISTS(HttpStatus.CONFLICT, "Branch Already Exists")
    , BRANCH_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Branch Create Failed")
    , BRANCH_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Branch Delete Failed")
    , BRANCH_NOT_FOUND(HttpStatus.NOT_FOUND, "Branch Not Found")
    , BRANCH_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Branch Load Failed")

    , HEAD_POINT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Head Point Failed")

    , COMMIT_NOT_FOUND(HttpStatus.NOT_FOUND, "Commit Not Found")
    , COMMIT_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Commit Load Failed")
    , COMMIT_CREATE_FAILED(HttpStatus.NOT_FOUND, "Commit Create Found")

    , COMMIT_TREE_NOT_FOUND(HttpStatus.NOT_FOUND, "Commit Tree Not Found")
    , COMMIT_TREE_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Commit Tree Load Failed")


    , FILE_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "File Load Failed")

    , INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error Occur")
    ;

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

}
