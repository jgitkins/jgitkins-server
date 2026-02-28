package io.jgitkins.server.presentation.advice.mapper;

import io.jgitkins.server.application.common.ErrorCode;
import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ApplicationErrorHttpStatusMapper implements ErrorHttpStatusMapper {

    @Override
    public boolean supports(io.jgitkins.server.common.error.ErrorCode errorCode) {
        return errorCode instanceof ErrorCode || errorCode instanceof ApplicationErrorCode;
    }

    @Override
    public HttpStatus map(io.jgitkins.server.common.error.ErrorCode errorCode) {
        if (errorCode instanceof ApplicationErrorCode applicationErrorCode) {
            return mapApplication(applicationErrorCode);
        }
        return mapLegacy((ErrorCode) errorCode);
    }

    private HttpStatus mapApplication(ApplicationErrorCode errorCode) {
        return switch (errorCode) {
            case APP_BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case APP_UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case APP_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case APP_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case APP_CONFLICT -> HttpStatus.CONFLICT;
            case APP_UNPROCESSABLE -> HttpStatus.UNPROCESSABLE_ENTITY;
            case APP_INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private HttpStatus mapLegacy(ErrorCode errorCode) {
        return switch (errorCode) {
            case BAD_REQUEST,
                 RUNNER_INVALID_TOKEN -> HttpStatus.BAD_REQUEST;

            case REPOSITORY_NOT_FOUND,
                 SOURCE_BRANCH_NOT_FOUND,
                 BRANCH_NOT_FOUND,
                 COMMIT_NOT_FOUND,
                 COMMIT_TREE_NOT_FOUND,
                 RUNNER_NOT_FOUND,
                 ORGANIZE_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case REPOSITORY_ALREADY_EXISTS,
                 BRANCH_ALREADY_EXISTS,
                 ORGANIZE_ALREADY_EXISTS,
                 USERNAME_ALREADY_EXISTS,
                 RUNNER_ALREADY_ACTIVE -> HttpStatus.CONFLICT;

            case REPOSITORY_DOES_NOT_INITIALIZED -> HttpStatus.UNPROCESSABLE_ENTITY;

            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;

            case INTERNAL_SERVER_ERROR,
                 REPOSITORY_CREATE_FAILED,
                 BRANCH_CREATE_FAILED,
                 BRANCH_DELETE_FAILED,
                 BRANCH_LOAD_FAILED,
                 HEAD_POINT_FAILED,
                 COMMIT_LOAD_FAILED,
                 COMMIT_CREATE_FAILED,
                 COMMIT_TREE_LOAD_FAILED,
                 FILE_LOAD_FAILED,
                 RUNNER_DELETE_FAILED,
                 RUNNER_ACTIVATION_FAILED,
                 REPOSITORY_DELETE_FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}

