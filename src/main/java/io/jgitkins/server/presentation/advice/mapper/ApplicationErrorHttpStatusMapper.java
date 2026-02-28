package io.jgitkins.server.presentation.advice.mapper;

import io.jgitkins.server.application.common.error.ApplicationErrorCode;
import io.jgitkins.server.common.error.ErrorCode;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ApplicationErrorHttpStatusMapper implements ErrorHttpStatusMapper {

    @Override
    public boolean supports(ErrorCode errorCode) {
        return errorCode instanceof ApplicationErrorCode;
    }

    @Override
    public HttpStatus map(ErrorCode errorCode) {
        return mapApplication((ApplicationErrorCode) errorCode);
    }

    private HttpStatus mapApplication(ApplicationErrorCode errorCode) {
        return switch (errorCode) {
            case BAD_REQUEST,
                 RUNNER_INVALID_TOKEN -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND,
                 ORGANIZE_NOT_FOUND,
                 REPOSITORY_NOT_FOUND,
                 BRANCH_NOT_FOUND,
                 COMMIT_NOT_FOUND,
                 SOURCE_BRANCH_NOT_FOUND,
                 COMMIT_TREE_NOT_FOUND,
                 RUNNER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT,
                 REPOSITORY_ALREADY_EXISTS,
                 BRANCH_ALREADY_EXISTS,
                 RUNNER_ALREADY_ACTIVE,
                 ORGANIZE_ALREADY_EXISTS,
                 USERNAME_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case UNPROCESSABLE,
                 REPOSITORY_DOES_NOT_INITIALIZED -> HttpStatus.UNPROCESSABLE_ENTITY;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

}
